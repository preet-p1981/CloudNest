package com.preet.CloudNest.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class clerkjsonkeypro {

    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    private final Map<String, PublicKey> keycache = new HashMap<>();
    private long lastfetchtime = 0;
    private static final long CACHE_TTL = 3600 * 1000; //1 hour

    public PublicKey getpublickey(String kid) throws Exception {
        if (keycache.containsKey(kid) && System.currentTimeMillis() - lastfetchtime < CACHE_TTL) {
            return keycache.get(kid);
        }
        refreshKeys();
        return keycache.get(kid);
    }

    private void refreshKeys() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jwks = mapper.readTree(new URL(jwksUrl));

        JsonNode keys = jwks.get("keys");
        for (JsonNode keyNode : keys) {
            String kid = keyNode.get("kid").asText();
            String kty = keyNode.get("kty").asText();
            String alg = keyNode.get("alg").asText();

            if ("RSA".equals(kty) && "RS256".equals(alg)) {
                String n = keyNode.get("n").asText();
                String e = keyNode.get("e").asText();

                PublicKey publickey = createPublicKey(n, e);
                keycache.put(kid, publickey);
            }
        }
        lastfetchtime = System.currentTimeMillis();
    }

    private PublicKey createPublicKey(String n, String e) throws Exception {
        byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

        BigInteger modulusbigInt = new BigInteger(1, modulusBytes);
        BigInteger exponentbigInt = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusbigInt, exponentbigInt);
        KeyFactory factory =KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }
}
