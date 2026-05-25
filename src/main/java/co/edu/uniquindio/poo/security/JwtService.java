package co.edu.uniquindio.poo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de tokens JWT.
 * Genera, valida y extrae información de los tokens.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extrae el email (subject) del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token JWT para el usuario.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        // Agregar roles al token
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        extraClaims.put("roles", roles);

        return generateToken(extraClaims, userDetails);
    }

    /**
     * Genera un token JWT con claims adicionales.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            String token = Jwts.builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSignInKey(), Jwts.SIG.HS256)
                    .compact();
            System.out.println("✓ JWT Token generado exitosamente para: " + userDetails.getUsername());
            return token;
        } catch (Exception e) {
            System.out.println("❌ Error generando JWT Token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error generando JWT Token", e);
        }
    }

    /**
     * Valida si el token es válido para el usuario.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica si el token ha expirado.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae todos los claims del token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene la clave de firma a partir del secret.
     * Maneja tanto secrets en BASE64 como en texto plano.
     */
    private SecretKey getSignInKey() {
        if (secretKey == null || secretKey.isEmpty()) {
            System.out.println("❌ ERROR: JWT_SECRET no está configurado!");
            throw new RuntimeException("JWT_SECRET environment variable is not set");
        }
        
        byte[] keyBytes;
        try {
            // Intentar decodificar como BASE64
            keyBytes = Decoders.BASE64.decode(secretKey);
            System.out.println("✓ JWT_SECRET decodificado como BASE64");
        } catch (IllegalArgumentException e) {
            // Si no es BASE64 válido, usar como texto plano
            System.out.println("⚠️  JWT_SECRET no está en BASE64, usando como texto plano");
            keyBytes = secretKey.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Obtiene el tiempo de expiración configurado.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}
