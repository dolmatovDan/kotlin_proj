plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

apply(from = "./ktlint.gradle")

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}