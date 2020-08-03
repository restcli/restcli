plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
}

group = "uos.dev"
version = "1.1"

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
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    workingDir = File(rootDir, "src/test/resources/requests")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "uos.dev.restcli.AppKt"
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
