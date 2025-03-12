plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "webir"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.apache.lucene:lucene-core:10.1.0")
    implementation("org.apache.lucene:lucene-analysis-common:10.1.0")
    implementation("org.apache.lucene:lucene-queryparser:10.1.0")
    implementation("org.apache.lucene:lucene-highlighter:10.1.0")
    implementation("org.apache.lucene:lucene-queries:10.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JavaExec>("index") {
    group = "application"
    description = "Index books"
    mainClass.set("webir.booksearchengine.BookIndexerCliApplication")
    classpath = sourceSets.main.get().runtimeClasspath
}