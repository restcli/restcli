plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    `maven-publish`
}

group = "uos.dev"
version = "1.7.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    val junitVersion = "5.6.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("info.picocli:picocli:4.3.2")
    kapt("info.picocli:picocli-codegen:4.3.2")
    testImplementation("com.google.truth:truth:1.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.7.2")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.0")
    implementation("commons-validator:commons-validator:1.6")
    implementation("com.jakewharton.picnic:picnic:0.5.0")
    implementation("com.github.ajalt:mordant:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("io.github.microutils:kotlin-logging:1.8.3")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "uos.dev.restcli.AppKt"
        attributes["Multi-Release"] = true
    }
    archiveBaseName.set("restcli")
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}
