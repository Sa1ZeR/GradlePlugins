plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.2.21"
    id("maven-publish")
}

group = "com.sa1zer.gradleplugins"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.liquibase.core)
    implementation(libs.postgresql)
    implementation(libs.testcontainers)
    implementation(libs.testcontainers.postgresql)
    implementation(libs.jooq)
    implementation(libs.jooq.meta)
    implementation(libs.jooq.codegen)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("jooqgenerator") {
            id = "com.sa1zer.gradleplugins.jooqgenerator"
            implementationClass = "com.sa1zer.gradleplugins.jooqgenerator.JooqGeneratorPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}