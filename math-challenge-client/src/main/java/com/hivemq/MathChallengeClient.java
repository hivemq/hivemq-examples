package com.hivemq;

import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MathChallengeClient {

    public static void main(final @NotNull String[] args) {
        final Mqtt5Client client = Mqtt5Client.builder()
                .serverAddress(new InetSocketAddress("localhost", 1883))
                .enhancedAuth(new MathChallengeEnhancedAuthMechanism())
                .build();

        client.toBlocking().connect();
    }

    private static class MathChallengeEnhancedAuthMechanism implements Mqtt5EnhancedAuthMechanism {

        @Override
        public @NotNull MqttUtf8String getMethod() {
            return MqttUtf8String.of("mathChallenge");
        }

        @Override
        public int getTimeout() {
            return (int) Duration.ofMinutes(3).getSeconds();
        }

        @Override
        public @NotNull
        CompletableFuture<Void> onAuth(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Connect connect, @NotNull Mqtt5EnhancedAuthBuilder authBuilder) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public @NotNull CompletableFuture<Boolean> onContinue(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth, @NotNull Mqtt5AuthBuilder authBuilder) {
            final Optional<ByteBuffer> authData = auth.getData();
            if (authData.isPresent() && "mathChallenge".equals(auth.getMethod().toString())) {

                final byte[] array = new byte[authData.get().remaining()];
                authData.get().get(array);

                final String challenge = new String(array);

                final String[] split = challenge.split("\\+");

                final String response = "" + (Integer.parseInt(split[0]) + Integer.parseInt(split[1]));

                authBuilder.data(response.getBytes());
                return CompletableFuture.completedFuture(true);
            }
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public @NotNull CompletableFuture<Boolean> onAuthSuccess(
                final @NotNull Mqtt5ClientConfig clientConfig,
                final @NotNull Mqtt5ConnAck connAck) {

            System.out.println("connected!");
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public @NotNull CompletableFuture<Void> onReAuth(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5AuthBuilder authBuilder) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public @NotNull CompletableFuture<Boolean> onReAuthSuccess(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public void onAuthRejected(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5ConnAck connAck) {

        }

        @Override
        public void onReAuthRejected(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Disconnect disconnect) {

        }

        @Override
        public void onAuthError(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Throwable cause) {

        }

        @Override
        public void onReAuthError(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Throwable cause) {

        }
    }
}
