package org.jenkinsci.plugins.redpen.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import jakarta.xml.bind.DatatypeConverter;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;


public class JWTUtility {

    private JWTUtility() {
        // empty
    }

    public static String getJWTToken(String privateKeyContent,
                                     String userServiceConnectionId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String token = "";
        String privateKeyPEM = privateKeyContent
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END RSA PRIVATE KEY-----", "");
        byte[] encoded =
                DatatypeConverter.parseBase64Binary(privateKeyPEM.trim());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);


        try {
            Algorithm algorithm = Algorithm.RSA256(null, privateKey);
            token = JWT.create()
                    .withIssuer("auth0")
                    .withSubject(userServiceConnectionId)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            //Invalid Signing configuration / Couldn't convert Claims.
        }
        return token;
    }
}
