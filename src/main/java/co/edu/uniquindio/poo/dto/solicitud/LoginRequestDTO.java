package co.edu.uniquindio.poo.dto.solicitud;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * DTO para la solicitud de inicio de sesión optimizado como Record.
 */
@Builder
public record LoginRequestDTO(
    @NotBlank(message = "El email o identificación es obligatorio")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}
