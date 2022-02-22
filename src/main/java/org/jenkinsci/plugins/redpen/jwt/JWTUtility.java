package org.jenkinsci.plugins.redpen.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

public class JWTUtility {

    public static final String SC_ID = "serviceConnectionId";
    public static final String SECRET = "secret";
    public static final String ID = "id";
    public static final String EMAIL = "rji-jenkins-email";
    public static final String PASS = "rji-jenkins-secret";

    private JWTUtility() {
        // empty
    }

    public static String getJWTToken(String secretString, String email, String password) {

        byte[] decode = Base64.getDecoder().decode(secretString);
        String s = new String(decode, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(s);
        String serviceConnectionId = json.getString(SC_ID);
        String secret = json.getString(SECRET);
        String id = json.getString(ID);

        try {

            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("auth0")
                    .withSubject(serviceConnectionId)
                    .withClaim(ID, id)
                    .withClaim(EMAIL, email)
                    .withClaim(PASS, password)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            LOGGER.warning(exception.getMessage());
        }
        return "";

    }

    private static final Logger LOGGER = Logger.getLogger(JWTUtility.class.getName());
}
