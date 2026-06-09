import api from './index';

/**
 * Servicio para gestionar la configuración del sistema.
 * Incluye configuración global y configuración por profesional.
 * Usa la instancia 'api' con interceptor JWT para incluir el token automáticamente.
 */
const configurationService = {
  // ========== CONFIGURACIÓN GLOBAL ==========

  /**
   * Obtiene la configuración global del sistema.
   */
  getGlobalConfiguration: async () => {
    const response = await api.get('/api/v1/configuration/global');
    return response.data;
  },

  /**
   * Actualiza la ventana de tiempo para agendar citas.
   */
  updateAppointmentWindow: async (weeks) => {
    const response = await api.put(
      '/api/v1/configuration/global/appointment-window',
      { weeks }
    );
    return response.data;
  },

  // ========== CONFIGURACIÓN POR PROFESIONAL ==========

  /**
   * Obtiene los horarios configurados de un profesional.
   */
  getDoctorSchedule: async (doctorId) => {
    const response = await api.get(
      `/api/v1/configuration/doctor/${doctorId}/schedule`
    );
    return response.data;
  },

  /**
   * Actualiza los horarios de un profesional.
   */
  updateDoctorSchedule: async (doctorId, schedules) => {
    const response = await api.put(
      `/api/v1/configuration/doctor/${doctorId}/schedule`,
      { schedules }
    );
    return response.data;
  },

  /**
   * Elimina todos los horarios de un profesional.
   */
  deleteDoctorSchedule: async (doctorId) => {
    const response = await api.delete(
      `/api/v1/configuration/doctor/${doctorId}/schedule`
    );
    return response.data;
  },

  // ========== MÉDICOS ==========

  /**
   * Lista todos los médicos del sistema.
   */
  listAllDoctors: async () => {
    const response = await api.get('/api/v1/medical/doctors');
    return response.data;
  },
};

export default configurationService;
