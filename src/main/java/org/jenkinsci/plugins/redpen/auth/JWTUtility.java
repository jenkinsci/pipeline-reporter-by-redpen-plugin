package org.jenkinsci.plugins.redpen.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class JWTUtility {
    private static final Logger LOGGER = Logger.getLogger(JWTUtility.class.getName());

    public static final String SC_ID = "serviceConnectionId";
    public static final String SECRET = "secret";
    public static final String ID = "id";
    public static final String EMAIL = "rji-jenkins-email";
    public static final String PASS = "rji-jenkins-secret";
    public static final String ISSUER = "auth0";

    private JWTUtility() {
    }

    /**
     * This Method Generates JWT.
     * @param secretString : Redpen secret token
     * @param email : Jira user email
     * @param password : Jira User PAT Token
     * @return JwtToken
     */
    public static String getJWTToken(String secretString, String email, String password) {
        byte[] decode = Base64.getDecoder().decode(secretString);
        String s = new String(decode, StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(s);
        String serviceConnectionId = json.getString(SC_ID);
        String secret = json.getString(SECRET);
        String id = json.getString(ID);

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create().withIssuer(ISSUER).withSubject(serviceConnectionId).withClaim(ID, id).withClaim(EMAIL, email).withClaim(PASS, password).sign(algorithm);
        } catch (JWTCreationException exception) {
            LOGGER.warning(exception.getMessage());
        }
        return null;
    }
}

