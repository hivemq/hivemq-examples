package com.hivemq.examples.testcontainer.junit4;

import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

@SuppressWarnings("rawtypes")
public class HiveMQTestContainerRule extends TestWatcher {

    private static final @NotNull String HIVEMQ_CE_IMAGE = "hivemq/hivemq-ce";
    private static final @NotNull String HIVEMQ_CE_VERSION = "latest";
    public static final int MQTT_PORT = 1883;

    private final @NotNull GenericContainer container;

    public HiveMQTestContainerRule() {
        container = new GenericContainer(HIVEMQ_CE_IMAGE + ":" + HIVEMQ_CE_VERSION);
        container.withExposedPorts(MQTT_PORT);

        final LogMessageWaitStrategy waitStrategy = new LogMessageWaitStrategy();
        waitStrategy.withRegEx(".*Started HiveMQ in.*");
    }

    @Override
    protected void starting(final @NotNull Description description) {
        container.start();
    }

    @Override
    protected void finished(final @NotNull Description description) {
        container.stop();
    }

    public int getMqttPort() {
        return container.getMappedPort(MQTT_PORT);
    }
}
