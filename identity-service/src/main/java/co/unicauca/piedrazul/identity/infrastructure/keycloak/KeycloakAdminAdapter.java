package co.unicauca.piedrazul.identity.infrastructure.keycloak;

import co.unicauca.piedrazul.identity.domain.entities.User;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Adaptador que sincroniza usuarios con Keycloak usando el Admin REST API.
 * Cuando un usuario se registra en el identity-service, también se crea en Keycloak
 * para que pueda autenticarse y obtener tokens JWT.
 */
@Component
public class KeycloakAdminAdapter {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.admin.realm:piedrazul}")
    private String realm;

    public KeycloakAdminAdapter(Keycloak keycloakAdmin) {
        this.keycloakAdmin = keycloakAdmin;
    }

    /**
     * Crea el usuario en Keycloak y le asigna su rol de realm.
     * Llamado justo después de guardar el usuario en la BD local.
     *
     * @param user          usuario ya persistido (con ID asignado)
     * @param plainPassword contraseña en texto plano (antes del BCrypt)
     * @param roleName      rol del realm: ADMIN, DOCTOR, PACIENTE, AGENDADOR
     */
    public void createUser(User user, String plainPassword, String roleName) {
        RealmResource realmResource = keycloakAdmin.realm(realm);

        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(user.getUsername());
        userRep.setEmail(user.getUsername());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getFirstSurname());
        userRep.setEnabled(true);
        userRep.setEmailVerified(true);

        // Atributo personalizado: ID interno del sistema (visible en el token via mapper)
        userRep.setAttributes(Map.of("userId", List.of(String.valueOf(user.getId()))));

        // Credencial permanente (no temporal)
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(plainPassword);
        credential.setTemporary(false);
        userRep.setCredentials(List.of(credential));

        Response response = realmResource.users().create(userRep);
        String keycloakUserId = CreatedResponseUtil.getCreatedId(response);

        // Asignar rol del realm
        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
        realmResource.users().get(keycloakUserId).roles().realmLevel().add(List.of(role));
    }

    /**
     * Deshabilita el usuario en Keycloak cuando se desactiva en el sistema.
     */
    public void disableUser(String username) {
        RealmResource realmResource = keycloakAdmin.realm(realm);
        List<UserRepresentation> users = realmResource.users().searchByUsername(username, true);
        if (!users.isEmpty()) {
            UserRepresentation userRep = users.get(0);
            userRep.setEnabled(false);
            realmResource.users().get(userRep.getId()).update(userRep);
        }
    }
}
