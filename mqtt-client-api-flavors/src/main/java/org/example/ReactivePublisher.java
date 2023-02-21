package org.example;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yannick Weber
 */
public class ReactivePublisher {

    public static void main(final String @NotNull [] args) {
        final var reactiveClient = Mqtt5Client.builder()
                .identifier("reactive-publisher")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildRx();

        final Completable disconnect = reactiveClient.disconnect().doOnComplete(() -> {
            System.out.println("Successfully disconnected!");
        });

        final Flowable<Mqtt5Publish> publish = Flowable.range(0, 10) //
                .map(integer -> Mqtt5Publish.builder()
                        .topic("example/topic/reactive")
                        .payload(("example #" + integer).getBytes(UTF_8))
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .build()) //
                .doOnComplete(() -> {
                    System.out.println("Successfully published!");
                    disconnect.subscribe();
                }).doOnError(throwable -> {
                    System.out.println("Error while publishing!");
                    throwable.printStackTrace();
                });

        reactiveClient.connect() //
                .doOnSuccess(connAck -> {
                    System.out.println("Successfully connected!");
                    reactiveClient.publish(publish).subscribe();
                }).doOnError(throwable -> { //
                    System.out.println("Error while connecting!");
                    throwable.printStackTrace();
                }).subscribe();
    }
}
