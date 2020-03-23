package com.hivemq.examples.testcontainer.junit5;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

@SuppressWarnings("rawtypes")
public class HiveMQTestContainerExtension implements BeforeEachCallback, AfterEachCallback {

    private static final @NotNull String HIVEMQ_CE_IMAGE = "hivemq/hivemq-ce";
    private static final @NotNull String HIVEMQ_CE_VERSION = "latest";
    public static final int MQTT_PORT = 1883;

    private final @NotNull GenericContainer container;

    public HiveMQTestContainerExtension() {
        container = new GenericContainer(HIVEMQ_CE_IMAGE + ":" + HIVEMQ_CE_VERSION);
        container.withExposedPorts(MQTT_PORT);

        final LogMessageWaitStrategy waitStrategy = new LogMessageWaitStrategy();
        waitStrategy.withRegEx(".*Started HiveMQ in.*");
        container.waitingFor(waitStrategy);
    }

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) throws Exception {
        container.start();
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) throws Exception {
        container.stop();
    }

    public int getMqttPort() {
        return container.getMappedPort(MQTT_PORT);
    }
}
