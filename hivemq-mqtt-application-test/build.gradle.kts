plugins {
    java
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("com.hivemq:hivemq-mqtt-client:1.3.0")
    implementation("com.github.testcontainers.testcontainers-java:hivemq:244c77c")
    implementation("org.jetbrains:annotations:23.0.0")

    testImplementation("ch.qos.logback:logback-classic:1.2.11")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.testcontainers:junit-jupiter:1.16.3")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.mockito:mockito-core:4.4.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}