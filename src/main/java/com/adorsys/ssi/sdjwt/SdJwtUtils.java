
package com.adorsys.ssi.sdjwt;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Optional;

import org.keycloak.common.util.Base64Url;
import org.keycloak.jose.jws.crypto.HashUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class SdJwtUtils {

    public static final ObjectMapper mapper = new ObjectMapper();
    private static SecureRandom RANDOM = new SecureRandom();

    public static String encodeNoPad(byte[] bytes) {
        return Base64Url.encode(bytes);
    }

    public static String hashAndBase64EncodeNoPad(byte[] disclosureBytes, String hashAlg) {
        return encodeNoPad(HashUtils.hash(hashAlg, disclosureBytes));
    }

    public static String requireNonEmpty(String str, String message) {
        return Optional.ofNullable(str)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    public static String randomSalt() {
        // 16 bytes for 128-bit entropy.
        // Base64url-encoded
        return encodeNoPad(randomBytes(16));
    }

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static String printJsonArray(Object[] array) throws JsonProcessingException {
        if (arrayEltSpaced) {
            return arraySpacedPrettyPrinter.writer.writeValueAsString(array);
        } else {
            return mapper.writeValueAsString(array);
        }
    }

    static ArraySpacedPrettyPrinter arraySpacedPrettyPrinter = new ArraySpacedPrettyPrinter();

    static class ArraySpacedPrettyPrinter extends MinimalPrettyPrinter {
        final ObjectMapper prettyPrinObjectMapper;
        final ObjectWriter writer;

        public ArraySpacedPrettyPrinter() {
            prettyPrinObjectMapper = new ObjectMapper();
            prettyPrinObjectMapper.setDefaultPrettyPrinter(this);
            writer = prettyPrinObjectMapper.writer(this);
        }

        @Override
        public void writeArrayValueSeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(',');
            jg.writeRaw(' ');
        }

        @Override
        public void writeObjectEntrySeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(',');
            jg.writeRaw(' '); // Add a space after comma
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
            jg.writeRaw(':');
            jg.writeRaw(' '); // Add a space after comma
        }
    }

    public static boolean arrayEltSpaced = true;
}
