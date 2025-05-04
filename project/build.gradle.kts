plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")  // Jackson Converter

    // Jackson XML
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")  // XML support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")  // Kotlin support

    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")  // HTTP logging

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}

apply(from = "./ktlint.gradle")

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
