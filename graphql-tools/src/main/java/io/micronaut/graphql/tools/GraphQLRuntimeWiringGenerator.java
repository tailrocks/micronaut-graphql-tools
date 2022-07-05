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
import io.micronaut.core.annotation.NonNull;
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
import static io.micronaut.graphql.tools.MicronautUtils.getExecutableMethodFullName;
import static io.micronaut.graphql.tools.MicronautUtils.getMethodName;
import static io.micronaut.graphql.tools.MicronautUtils.getPropertyMethodName;
import static io.micronaut.graphql.tools.MicronautUtils.unwrapArgument;
import static io.micronaut.graphql.tools.GraphQLUtils.getTypeName;
import static io.micronaut.graphql.tools.GraphQLUtils.unwrapNonNullType;
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
    private final SchemaParserDictionary schemaParserDictionary;
    private final Provider<GraphQLSchema> graphQLSchemaProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, MappingItem> mappingRegistry = new HashMap<>();

    private final RuntimeWiring.Builder rootRuntimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
            .wiringFactory(new DefaultWiringFactory())
            .scalar(Scalars.GraphQLLong)
            .scalar(Scalars.GraphQLShort)
            .scalar(Scalars.GraphQLBigDecimal)
            .scalar(Scalars.GraphQLBigInteger);

    GraphQLRuntimeWiringGenerator(ApplicationContext applicationContext,
                                  GraphQLBeanIntrospectionRegistry graphQLBeanIntrospectionRegistry,
                                  GraphQLResolversRegistry graphQLResolversRegistry,
                                  TypeDefinitionRegistry typeDefinitionRegistry,
                                  SchemaParserDictionary schemaParserDictionary,
                                  Provider<GraphQLSchema> graphQLSchemaProvider) {
        requireNonNull("applicationContext", applicationContext);
        requireNonNull("graphQLBeanIntrospectionRegistry", graphQLBeanIntrospectionRegistry);
        requireNonNull("graphQLResolversRegistry", graphQLResolversRegistry);
        requireNonNull("typeDefinitionRegistry", typeDefinitionRegistry);
        requireNonNull("schemaParserDictionary", schemaParserDictionary);
        requireNonNull("graphQLSchemaProvider", graphQLSchemaProvider);

        this.applicationContext = applicationContext;
        this.graphQLBeanIntrospectionRegistry = graphQLBeanIntrospectionRegistry;
        this.graphQLResolversRegistry = graphQLResolversRegistry;
        this.typeDefinitionRegistry = typeDefinitionRegistry;
        this.schemaParserDictionary = schemaParserDictionary;
        this.graphQLSchemaProvider = graphQLSchemaProvider;

        objectMapper.registerModule(new BeanIntrospectionModule());
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

    void processExecutableMethod(
            ObjectTypeDefinition objectTypeDefinition,
            FieldDefinition fieldDefinition,
            TypeRuntimeWiring.Builder typeRuntimeWiringBuilder,
            Executable<Object, ?> executable,
            ReturnType<?> returnType,
            @Nullable Class<?> sourceClass,
            @Nullable Object instance
    ) {
        MappingContext mappingContext = MappingContext.forField(
                objectTypeDefinition,
                fieldDefinition,
                executable.getDeclaringType(),
                getExecutableMethodFullName(executable)
        );

        checkInputValueDefinitions(mappingContext, executable, sourceClass);

        List<ArgumentDetails> argumentDetails = processInputArguments(mappingContext, executable, sourceClass);

        typeRuntimeWiringBuilder.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                objectMapper,
                executable,
                argumentDetails,
                instance
        ));

        Argument<?> argument = returnType.asArgument();

        processArgument(argument, mappingContext);
    }

    private void checkInputValueDefinitions(MappingContext mappingContext, Executable<?, ?> executable,
                                            @Nullable Class<?> sourceClass) {
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

        ArrayList<String> suggestedMethodArgs = new ArrayList<>();

        if (sourceClass != null) {
            String parameterName = mappingContext.getObjectTypeDefinition().getName().substring(0, 1).toLowerCase() +
                    mappingContext.getObjectTypeDefinition().getName().substring(1);
            suggestedMethodArgs.add(sourceClass.getName() + " " + parameterName);
        }

        // TODO map arguments to real classes?
        suggestedMethodArgs.addAll(
                mappingContext.getFieldDefinition().getInputValueDefinitions().stream()
                        .map(it -> getTypeName(it.getType()).getName() + " " + it.getName())
                        .collect(Collectors.toList())
        );

        String suggestedMethodArgsAsString = suggestedMethodArgs.isEmpty()
                ? null : "(" + String.join(", ", suggestedMethodArgs) + ")";

        if (currentArgs > requiredArgs) {
            throw new IncorrectArgumentCountException(
                    mappingContext,
                    false,
                    currentArgs,
                    requiredArgs,
                    suggestedMethodArgsAsString
            );
        } else {
            throw new IncorrectArgumentCountException(
                    mappingContext,
                    true,
                    currentArgs,
                    requiredArgs,
                    suggestedMethodArgsAsString
            );
        }
    }

    private void checkInputValueDefinitions(MappingContext mappingContext, BeanProperty<?, ?> beanProperty) {
        if (mappingContext.getFieldDefinition().getInputValueDefinitions().size() == 0) {
            return;
        }

        int requiredArgs = mappingContext.getFieldDefinition().getInputValueDefinitions().size();
        if (requiredArgs > 0) {
            // TODO custom exception
            throw new RuntimeException("Too less arguments in the property `" + beanProperty.getName() + "` (" + beanProperty.getDeclaringType() + "), required " + requiredArgs + " (excluded DataFetchingEnvironment)");
        }
    }

    private List<ArgumentDetails> processInputArguments(
            MappingContext mappingContext,
            Executable<?, ?> executable,
            @Nullable Class<?> sourceClass
    ) {
        List<InputValueDefinition> inputs = mappingContext.getFieldDefinition().getInputValueDefinitions();
        List<Argument<?>> arguments = Arrays.stream(executable.getArguments()).collect(Collectors.toList());

        if (inputs.isEmpty() && arguments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Class<?>> argumentClasses = arguments.stream()
                .map(TypeInformation::getType)
                .collect(Collectors.toList());

        // TODO validates the first argument is source argument

        boolean containsSourceArgument = false;

        if (sourceClass != null) {
            // TODO, do we still need it
            /*
            if (sourceClass.isInterface()) {
                if (sourceClass.isAssignableFrom(argumentClasses.get(0))) {
                    argumentClasses.remove(0);

                    containsSourceArgument = true;
                } else {
                    throw new InvalidSourceArgumentException(
                            objectTypeDefinition.getName(),
                            fieldDefinition.getName(),
                            executable.getDeclaringType(),
                            getExecutableMethodFullName(executable),
                            argumentClasses.get(0),
                            sourceClass
                    );
                }
            } else {

             */

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

            // TODO need it?
                /*
            }

                 */
        }

        boolean containsEnvironmentArgument = false;

        if (argumentClasses.size() > 0) {
            if (argumentClasses.get(argumentClasses.size() - 1).equals(DataFetchingEnvironment.class)) {
                argumentClasses.remove(argumentClasses.size() - 1);

                containsEnvironmentArgument = true;
            }
        }

        ArrayList<ArgumentDetails> result = new ArrayList<>();

        if (containsSourceArgument) {
            result.add(new ArgumentDetails(ArgumentDetails.SOURCE_ARGUMENT, null));
        }

        for (int i = 0; i < inputs.size(); i++) {
            InputValueDefinition inputValueDefinition = inputs.get(i);
            Class<?> returnType = argumentClasses.get(i);

            TypeName typeName = (TypeName) unwrapNonNullType(inputValueDefinition.getType());

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

                result.add(new ArgumentDetails(inputs.get(i).getName(), returnType));
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
                        MappingContext.forArgument(mappingContext, i),
                        returnType
                );

                result.add(new ArgumentDetails(inputs.get(i).getName(), inputClass));
            }
        }

        if (containsEnvironmentArgument) {
            result.add(new ArgumentDetails(ArgumentDetails.DATA_FETCHING_ENVIRONMENT_ARGUMENT, null));
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

            processReturnType(listReturnType, mappingContext, listFieldType);
        } else {
            processReturnType(returnType, mappingContext, fieldType);
        }
    }

    private void processReturnType(Class<?> returnType, MappingContext mappingContext, Type<?> graphQlType) {
        if (graphQlType instanceof NonNullType) {
            graphQlType = unwrapNonNullType(graphQlType);
        }

        if (!(graphQlType instanceof TypeName)) {
            throw new UnsupportedOperationException("Unsupported type: " + graphQlType);
        }

        TypeName typeName = (TypeName) graphQlType;

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
                throw new UnsupportedOperationException("Unsupported type definition: " + typeDefinition);
            }
        }
    }

    private void processOperationTypeDefinition(OperationTypeDefinition operationTypeDefinition) {
        ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition)
                getTypeDefinition(operationTypeDefinition.getTypeName());

        rootRuntimeWiringBuilder.type(operationTypeDefinition.getTypeName().getName(), typeWiring -> {
            for (FieldDefinition fieldDefinition : objectTypeDefinition.getFieldDefinitions()) {
                processRootFieldDefinition(fieldDefinition, objectTypeDefinition, typeWiring);
            }

            return typeWiring;
        });
    }

    private void processRootFieldDefinition(FieldDefinition fieldDefinition, ObjectTypeDefinition objectTypeDefinition,
                                            TypeRuntimeWiring.Builder typeRuntimeWiringBuilder) {
        BeanDefinitionAndMethod beanDefinitionAndMethod =
                graphQLResolversRegistry.getRootExecutableMethod(fieldDefinition.getName());

        ExecutableMethod<Object, ?> executable = beanDefinitionAndMethod.getExecutableMethod();

        processExecutableMethod(
                objectTypeDefinition,
                fieldDefinition,
                typeRuntimeWiringBuilder,
                executable,
                executable.getReturnType(),
                null,
                applicationContext.getBean(beanDefinitionAndMethod.getBeanDefinition())
        );
    }

    private void processEnumTypeDefinition(EnumTypeDefinition typeDefinition, Class<?> targetClass, boolean input,
                                           MappingContext mappingContext) {
        requireNonNull("typeDefinition", typeDefinition);
        requireNonNull("targetClass", targetClass);

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

        String typeName = typeDefinition.getName();

        MappingItem mappingItem = mappingRegistry.get(typeName);

        if (mappingItem != null) {
            if (!targetClass.equals(mappingItem.targetEnum)) {
                throw new MappingConflictException(
                        mappingContext,
                        "enum",
                        typeName,
                        targetClass,
                        mappingItem.targetEnum
                );
            }

            // already processed
            return;
        }

        List<String> expectedValues = typeDefinition.getEnumValueDefinitions().stream()
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

        mappingRegistry.put(typeName, new MappingItem(targetClass, null));
    }

    private void processUnionTypeDefinition(UnionTypeDefinition unionTypeDefinition, Class<?> returnType,
                                            MappingContext mappingContext) {
        // TODO skip processing already processed type

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
            registerUnionMapping(unionTypeDefinition.getName(), returnType);

            Map<Class<?>, String> objectTypes = new HashMap<>();

            for (Type<?> type : unionTypeDefinition.getMemberTypes()) {
                TypeName typeName = getTypeName(type);
                TypeDefinition<?> typeDefinition = getTypeDefinition(typeName);

                if (typeDefinition instanceof ObjectTypeDefinition) {
                    Class<?> clazz = Optional
                            .ofNullable(schemaParserDictionary.getTypes().get(typeDefinition.getName()))
                            .orElseThrow(() -> new UnionTypeMappingNotProvidedException(
                                    mappingContext, typeName.getName(), unionTypeDefinition.getName()
                            ));

                    processObjectTypeDefinition((ObjectTypeDefinition) typeDefinition, clazz, mappingContext);

                    objectTypes.put(clazz, typeName.getName());
                } else {
                    throw new UnsupportedOperationException("Unsupported type definition: " + typeDefinition);
                }
            }

            typeRuntimeWiringBuilder.typeResolver(new UnionTypeResolver(graphQLSchemaProvider, objectTypes));
            return typeRuntimeWiringBuilder;
        });
    }

    private void processObjectTypeDefinition(ObjectTypeDefinition objectTypeDefinition, Class<?> returnType,
                                             MappingContext mappingContext) {
        Optional.ofNullable(schemaParserDictionary.getTypes().get(objectTypeDefinition.getName()))
                .ifPresent((it) -> {
                    if (!it.equals(returnType)) {
                        // TODO compare types
                    }
                });

        if (isJavaBuiltInClass(returnType)) {
            throw IncorrectClassMappingException.forField(
                    IncorrectClassMappingException.MappingType.DETECT_TYPE,
                    IncorrectClassMappingException.MappingType.CUSTOM_CLASS,
                    mappingContext,
                    returnType,
                    null
            );
        }

        BeanIntrospection<Object> beanIntrospection = graphQLBeanIntrospectionRegistry.getGraphQlTypeBeanIntrospection(
                mappingContext,
                returnType
        );

        if (registerBeanIntrospectionMapping(objectTypeDefinition.getName(), beanIntrospection)) {
            rootRuntimeWiringBuilder.type(objectTypeDefinition.getName(), typeWiring -> {
                typeWiring.defaultDataFetcher(new MicronautIntrospectionDataFetcher(beanIntrospection));

                for (FieldDefinition fieldDefinition : objectTypeDefinition.getFieldDefinitions()) {
                    processFieldDefinition(
                            fieldDefinition,
                            objectTypeDefinition,
                            beanIntrospection,
                            typeWiring
                    );
                }

                return typeWiring;
            });
        }
    }

    private void processFieldDefinition(
            FieldDefinition fieldDefinition,
            ObjectTypeDefinition objectTypeDefinition,
            BeanIntrospection<Object> beanIntrospection,
            TypeRuntimeWiring.Builder typeRuntimeWiringBuilder
    ) {
        MappingContext mappingContext = MappingContext.forField(objectTypeDefinition, fieldDefinition);

        Optional<BeanProperty<Object, Object>> beanProperty =
                beanIntrospection.getProperty(fieldDefinition.getName());
        List<BeanMethod<Object, ?>> beanMethods = beanIntrospection.getBeanMethods().stream()
                .filter(it -> it.getName().equals(fieldDefinition.getName()) || it.getName().equals(getMethodName(fieldDefinition.getName())))
                .collect(Collectors.toList());

        if (beanProperty.isPresent() && !beanMethods.isEmpty()) {
            // TODO
            throw new RuntimeException("Found `" + fieldDefinition.getName() + "` property and bean method: " + beanIntrospection.getBeanType());
        }

        if (beanMethods.size() > 1) {
            // TODO
            throw new RuntimeException("Found multiple bean methods `" + fieldDefinition.getName() + "`: " + beanIntrospection.getBeanType());
        }

        if (beanProperty.isPresent()) {
            Argument<?> argument = beanProperty.get().asArgument();

            mappingContext = MappingContext.forField(
                    mappingContext,
                    beanIntrospection.getBeanType(),
                    getPropertyMethodName(beanProperty.get())
            );

            checkInputValueDefinitions(mappingContext, beanProperty.get());

            processArgument(argument, mappingContext);

            return;
        }

        if (!beanMethods.isEmpty()) {
            BeanMethod<Object, ?> beanMethod = beanMethods.get(0);

            processExecutableMethod(
                    objectTypeDefinition,
                    fieldDefinition,
                    typeRuntimeWiringBuilder,
                    beanMethod,
                    beanMethod.getReturnType(),
                    null,
                    null
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

        BeanDefinitionAndMethod beanDefinitionAndMethod = graphQLResolversRegistry
                .getTypeExecutableMethod(sourceClass, fieldDefinition.getName());

        ExecutableMethod<Object, ?> executable = beanDefinitionAndMethod.getExecutableMethod();

        processExecutableMethod(
                objectTypeDefinition,
                fieldDefinition,
                typeRuntimeWiringBuilder,
                executable,
                executable.getReturnType(),
                sourceClass,
                applicationContext.getBean(beanDefinitionAndMethod.getBeanDefinition())
        );
    }

    private void processInputObjectTypeDefinition(InputObjectTypeDefinition objectTypeDefinition,
                                                  MappingContext mappingContext, Class<?> targetClass) {
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

        if (registerBeanIntrospectionMapping(objectTypeDefinition.getName(), beanIntrospection)) {
            for (InputValueDefinition inputValueDefinition : objectTypeDefinition.getInputValueDefinitions()) {
                processInputValueDefinition(inputValueDefinition, beanIntrospection);
            }
        }
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
        } else if (fieldType instanceof TypeName) {
            processInputType((TypeName) fieldType, null, returnType);
        } else {
            // TODO
            throw new RuntimeException("Unknown field type: " + fieldType);
        }
    }

    @Nullable
    private Class<?> processInputType(TypeName typeName, MappingContext mappingContext, Class<?> clazz) {
        TypeDefinition<?> typeDefinition = getTypeDefinition(typeName);

        if (typeDefinition instanceof InputObjectTypeDefinition) {
            processInputObjectTypeDefinition((InputObjectTypeDefinition) typeDefinition, mappingContext, clazz);

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
            throw new UnsupportedOperationException("Unsupported type `" + typeName.getName() + "` with definition " + typeDefinition);
        }
    }

    private boolean registerBeanIntrospectionMapping(String type, BeanIntrospection<?> beanIntrospection) {
        requireNonNull("type", type);
        requireNonNull("beanIntrospection", beanIntrospection);

        MappingItem mappingItem = mappingRegistry.get(type);

        if (mappingItem != null) {
            if (mappingItem.beanIntrospection == null) {
                // TODO
                throw new RuntimeException("Empty bean introspection in mapping item");
            }
            if (!beanIntrospection.equals(mappingItem.beanIntrospection)) {
                // TODO
                throw new RuntimeException("Detected conflicted type");
            }
            // already processed
            return false;
        }

        mappingRegistry.put(type, new MappingItem(beanIntrospection));

        return true;
    }

    private boolean registerUnionMapping(String type, Class<?> interfaceClass) {
        requireNonNull("type", type);
        requireNonNull("interfaceClass", interfaceClass);

        MappingItem mappingItem = mappingRegistry.get(type);

        if (mappingItem != null) {
            if (!interfaceClass.equals(mappingItem.targetInterface)) {
                // TODO
                throw new RuntimeException("Detected conflicted type");
            }
            // already processed
            return false;
        }

        mappingRegistry.put(type, new MappingItem(null, interfaceClass));

        return true;
    }

    private static class MappingItem {

        @Nullable final BeanIntrospection<?> beanIntrospection;

        @Nullable final Class<?> targetEnum;
        @Nullable final Class<?> targetInterface;

        private MappingItem(@NonNull BeanIntrospection<?> beanIntrospection) {
            requireNonNull("beanIntrospection", beanIntrospection);

            this.beanIntrospection = beanIntrospection;
            this.targetEnum = null;
            this.targetInterface = null;
        }

        public MappingItem(@Nullable Class<?> targetEnum, @Nullable Class<?> targetInterface) {
            if (targetEnum == null && targetInterface == null) {
                throw new IllegalArgumentException("targetEnum and targetInterface both can not be null");
            }

            if (targetEnum != null && targetInterface != null) {
                throw new IllegalArgumentException("targetEnum and targetInterface both can not be not-null");
            }

            this.beanIntrospection = null;
            this.targetEnum = targetEnum;
            this.targetInterface = targetInterface;
        }
    }

}
