
package com.adorsys.ssi.sdjwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.keycloak.sdjwt.DisclosureSpec;
import org.keycloak.sdjwt.IssuerSignedJWT;
import org.keycloak.sdjwt.SdJwt;
import org.keycloak.sdjwt.SdJwtClaim;
import org.keycloak.sdjwt.VisibleSdJwtClaim;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class IssuerSignedJWTTest {
    /**
     * If issuer decides to disclose everything, payload of issuer signed JWT should
     * be same as the claim set.
     * This is essential for backward compatibility with non sd based jwt issuance.
     *
     */
    @Test
    public void testIssuerSignedJWTPayloadWithValidClaims() {
        JsonNode claimSet = TestUtils.readClaimSet(getClass(), "sdjwt/s6.1-holder-claims.json");

        List<org.keycloak.sdjwt.SdJwtClaim> claims = new ArrayList<>();
        claimSet.fields().forEachRemaining(entry -> claims.add(
                VisibleSdJwtClaim.builder().withClaimName(entry.getKey()).withClaimValue(entry.getValue()).build()));

        org.keycloak.sdjwt.IssuerSignedJWT jwt = org.keycloak.sdjwt.IssuerSignedJWT.builder().withClaims(claims).build();

        assertEquals(claimSet, jwt.getPayload());
    }

    @Test
    public void testIssuerSignedJWTPayloadThrowsExceptionForDuplicateClaims() {
        JsonNode claimSet = TestUtils.readClaimSet(getClass(), "sdjwt/s6.1-holder-claims.json");

        List<SdJwtClaim> claims = new ArrayList<>();

        // First fill claims
        claimSet.fields().forEachRemaining(entry -> claims.add(
                VisibleSdJwtClaim.builder().withClaimName(entry.getKey()).withClaimValue(entry.getValue()).build()));

        // First fill claims
        claimSet.fields().forEachRemaining(entry -> claims.add(
                VisibleSdJwtClaim.builder().withClaimName(entry.getKey()).withClaimValue(entry.getValue()).build()));

        // All claims are duplicate.
        assertEquals(claims.size(), claimSet.size() * 2);

        // Expecting exception
        assertThrows(IllegalArgumentException.class, () -> org.keycloak.sdjwt.IssuerSignedJWT.builder().withClaims(claims).build());
    }

    @Test
    public void testIssuerSignedJWTWithUndisclosedClaims6_1() {
        JsonNode claimSet = TestUtils.readClaimSet(getClass(), "sdjwt/s6.1-holder-claims.json");

        org.keycloak.sdjwt.DisclosureSpec disclosureSpec = org.keycloak.sdjwt.DisclosureSpec.builder()
                .withUndisclosedClaim("email", "JnwGqRFZjMprsoZobherdQ")
                .withUndisclosedClaim("phone_number", "ffZ03jm_zeHyG4-yoNt6vg")
                .withUndisclosedClaim("address", "INhOGJnu82BAtsOwiCJc_A")
                .withUndisclosedClaim("birthdate", "d0l3jsh5sBzj2oEhZxrJGw").build();

        org.keycloak.sdjwt.SdJwt sdJwt = org.keycloak.sdjwt.SdJwt.builder().withDisclosureSpec(disclosureSpec).withClaimSet(claimSet).build();

        org.keycloak.sdjwt.IssuerSignedJWT jwt = sdJwt.getIssuerSignedJWT();

        JsonNode expected = TestUtils.readClaimSet(getClass(), "sdjwt/s6.1-issuer-payload.json");
        assertEquals(expected, jwt.getPayload());
    }

    @Test
    public void testIssuerSignedJWTWithUndisclosedClaims3_3() {
        org.keycloak.sdjwt.DisclosureSpec disclosureSpec = DisclosureSpec.builder()
                .withUndisclosedClaim("given_name", "2GLC42sKQveCfGfryNRN9w")
                .withUndisclosedClaim("family_name", "eluV5Og3gSNII8EYnsxA_A")
                .withUndisclosedClaim("email", "6Ij7tM-a5iVPGboS5tmvVA")
                .withUndisclosedClaim("phone_number", "eI8ZWm9QnKPpNPeNenHdhQ")
                .withUndisclosedClaim("address", "Qg_O64zqAxe412a108iroA")
                .withUndisclosedClaim("birthdate", "AJx-095VPrpTtN4QMOqROA")
                .withUndisclosedClaim("is_over_18", "Pc33JM2LchcU_lHggv_ufQ")
                .withUndisclosedClaim("is_over_21", "G02NSrQfjFXQ7Io09syajA")
                .withUndisclosedClaim("is_over_65", "lklxF5jMYlGTPUovMNIvCA")
                .build();

        // Read claims provided by the holder
        JsonNode holderClaimSet = TestUtils.readClaimSet(getClass(), "sdjwt/s3.3-holder-claims.json");
        // Read claims added by the issuer
        JsonNode issuerClaimSet = TestUtils.readClaimSet(getClass(), "sdjwt/s3.3-issuer-claims.json");

        // Merge both
        ((ObjectNode) holderClaimSet).setAll((ObjectNode) issuerClaimSet);

        org.keycloak.sdjwt.SdJwt sdJwt = SdJwt.builder()
                .withDisclosureSpec(disclosureSpec)
                .withClaimSet(holderClaimSet)
                .build();
        IssuerSignedJWT jwt = sdJwt.getIssuerSignedJWT();

        JsonNode expected = TestUtils.readClaimSet(getClass(), "sdjwt/s3.3-issuer-payload.json");
        assertEquals(expected, jwt.getPayload());
    }
}
