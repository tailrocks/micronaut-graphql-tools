/*
 * Copyright 2017-2022 original authors
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
import io.micronaut.context.annotation.Infrastructure;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanMethod;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.graphql.tools.annotation.GraphQLInput;
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver;
import io.micronaut.graphql.tools.exceptions.CustomTypeMappedToBuiltInClassException;
import io.micronaut.graphql.tools.exceptions.IncorrectArgumentCountException;
import io.micronaut.graphql.tools.exceptions.IncorrectBuiltInScalarMappingException;
import io.micronaut.graphql.tools.exceptions.InvalidSourceArgumentException;
import io.micronaut.graphql.tools.exceptions.MethodNotFoundException;
import io.micronaut.graphql.tools.exceptions.SchemaDefinitionEmptyException;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.jackson.modules.BeanIntrospectionModule;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.micronaut.core.util.ArgumentUtils.requireNonNull;
import static io.micronaut.graphql.tools.BeanIntrospectionUtils.generateGetMethodName;

/**
 * @author Alexey Zhokhov
 */
@Singleton
@Infrastructure
public class GraphQLMappingContext {

    private static final Map<String, Set<Class>> SYSTEM_TYPES = new HashMap<>();

    // TODO validate all system types
    static {
        SYSTEM_TYPES.put("Int", new HashSet<>(Arrays.asList(int.class, Integer.class)));
        SYSTEM_TYPES.put("Float", new HashSet<>(Arrays.asList(float.class, Float.class)));
        SYSTEM_TYPES.put("String", new HashSet<>(Arrays.asList(String.class)));
        SYSTEM_TYPES.put("Boolean", new HashSet<>(Arrays.asList(boolean.class, Boolean.class)));
        SYSTEM_TYPES.put("ID", new HashSet<>(Arrays.asList(String.class)));
        SYSTEM_TYPES.put("BigDecimal", new HashSet<>(Arrays.asList(BigDecimal.class)));
        SYSTEM_TYPES.put("BigInteger", new HashSet<>(Arrays.asList(BigInteger.class)));
        SYSTEM_TYPES.put("Char", new HashSet<>(Arrays.asList(char.class, Character.class)));
        SYSTEM_TYPES.put("Short", new HashSet<>(Arrays.asList(short.class, Short.class)));
        SYSTEM_TYPES.put("Long", new HashSet<>(Arrays.asList(long.class, Long.class)));
    }

    private static final Set<Class> SYSTEM_TYPES_CACHE = SYSTEM_TYPES.values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    private final ApplicationContext applicationContext;
    private final GraphQLBeanIntrospectionRegistry graphQLBeanIntrospectionRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<BeanDefinitionAndMethods> rootResolvers = new ArrayList<>();
    private final Map<Class, List<BeanDefinitionAndMethods>> typeResolvers = new HashMap<>();

    private final Map<String, MappingItem> mappingRegistry = new HashMap<>();

    private final RuntimeWiring.Builder rootRuntimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
            .wiringFactory(new DefaultWiringFactory());

    private Map<String, TypeDefinition> types;

    public GraphQLMappingContext(ApplicationContext applicationContext,
                                 GraphQLBeanIntrospectionRegistry graphQLBeanIntrospectionRegistry) {
        this.applicationContext = applicationContext;
        this.graphQLBeanIntrospectionRegistry = graphQLBeanIntrospectionRegistry;

        objectMapper.registerModule(new BeanIntrospectionModule());
    }

    public RuntimeWiring generateRuntimeWiring(TypeDefinitionRegistry typeRegistry,
                                               SchemaParserDictionary schemaParserDictionary,
                                               Provider<GraphQLSchema> graphQLSchemaProvider) {
        requireNonNull("typeRegistry", typeRegistry);
        requireNonNull("schemaParserDictionary", schemaParserDictionary);
        requireNonNull("graphQLSchemaProvider", graphQLSchemaProvider);

        types = typeRegistry.types();
        mappingRegistry.clear();

        for (Map.Entry<String, Class> entry : schemaParserDictionary.getUnions().entrySet()) {
            registerUnionMapping(entry.getKey(), entry.getValue());
        }

        // TODO
        /*
        for (Map.Entry<String, Class> entry : schemaParserDictionary.getTypes().entrySet()) {
            BeanIntrospection beanIntrospection =
                    graphQLBeanIntrospectionRegistry.requireGraphQLModel(entry.getValue());

            registerObjectType(entry.getKey(), beanIntrospection);
        }
         */

        SchemaDefinition schemaDefinition = typeRegistry.schemaDefinition()
                .orElseThrow(SchemaDefinitionEmptyException::new);

        for (OperationTypeDefinition operationTypeDefinition : schemaDefinition.getOperationTypeDefinitions()) {
            processOperationTypeDefinition(operationTypeDefinition);
        }

        List<UnionTypeDefinition> unionTypeDefinitions = (List) types.values().stream()
                .filter(it -> it instanceof UnionTypeDefinition)
                .collect(Collectors.toList());

        for (UnionTypeDefinition unionTypeDefinition : unionTypeDefinitions) {
            processUnionTypeDefinition(unionTypeDefinition, graphQLSchemaProvider);
        }

        return rootRuntimeWiringBuilder.build();
    }

    void registerRootExecutableMethod(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        rootResolvers.stream().filter(it -> it.beanDefinition.equals(beanDefinition)).findFirst().orElseGet(() -> {
            BeanDefinitionAndMethods item = new BeanDefinitionAndMethods(beanDefinition);
            rootResolvers.add(item);
            return item;
        }).addExecutableMethod(method);
    }

    void registerTypeExecutableMethod(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        Optional<AnnotationClassValue<Class>> annotationValue =
                (Optional) beanDefinition.getValue(GraphQLTypeResolver.class, AnnotationMetadata.VALUE_MEMBER);

        if (!annotationValue.isPresent()) {
            throw new RuntimeException("Empty value for " + GraphQLTypeResolver.class.getSimpleName() + " annotation");
        }

        Class<?> modelClass = annotationValue.get().getType().get();

        typeResolvers.putIfAbsent(modelClass, new ArrayList<>());

        typeResolvers.get(modelClass).stream().filter(it -> it.beanDefinition.equals(beanDefinition)).findFirst().orElseGet(() -> {
            BeanDefinitionAndMethods item = new BeanDefinitionAndMethods(beanDefinition);
            typeResolvers.get(modelClass).add(item);
            return item;
        }).addExecutableMethod(method);
    }

    private TypeDefinition getTypeDefinition(TypeName typeName) {
        return Optional.ofNullable(types.get(typeName.getName())).orElseThrow(() -> {
            // TODO
            throw new RuntimeException("TypeDefinition not found by name: " + typeName.getName());
        });
    }

    private boolean isUnion(Class modelInterface) {
        for (Map.Entry<String, MappingItem> entry : mappingRegistry.entrySet()) {
            if (modelInterface.equals(entry.getValue().targetInterface)) {
                return true;
            }
        }
        return false;
    }

    private void processOperationTypeDefinition(OperationTypeDefinition operationTypeDefinition) {
        ObjectTypeDefinition typeDefinition = (ObjectTypeDefinition)
                getTypeDefinition(operationTypeDefinition.getTypeName());

        rootRuntimeWiringBuilder.type(operationTypeDefinition.getTypeName().getName(), (typeWiring) -> {
            for (FieldDefinition fieldDefinition : typeDefinition.getFieldDefinitions()) {
                processRootFieldDefinition(fieldDefinition, typeDefinition, typeWiring);
            }

            return typeWiring;
        });
    }

    private void processRootFieldDefinition(FieldDefinition fieldDefinition,
                                            ObjectTypeDefinition objectTypeDefinition,
                                            TypeRuntimeWiring.Builder runtimeWiringBuilder) {
        BeanDefinitionAndMethod beanDefinitionAndMethod = findExecutableMethodByFieldName(fieldDefinition.getName());

        if (beanDefinitionAndMethod == null) {
            throw new MethodNotFoundException(fieldDefinition.getName());
        }

        checkInputValueDefinitions(fieldDefinition, objectTypeDefinition, beanDefinitionAndMethod.executableMethod, null);

        List<ArgumentDetails> argumentDetails = processInputArguments(
                fieldDefinition, objectTypeDefinition, beanDefinitionAndMethod.executableMethod, null
        );

        runtimeWiringBuilder.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                objectMapper,
                beanDefinitionAndMethod.executableMethod,
                argumentDetails,
                applicationContext.getBean(beanDefinitionAndMethod.beanDefinition)
        ));

        if (!(fieldDefinition.getType() instanceof ListType) &&
                getTypeDefinition(getTypeName(fieldDefinition.getType())) instanceof UnionTypeDefinition) {
            // TODO skip for now
        } else {
            ReturnType<Object> returnType = beanDefinitionAndMethod.executableMethod.getReturnType();

            Class clazz = unwrapArgument(returnType.asArgument()).getType();

            processReturnType(
                    fieldDefinition,
                    objectTypeDefinition,
                    fieldDefinition.getType(),
                    clazz,
                    beanDefinitionAndMethod.executableMethod.getDeclaringType(),
                    beanDefinitionAndMethod.executableMethod.getMethodName()
            );
        }
    }

    private List<ArgumentDetails> processInputArguments(
            FieldDefinition fieldDefinition,
            ObjectTypeDefinition objectTypeDefinition,
            Executable<Object, Object> executable,
            @Nullable Class sourceClass
    ) {
        List<InputValueDefinition> inputs = fieldDefinition.getInputValueDefinitions();
        List<Argument> arguments = Arrays.stream(executable.getArguments()).collect(Collectors.toList());

        if (inputs.isEmpty() && arguments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Class> argumentClasses = arguments.stream()
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
                        objectTypeDefinition.getName(),
                        fieldDefinition.getName(),
                        executable.getDeclaringType(),
                        getExecutableMethodFullName(executable),
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
            Class returnType = argumentClasses.get(i);
            int position = i + 1;

            TypeName typeName = (TypeName) unwrapNonNullType(inputValueDefinition.getType());

            // FIXME
            Class inputClass = processInputTypeDefinition(inputValueDefinition, typeName, returnType, null, null);

            result.add(new ArgumentDetails(inputs.get(i).getName(), inputClass));
        }

        if (containsEnvironmentArgument) {
            result.add(new ArgumentDetails(ArgumentDetails.DATA_FETCHING_ENVIRONMENT_ARGUMENT, null));
        }

        return result;
    }

    private void processUnionTypeDefinition(UnionTypeDefinition unionTypeDefinition,
                                            Provider<GraphQLSchema> graphQLSchemaProvider) {
        rootRuntimeWiringBuilder.type(unionTypeDefinition.getName(), (typeWiring) -> {
            List<String> graphQlTypes = unionTypeDefinition.getMemberTypes().stream()
                    .map(it -> ((TypeName) it).getName())
                    .collect(Collectors.toList());

            Map<Class, String> objectTypes = new HashMap<>();

            for (String graphQlType : graphQlTypes) {
                MappingItem mappingItem = mappingRegistry.get(graphQlType);

                if (mappingItem == null) {
                    // TODO
                    throw new RuntimeException("Mapping item not found for type: " + graphQlType);
                }
                objectTypes.put(mappingItem.beanIntrospection.getBeanType(), graphQlType);
            }

            typeWiring.typeResolver(new UnionTypeResolver(graphQLSchemaProvider, objectTypes));
            return typeWiring;
        });
    }

    @Nullable
    private Class processInputTypeDefinition(InputValueDefinition inputValueDefinition,
                                             TypeName typeName,
                                             Class clazz,
                                             Class mappedClass,
                                             String mappedMethodName) {
        TypeDefinition typeDefinition = getTypeDefinition(typeName);

        if (typeDefinition instanceof InputObjectTypeDefinition) {
            processInputObjectTypeDefinition((InputObjectTypeDefinition) typeDefinition, clazz);

            return clazz;
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            registerEnumMapping((EnumTypeDefinition) typeDefinition, clazz);

            return null;
        } else if (isBuiltInType(typeName)) {
            Set<Class> supportedClasses = SYSTEM_TYPES.get(typeName);

            if (!supportedClasses.contains(clazz)) {
                throw new IncorrectBuiltInScalarMappingException(
                        typeName.getName(),
                        inputValueDefinition.getName(),
                        mappedClass,
                        mappedMethodName,
                        clazz,
                        supportedClasses
                );
            }

            return null;
        } else {
            throw new UnsupportedOperationException("Unsupported type `" + typeName.getName() + "` with definition " + typeDefinition);
        }
    }

    private void processInputObjectTypeDefinition(InputObjectTypeDefinition objectTypeDefinition, Class targetClass) {
        BeanIntrospection beanIntrospection = BeanIntrospection.getIntrospection(targetClass);

        if (beanIntrospection == null) {
            throw new RuntimeException("No bean introspection found for: " + targetClass + ". Probably " + GraphQLInput.class.getSimpleName() + " annotation was not added or processed by Micronaut.");
        }

        if (registerBeanIntrospectionMapping(objectTypeDefinition.getName(), beanIntrospection)) {
            for (InputValueDefinition inputValueDefinition : objectTypeDefinition.getInputValueDefinitions()) {
                // TODO Maybe can be moved to some method???

                Optional<BeanProperty<Object, Object>> property = beanIntrospection.getProperty(inputValueDefinition.getName());

                if (!property.isPresent()) {
                    // TODO custom exception
                    throw new RuntimeException("Property `" + inputValueDefinition.getName() + "` not found in: " + beanIntrospection.getBeanType());
                }

                Type fieldType = unwrapNonNullType(inputValueDefinition.getType());
                Class returnType = property.get().getType();

                if (fieldType instanceof ListType) {
                    if (!returnType.equals(List.class)) {
                        throw new RuntimeException("Wrong return type");
                    }

                    // TODO validate primitive type

                    // TODO process sub types
                    //throw new UnsupportedOperationException();
                } else if (fieldType instanceof TypeName) {
                    // TODO custom exception
                    processInputTypeDefinition(inputValueDefinition, (TypeName) fieldType, returnType, null, null);
                } else {
                    throw new RuntimeException("Unknown field type: " + fieldType);
                }
            }
        }
    }

    private void checkInputValueDefinitions(FieldDefinition fieldDefinition, BeanProperty beanProperty) {
        if (fieldDefinition.getInputValueDefinitions().size() == 0) {
            return;
        }

        int requiredArgs = fieldDefinition.getInputValueDefinitions().size();
        if (requiredArgs > 0) {
            // TODO custom exception
            throw new RuntimeException("Too less arguments in the property `" + beanProperty.getName() + "` (" + beanProperty.getDeclaringType() + "), required " + requiredArgs + " (excluded DataFetchingEnvironment)");
        }
    }

    private void checkInputValueDefinitions(FieldDefinition fieldDefinition,
                                            ObjectTypeDefinition objectTypeDefinition,
                                            Executable executable,
                                            Class sourceClass) {
        int requiredArgs = fieldDefinition.getInputValueDefinitions().size();

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
            String parameterName = objectTypeDefinition.getName().substring(0, 1).toLowerCase() +
                    objectTypeDefinition.getName().substring(1);
            suggestedMethodArgs.add(sourceClass.getName() + " " + parameterName);
        }

        suggestedMethodArgs.addAll(
                fieldDefinition.getInputValueDefinitions().stream()
                        .map(it -> getTypeName(it.getType()) + " " + it.getName())
                        .collect(Collectors.toList())
        );

        String suggestedMethodArgsAsString = suggestedMethodArgs.isEmpty() ? null : "(" + suggestedMethodArgs.stream()
                .collect(Collectors.joining(", ")) + ")";

        if (currentArgs > requiredArgs) {
            throw new IncorrectArgumentCountException(
                    false,
                    objectTypeDefinition.getName(),
                    fieldDefinition.getName(),
                    executable.getDeclaringType(),
                    getExecutableMethodFullName(executable),
                    currentArgs,
                    requiredArgs,
                    suggestedMethodArgsAsString
            );
        } else {
            throw new IncorrectArgumentCountException(
                    true,
                    objectTypeDefinition.getName(),
                    fieldDefinition.getName(),
                    executable.getDeclaringType(),
                    getExecutableMethodFullName(executable),
                    currentArgs,
                    requiredArgs,
                    suggestedMethodArgsAsString
            );
        }
    }

    private String getExecutableMethodFullName(Executable executable) {
        if (executable instanceof ExecutableMethod) {
            ExecutableMethod executableMethod = (ExecutableMethod) executable;

            String args = Arrays.stream(executableMethod.getArguments())
                    .map(arg -> arg.getTypeString(false) + " " + arg.getName())
                    .collect(Collectors.joining(", "));
            return executableMethod.getName() + "(" + args + ")";
        } else if (executable instanceof BeanMethod) {
            BeanMethod beanMethod = (BeanMethod) executable;
            return beanMethod.getName();
        } else {
            throw new UnsupportedOperationException("Unsupported executable class: " + executable.getClass());
        }
    }

    private void processObjectTypeDefinition(ObjectTypeDefinition objectTypeDefinition, BeanIntrospection beanIntrospection) {
        requireNonNull("typeDefinition", objectTypeDefinition);

        rootRuntimeWiringBuilder.type(objectTypeDefinition.getName(), (typeWiring) -> {
            typeWiring.defaultDataFetcher(new MicronautIntrospectionDataFetcher(beanIntrospection));

            for (FieldDefinition fieldDefinition : objectTypeDefinition.getFieldDefinitions()) {
                Optional<BeanProperty> beanProperty = beanIntrospection.getProperty(fieldDefinition.getName());
                List<BeanMethod> beanMethods = ((Collection<BeanMethod>) beanIntrospection.getBeanMethods()).stream()
                        .filter(it -> it.getName().equals(fieldDefinition.getName()) || it.getName().equals(generateGetMethodName(fieldDefinition.getName())))
                        .collect(Collectors.toList());

                if (beanProperty.isPresent() && !beanMethods.isEmpty()) {
                    throw new RuntimeException("Found `" + fieldDefinition.getName() + "` property and bean method: " + beanIntrospection.getBeanType());
                }

                if (beanMethods.size() > 1) {
                    throw new RuntimeException("Found multiple bean methods `" + fieldDefinition.getName() + "`: " + beanIntrospection.getBeanType());
                }

                // TODO validate parameters (schema and methods)

                if (!beanProperty.isPresent() && beanMethods.isEmpty()) {
                    Class interfaceClass = graphQLBeanIntrospectionRegistry.getInterfaceClass(beanIntrospection.getBeanType());

                    // TODO better exception
                    BeanDefinitionAndMethod beanDefinitionAndMethod = findModelExecutableMethod(interfaceClass, fieldDefinition.getName())
                            .orElseThrow(() -> new RuntimeException("Property or method `" + fieldDefinition.getName() + "` not found: " + beanIntrospection.getBeanType()));

                    // TODO validate parameters for executable method

                    // count with source argument
                    checkInputValueDefinitions(fieldDefinition, objectTypeDefinition,
                            beanDefinitionAndMethod.executableMethod, interfaceClass);

                    List<ArgumentDetails> argumentDetails = processInputArguments(
                            fieldDefinition, objectTypeDefinition, beanDefinitionAndMethod.executableMethod,
                            interfaceClass
                    );

                    typeWiring.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                            objectMapper,
                            beanDefinitionAndMethod.executableMethod,
                            argumentDetails,
                            applicationContext.getBean(beanDefinitionAndMethod.beanDefinition)
                    ));

                    Argument argument = beanDefinitionAndMethod.executableMethod.getReturnType().asArgument();

                    processReturnTypeForBeanProperty(fieldDefinition, objectTypeDefinition, argument);
                } else if (beanProperty.isPresent()) {
                    Argument argument = beanProperty.get().asArgument();

                    checkInputValueDefinitions(fieldDefinition, beanProperty.get());

                    processReturnTypeForBeanProperty(fieldDefinition, objectTypeDefinition, argument);
                } else if (!beanMethods.isEmpty()) {
                    BeanMethod beanMethod = beanMethods.get(0);

                    checkInputValueDefinitions(fieldDefinition, objectTypeDefinition, beanMethod, null);

                    List<ArgumentDetails> argumentDetails = processInputArguments(
                            fieldDefinition, objectTypeDefinition, beanMethod, null
                    );

                    typeWiring.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                            objectMapper,
                            beanMethod,
                            argumentDetails,
                            null
                    ));

                    Argument argument = beanMethod.getReturnType().asArgument();

                    processReturnTypeForBeanProperty(fieldDefinition, objectTypeDefinition, argument);
                }
            }

            return typeWiring;
        });
    }

    private void processReturnTypeForBeanProperty(FieldDefinition fieldDefinition,
                                                  ObjectTypeDefinition objectTypeDefinition,
                                                  Argument argument) {
        Argument unwrappedArgument = unwrapArgument(argument);

        Type fieldType = unwrapNonNullType(fieldDefinition.getType());
        Class returnType = unwrappedArgument.getType();

        if (fieldType instanceof ListType) {
            if (!returnType.equals(List.class)) {
                // TODO make the message more clear
                throw new RuntimeException("Wrong return type");
            }

            Type listFieldType = ((ListType) fieldType).getType();
            // TODO check
            Class listReturnType = unwrappedArgument.getFirstTypeVariable().get().getType();

            processReturnType(
                    fieldDefinition,
                    objectTypeDefinition,
                    listFieldType,
                    listReturnType,
                    argument.getType(),
                    argument.getName()
            );
        } else {
            processReturnType(
                    fieldDefinition,
                    objectTypeDefinition,
                    fieldType,
                    returnType,
                    argument.getType(),
                    argument.getName()
            );
        }
    }

    private Argument unwrapArgument(Argument argument) {
        if (argument.isAsync()) {
            return argument.getFirstTypeVariable().get();
        }

        if (argument.isReactive()) {
            return argument.getFirstTypeVariable().get();
        }

        return argument;
    }

    private Optional<BeanDefinitionAndMethod> findModelExecutableMethod(Class beanType, String methodName) {
        List<BeanDefinitionAndMethods> items = typeResolvers.get(beanType);

        if (items == null) {
            return Optional.empty();
        }

        for (BeanDefinitionAndMethods item : items) {
            for (ExecutableMethod executableMethod : item.executableMethods) {
                if (executableMethod.getMethodName().equals(methodName)) {
                    return Optional.of(new BeanDefinitionAndMethod(item.beanDefinition, executableMethod));
                }
            }
        }

        return Optional.empty();
    }

    private void processReturnType(
            FieldDefinition fieldDefinition,
            ObjectTypeDefinition objectTypeDefinition,
            Type graphQlType,
            Class returnType,
            Class mappedClass,
            String mappedMethodName
    ) {
        if (!(graphQlType instanceof ListType) &&
                getTypeDefinition(getTypeName(graphQlType)) instanceof UnionTypeDefinition) {
            // TODO skip for now
        } else {
            if (!(graphQlType instanceof TypeName)) {
                throw new UnsupportedOperationException("Unsupported type: " + graphQlType);
            }

            TypeName typeName = (TypeName) graphQlType;

            if (isBuiltInType(typeName)) {
                Set<Class> supportedClasses = SYSTEM_TYPES.get(getTypeName(graphQlType).getName());

                if (!supportedClasses.contains(returnType)) {
                    throw new IncorrectBuiltInScalarMappingException(
                            objectTypeDefinition.getName(),
                            fieldDefinition.getName(),
                            mappedClass,
                            mappedMethodName,
                            returnType,
                            supportedClasses
                    );
                }
            } else {
                TypeDefinition typeDefinition = getTypeDefinition(typeName);

                if (typeDefinition instanceof EnumTypeDefinition) {
                    registerEnumMapping((EnumTypeDefinition) typeDefinition, returnType);
                } else {
                    if (isBuiltInType(returnType)) {
                        throw new CustomTypeMappedToBuiltInClassException(
                                objectTypeDefinition.getName(),
                                fieldDefinition.getName(),
                                mappedClass,
                                mappedMethodName,
                                returnType
                        );
                    }

                    if (!(typeDefinition instanceof ObjectTypeDefinition)) {
                        // TODO custom exception
                        throw new RuntimeException("Must be ObjectTypeDefinition for type `" + typeName.getName() + "`, but " +
                                "found: " + typeDefinition);
                    }

                    BeanIntrospection beanIntrospection = graphQLBeanIntrospectionRegistry.getGraphQlTypeBeanIntrospection(
                            returnType,
                            fieldDefinition,
                            objectTypeDefinition,
                            mappedClass,
                            mappedMethodName
                    );

                    if (registerBeanIntrospectionMapping(typeDefinition.getName(), beanIntrospection)) {
                        // TODO compare fields

                        processObjectTypeDefinition((ObjectTypeDefinition) typeDefinition, beanIntrospection);
                    }
                }
            }
        }
    }

    private boolean registerBeanIntrospectionMapping(String type, BeanIntrospection beanIntrospection) {
        requireNonNull("type", type);
        requireNonNull("beanIntrospection", beanIntrospection);

        MappingItem mappingItem = mappingRegistry.get(type);

        if (mappingItem != null) {
            if (mappingItem.beanIntrospection == null) {
                throw new RuntimeException("Empty bean introspection in mapping item");
            }
            if (!beanIntrospection.equals(mappingItem.beanIntrospection)) {
                throw new RuntimeException("Detected conflicted type");
            }
            // already processed
            return false;
        }

        mappingRegistry.put(type, new MappingItem(beanIntrospection));

        return true;
    }

    private void registerEnumMapping(EnumTypeDefinition typeDefinition, Class targetClass) {
        requireNonNull("typeDefinition", typeDefinition);
        requireNonNull("targetClass", targetClass);

        if (!targetClass.isEnum()) {
            throw new RuntimeException("Target class is not enum " + targetClass + " for definition " + typeDefinition);
        }

        String type = typeDefinition.getName();

        MappingItem mappingItem = mappingRegistry.get(type);

        if (mappingItem != null) {
            if (!targetClass.equals(mappingItem.targetEnum)) {
                throw new RuntimeException("Detected conflicted type");
            }

            // already processed
            return;
        }

        List<String> expectedValues = typeDefinition.getEnumValueDefinitions().stream()
                .map(EnumValueDefinition::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> existingValues = Arrays.stream(targetClass.getEnumConstants()).map(it -> ((Enum) it).name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> unresolvedValues = expectedValues.stream()
                .filter(it -> !existingValues.contains(it))
                .collect(Collectors.toList());

        if (!unresolvedValues.isEmpty()) {
            throw new RuntimeException("Found unresolved enums: " + unresolvedValues);
        }

        mappingRegistry.put(type, new MappingItem(targetClass, null));
    }

    private boolean registerUnionMapping(String type, Class interfaceClass) {
        requireNonNull("type", type);
        requireNonNull("interfaceClass", interfaceClass);

        // TODO check if it's an interface

        MappingItem mappingItem = mappingRegistry.get(type);

        if (mappingItem != null) {
            if (!interfaceClass.equals(mappingItem.targetInterface)) {
                throw new RuntimeException("Detected conflicted type");
            }
            // already processed
            return false;
        }

        mappingRegistry.put(type, new MappingItem(null, interfaceClass));

        return true;
    }

    private static TypeName getTypeName(Type type) {
        type = unwrapNonNullType(type);
        if (type instanceof TypeName) {
            return ((TypeName) type);
        }
        throw new UnsupportedOperationException("Unknown type: " + type);
    }

    private static Type unwrapNonNullType(Type type) {
        if (type instanceof NonNullType) {
            return unwrapNonNullType(((NonNullType) type).getType());
        }
        return type;
    }

    private BeanDefinitionAndMethod findExecutableMethodByFieldName(String operationName) {
        // TODO optimize me pls
        for (BeanDefinitionAndMethods resolverItem : rootResolvers) {
            for (ExecutableMethod executableMethod : resolverItem.executableMethods) {
                if (executableMethod.getMethodName().equals(operationName)) {
                    return new BeanDefinitionAndMethod(resolverItem.beanDefinition, executableMethod);
                }
            }
        }
        return null;
    }

    private static boolean isBuiltInType(TypeName typeName) {
        return SYSTEM_TYPES.containsKey(typeName.getName());
    }

    private static boolean isBuiltInType(Class clazz) {
        return SYSTEM_TYPES_CACHE.contains(clazz);
    }

    private class BeanDefinitionAndMethod {
        final BeanDefinition<Object> beanDefinition;
        final ExecutableMethod<Object, Object> executableMethod;

        private BeanDefinitionAndMethod(@NonNull BeanDefinition beanDefinition, @NonNull ExecutableMethod executableMethod) {
            requireNonNull("beanDefinition", beanDefinition);
            requireNonNull("executableMethod", executableMethod);

            this.beanDefinition = beanDefinition;
            this.executableMethod = executableMethod;
        }
    }

    private class BeanDefinitionAndMethods {
        final BeanDefinition<Object> beanDefinition;
        final Set<ExecutableMethod<Object, Object>> executableMethods = new HashSet<>();

        private BeanDefinitionAndMethods(@NonNull BeanDefinition beanDefinition) {
            requireNonNull("beanDefinition", beanDefinition);

            this.beanDefinition = beanDefinition;
        }

        private void addExecutableMethod(@NonNull ExecutableMethod executableMethod) {
            requireNonNull("executableMethod", executableMethod);

            executableMethods.add(executableMethod);
        }
    }

    private class MappingItem {

        @Nullable final BeanIntrospection beanIntrospection;

        @Nullable final Class targetEnum;
        @Nullable final Class targetInterface;

        private MappingItem(@NonNull BeanIntrospection beanIntrospection) {
            requireNonNull("beanIntrospection", beanIntrospection);

            this.beanIntrospection = beanIntrospection;
            this.targetEnum = null;
            this.targetInterface = null;
        }

        public MappingItem(@Nullable Class targetEnum, @Nullable Class targetInterface) {
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
