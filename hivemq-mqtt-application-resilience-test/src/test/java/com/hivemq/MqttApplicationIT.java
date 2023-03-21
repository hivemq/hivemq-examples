package com.hivemq;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.examples.MqttApplication;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Yannick Weber
 */
@Testcontainers
public class MqttApplicationIT {

    private static final @NotNull DockerImageName TOXIPROXY_IMAGE =
            DockerImageName.parse("ghcr.io/shopify/toxiproxy:latest");
    private static final @NotNull DockerImageName HIVEMQ_IMAGE = DockerImageName.parse("hivemq/hivemq-ce:latest");

    private final @NotNull Network network = Network.newNetwork();

    @Container
    public final @NotNull HiveMQContainer hivemq = new HiveMQContainer(HIVEMQ_IMAGE) //
            .withNetwork(network) //
            .withNetworkAliases("hivemq"); //

    @Container
    private final @NotNull ToxiproxyContainer toxiproxy = new ToxiproxyContainer(TOXIPROXY_IMAGE) //
            .withNetwork(network); //

    @AfterEach
    void tearDown() {
        network.close();
    }

    @Test
    void testMqttApplication() throws Exception {
        final ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
        final Proxy proxy = toxiproxyClient.createProxy("hivemqProxy", "0.0.0.0:8666", "hivemq:1883");

        final Mqtt5BlockingClient testClient = Mqtt5Client.builder()
                .serverHost(hivemq.getHost()) //
                .serverPort(hivemq.getMqttPort()) //
                .buildBlocking();
        testClient.connect();
        final Mqtt5BlockingClient.Mqtt5Publishes publishes = testClient.publishes(MqttGlobalPublishFilter.ALL);
        testClient.subscribeWith().topicFilter("#").send();

        final MqttApplication mqttApplication = new MqttApplication(toxiproxy.getHost(), toxiproxy.getMappedPort(8666));
        mqttApplication.start();

        Thread.sleep(1000);

        proxy.toxics().timeout("timeout-down", ToxicDirection.DOWNSTREAM, 0);
        proxy.toxics().timeout("timeout-up", ToxicDirection.UPSTREAM, 0);
        System.out.println("Cut connection with timeout");

        Thread.sleep(1000);

        proxy.toxics().get("timeout-down").remove();
        proxy.toxics().get("timeout-up").remove();
        System.out.println("Re-opened connection");

        Thread.sleep(1000);

        proxy.toxics().bandwidth("bandwidth-down", ToxicDirection.DOWNSTREAM, 0);
        proxy.toxics().bandwidth("bandwidth-up", ToxicDirection.UPSTREAM, 0);
        System.out.println("Cut connection with bandwidth");

        Thread.sleep(1000);

        proxy.toxics().get("bandwidth-down").remove();
        proxy.toxics().get("bandwidth-up").remove();
        System.out.println("Re-opened connection");

        for (int i = 0; i < 100; i++) {
            final Mqtt5Publish receive = publishes.receive();
            final String payloadAsString = new String(receive.getPayloadAsBytes(), StandardCharsets.UTF_8);
            System.out.println("Received message with payload: " + payloadAsString);
            assertEquals(String.valueOf(i), payloadAsString);
        }
    }
}
