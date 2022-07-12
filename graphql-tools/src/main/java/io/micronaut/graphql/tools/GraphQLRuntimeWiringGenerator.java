/*
 * Copyright 2021-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.graphql.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Scalars;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.OperationTypeDefinition;
import graphql.language.SchemaDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.language.UnionTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.beans.BeanMethod;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.graphql.tools.annotation.GraphQLInput;
import io.micronaut.graphql.tools.exceptions.ClassNotIntrospectedException;
import io.micronaut.graphql.tools.exceptions.IncorrectArgumentCountException;
import io.micronaut.graphql.tools.exceptions.IncorrectClassMappingException;
import io.micronaut.graphql.tools.exceptions.InvalidSourceArgumentException;
import io.micronaut.graphql.tools.exceptions.MappingConflictException;
import io.micronaut.graphql.tools.exceptions.MissingEnumValuesException;
import io.micronaut.graphql.tools.exceptions.MultipleMethodsFoundException;
import io.micronaut.graphql.tools.exceptions.RootResolverNotFoundException;
import io.micronaut.graphql.tools.exceptions.SchemaDefinitionNotProvidedException;
import io.micronaut.graphql.tools.exceptions.UnionTypeMappingNotProvidedException;
import io.micronaut.graphql.tools.schema.DefaultWiringFactory;
import io.micronaut.graphql.tools.schema.MicronautExecutableMethodDataFetcher;
import io.micronaut.graphql.tools.schema.MicronautIntrospectionDataFetcher;
import io.micronaut.graphql.tools.schema.UnionTypeResolver;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.jackson.modules.BeanIntrospectionModule;
import jakarta.inject.Provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;
import static io.micronaut.graphql.tools.GraphQLUtils.getTypeName;
import static io.micronaut.graphql.tools.GraphQLUtils.requireTypeName;
import static io.micronaut.graphql.tools.GraphQLUtils.unsupportedTypeDefinition;
import static io.micronaut.graphql.tools.GraphQLUtils.unwrapNonNullType;
import static io.micronaut.graphql.tools.MicronautUtils.getExecutableMethodFullName;
import static io.micronaut.graphql.tools.MicronautUtils.getMethodName;
import static io.micronaut.graphql.tools.MicronautUtils.getPropertyMethodName;
import static io.micronaut.graphql.tools.MicronautUtils.toMap;
import static io.micronaut.graphql.tools.MicronautUtils.unwrapArgument;
import static io.micronaut.graphql.tools.SystemTypes.getSupportedClasses;
import static io.micronaut.graphql.tools.SystemTypes.isGraphQlBuiltInType;
import static io.micronaut.graphql.tools.SystemTypes.isJavaBuiltInClass;

/**
 * @author Alexey Zhokhov
 */
@Internal
class GraphQLRuntimeWiringGenerator {

    private final ApplicationContext applicationContext;
    private final GraphQLBeanIntrospectionRegistry graphQLBeanIntrospectionRegistry;
    private final GraphQLResolversRegistry graphQLResolversRegistry;
    private final TypeDefinitionRegistry typeDefinitionRegistry;
    private final SchemaMappingDictionary schemaMappingDictionary;
    private final Provider<GraphQLSchema> graphQLSchemaProvider;
    private final ObjectMapper objectMapper;
    private final RuntimeWiring.Builder rootRuntimeWiringBuilder;

    private final Map<String, Class<?>> processedTypes = new HashMap<>();

    GraphQLRuntimeWiringGenerator(ApplicationContext applicationContext,
                                  GraphQLBeanIntrospectionRegistry graphQLBeanIntrospectionRegistry,
                                  GraphQLResolversRegistry graphQLResolversRegistry,
                                  TypeDefinitionRegistry typeDefinitionRegistry,
                                  SchemaMappingDictionary schemaMappingDictionary,
                                  Provider<GraphQLSchema> graphQLSchemaProvider) {
        requireNonNull("applicationContext", applicationContext);
        requireNonNull("graphQLBeanIntrospectionRegistry", graphQLBeanIntrospectionRegistry);
        requireNonNull("graphQLResolversRegistry", graphQLResolversRegistry);
        requireNonNull("typeDefinitionRegistry", typeDefinitionRegistry);
        requireNonNull("schemaParserDictionary", schemaMappingDictionary);
        requireNonNull("graphQLSchemaProvider", graphQLSchemaProvider);

        this.applicationContext = applicationContext;
        this.graphQLBeanIntrospectionRegistry = graphQLBeanIntrospectionRegistry;
        this.graphQLResolversRegistry = graphQLResolversRegistry;
        this.typeDefinitionRegistry = typeDefinitionRegistry;
        this.schemaMappingDictionary = schemaMappingDictionary;
        this.graphQLSchemaProvider = graphQLSchemaProvider;

        this.objectMapper = new ObjectMapper()
                .registerModule(new BeanIntrospectionModule());
        this.rootRuntimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
                .wiringFactory(new DefaultWiringFactory())
                .scalar(Scalars.GraphQLLong)
                .scalar(Scalars.GraphQLShort)
                .scalar(Scalars.GraphQLBigDecimal)
                .scalar(Scalars.GraphQLBigInteger);
    }

    RuntimeWiring generate() {
        SchemaDefinition schemaDefinition = typeDefinitionRegistry.schemaDefinition()
                .orElseThrow(SchemaDefinitionNotProvidedException::new);

        if (!graphQLResolversRegistry.hasRootResolvers()) {
            throw new RootResolverNotFoundException();
        }

        for (OperationTypeDefinition operationTypeDefinition : schemaDefinition.getOperationTypeDefinitions()) {
            processOperationTypeDefinition(operationTypeDefinition);
        }

        return rootRuntimeWiringBuilder.build();
    }

    private TypeDefinition<?> getTypeDefinition(TypeName typeName) {
        return typeDefinitionRegistry.getType(typeName.getName()).orElseThrow(() -> {
            throw new IllegalStateException("TypeDefinition not found by name: " + typeName.getName());
        });
    }

    void processExecutableMethod(Executable<Object, ?> executable, ReturnType<?> returnType,
                                 @Nullable Class<?> sourceClass, @Nullable Object instance,
                                 TypeRuntimeWiring.Builder typeRuntimeWiringBuilder, MappingContext mappingContext) {
        mappingContext = MappingContext.forField(
                mappingContext,
                executable.getDeclaringType(),
                getExecutableMethodFullName(executable)
        );

        List<ArgumentDefinition> argumentDefinitions = calculateArgumentDefinitions(executable, sourceClass, mappingContext);

        typeRuntimeWiringBuilder.dataFetcher(
                mappingContext.getFieldDefinition().getName(),
                new MicronautExecutableMethodDataFetcher(objectMapper, executable, argumentDefinitions, instance)
        );

        processArgument(returnType.asArgument(), mappingContext);
    }

    private void checkArgumentCount(Executable<?, ?> executable, @Nullable Class<?> sourceClass,
                                    MappingContext mappingContext) {
        int requiredArgs = mappingContext.getFieldDefinition().getInputValueDefinitions().size();

        if (sourceClass != null) {
            requiredArgs = requiredArgs + 1;
        }

        if (requiredArgs == 0 && executable.getArguments().length == 0) {
            return;
        }

        int currentArgs = (int) Arrays.stream(executable.getArguments())
                .filter(it -> !it.getType().isAssignableFrom(DataFetchingEnvironment.class))
                .count();

        if (requiredArgs == currentArgs) {
            return;
        }

        if (currentArgs > requiredArgs) {
            throw new IncorrectArgumentCountException(
                    mappingContext,
                    false,
                    currentArgs,
                    requiredArgs,
                    generateSuggestedMethod(getMethodName(executable), sourceClass, mappingContext)
            );
        } else {
            throw new IncorrectArgumentCountException(
                    mappingContext,
                    true,
                    currentArgs,
                    requiredArgs,
                    generateSuggestedMethod(getMethodName(executable), sourceClass, mappingContext)
            );
        }
    }

    private void checkArgumentCount(BeanProperty<?, ?> beanProperty, MappingContext mappingContext) {
        if (mappingContext.getFieldDefinition().getInputValueDefinitions().isEmpty()) {
            return;
        }

        int requiredArgs = mappingContext.getFieldDefinition().getInputValueDefinitions().size();
        if (requiredArgs > 0) {
            throw new IncorrectArgumentCountException(
                    mappingContext,
                    true,
                    0,
                    requiredArgs,
                    generateSuggestedMethod(beanProperty.getName(), null, mappingContext)
            );
        }
    }

    private String generateSuggestedMethod(String methodName, @Nullable Class<?> sourceClass,
                                           MappingContext mappingContext) {
        ArrayList<String> suggestedMethodArgs = new ArrayList<>();

        if (sourceClass != null) {
            String parameterName = mappingContext.getObjectTypeDefinition().getName().substring(0, 1).toLowerCase() +
                    mappingContext.getObjectTypeDefinition().getName().substring(1);
            suggestedMethodArgs.add(sourceClass.getName() + " " + parameterName);
        }

        suggestedMethodArgs.addAll(
                mappingContext.getFieldDefinition().getInputValueDefinitions().stream()
                        .map(it -> getTypeName(it.getType()).getName() + " " + it.getName())
                        .collect(Collectors.toList())
        );

        return suggestedMethodArgs.isEmpty() ? null : methodName + "(" + String.join(", ", suggestedMethodArgs) + ")";
    }

    private List<ArgumentDefinition> calculateArgumentDefinitions(Executable<?, ?> executable,
                                                                  @Nullable Class<?> sourceClass,
                                                                  MappingContext mappingContext) {
        checkArgumentCount(executable, sourceClass, mappingContext);

        List<InputValueDefinition> inputs = mappingContext.getFieldDefinition().getInputValueDefinitions();
        List<Argument<?>> arguments = Arrays.stream(executable.getArguments()).collect(Collectors.toList());

        if (inputs.isEmpty() && arguments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Class<?>> argumentClasses = arguments.stream()
                .map(TypeInformation::getType)
                .collect(Collectors.toList());

        boolean containsSourceArgument = false;

        if (sourceClass != null) {
            if (argumentClasses.get(0).equals(sourceClass)) {
                argumentClasses.remove(0);

                containsSourceArgument = true;
            } else {
                throw new InvalidSourceArgumentException(
                        mappingContext,
                        argumentClasses.get(0),
                        sourceClass
                );
            }
        }

        boolean containsEnvironmentArgument = false;

        if (!argumentClasses.isEmpty()
                && argumentClasses.get(argumentClasses.size() - 1).equals(DataFetchingEnvironment.class)) {
            argumentClasses.remove(argumentClasses.size() - 1);

            containsEnvironmentArgument = true;
        }

        ArrayList<ArgumentDefinition> result = new ArrayList<>();

        if (containsSourceArgument) {
            result.add(new ArgumentDefinition(ArgumentDefinition.SOURCE_ARGUMENT, null));
        }

        for (int i = 0; i < inputs.size(); i++) {
            InputValueDefinition inputValueDefinition = inputs.get(i);
            Class<?> returnType = argumentClasses.get(i);

            TypeName typeName = requireTypeName(unwrapNonNullType(inputValueDefinition.getType()));

            if (SystemTypes.isGraphQlBuiltInType(typeName)) {
                Set<Class<?>> supportedClasses = getSupportedClasses(typeName);

                if (!supportedClasses.contains(returnType)) {
                    throw IncorrectClassMappingException.forArgument(
                            IncorrectClassMappingException.MappingType.DETECT_TYPE,
                            IncorrectClassMappingException.MappingType.BUILT_IN_JAVA_CLASS,
                            MappingContext.forArgument(mappingContext, i),
                            returnType,
                            supportedClasses
                    );
                }

                result.add(new ArgumentDefinition(inputs.get(i).getName(), returnType));
            } else {
                if (returnType.isInterface()) {
                    throw IncorrectClassMappingException.forArgument(
                            IncorrectClassMappingException.MappingType.DETECT_TYPE,
                            IncorrectClassMappingException.MappingType.CUSTOM_CLASS,
                            MappingContext.forArgument(mappingContext, i),
                            returnType,
                            null
                    );
                }

                Class<?> inputClass = processInputType(
                        typeName,
                        returnType,
                        MappingContext.forArgument(mappingContext, i)
                );

                result.add(new ArgumentDefinition(inputs.get(i).getName(), inputClass));
            }
        }

        if (containsEnvironmentArgument) {
            result.add(new ArgumentDefinition(ArgumentDefinition.DATA_FETCHING_ENVIRONMENT_ARGUMENT, null));
        }

        return result;
    }

    private void processArgument(Argument<?> argument, MappingContext mappingContext) {
        Argument<?> unwrappedArgument = unwrapArgument(argument);

        Type<?> fieldType = unwrapNonNullType(mappingContext.getFieldDefinition().getType());
        Class<?> returnType = unwrappedArgument.getType();

        if (fieldType instanceof ListType) {
            if (!returnType.equals(List.class)) {
                // TODO make the message more clear
                throw new RuntimeException("Wrong return type");
            }

            Type<?> listFieldType = ((ListType) fieldType).getType();
            // TODO check
            Class<?> listReturnType = unwrappedArgument.getFirstTypeVariable().get().getType();

            // TODO is mapping details is corrrect?

            processReturnType(listReturnType, listFieldType, mappingContext);
        } else {
            processReturnType(returnType, fieldType, mappingContext);
        }
    }

    private void processReturnType(Class<?> returnType, Type<?> graphQlType, MappingContext mappingContext) {
        if (graphQlType instanceof NonNullType) {
            graphQlType = unwrapNonNullType(graphQlType);
        }

        TypeName typeName = requireTypeName(graphQlType);

        if (isGraphQlBuiltInType(typeName)) {
            Set<Class<?>> supportedClasses = getSupportedClasses(getTypeName(graphQlType));

            if (!supportedClasses.contains(returnType)) {
                throw new IncorrectClassMappingException(
                        "The field is mapped to the incorrect class.",
                        mappingContext,
                        returnType,
                        supportedClasses
                );
            }
        } else {
            TypeDefinition<?> typeDefinition = getTypeDefinition(typeName);

            if (typeDefinition instanceof EnumTypeDefinition) {
                processEnumTypeDefinition((EnumTypeDefinition) typeDefinition, returnType, false, mappingContext);
            } else if (typeDefinition instanceof UnionTypeDefinition) {
                processUnionTypeDefinition((UnionTypeDefinition) typeDefinition, returnType, mappingContext);
            } else if (typeDefinition instanceof ObjectTypeDefinition) {
                processObjectTypeDefinition((ObjectTypeDefinition) typeDefinition, returnType, mappingContext);
            } else {
                throw unsupportedTypeDefinition(typeDefinition);
            }
        }
    }

    private void processOperationTypeDefinition(OperationTypeDefinition operationTypeDefinition) {
        ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition)
                getTypeDefinition(operationTypeDefinition.getTypeName());

        rootRuntimeWiringBuilder.type(operationTypeDefinition.getTypeName().getName(), typeRuntimeWiringBuilder -> {
            for (FieldDefinition fieldDefinition : objectTypeDefinition.getFieldDefinitions()) {
                MappingContext mappingContext = MappingContext.forField(objectTypeDefinition, fieldDefinition);

                List<BeanDefinitionAndMethod> beanDefinitionAndMethods =
                        graphQLResolversRegistry.getRootExecutableMethod(fieldDefinition.getName());

                if (beanDefinitionAndMethods.size() > 1) {
                    throw new MultipleMethodsFoundException(mappingContext, toMap(beanDefinitionAndMethods));
                }

                BeanDefinitionAndMethod beanDefinitionAndMethod = beanDefinitionAndMethods.get(0);

                ExecutableMethod<Object, ?> executable = beanDefinitionAndMethod.getExecutableMethod();

                processExecutableMethod(
                        executable,
                        executable.getReturnType(),
                        null,
                        applicationContext.getBean(beanDefinitionAndMethod.getBeanDefinition()),
                        typeRuntimeWiringBuilder,
                        mappingContext
                );
            }

            return typeRuntimeWiringBuilder;
        });
    }

    private void processEnumTypeDefinition(EnumTypeDefinition enumTypeDefinition, Class<?> targetClass, boolean input,
                                           MappingContext mappingContext) {
        processIfNotProcessed(enumTypeDefinition, targetClass, mappingContext, () -> {
            if (!targetClass.isEnum()) {
                if (input) {
                    throw IncorrectClassMappingException.forArgument(
                            IncorrectClassMappingException.MappingType.DETECT_TYPE,
                            IncorrectClassMappingException.MappingType.ENUM,
                            mappingContext,
                            targetClass,
                            null
                    );
                } else {
                    throw IncorrectClassMappingException.forField(
                            IncorrectClassMappingException.MappingType.DETECT_TYPE,
                            IncorrectClassMappingException.MappingType.ENUM,
                            mappingContext,
                            targetClass,
                            null
                    );
                }
            }

            List<String> expectedValues = enumTypeDefinition.getEnumValueDefinitions().stream()
                    .map(EnumValueDefinition::getName)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            List<String> existingValues = Arrays.stream(targetClass.getEnumConstants()).map(it -> ((Enum<?>) it).name())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            List<String> missingValues = expectedValues.stream()
                    .filter(it -> !existingValues.contains(it))
                    .collect(Collectors.toList());

            if (!missingValues.isEmpty()) {
                throw new MissingEnumValuesException(mappingContext, missingValues);
            }
        });
    }

    private void processUnionTypeDefinition(UnionTypeDefinition unionTypeDefinition, Class<?> returnType,
                                            MappingContext mappingContext) {
        processIfNotProcessed(unionTypeDefinition, returnType, mappingContext, () -> {
            if (!returnType.isInterface()) {
                throw IncorrectClassMappingException.forField(
                        IncorrectClassMappingException.MappingType.DETECT_TYPE,
                        IncorrectClassMappingException.MappingType.INTERFACE,
                        mappingContext,
                        returnType,
                        null
                );
            }

            rootRuntimeWiringBuilder.type(unionTypeDefinition.getName(), typeRuntimeWiringBuilder -> {
                Map<Class<?>, String> objectTypes = new HashMap<>();

                for (Type<?> type : unionTypeDefinition.getMemberTypes()) {
                    TypeName typeName = getTypeName(type);
                    TypeDefinition<?> typeDefinition = getTypeDefinition(typeName);

                    if (typeDefinition instanceof ObjectTypeDefinition) {
                        Class<?> clazz = Optional
                                .ofNullable(schemaMappingDictionary.getTypes().get(typeDefinition.getName()))
                                .orElseThrow(() -> new UnionTypeMappingNotProvidedException(
                                        mappingContext, typeName.getName(), unionTypeDefinition.getName()
                                ));

                        processObjectTypeDefinition((ObjectTypeDefinition) typeDefinition, clazz, mappingContext);

                        objectTypes.put(clazz, typeName.getName());
                    } else {
                        throw unsupportedTypeDefinition(typeDefinition);
                    }
                }

                typeRuntimeWiringBuilder.typeResolver(new UnionTypeResolver(graphQLSchemaProvider, objectTypes));
                return typeRuntimeWiringBuilder;
            });
        });
    }

    private void processObjectTypeDefinition(ObjectTypeDefinition objectTypeDefinition, Class<?> targetClass,
                                             MappingContext mappingContext) {
        processIfNotProcessed(objectTypeDefinition, targetClass, mappingContext, () -> {
            Optional.ofNullable(schemaMappingDictionary.getTypes().get(objectTypeDefinition.getName()))
                    .ifPresent(registeredClass -> {
                        if (!registeredClass.equals(targetClass)) {
                            throw new MappingConflictException(
                                    mappingContext,
                                    "type",
                                    objectTypeDefinition.getName(),
                                    targetClass,
                                    registeredClass
                            );
                        }
                    });

            if (isJavaBuiltInClass(targetClass)) {
                throw IncorrectClassMappingException.forField(
                        IncorrectClassMappingException.MappingType.DETECT_TYPE,
                        IncorrectClassMappingException.MappingType.CUSTOM_CLASS,
                        mappingContext,
                        targetClass,
                        null
                );
            }

            BeanIntrospection<Object> beanIntrospection = graphQLBeanIntrospectionRegistry.getGraphQlTypeBeanIntrospection(
                    mappingContext,
                    targetClass
            );

            rootRuntimeWiringBuilder.type(objectTypeDefinition.getName(), typeRuntimeWiringBuilder -> {
                typeRuntimeWiringBuilder.defaultDataFetcher(new MicronautIntrospectionDataFetcher(beanIntrospection));

                for (FieldDefinition fieldDefinition : objectTypeDefinition.getFieldDefinitions()) {
                    processFieldDefinition(
                            fieldDefinition,
                            objectTypeDefinition,
                            typeRuntimeWiringBuilder,
                            beanIntrospection
                    );
                }

                return typeRuntimeWiringBuilder;
            });
        });
    }

    private void processFieldDefinition(FieldDefinition fieldDefinition, ObjectTypeDefinition objectTypeDefinition,
                                        TypeRuntimeWiring.Builder typeRuntimeWiringBuilder,
                                        BeanIntrospection<Object> beanIntrospection) {
        MappingContext mappingContext = MappingContext.forField(objectTypeDefinition, fieldDefinition);

        Optional<BeanProperty<Object, Object>> beanProperty =
                beanIntrospection.getProperty(fieldDefinition.getName());
        List<BeanMethod<Object, ?>> beanMethods = beanIntrospection.getBeanMethods().stream()
                .filter(it -> it.getName().equals(fieldDefinition.getName()))
                .collect(Collectors.toList());

        if (beanProperty.isPresent() && !beanMethods.isEmpty()) {
            throw new MultipleMethodsFoundException(mappingContext, toMap(beanMethods, beanProperty.get()));
        }

        if (beanMethods.size() > 1) {
            throw new MultipleMethodsFoundException(mappingContext, toMap(beanMethods, null));
        }

        if (beanProperty.isPresent()) {
            Argument<?> argument = beanProperty.get().asArgument();

            mappingContext = MappingContext.forField(
                    mappingContext,
                    beanIntrospection.getBeanType(),
                    getPropertyMethodName(beanProperty.get())
            );

            // the bean property don't have arguments, that's why we only validates arguments count, not exact types
            checkArgumentCount(beanProperty.get(), mappingContext);

            processArgument(argument, mappingContext);

            return;
        }

        if (!beanMethods.isEmpty()) {
            BeanMethod<Object, ?> beanMethod = beanMethods.get(0);

            processExecutableMethod(
                    beanMethod,
                    beanMethod.getReturnType(),
                    null,
                    null,
                    typeRuntimeWiringBuilder,
                    mappingContext
            );

            return;
        }

        Class<?> sourceClass = graphQLBeanIntrospectionRegistry.getInterfaceClass(beanIntrospection.getBeanType());

        if (sourceClass.isPrimitive() || sourceClass.isEnum() || sourceClass.isAnnotation()) {
            throw IncorrectClassMappingException.forField(
                    IncorrectClassMappingException.MappingType.DETECT_TYPE,
                    IncorrectClassMappingException.MappingType.CUSTOM_CLASS,
                    mappingContext,
                    sourceClass,
                    null
            );
        }

        List<BeanDefinitionAndMethod> beanDefinitionAndMethods = graphQLResolversRegistry
                .getTypeExecutableMethod(sourceClass, fieldDefinition.getName());

        if (beanDefinitionAndMethods.size() > 1) {
            throw new MultipleMethodsFoundException(mappingContext, toMap(beanDefinitionAndMethods));
        }

        BeanDefinitionAndMethod beanDefinitionAndMethod = beanDefinitionAndMethods.get(0);

        ExecutableMethod<Object, ?> executable = beanDefinitionAndMethod.getExecutableMethod();

        processExecutableMethod(
                executable,
                executable.getReturnType(),
                sourceClass,
                applicationContext.getBean(beanDefinitionAndMethod.getBeanDefinition()),
                typeRuntimeWiringBuilder,
                mappingContext
        );
    }

    private void processInputObjectTypeDefinition(InputObjectTypeDefinition inputObjectTypeDefinition,
                                                  Class<?> targetClass, MappingContext mappingContext) {
        processIfNotProcessed(inputObjectTypeDefinition, targetClass, mappingContext, () -> {
            BeanIntrospection<Object> beanIntrospection = BeanIntrospector.SHARED
                    .findIntrospection((Class<Object>) targetClass)
                    .orElseThrow(() -> new ClassNotIntrospectedException(mappingContext, targetClass, GraphQLInput.class));

            if (
                    beanIntrospection.getBeanType().isInterface()
                            || beanIntrospection.getBeanType().isEnum()
                            || beanIntrospection.getBeanType().isAnnotation()
            ) {
                throw IncorrectClassMappingException.forArgument(
                        IncorrectClassMappingException.MappingType.DETECT_TYPE,
                        IncorrectClassMappingException.MappingType.CUSTOM_CLASS,
                        mappingContext,
                        targetClass,
                        null
                );
            }

            for (InputValueDefinition inputValueDefinition : inputObjectTypeDefinition.getInputValueDefinitions()) {
                processInputValueDefinition(inputValueDefinition, beanIntrospection);
            }
        });
    }

    private void processInputValueDefinition(InputValueDefinition inputValueDefinition,
                                             BeanIntrospection<Object> beanIntrospection) {
        // TODO Maybe can be moved to some method???

        Optional<BeanProperty<Object, Object>> property = beanIntrospection.getProperty(inputValueDefinition.getName());

        if (!property.isPresent()) {
            // TODO custom exception
            throw new RuntimeException("Property `" + inputValueDefinition.getName() + "` not found in: " + beanIntrospection.getBeanType());
        }

        Type<?> fieldType = unwrapNonNullType(inputValueDefinition.getType());
        Class<?> returnType = property.get().getType();

        if (fieldType instanceof ListType) {
            if (!returnType.equals(List.class)) {
                // TODO
                throw new RuntimeException("Wrong return type");
            }

            // TODO validate primitive type

            // TODO process sub types
            //throw new UnsupportedOperationException();
        } else {
            processInputType(fieldType, returnType, null);
        }
    }

    @Nullable
    private Class<?> processInputType(Type<?> graphQlType, Class<?> clazz, MappingContext mappingContext) {
        TypeName typeName = requireTypeName(graphQlType);

        TypeDefinition<?> typeDefinition = getTypeDefinition(typeName);

        if (typeDefinition instanceof InputObjectTypeDefinition) {
            processInputObjectTypeDefinition((InputObjectTypeDefinition) typeDefinition, clazz, mappingContext);

            return clazz;
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            processEnumTypeDefinition((EnumTypeDefinition) typeDefinition, clazz, true, mappingContext);

            return null;
        } else if (isGraphQlBuiltInType(typeName)) {
            Set<Class<?>> supportedClasses = getSupportedClasses(typeName);

            if (!supportedClasses.contains(clazz)) {
                throw IncorrectClassMappingException.forArgument(
                        IncorrectClassMappingException.MappingType.DETECT_TYPE,
                        IncorrectClassMappingException.MappingType.BUILT_IN_JAVA_CLASS,
                        mappingContext,
                        clazz,
                        supportedClasses
                );
            }

            return null;
        } else {
            throw unsupportedTypeDefinition(typeDefinition);
        }
    }

    private void processIfNotProcessed(TypeDefinition<?> typeDefinition, Class<?> targetClass,
                                       MappingContext mappingContext, Runnable runnable) {
        if (processedTypes.containsKey(typeDefinition.getName())) {
            Class<?> processedClass = processedTypes.get(typeDefinition.getName());

            String graphQlType;
            if (typeDefinition instanceof EnumTypeDefinition) {
                graphQlType = "enum";
            } else if (typeDefinition instanceof UnionTypeDefinition) {
                graphQlType = "union";
            } else if (typeDefinition instanceof ObjectTypeDefinition) {
                graphQlType = "type";
            } else if (typeDefinition instanceof InputObjectTypeDefinition) {
                graphQlType = "input";
            } else {
                throw unsupportedTypeDefinition(typeDefinition);
            }

            if (!targetClass.equals(processedClass)) {
                throw new MappingConflictException(
                        mappingContext,
                        graphQlType,
                        typeDefinition.getName(),
                        targetClass,
                        processedClass
                );
            }

            // do nothing as it already processed
            return;
        }

        runnable.run();

        processedTypes.put(typeDefinition.getName(), targetClass);
    }

}
