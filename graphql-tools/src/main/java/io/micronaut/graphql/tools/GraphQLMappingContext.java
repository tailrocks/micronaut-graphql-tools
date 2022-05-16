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
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.beans.BeanMethod;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import io.micronaut.core.type.ReturnType;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.graphql.tools.annotation.GraphQLInput;
import io.micronaut.graphql.tools.annotation.GraphQLType;
import io.micronaut.graphql.tools.annotation.GraphQLTypeResolver;
import io.micronaut.graphql.tools.exceptions.IncorrectBuiltInScalarMappingException;
import io.micronaut.graphql.tools.exceptions.IncorrectMappingException;
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
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.micronaut.graphql.tools.BeanIntrospectionUtils.generateGetMethodName;

/**
 * @author Alexey Zhokhov
 */
@Singleton
@Infrastructure
public class GraphQLMappingContext {

    private static final Map<String, Set<Class>> SYSTEM_TYPES = new HashMap<>();

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<BeanDefinitionAndMethods> rootResolvers = new ArrayList<>();
    private final Map<Class, List<BeanDefinitionAndMethods>> typeResolvers = new HashMap<>();

    private final Map<String, MappingItem> mappingRegistry = new HashMap<>();

    // key: implementation
    // value: related interface
    private final Map<Class, Class> typeInterfaces = new HashMap<>();

    private final Map<Class, BeanIntrospection> typeIntrospections = new HashMap<>();
    private final Map<Class, BeanIntrospection> inputIntrospections = new HashMap<>();

    private final RuntimeWiring.Builder rootRuntimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
            .wiringFactory(new DefaultWiringFactory());

    private Map<String, TypeDefinition> types;

    public GraphQLMappingContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        objectMapper.registerModule(new BeanIntrospectionModule());
    }

    public RuntimeWiring generateRuntimeWiring(TypeDefinitionRegistry typeRegistry,
                                               SchemaParserDictionary schemaParserDictionary,
                                               Provider<GraphQLSchema> graphQLSchemaProvider) {
        ArgumentUtils.requireNonNull("typeRegistry", typeRegistry);
        ArgumentUtils.requireNonNull("schemaParserDictionary", schemaParserDictionary);

        types = typeRegistry.types();
        mappingRegistry.clear();

        for (Map.Entry<String, Class> entry : schemaParserDictionary.getUnions().entrySet()) {
            registerUnionMapping(entry.getKey(), entry.getValue());
        }

        loadTypeIntrospections();
        loadInputIntrospections();

        for (Map.Entry<String, Class> entry : schemaParserDictionary.getTypes().entrySet()) {
            BeanIntrospection beanIntrospection = requireGraphQLModel(entry.getValue());

            registerObjectType(entry.getKey(), beanIntrospection);
        }

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

    private void loadTypeIntrospections() {
        typeIntrospections.clear();
        typeInterfaces.clear();

        for (BeanIntrospection<Object> introspection : BeanIntrospector.SHARED.findIntrospections(GraphQLType.class)) {
            if (
                    introspection.getBeanType().isInterface()
                            || introspection.getBeanType().isEnum()
                            || introspection.getBeanType().isAnnotation()
            ) {
                throw new RuntimeException(GraphQLType.class.getSimpleName() + " annotation can be used only on classes. Found wrong usage: " + introspection.getBeanType());
            }
            typeIntrospections.put(introspection.getBeanType(), introspection);

            AnnotationValue<GraphQLType> annotationValue = introspection.getAnnotation(GraphQLType.class);
            Class modelInterface = annotationValue.get(AnnotationMetadata.VALUE_MEMBER, Class.class).get();

            if (!modelInterface.equals(Void.class)) {
                if (!isUnion(modelInterface)) {
                    if (typeInterfaces.containsValue(modelInterface)) {
                        throw new RuntimeException("Found multiple implementations for: " + modelInterface);
                    }

                    typeInterfaces.put(introspection.getBeanType(), modelInterface);

                    if (!modelInterface.isAssignableFrom(introspection.getBeanType())) {
                        throw new RuntimeException(introspection.getBeanType() + " is not implementing " + modelInterface);
                    }
                }
            }
        }
    }

    private boolean isUnion(Class modelInterface) {
        for (Map.Entry<String, MappingItem> entry : mappingRegistry.entrySet()) {
            if (modelInterface.equals(entry.getValue().targetInterface)) {
                return true;
            }
        }
        return false;
    }

    private void loadInputIntrospections() {
        inputIntrospections.clear();

        for (BeanIntrospection<Object> introspection : BeanIntrospector.SHARED.findIntrospections(GraphQLInput.class)) {
            if (
                    introspection.getBeanType().isInterface()
                            || introspection.getBeanType().isEnum()
                            || introspection.getBeanType().isAnnotation()
            ) {
                throw new RuntimeException(GraphQLInput.class.getSimpleName() + " annotation can be used only on classes. Found wrong usage: " + introspection.getBeanType());
            }
            inputIntrospections.put(introspection.getBeanType(), introspection);
        }
    }

    private void processOperationTypeDefinition(OperationTypeDefinition operationTypeDefinition) {
        ObjectTypeDefinition typeDefinition = (ObjectTypeDefinition)
                types.get(operationTypeDefinition.getTypeName().getName());

        rootRuntimeWiringBuilder.type(operationTypeDefinition.getTypeName().getName(), (typeWiring) -> {
            for (FieldDefinition fieldDefinition : typeDefinition.getFieldDefinitions()) {
                processRootFieldDefinition(fieldDefinition, typeWiring);
            }

            return typeWiring;
        });
    }

    private void processRootFieldDefinition(FieldDefinition fieldDefinition,
                                            TypeRuntimeWiring.Builder runtimeWiringBuilder) {
        BeanDefinitionAndMethod beanDefinitionAndMethod = findExecutableMethodByFieldName(fieldDefinition.getName());

        if (beanDefinitionAndMethod == null) {
            throw new MethodNotFoundException(fieldDefinition.getName());
        }

        checkInputValueDefinitions(fieldDefinition, beanDefinitionAndMethod.executableMethod, null);

        List<ArgumentDetails> argumentDetails = processInputArguments(
                fieldDefinition, beanDefinitionAndMethod.executableMethod, null
        );

        runtimeWiringBuilder.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                objectMapper,
                beanDefinitionAndMethod.executableMethod,
                argumentDetails,
                applicationContext.getBean(beanDefinitionAndMethod.beanDefinition)
        ));

        if (!(fieldDefinition.getType() instanceof ListType) &&
                types.get(getTypeName(fieldDefinition.getType())) instanceof UnionTypeDefinition) {
            // TODO skip for now
        } else {
            detectReturnType(fieldDefinition, beanDefinitionAndMethod.executableMethod);

            ReturnType<Object> returnType = beanDefinitionAndMethod.executableMethod.getReturnType();

            if (returnType.getType().isAssignableFrom(CompletionStage.class)) {
                Class clazz = returnType.asArgument().getFirstTypeVariable().get().getType();

                processReturnType(
                        fieldDefinition,
                        fieldDefinition.getType(),
                        clazz,
                        beanDefinitionAndMethod.executableMethod.getDeclaringType(),
                        beanDefinitionAndMethod.executableMethod.getMethodName()
                );
            } else {
                processReturnType(
                        fieldDefinition,
                        fieldDefinition.getType(),
                        returnType.getType(),
                        beanDefinitionAndMethod.executableMethod.getDeclaringType(),
                        beanDefinitionAndMethod.executableMethod.getMethodName()
                );
            }
        }
    }

    private List<ArgumentDetails> processInputArguments(
            FieldDefinition fieldDefinition,
            Executable<Object, Object> executable,
            @Nullable Class sourceClass
    ) {
        List<InputValueDefinition> inputs = fieldDefinition.getInputValueDefinitions();
        List<Argument> arguments = Arrays.stream(executable.getArguments()).collect(Collectors.toList());

        if (inputs.isEmpty() && arguments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Class> argumentClasses = arguments.stream()
                .map(it -> it.getType())
                .collect(Collectors.toList());

        boolean containsSourceArgument = false;

        if (sourceClass != null) {
            if (sourceClass.isInterface()) {
                if (sourceClass.isAssignableFrom(argumentClasses.get(0))) {
                    argumentClasses.remove(0);

                    containsSourceArgument = true;
                }
            } else {
                if (argumentClasses.get(0).equals(sourceClass)) {
                    argumentClasses.remove(0);

                    containsSourceArgument = true;
                }
            }
        }

        boolean containsEnvironmentArgument = false;

        if (argumentClasses.get(argumentClasses.size() - 1).equals(DataFetchingEnvironment.class)) {
            argumentClasses.remove(argumentClasses.size() - 1);

            containsEnvironmentArgument = true;
        }

        if (inputs.size() != argumentClasses.size()) {
            // TODO
            throw new RuntimeException("Wrong arguments count");
        }

        ArrayList<ArgumentDetails> result = new ArrayList<>();

        if (containsSourceArgument) {
            result.add(new ArgumentDetails(ArgumentDetails.SOURCE_ARGUMENT, null));
        }

        for (int i = 0; i < inputs.size(); i++) {
            Class inputClass = validateAndRegister(inputs.get(i), argumentClasses.get(i), executable, i + 1);

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
    private Class validateAndRegister(InputValueDefinition inputValueDefinition, Class clazz,
                                      Executable<Object, Object> executable,
                                      int position) {
        TypeName typeName = (TypeName) skipNonNullType(inputValueDefinition.getType());

        // TODO custom exception
        return processInputTypeDefinition(typeName, clazz, (incorrectBuiltInScalarMappingException) ->
                incorrectBuiltInScalarMappingException.withMethodName(getExecutableMethodFullName(executable)).withPosition(position));
    }

    @Nullable
    private Class processInputTypeDefinition(TypeName typeName, Class clazz,
                                             Consumer<IncorrectBuiltInScalarMappingException> exceptionConsumer) {
        TypeDefinition typeDefinition = types.get(typeName.getName());

        if (typeDefinition instanceof InputObjectTypeDefinition) {
            processInputObjectTypeDefinition((InputObjectTypeDefinition) typeDefinition, clazz);

            return clazz;
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            processEnumTypeDefinition((EnumTypeDefinition) typeDefinition, clazz);

            return null;
        } else if (isBuiltInType(typeName)) {
            validateBuiltInType(typeName.getName(), clazz, exceptionConsumer);

            return null;
        } else {
            throw new RuntimeException("Unsupported type `" + typeName.getName() + "` with definition " + typeDefinition);
        }
    }

    private void validateBuiltInType(String graphQlTypeName, Class providedClass,
                                     @Nullable Consumer<IncorrectBuiltInScalarMappingException> exceptionConsumer) {
        Set<Class> requiredClass = SYSTEM_TYPES.get(graphQlTypeName);

        if (!requiredClass.contains(providedClass)) {
            IncorrectBuiltInScalarMappingException exception = new IncorrectBuiltInScalarMappingException(
                    graphQlTypeName, providedClass, requiredClass
            );
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(exception);
            }
            throw exception;
        }
    }

    private void processEnumTypeDefinition(EnumTypeDefinition typeDefinition, Class targetClass) {
        if (targetClass.isEnum()) {
            if (registerEnumMapping(typeDefinition.getName(), targetClass)) {
                // TODO compare values
            }
        } else {
            throw new RuntimeException("Target class is not enum " + targetClass + " for definition " + typeDefinition);
        }
    }

    private void processInputObjectTypeDefinition(InputObjectTypeDefinition objectTypeDefinition, Class targetClass) {
        BeanIntrospection beanIntrospection = inputIntrospections.get(targetClass);

        if (beanIntrospection == null) {
            throw new RuntimeException("No bean introspection found for: " + targetClass + ". Probably " + GraphQLInput.class.getSimpleName() + " annotation was not added or processed by Micronaut.");
        }

        if (registerBeanIntrospectionMapping(objectTypeDefinition.getName(), beanIntrospection)) {
            for (InputValueDefinition inputValueDefinition : objectTypeDefinition.getInputValueDefinitions()) {
                Optional<BeanProperty<Object, Object>> property = beanIntrospection.getProperty(inputValueDefinition.getName());

                if (!property.isPresent()) {
                    // TODO custom exception
                    throw new RuntimeException("Property `" + inputValueDefinition.getName() + "` not found in: " + beanIntrospection.getBeanType());
                }

                Type fieldType = skipNonNullType(inputValueDefinition.getType());
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
                    processInputTypeDefinition((TypeName) fieldType, returnType, (requiredClass) -> new RuntimeException("Invalid type in property `" + property.get().getName() + "` (" + beanIntrospection.getBeanType() + "), required: " + requiredClass));
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

    private void checkInputValueDefinitions(FieldDefinition fieldDefinition, Executable executable,
                                            @Nullable String sourceType) {
        if (fieldDefinition.getInputValueDefinitions().size() == 0 && executable.getArguments().length == 0) {
            return;
        }

        int requiredArgs = fieldDefinition.getInputValueDefinitions().size() + (sourceType != null ? 1 : 0);

        ArrayList<String> methodArgs = new ArrayList<>();

        if (sourceType != null) {
            methodArgs.add(sourceType + " " + sourceType.substring(0, 1).toLowerCase() + sourceType.substring(1));
        }

        methodArgs.addAll(
                fieldDefinition.getInputValueDefinitions().stream()
                        .map(it -> getTypeName(it.getType()) + " " + it.getName())
                        .collect(Collectors.toList())
        );

        String methodArgsString = methodArgs.stream().collect(Collectors.joining(", "));

        if (requiredArgs > executable.getArguments().length) {
            // TODO custom exception
            throw new RuntimeException("Too less arguments in the method " + getExecutableMethodFullName(executable) + ", required " + requiredArgs + " arg(s): " + methodArgsString + " (excluded DataFetchingEnvironment)");
        }

        int currentArgs = (int) Arrays.stream(executable.getArguments())
                .filter(it -> !it.getType().isAssignableFrom(DataFetchingEnvironment.class))
                .count();

        if (currentArgs > requiredArgs) {
            // TODO custom exception
            throw new RuntimeException("Too much arguments in the method " + getExecutableMethodFullName(executable) + ", required " + requiredArgs + " arg(s): " + methodArgsString + " (excluded DataFetchingEnvironment)");
        }

        if (requiredArgs > currentArgs) {
            // TODO custom exception
            throw new RuntimeException("Too less arguments in the method " + getExecutableMethodFullName(executable) + ", required " + requiredArgs + " arg(s): " + methodArgsString + " (excluded DataFetchingEnvironment)");
        }
    }

    private String getExecutableMethodFullName(Executable executable) {
        if (executable instanceof ExecutableMethod) {
            ExecutableMethod executableMethod = (ExecutableMethod) executable;
            return "`" + executableMethod.getMethodName() + "` (" + executableMethod.getDeclaringType() + ")";
        } else if (executable instanceof BeanMethod) {
            BeanMethod beanMethod = (BeanMethod) executable;
            return "`" + beanMethod.getName() + "` (" + beanMethod.getDeclaringBean().getBeanType() + ")";
        } else {
            throw new UnsupportedOperationException("Unknown executable class: " + executable.getClass());
        }
    }

    private void processObjectTypeDefinition(ObjectTypeDefinition objectTypeDefinition, BeanIntrospection beanIntrospection) {
        ArgumentUtils.requireNonNull("typeDefinition", objectTypeDefinition);

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
                    Class interfaceClass = typeInterfaces.getOrDefault(beanIntrospection.getBeanType(), beanIntrospection.getBeanType());

                    // TODO better exception
                    BeanDefinitionAndMethod beanDefinitionAndMethod = findModelExecutableMethod(interfaceClass, fieldDefinition.getName())
                            .orElseThrow(() -> new RuntimeException("Property or method `" + fieldDefinition.getName() + "` not found: " + beanIntrospection.getBeanType()));

                    // TODO validate parameters for executable method

                    // count with source argument
                    checkInputValueDefinitions(fieldDefinition, beanDefinitionAndMethod.executableMethod, objectTypeDefinition.getName());

                    List<ArgumentDetails> argumentDetails = processInputArguments(
                            fieldDefinition, beanDefinitionAndMethod.executableMethod, interfaceClass
                    );

                    typeWiring.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                            objectMapper,
                            beanDefinitionAndMethod.executableMethod,
                            argumentDetails,
                            applicationContext.getBean(beanDefinitionAndMethod.beanDefinition)
                    ));

                    Argument argument = beanDefinitionAndMethod.executableMethod.getReturnType().asArgument();

                    processReturnTypeForBeanProperty(fieldDefinition, argument);
                } else if (beanProperty.isPresent()) {
                    Argument argument = beanProperty.get().asArgument();

                    checkInputValueDefinitions(fieldDefinition, beanProperty.get());

                    processReturnTypeForBeanProperty(fieldDefinition, argument);
                } else if (!beanMethods.isEmpty()) {
                    BeanMethod beanMethod = beanMethods.get(0);

                    checkInputValueDefinitions(fieldDefinition, beanMethod, null);

                    List<ArgumentDetails> argumentDetails = processInputArguments(
                            fieldDefinition, beanMethod, null
                    );

                    typeWiring.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                            objectMapper,
                            beanMethod,
                            argumentDetails,
                            null
                    ));

                    Argument argument = beanMethod.getReturnType().asArgument();

                    processReturnTypeForBeanProperty(fieldDefinition, argument);
                }
            }

            return typeWiring;
        });
    }

    private void processReturnTypeForBeanProperty(FieldDefinition fieldDefinition, Argument argument) {
        Argument unwrappedArgument = skipCompletionStage(argument);

        Type fieldType = skipNonNullType(fieldDefinition.getType());
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
                    listFieldType,
                    listReturnType,
                    argument.getType(),
                    argument.getName()
            );
        } else {
            processReturnType(
                    fieldDefinition,
                    fieldType,
                    returnType,
                    argument.getType(),
                    argument.getName()
            );
        }
    }

    private Argument skipCompletionStage(Argument argument) {
        if (argument.getType().isAssignableFrom(CompletionStage.class)) {
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
            Type graphQlType,
            Class returnType,
            Class mappedClass,
            String mappedMethodName
    ) {
        if (!(graphQlType instanceof ListType) &&
                types.get(getTypeName(graphQlType)) instanceof UnionTypeDefinition) {
            // TODO skip for now
        } else {
            if (graphQlType instanceof TypeName) {
                TypeName typeName = (TypeName) graphQlType;

                if (isBuiltInType(typeName)) {
                    validateBuiltInType(getTypeName(graphQlType), returnType, exceptionConsumer -> {
                        exceptionConsumer.withMethodName(fieldDefinition.getName());
                    });
                } else if (returnType.isEnum()) {
                    registerEnumMapping(getTypeName(graphQlType), returnType);
                } else {
                    if (isBuiltInType(returnType)) {
                        throw new IncorrectMappingException(
                                typeName.getName(),
                                fieldDefinition.getName(),
                                mappedClass,
                                mappedMethodName,
                                returnType
                        );
                    }
                    registerObjectType(getTypeName(graphQlType), requireGraphQLModel(returnType));
                }
            } else {
                throw new UnsupportedOperationException("");
            }
        }
    }

    private void registerObjectType(String type, BeanIntrospection beanIntrospection) {
        TypeDefinition typeDefinition = types.get(type);

        if (typeDefinition == null) {
            // TODO custom exception
            throw new RuntimeException("TypeDefinition not found for type: " + type);
        }

        if (!(typeDefinition instanceof ObjectTypeDefinition)) {
            // TODO custom exception
            throw new RuntimeException("Must be ObjectTypeDefinition for type `" + type + "`, but found: " + typeDefinition);
        }

        if (registerBeanIntrospectionMapping(typeDefinition.getName(), beanIntrospection)) {
            // TODO compare fields

            processObjectTypeDefinition((ObjectTypeDefinition) typeDefinition, beanIntrospection);
        }
    }

    private boolean registerBeanIntrospectionMapping(String type, BeanIntrospection beanIntrospection) {
        ArgumentUtils.requireNonNull("type", type);
        ArgumentUtils.requireNonNull("beanIntrospection", beanIntrospection);

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

    private boolean registerEnumMapping(String type, Class enumClass) {
        ArgumentUtils.requireNonNull("type", type);
        ArgumentUtils.requireNonNull("enumClass", enumClass);

        MappingItem mappingItem = mappingRegistry.get(type);

        if (mappingItem != null) {
            if (!enumClass.equals(mappingItem.targetEnum)) {
                throw new RuntimeException("Detected conflicted type");
            }
            // already processed
            return false;
        }

        mappingRegistry.put(type, new MappingItem(enumClass, null));

        return true;
    }

    private boolean registerUnionMapping(String type, Class interfaceClass) {
        ArgumentUtils.requireNonNull("type", type);
        ArgumentUtils.requireNonNull("interfaceClass", interfaceClass);

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

    private String getTypeName(Type type) {
        type = skipNonNullType(type);
        if (type instanceof TypeName) {
            return ((TypeName) type).getName();
        }
        throw new UnsupportedOperationException("Unknown type: " + type);
    }

    private Type skipNonNullType(Type type) {
        if (type instanceof NonNullType) {
            return skipNonNullType(((NonNullType) type).getType());
        }
        return type;
    }

    // TODO rename me pls
    private void detectReturnType(FieldDefinition fieldDefinition, ExecutableMethod<Object, Object> executableMethod) {

    }

    private BeanIntrospection requireGraphQLModel(Class clazz) {
        try {
            BeanIntrospection beanIntrospection;

            if (clazz.isInterface()) {
                Class implementationClass = typeInterfaces.get(clazz);

                // TODO optimize?
                for (Map.Entry<Class, Class> entry : typeInterfaces.entrySet()) {
                    if (clazz.equals(entry.getValue())) {
                        implementationClass = entry.getKey();
                    }
                }

                if (implementationClass == null) {
                    throw new RuntimeException("Can not find implementation class for: " + clazz);
                }

                beanIntrospection = BeanIntrospection.getIntrospection(implementationClass);
            } else {
                beanIntrospection = BeanIntrospection.getIntrospection(clazz);
            }

            if (beanIntrospection.getAnnotation(GraphQLType.class) == null) {
                throw new RuntimeException(clazz + " is not annotated with " + GraphQLType.class.getSimpleName());
            }

            return beanIntrospection;
        } catch (IntrospectionException introspectionException) {
            throw new RuntimeException(clazz + " is not introspected. Probably " + GraphQLType.class.getSimpleName() + " annotation was not added or processed by Micronaut.");
        }
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

    private boolean isBuiltInType(TypeName typeName) {
        return SYSTEM_TYPES.containsKey(typeName.getName());
    }

    private boolean isBuiltInType(Class clazz) {
        return SYSTEM_TYPES_CACHE.contains(clazz);
    }

    private class BeanDefinitionAndMethod {
        final BeanDefinition<Object> beanDefinition;
        final ExecutableMethod<Object, Object> executableMethod;

        private BeanDefinitionAndMethod(@NonNull BeanDefinition beanDefinition, @NonNull ExecutableMethod executableMethod) {
            ArgumentUtils.requireNonNull("beanDefinition", beanDefinition);
            ArgumentUtils.requireNonNull("executableMethod", executableMethod);

            this.beanDefinition = beanDefinition;
            this.executableMethod = executableMethod;
        }
    }

    private class BeanDefinitionAndMethods {
        final BeanDefinition<Object> beanDefinition;
        final Set<ExecutableMethod<Object, Object>> executableMethods = new HashSet<>();

        private BeanDefinitionAndMethods(@NonNull BeanDefinition beanDefinition) {
            ArgumentUtils.requireNonNull("beanDefinition", beanDefinition);

            this.beanDefinition = beanDefinition;
        }

        private void addExecutableMethod(@NonNull ExecutableMethod executableMethod) {
            ArgumentUtils.requireNonNull("executableMethod", executableMethod);

            executableMethods.add(executableMethod);
        }
    }

    private class MappingItem {

        @Nullable final BeanIntrospection beanIntrospection;

        @Nullable final Class targetEnum;
        @Nullable final Class targetInterface;

        private MappingItem(@NonNull BeanIntrospection beanIntrospection) {
            ArgumentUtils.requireNonNull("beanIntrospection", beanIntrospection);

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
