package org.example;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import io.reactivex.Flowable;
import io.reactivex.Single;
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

        final Single<Mqtt5SubAck> subAckSingle =
                reactiveClient.subscribeWith().topicFilter("example/topic/#").applySubscribe() //
                        .doOnError(throwable -> {
                            System.out.println("Error while subscribing!");
                            throwable.printStackTrace();
                        }).doOnSuccess(mqtt5SubAck -> {
                            System.out.println("Successfully subscribed!");
                        });

        reactiveClient.connect() //
                .doOnSuccess(connAck -> {
                    System.out.println("Successfully connected!");
                    subAckSingle.subscribe();
                }).doOnError(throwable -> { //
                    System.out.println("Error while connecting!");
                    throwable.printStackTrace();
                }).subscribe();
    }
}
