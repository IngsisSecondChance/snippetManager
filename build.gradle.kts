plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
    id("jacoco")
    kotlin("jvm") version "2.2.20"
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
    maven{
        name = "GitHubPackagesAustral"
        url = uri("https://maven.pkg.github.com/austral-ingsis/class-redis-streams")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
    maven{
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/sonpipe0/spring-serializer")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.printScript.microservices:serializer:1.0.15")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.austral.ingsis:redis-streams-mvc:0.1.13") {
        exclude(group = "org.springframework.boot")
        exclude(group = "io.projectreactor")
    }
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    exclude("**/RedisTests.class")
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