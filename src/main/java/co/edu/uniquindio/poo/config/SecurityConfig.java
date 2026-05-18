package co.edu.uniquindio.poo.config;

import java.util.List;

import co.edu.uniquindio.poo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad del sistema con JWT.
 * RF-13: Autorización básica de operaciones.
 * Hito 3: Seguridad JWT implementada.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/api").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // RF-13: Autorización por roles
                        // Solicitudes - acceso más flexible para desarrollo
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes/**").authenticated()

                        // RF-13: Autorización por roles estricta
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/estado")
                        .hasAnyRole("ADMINISTRATIVO", "RESPONSABLE")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/clasificar").hasRole("ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/priorizar").hasRole("ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/asignar")
                        .hasAnyRole("ADMINISTRATIVO", "RESPONSABLE")
                        .requestMatchers(HttpMethod.PUT, "/api/solicitudes/*/cerrar")
                        .hasAnyRole("ADMINISTRATIVO", "RESPONSABLE")
                        .requestMatchers(HttpMethod.DELETE, "/api/solicitudes/**").hasRole("ADMINISTRATIVO")

                        // Usuarios - acceso más flexible para desarrollo
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/responsables-activos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasRole("ADMINISTRATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("ADMINISTRATIVO")

                        // IA
                        .requestMatchers(HttpMethod.GET, "/api/ia/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/ia/**").authenticated()

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated())
                // Configuración stateless para JWT (sin sesiones)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Habilitar Basic Auth para pruebas de integración con httpBasic()
                .httpBasic(org.springframework.security.config.Customizer.withDefaults())
                // Agregar el filtro JWT antes del filtro de autenticación estándar
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
