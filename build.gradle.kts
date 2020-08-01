plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
}

group = "uos.dev"
version = "1.0-SNAPSHOT"

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
}

tasks.named<Test>("test") {
    useJUnitPlatform()
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
