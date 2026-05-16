package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.dto.common.ApiResponseDTO;
import co.edu.uniquindio.poo.dto.usuario.UsuarioRequestDTO;
import co.edu.uniquindio.poo.dto.usuario.UsuarioResponseDTO;
import co.edu.uniquindio.poo.model.entity.Usuario;
import co.edu.uniquindio.poo.repository.UsuarioRepository;
import co.edu.uniquindio.poo.security.JwtService;
import co.edu.uniquindio.poo.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el correo especificado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña ingresada es incorrecta");
        }

        if (!usuario.getActivo()) {
            throw new IllegalArgumentException(
                    "No puedes ingresar porque este usuario se encuentra inactivo o bloqueado en el sistema");
        }

        // Generar token JWT real usando el servicio de JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwtToken = jwtService.generateToken(userDetails);

        UsuarioResponseDTO response = UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .identificacion(usuario.getIdentificacion())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .build();

        // Devolver el token JWT en el campo mensaje
        return ResponseEntity.ok(ApiResponseDTO.exitoso(jwtToken, response));
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> registro(@Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(201).body(ApiResponseDTO.exitoso("Usuario registrado con exito", response));
    }
}
