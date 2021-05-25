package io.github.expatiat.micronaut.graphql.tools;

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
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLInput;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLType;
import io.github.expatiat.micronaut.graphql.tools.annotation.GraphQLTypeResolver;
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
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

import javax.inject.Singleton;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.expatiat.micronaut.graphql.tools.BeanIntrospectionUtils.generateGetMethodName;

/**
 * @author Alexey Zhokhov
 */
@Singleton
@Infrastructure
public class GraphQLMappingContext {

    private static final Map<String, Class> SYSTEM_TYPES = new HashMap<>();

    static {
        SYSTEM_TYPES.put("Int", Integer.class);
        SYSTEM_TYPES.put("Float", Float.class);
        SYSTEM_TYPES.put("String", String.class);
        SYSTEM_TYPES.put("Boolean", Boolean.class);
        SYSTEM_TYPES.put("ID", String.class);
        SYSTEM_TYPES.put("BigDecimal", BigDecimal.class);
        SYSTEM_TYPES.put("BigInteger", BigInteger.class);
        SYSTEM_TYPES.put("Char", Character.class);
        SYSTEM_TYPES.put("Short", Short.class);
        SYSTEM_TYPES.put("Long", Long.class);
    }

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

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

    public GraphQLMappingContext(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    public RuntimeWiring generateRuntimeWiring(TypeDefinitionRegistry typeRegistry,
                                               SchemaParserDictionary schemaParserDictionary) {
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

        SchemaDefinition schemaDefinition = typeRegistry.schemaDefinition().orElseThrow(() ->
                new RuntimeException("TypeDefinitionRegistry with null SchemaDefinition"));

        for (OperationTypeDefinition operationTypeDefinition : schemaDefinition.getOperationTypeDefinitions()) {
            processOperationTypeDefinition(operationTypeDefinition);
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
            throw new RuntimeException("The method `" + fieldDefinition.getName() + "` not found in any GraphQL query resolvers");
        }

        checkInputValueDefinitions(fieldDefinition, beanDefinitionAndMethod.executableMethod, 0);

        List<ArgumentDetails> argumentDetails = processInputArguments(
                fieldDefinition, beanDefinitionAndMethod.executableMethod, null
        );

        runtimeWiringBuilder.dataFetcher(fieldDefinition.getName(), new MicronautExecutableMethodDataFetcher(
                objectMapper,
                beanDefinitionAndMethod.executableMethod,
                argumentDetails,
                applicationContext.getBean(beanDefinitionAndMethod.beanDefinition)
        ));

        BeanIntrospection beanIntrospection = detectReturnType(beanDefinitionAndMethod.executableMethod.getReturnType());

        registerObjectType(getTypeName(fieldDefinition.getType()), beanIntrospection);
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

    @Nullable
    private Class validateAndRegister(InputValueDefinition inputValueDefinition, Class clazz,
                                      Executable<Object, Object> executable,
                                      int position) {
        TypeName typeName = (TypeName) skipNonNullType(inputValueDefinition.getType());

        // TODO custom exception
        return processInputTypeDefinition(typeName, clazz, (requiredClass) -> new RuntimeException("Invalid " + position + " parameter type in method " + getExecutableMethodFullName(executable) + ", required: " + requiredClass));
    }

    @Nullable
    private Class processInputTypeDefinition(TypeName typeName, Class clazz, Function<Class, RuntimeException> exceptionSupplier) {
        TypeDefinition typeDefinition = types.get(typeName.getName());

        if (typeDefinition instanceof InputObjectTypeDefinition) {
            processInputObjectTypeDefinition((InputObjectTypeDefinition) typeDefinition, clazz);

            return clazz;
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            processEnumTypeDefinition((EnumTypeDefinition) typeDefinition, clazz);

            return null;
        } else if (isBuiltInType(typeName)) {
            Class requiredClass = SYSTEM_TYPES.get(typeName.getName());

            if (!clazz.equals(requiredClass)) {
                throw exceptionSupplier.apply(requiredClass);
            }

            return null;
        } else {
            throw new RuntimeException("Unsupported type `" + typeName.getName() + "` with definition " + typeDefinition);
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
                    throw new UnsupportedOperationException();
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
                                            int additionalRequiredArgs) {
        if (fieldDefinition.getInputValueDefinitions().size() == 0 && executable.getArguments().length == 0) {
            return;
        }

        int requiredArgs = fieldDefinition.getInputValueDefinitions().size() + additionalRequiredArgs;
        if (requiredArgs > executable.getArguments().length) {
            // TODO custom exception
            throw new RuntimeException("Too less arguments in the method " + getExecutableMethodFullName(executable) + ", required " + requiredArgs + " (excluded DataFetchingEnvironment)");
        }

        int currentArgs = (int) Arrays.stream(executable.getArguments())
                .filter(it -> !it.getType().isAssignableFrom(DataFetchingEnvironment.class))
                .count();

        if (currentArgs > requiredArgs) {
            // TODO custom exception
            throw new RuntimeException("Too much arguments in the method " + getExecutableMethodFullName(executable) + ", required " + requiredArgs + " (excluded DataFetchingEnvironment)");
        }

        if (requiredArgs > currentArgs) {
            // TODO custom exception
            throw new RuntimeException("Too less arguments in the method " + getExecutableMethodFullName(executable) + ", required " + requiredArgs + " (excluded DataFetchingEnvironment)");
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
                    checkInputValueDefinitions(fieldDefinition, beanDefinitionAndMethod.executableMethod, 1);

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

                    checkInputValueDefinitions(fieldDefinition, beanMethod, 0);

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

            processReturnType(listFieldType, listReturnType);
        } else {
            processReturnType(fieldType, returnType);
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

    private void processReturnType(Type fieldType, Class returnType) {
        if (isBuiltInType(returnType)) {
            // TODO check is types are matched
        } else if (returnType.isEnum()) {
            registerEnumMapping(getTypeName(fieldType), returnType);
        } else {
            registerObjectType(getTypeName(fieldType), requireGraphQLModel(returnType));
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

    private BeanIntrospection detectReturnType(ReturnType returnType) {
        if (returnType.getType().isAssignableFrom(CompletionStage.class)) {
            return requireGraphQLModel(returnType.asArgument().getFirstTypeVariable().get().getType());
        }

        return requireGraphQLModel(returnType.getType());
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
        if (clazz.equals(int.class)) {
            return true;
        } else if (clazz.equals(long.class)) {
            return true;
        } else if (clazz.equals(float.class)) {
            return true;
        } else if (clazz.equals(boolean.class)) {
            return true;
        } else if (clazz.equals(char.class)) {
            return true;
        } else if (clazz.equals(short.class)) {
            return true;
        }
        return SYSTEM_TYPES.containsValue(clazz);
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
