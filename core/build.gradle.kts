plugins {
    id("java")
    alias(libs.plugins.spotbugs)
}

group = "com.example.docsearch"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

spotbugs {
    excludeFilter.set(rootProject.file("gradle/spotbugs-exclude.xml"))
}

repositories {
    mavenCentral()
}

dependencies {
    // Test
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}