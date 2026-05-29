package co.unicauca.piedrazul.appointment.domain.validator;

import co.unicauca.piedrazul.appointment.domain.entities.Appointment;
import co.unicauca.piedrazul.appointment.domain.entities.UserCache;
import co.unicauca.piedrazul.appointment.domain.repository.UserCacheRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Valida que el paciente y el médico existan y estén activos.
 * Busca primero en el cache Redis; si no está, consulta el identity-service
 * y guarda el resultado en cache para futuras validaciones.
 */
@Component
public class ExistenceAppointmentValidator implements AppointmentValidator {

    private final UserCacheRepository userCacheRepository;
    private final RestTemplate        restTemplate;

    private static final String IDENTITY_URL = "http://identity-service:8081";

    public ExistenceAppointmentValidator(UserCacheRepository userCacheRepository,
                                         RestTemplate restTemplate) {
        this.userCacheRepository = userCacheRepository;
        this.restTemplate        = restTemplate;
    }

    @Override
    public void validate(Appointment appointment, List<Appointment> existingOnDate) {
        UserCache patient = resolveUser(appointment.getPatientId(), "Paciente");
        if (!patient.isActive()) {
            throw new IllegalArgumentException("El paciente está inactivo");
        }

        UserCache doctor = resolveUser(appointment.getDoctorId(), "Médico");
        if (!doctor.isActive()) {
            throw new IllegalArgumentException("El médico está inactivo");
        }
    }

    /**
     * Busca el usuario en Redis; si no está, lo consulta al identity-service,
     * lo persiste en cache y lo retorna.
     */
    private UserCache resolveUser(int userId, String role) {
        Optional<UserCache> cached = userCacheRepository.findById(userId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Fallback: consultar identity-service
        try {
            String url = IDENTITY_URL + "/api/v1/identity/users/" + userId;

            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(url, Map.class);

            if (body == null) {
                throw new IllegalArgumentException(role + " no encontrado con ID: " + userId);
            }

            // El response de identity-service: { id, fullName, state, roles: [...] }
            String fullName  = String.valueOf(body.getOrDefault("fullName", ""));
            String state     = String.valueOf(body.getOrDefault("state", "ACTIVO"));
            String firstRole = "";
            Object rolesObj  = body.get("roles");
            if (rolesObj instanceof List<?> rolesList && !rolesList.isEmpty()) {
                firstRole = String.valueOf(rolesList.get(0));
            }

            UserCache user = new UserCache();
            user.setUserId(userId);
            user.setFullName(fullName);
            user.setRole(firstRole);
            user.setState(state);

            // Guardar en cache para no volver a consultar
            userCacheRepository.save(user);
            return user;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(role + " no encontrado con ID: " + userId);
        }
    }
}
