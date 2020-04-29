package com.hivemq.examples.smartaquarium.mqtt;

import com.hivemq.examples.smartaquarium.equipment.Co2;
import com.hivemq.examples.smartaquarium.equipment.Light;
import com.hivemq.examples.smartaquarium.equipment.Pump;
import com.hivemq.examples.smartaquarium.equipment.TemperatureSensor;
import org.eclipse.paho.client.mqttv3.*;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SmartAquariumClient implements MqttCallback {

    public static final @NotNull String LIGHT_TOPIC = "equipment/light";
    public static final @NotNull String CO2_TOPIC = "equipment/co2";
    public static final @NotNull String PUMP_TOPIC = "equipment/pump";
    public static final @NotNull String TEMPERATURE_TOPIC = "status/temperature";

    private final @NotNull Light light;
    private final @NotNull Co2 co2;
    private final @NotNull Pump pump;

    private final @NotNull MqttClient client;
    private final @NotNull TemperatureSensor temperatureSensor;

    public SmartAquariumClient(
            final @NotNull String brokerUri,
            final @NotNull Light light,
            final @NotNull Co2 co2,
            final @NotNull Pump pump,
            final @NotNull TemperatureSensor temperatureSensor) throws MqttException {

        this.light = light;
        this.co2 = co2;
        this.pump = pump;
        this.temperatureSensor = temperatureSensor;

        client = new MqttClient(brokerUri, "smartaquarium");
        client.connect();

        client.setCallback(this);

        client.subscribe(LIGHT_TOPIC, 2);
        client.subscribe(CO2_TOPIC, 2);
        client.subscribe(PUMP_TOPIC, 2);
    }

    public void publishTemperature() throws MqttException {
        final String temperatureString = String.format(Locale.US, "%.1f°C", temperatureSensor.getCelsius());
        final MqttMessage temperatureMessage = new MqttMessage(temperatureString.getBytes(StandardCharsets.UTF_8));
        client.publish(TEMPERATURE_TOPIC, temperatureMessage);
    }

    @Override
    public void messageArrived(final @NotNull String topic, final @NotNull MqttMessage mqttMessage) {
        final String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
        switch (topic) {
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

    @Override
    public void connectionLost(final @NotNull Throwable throwable) {

    }

    @Override
    public void deliveryComplete(final @NotNull IMqttDeliveryToken iMqttDeliveryToken) {

    }

}
