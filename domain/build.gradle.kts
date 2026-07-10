plugins {
    id("java")
}

group = "com.example.docsearch"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    // Spring（DI/ライフサイクル）。バージョンは Spring Boot BOM で管理
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.context)
    implementation(libs.slf4j.api)
    testImplementation(libs.spring.boot.starter.test)

    // Lucene（全文検索）
    implementation(libs.lucene.core)
    implementation(libs.lucene.analysis.common)
    implementation(libs.lucene.analysis.kuromoji)
    implementation(libs.lucene.queryparser)
    implementation(libs.lucene.highlighter)

    // Tika（本文抽出: pptx/xlsx/docx/pdf/md など）
    implementation(libs.tika.core)
    implementation(libs.tika.parser.pdf) // pdf
    implementation(libs.tika.parser.text) // text
    implementation(libs.tika.parser.microsoft) // microsoft

    // Test
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
