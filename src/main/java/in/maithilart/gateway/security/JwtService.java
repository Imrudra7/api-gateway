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
            return Jwts.parserBuilder()
                .requireIssuer("maithilart-auth")
                .requireAudience("maithilart-api")
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        } catch (Exception e) {
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

