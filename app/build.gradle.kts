plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))

    // Spring Boot
    implementation(libs.spring.boot.starter.web)
}

tasks.test {
    useJUnitPlatform()
}
