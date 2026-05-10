package co.unicauca.piedrazul.presentation.controllers;

import co.unicauca.piedrazul.domain.entities.SystemParameter;
import co.unicauca.piedrazul.domain.services.interfaces.ISystemParameterService;

public class SystemParameterController {

    private final ISystemParameterService parameterService;
    private String lastErrorMessage;

    public SystemParameterController(ISystemParameterService parameterService) {
        this.parameterService = parameterService;
    }

    // Recupera configuraciones dinámicas como 'NIT_CLINICA' o 'MAX_CITAS_DIA'
    public SystemParameter getSetting(String key) {
        try {
            lastErrorMessage = null;
            return parameterService.findParameter(key);
        } catch (IllegalArgumentException e) {
            lastErrorMessage = e.getMessage();
            return null;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}