plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
    id("jacoco")
    kotlin("jvm") version "2.0.21"
}

group = "ingsis"
version = "0.0.1-SNAPSHOT"
description = "ingsis project for Spring Boot"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/austral-ingsis/class-redis-streams")
        credentials {
            username = (project.findProperty("gpr.user") as String?) ?: System.getenv("USERNAME")
            password = (project.findProperty("gpr.key") as String?) ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    // --- Auth0 / JWT ---
    implementation("com.auth0:java-jwt:4.4.0")

    // --- Spring Boot starters ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // --- Validation / Hibernate ---
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")

    // --- Formatting ---
    implementation("com.google.googlejavaformat:google-java-format:1.19.2")

    // --- Spring Cloud ---
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // --- Redis básico ---
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("org.austral.ingsis:redis-streams-mvc:0.1.13")

    // --- Seguridad ---
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.security:spring-security-oauth2-core")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // --- Database ---
    runtimeOnly("org.postgresql:postgresql")

    // H2 para tests
    testImplementation("com.h2database:h2")

    // --- Testing ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.1"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

spotless {
    java {
        googleJavaFormat()
        target("src/**/*.java")
    }
}

checkstyle {
    toolVersion = "10.18.1"
    config = resources.text.fromFile("config/checkstyle/checkstyle.xml")
}

tasks.test {
    useJUnitPlatform()
    exclude("**/SnippetApplicationTests.class")
}