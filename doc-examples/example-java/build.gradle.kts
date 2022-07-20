plugins {
    id("java")
    id("io.micronaut.application")
    id("com.apollographql.apollo3").version("3.4.0")
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("example.Application")
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
}

dependencies {
    implementation(projects.graphqlTools)
    implementation("io.micronaut.graphql:micronaut-graphql")
    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("com.apollographql.apollo3:apollo-runtime:3.4.0")
}

apollo {
    packageName.set("example.client")
    schemaFile.set(file("src/main/resources/schema.graphqls"))
    generateKotlinModels.set(false)
}
