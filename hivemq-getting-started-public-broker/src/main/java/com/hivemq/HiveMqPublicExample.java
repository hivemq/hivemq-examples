package com.hivemq;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class HiveMqPublicExample {

    public static void main(String[] args) throws InterruptedException, UnknownHostException, SocketException {
        InetAddress localHost = InetAddress.getLocalHost();
        NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
        byte[] hardwareAddress = ni.getHardwareAddress(); // use this to get your MAC address, use it as a unique identifier

        // 1. create the client
        final Mqtt5Client client = Mqtt5Client.builder()
                .identifier("sensor-" + hardwareAddress) // use a unique identifier
                .serverHost("broker.hivemq.com") // use the public HiveMQ broker
                .automaticReconnectWithDefaultConfig() // the client automatically reconnects
                .build();

        // 2. connect the client
        client.toBlocking().connectWith()
                .willPublish()
                .topic("home/will")
                .payload("sensor gone".getBytes())
                .applyWillPublish()
                .send();

        // 3. subscribe and consume messages
        client.toAsync().subscribeWith()
                .topicFilter("home/#")
                .callback(publish -> {
                    System.out.println("Received message on topic " + publish.getTopic() + ": " +
                            new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
                })
                .send();

        // 4. simulate periodic publishing of sensor data
        while (true) {
            client.toBlocking().publishWith()
                    .topic("home/brightness")
                    .payload(getBrightness())
                    .send();

            TimeUnit.MILLISECONDS.sleep(500);

            client.toBlocking().publishWith()
                    .topic("home/temperature")
                    .payload(getTemperature())
                    .send();

            TimeUnit.MILLISECONDS.sleep(500);
        }


    }

    private static byte[] getBrightness() {
    // simulate a brightness sensor with values between 1000lux and 10000lux
        final int brightness = ThreadLocalRandom.current().nextInt(1_000, 10_000);
        return (brightness + "lux").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] getTemperature() {
    // simulate a temperature sensor with values between 20°C and 30°C
        final int temperature = ThreadLocalRandom.current().nextInt(20, 30);
        return (temperature + "°C").getBytes(StandardCharsets.UTF_8);
    }


}
