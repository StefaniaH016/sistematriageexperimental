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

        System.out.println("🔐 LOGIN ATTEMPT: email=" + email + ", password=" + password);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario no encontrado: " + email);
                    return new IllegalArgumentException("Usuario no encontrado con el correo especificado");
                });

        System.out.println("✓ Usuario encontrado: " + usuario.getEmail());
        System.out.println("  Contraseña en BD (hasheada): " + usuario.getPassword());
        System.out.println("  Contraseña ingresada: " + password);

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            System.out.println("❌ Contraseña incorrecta");
            throw new IllegalArgumentException("La contraseña ingresada es incorrecta");
        }

        System.out.println("✓ Contraseña correcta");

        if (!usuario.getActivo()) {
            System.out.println("❌ Usuario inactivo");
            throw new IllegalArgumentException(
                    "No puedes ingresar porque este usuario se encuentra inactivo o bloqueado en el sistema");
        }

        System.out.println("✓ Usuario activo");

        // Generar token JWT real usando el servicio de JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwtToken = jwtService.generateToken(userDetails);

        System.out.println("✓ Token JWT generado: " + jwtToken.substring(0, 20) + "...");

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

    @GetMapping("/debug/usuarios")
    public ResponseEntity<ApiResponseDTO<Object>> debugUsuarios() {
        var usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(ApiResponseDTO.exitoso("Usuarios en BD", usuarios));
    }
}
