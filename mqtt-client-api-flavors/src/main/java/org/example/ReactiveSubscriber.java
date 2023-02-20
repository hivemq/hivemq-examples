package org.example;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class ReactiveSubscriber {

    public static void main(final String @NotNull [] args) {
        final Mqtt5RxClient reactiveClient = Mqtt5Client.builder()
                .identifier("reactive-subscriber")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildRx();

        final Flowable<Mqtt5Publish> publishes = reactiveClient.publishes(MqttGlobalPublishFilter.ALL);
        publishes.subscribe(publish -> {
            System.out.println("Received publish with payload: " + new String(publish.getPayloadAsBytes(), UTF_8));
        });

        reactiveClient.connect().subscribe((connAck, connectThrowable) -> {
            if (connectThrowable != null) {
                System.out.println("Error while connecting!");
                connectThrowable.printStackTrace();
            } else {
                System.out.println("Successfully connected!");
                reactiveClient.subscribeWith().topicFilter("example/topic/#").applySubscribe().subscribe((subAck, subscribeThrowable) -> {
                    if (subscribeThrowable != null) {
                        System.out.println("Error while subscribing!");
                        subscribeThrowable.printStackTrace();
                    }
                    System.out.println("Successfully subscribed!");
                });
            }
        });
    }
}
