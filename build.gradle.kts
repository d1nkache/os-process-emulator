plugins {
    id("java")
}

group = "emulation.multithreading"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Lombok для использования в коде
    compileOnly("org.projectlombok:lombok:1.18.28")
    // Lombok для генерации кода во время компиляции
    annotationProcessor("org.projectlombok:lombok:1.18.28")

    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.projectlombok:lombok:1.18.28")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}