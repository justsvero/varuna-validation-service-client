package dev.svero.playground.varuna;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.PrivateKey;
import java.util.Date;
import java.util.UUID;

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

        return Jwts.builder()
                .setIssuedAt(now)
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(subject)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}