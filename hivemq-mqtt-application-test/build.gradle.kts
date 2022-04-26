plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.hivemq:hivemq-mqtt-client:1.3.0")
    implementation("org.jetbrains:annotations:23.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("ch.qos.logback:logback-classic:1.2.11")
    testImplementation("org.mockito:mockito-core:4.4.0")

    testImplementation("org.testcontainers:junit-jupiter:1.17.0")
    testImplementation("org.testcontainers:hivemq:1.17.0")
    testImplementation("org.testcontainers:testcontainers:1.17.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}