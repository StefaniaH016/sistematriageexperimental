package co.edu.uniquindio.poo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Controlador para la raíz de la API.
 * Proporciona información básica del estado del servicio.
 */
@RestController
public class ApiController {

    @GetMapping("/api")
    public Map<String, String> index() {
        return Map.of(
                "name", "Sistema de Triage y Gestión de Solicitudes Académicas API",
                "version", "1.0",
                "status", "Running",
                "docs", "/swagger-ui.html");
    }

    /**
     * Evita el error 500 cuando el navegador solicita favicon.ico automáticamente.
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
