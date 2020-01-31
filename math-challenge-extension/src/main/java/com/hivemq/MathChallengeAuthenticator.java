package com.hivemq;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.EnhancedAuthenticator;
import com.hivemq.extension.sdk.api.auth.parameter.EnhancedAuthConnectInput;
import com.hivemq.extension.sdk.api.auth.parameter.EnhancedAuthInput;
import com.hivemq.extension.sdk.api.auth.parameter.EnhancedAuthOutput;
import com.hivemq.extension.sdk.api.client.parameter.ConnectionAttributeStore;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;

public class MathChallengeAuthenticator implements EnhancedAuthenticator {

    private static final @NotNull Random random = new Random();

    @Override
    public void onConnect(@NotNull EnhancedAuthConnectInput input, @NotNull EnhancedAuthOutput output) {
        final Optional<String> authenticationMethod =
                input.getConnectPacket().getAuthenticationMethod();

        if (authenticationMethod.isPresent()
                && "mathChallenge".equals(authenticationMethod.get())) {

            final int first = random.nextInt();
            final int second = random.nextInt();
            final String challenge = first + "+" + second;
            final String expected = "" + (first + second);

            final ConnectionAttributeStore store =
                    input.getConnectionInformation().getConnectionAttributeStore();
            store.putAsString("mathChallengeExpected", expected);

            output.continueAuthentication(challenge.getBytes(StandardCharsets.UTF_8));
            return;
        }
        output.failAuthentication();
    }

    @Override
    public void onAuth(@NotNull EnhancedAuthInput input, @NotNull EnhancedAuthOutput output) {
        final String authenticationMethod =
                input.getAuthPacket().getAuthenticationMethod();

        if ("mathChallenge".equals(authenticationMethod)) {

            final ConnectionAttributeStore store =
                    input.getConnectionInformation().getConnectionAttributeStore();

            final Optional<String> mathChallengeExpected =
                    store.getAsString("mathChallengeExpected");
            final Optional<byte[]> authenticationData =
                    input.getAuthPacket().getAuthenticationDataAsArray();

            if (mathChallengeExpected.isEmpty() || authenticationData.isEmpty()) {
                    output.failAuthentication();
                return;
            }

            if (mathChallengeExpected.get().equals(new String(authenticationData.get()))) {
                    output.authenticateSuccessfully();
                return;
            }

        }
        output.failAuthentication();
    }
}
