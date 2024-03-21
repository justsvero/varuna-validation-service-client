package dev.svero.playground.varuna;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.security.PrivateKey;
import java.util.Date;
import java.util.UUID;

/**
 * Implements methods for handling JSON Web Token.
 *
 * @author Sven Roseler
 */
public class JWTUtils {
    /**
     * Generates a JSON Web Token.
     *
     * @param privateKey Private key to sign the token
     * @return Generated JSON Web Token
     */
    public String generateJwt(final String issuer, final String audience, final String subject,
                              final PrivateKey privateKey) {
        Date now = new Date();

        JwtBuilder builder = Jwts.builder()
                .issuedAt(now)
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(subject)
                .signWith(privateKey)
                ;

        builder.audience().add(audience);

        return builder.compact();
    }
}