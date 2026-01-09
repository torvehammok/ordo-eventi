import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version "2.2.21"
    id("com.google.protobuf") version "0.9.6"
    application
}

group = "io.github.torvehammok"
version = "1.0-SNAPSHOT"

application {
    mainClass = "io.github.torvehammok.OrdoEventiApplicationKt"
    applicationName = "ordo-eventi"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

val confluentVersion = "8.1.1"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.kafka:kafka-clients:3.7.0")
    implementation("info.picocli:picocli:4.7.6")
    implementation("ch.qos.logback:logback-classic:1.5.22")

    implementation("org.yaml:snakeyaml:2.5")
    implementation("de.danielbechler:java-object-diff:0.95")

    implementation("com.google.protobuf:protobuf-java:4.33.2")
    implementation("io.confluent:kafka-schema-registry-client:$confluentVersion")
    implementation("io.confluent:kafka-protobuf-provider:$confluentVersion")
    implementation("io.confluent:kafka-schema-serializer:$confluentVersion")
    implementation("io.confluent:kafka-protobuf-serializer:$confluentVersion")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    testImplementation("io.confluent:kafka-protobuf-serializer:$confluentVersion")


    testImplementation("org.testcontainers:testcontainers:2.0.3")
    testImplementation("org.testcontainers:kafka:1.21.4")
    testImplementation("org.assertj:assertj-core:3.27.6")

    implementation("org.slf4j:slf4j-api:2.0.17")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}


// task to run kotlin application
tasks.register<JavaExec>("topics-plan") {
    group = "ordo-eventi"
    description = "Run the Kotlin application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.torvehammok.OrdoEventiApplicationKt")
    args = listOf("topics-plan", "-c", "src/main/resources/configmap.yaml")
}

tasks.register<JavaExec>("topics-apply") {
    group = "ordo-eventi"
    description = "Run the Kotlin application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.torvehammok.OrdoEventiApplicationKt")
    args = listOf("topics-apply", "-c", "src/main/resources/configmap.yaml")
}

tasks.register<JavaExec>("schemas-plan") {
    group = "ordo-eventi"
    description = "Run the Kotlin application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.torvehammok.OrdoEventiApplicationKt")
    args = listOf("schemas-plan", "-c", "src/main/resources/configmap.yaml")
}

tasks.register<JavaExec>("bootstrap-all") {
    group = "ordo-eventi"
    description = "Run the Kotlin application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.torvehammok.OrdoEventiApplicationKt")
    args = listOf("run-all", "topics-apply", "schemas-apply", "-c", "src/main/resources/configmap.yaml")
}

tasks.register<JavaExec>("schemas-apply") {
    group = "ordo-eventi"
    description = "Run the Kotlin application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.torvehammok.OrdoEventiApplicationKt")
    args = listOf("schemas-apply", "-c", "src/main/resources/configmap.yaml")
}

tasks.register<JavaExec>("schemas-tree") {
    group = "ordo-eventi"
    description = "Run the Kotlin application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.torvehammok.OrdoEventiApplicationKt")
    args =
        listOf("schemas-tree", "-n", "io.github.torvehammok.proto.tictactoe", "-c", "src/main/resources/configmap.yaml")
}

tasks.register<JavaExec>("schemas-destroy") {
    group = "ordo-eventi-destructive"
    description = "Run the Kotlin application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.github.torvehammok.OrdoEventiApplicationKt")
    args = listOf("schemas-destroy", "-c", "src/main/resources/configmap.yaml")
}



protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.33.2"
    }
}

// add proto generated sources to main source set
sourceSets {
    main {
        java {
            srcDir("build/generated/source/proto/main/java")
        }
    }
}