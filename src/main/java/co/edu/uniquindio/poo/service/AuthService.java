package co.edu.uniquindio.poo.service;

import co.edu.uniquindio.poo.dto.solicitud.LoginRequestDTO;
import co.edu.uniquindio.poo.dto.common.LoginResponseDTO;
import co.edu.uniquindio.poo.model.entity.Usuario;
import co.edu.uniquindio.poo.repository.UsuarioRepository;
import co.edu.uniquindio.poo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación.
 * Gestiona el login y la generación de tokens JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Autentica al usuario y genera un token JWT.
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Autenticar con Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .or(() -> usuarioRepository.findByIdentificacion(request.email()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Crear UserDetails para generar el token
        UserDetails userDetails = User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();

        // Generar el token JWT
        String token = jwtService.generateToken(userDetails);

        // Construir la respuesta
        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .build();
    }
}
