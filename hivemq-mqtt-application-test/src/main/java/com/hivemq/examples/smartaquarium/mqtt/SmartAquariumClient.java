package com.hivemq.examples.smartaquarium.mqtt;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.examples.smartaquarium.equipment.Co2;
import com.hivemq.examples.smartaquarium.equipment.Light;
import com.hivemq.examples.smartaquarium.equipment.Pump;
import com.hivemq.examples.smartaquarium.equipment.TemperatureSensor;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SmartAquariumClient {

    public static final @NotNull String LIGHT_TOPIC = "/equipment/light";
    public static final @NotNull String CO2_TOPIC = "/equipment/co2";
    public static final @NotNull String PUMP_TOPIC = "/equipment/pump";
    public static final @NotNull String TEMPERATURE_TOPIC = "/status/temperature";

    private final @NotNull Light light;
    private final @NotNull Co2 co2;
    private final @NotNull Pump pump;

    private final @NotNull Mqtt5BlockingClient client;
    private final @NotNull TemperatureSensor temperatureSensor;

    public SmartAquariumClient(
            final @NotNull String brokerHost,
            final int brokerPort,
            final @NotNull Light light,
            final @NotNull Co2 co2,
            final @NotNull Pump pump,
            final @NotNull TemperatureSensor temperatureSensor) {

        this.light = light;
        this.co2 = co2;
        this.pump = pump;
        this.temperatureSensor = temperatureSensor;

        client = Mqtt5Client.builder().serverHost(brokerHost).serverPort(brokerPort).buildBlocking();
        client.connect();

        client.toAsync().publishes(MqttGlobalPublishFilter.ALL, this::accept);

        client.subscribeWith().topicFilter(LIGHT_TOPIC).send();
        client.subscribeWith().topicFilter(CO2_TOPIC).send();
        client.subscribeWith().topicFilter(PUMP_TOPIC).send();
    }

    private void accept(final @NotNull Mqtt5Publish publish) {
        final String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
        switch (publish.getTopic().toString()) {
            case LIGHT_TOPIC:
                if ("ON".equals(payload)) {
                    light.turnOn();
                } else if ("OFF".equals(payload)) {
                    light.turnOff();
                }
                break;
            case CO2_TOPIC:
                if ("ON".equals(payload)) {
                    co2.turnOn();
                } else if ("OFF".equals(payload)) {
                    co2.turnOff();
                }
                break;
            case PUMP_TOPIC:
                if ("ON".equals(payload)) {
                    pump.turnOn();
                } else if ("OFF".equals(payload)) {
                    pump.turnOff();
                }
                break;
        }
    }

    public void publishTemperature() {
        final String temperatureString = String.format(Locale.US, "%.1f°C", temperatureSensor.getCelsius());
        client.publishWith().topic(TEMPERATURE_TOPIC).payload(temperatureString.getBytes(StandardCharsets.UTF_8)).send();
    }

}
