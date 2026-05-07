package co.unicauca.piedrazul.identity.presentation.mapper;

import co.unicauca.piedrazul.identity.domain.entities.User;
import co.unicauca.piedrazul.identity.presentation.dto.IdentityDTOs.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper que convierte entidades User en DTOs de respuesta.
 * Patrón estructural: evita exponer la entidad directamente en la API.
 *
 * @author Santiago Solarte
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getState().name(),
                user.getRoles().stream()
                        .map(r -> r.getRoleName())
                        .toList()
        );
    }
}
