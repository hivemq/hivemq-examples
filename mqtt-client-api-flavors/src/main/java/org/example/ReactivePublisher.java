package org.example;

import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class ReactivePublisher {

    public static void main(final String @NotNull [] args) {
        final Mqtt5RxClient reactiveClient = Mqtt5Client.builder()
                .identifier("reactive-publisher")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildRx();

        final Flowable<Mqtt5Publish> publishes = Flowable.range(0, 10)
                .map(integer -> Mqtt5Publish.builder()
                        .topic("example/topic/reactive")
                        .payload(("example #" + integer).getBytes(UTF_8))
                        .build());

        reactiveClient.connect().subscribe((connAck, connectThrowable) -> {
            if (connectThrowable != null) {
                System.out.println("Error while connecting!");
                connectThrowable.printStackTrace();
            } else {
                System.out.println("Successfully connected!");
                reactiveClient.publish(publishes).doOnComplete(() -> {
                    System.out.println("Successfully published!");
                }).subscribe();
            }
        });
    }
}
