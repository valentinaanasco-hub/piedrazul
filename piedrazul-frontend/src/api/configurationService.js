import axios from 'axios';

const API_BASE_URL = 'http://localhost:8090/api/v1';

/**
 * Servicio para gestionar la configuración del sistema.
 * Incluye configuración global y configuración por profesional.
 */
const configurationService = {
  // ========== CONFIGURACIÓN GLOBAL ==========

  /**
   * Obtiene la configuración global del sistema.
   */
  getGlobalConfiguration: async () => {
    const response = await axios.get(`${API_BASE_URL}/configuration/global`);
    return response.data;
  },

  /**
   * Actualiza la ventana de tiempo para agendar citas.
   */
  updateAppointmentWindow: async (weeks) => {
    const response = await axios.put(
      `${API_BASE_URL}/configuration/global/appointment-window`,
      { weeks }
    );
    return response.data;
  },

  // ========== CONFIGURACIÓN POR PROFESIONAL ==========

  /**
   * Obtiene los horarios configurados de un profesional.
   */
  getDoctorSchedule: async (doctorId) => {
    const response = await axios.get(
      `${API_BASE_URL}/configuration/doctor/${doctorId}/schedule`
    );
    return response.data;
  },

  /**
   * Actualiza los horarios de un profesional.
   */
  updateDoctorSchedule: async (doctorId, schedules) => {
    const response = await axios.put(
      `${API_BASE_URL}/configuration/doctor/${doctorId}/schedule`,
      { schedules }
    );
    return response.data;
  },

  /**
   * Elimina todos los horarios de un profesional.
   */
  deleteDoctorSchedule: async (doctorId) => {
    const response = await axios.delete(
      `${API_BASE_URL}/configuration/doctor/${doctorId}/schedule`
    );
    return response.data;
  },

  // ========== MÉDICOS ==========

  /**
   * Lista todos los médicos del sistema.
   */
  listAllDoctors: async () => {
    const response = await axios.get(`${API_BASE_URL}/medical/doctors`);
    return response.data;
  },
};

export default configurationService;
