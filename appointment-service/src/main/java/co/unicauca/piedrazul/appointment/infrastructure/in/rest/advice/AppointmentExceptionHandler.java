package co.unicauca.piedrazul.appointment.infrastructure.in.rest.advice;

import co.unicauca.piedrazul.appointment.domain.exception.AppointmentConflictException;
import co.unicauca.piedrazul.appointment.domain.exception.AppointmentNotFoundException;
import co.unicauca.piedrazul.appointment.domain.exception.AppointmentValidationException;
import co.unicauca.piedrazul.appointment.infrastructure.in.rest.dto.AppointmentDTOs.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Adaptador de infraestructura: traduce excepciones de dominio a respuestas HTTP.
 * Centraliza el mapeo en un solo lugar — los controladores no necesitan try/catch.
 *
 * AppointmentNotFoundException    → 404 NOT FOUND
 * AppointmentConflictException    → 409 CONFLICT
 * AppointmentValidationException  → 422 UNPROCESSABLE ENTITY
 */
@RestControllerAdvice
public class AppointmentExceptionHandler {

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotFound(AppointmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<MessageResponse> handleConflict(AppointmentConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(AppointmentValidationException.class)
    public ResponseEntity<MessageResponse> handleValidation(AppointmentValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new MessageResponse(ex.getMessage()));
    }
}
