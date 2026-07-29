package in.maithilart.gateway.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateAndGetClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .requireIssuer("maithilart-auth")
                .requireAudience("maithilart-api")
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

            // --- DEBUG PRINTS START ---
            System.out.println("DEBUG: Token successfully validated!");
            System.out.println("DEBUG: Subject (UserId) -> " + claims.getSubject());
            System.out.println("DEBUG: Issuer -> " + claims.getIssuer());
            System.out.println("DEBUG: Audience -> " + claims.getAudience());
            System.out.println("DEBUG: Full Claims Map -> " + claims.toString());
            // --- DEBUG PRINTS END ---

            return claims;

        } catch (Exception e) {
            // Agar yahan error aaya, toh iska matlab token mein issue hai
            System.err.println("DEBUG ERROR: Token validation failed! Reason: " + e.getMessage());
            throw e; 
        }
    }

	// 2. Token se JTI nikalne ka method
    public String extractId(String token) {
        return extractClaim(token, Claims::getId);
    }

 // Generic method jo kisi bhi claim (ID, Subject, etc.) को extract karta hai
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Ye method poore JWT payload (Claims) ko parse karta hai
    private Claims extractAllClaims(String token) {
        return  Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

