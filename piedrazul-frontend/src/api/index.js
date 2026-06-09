import axios from 'axios'

const API_BASE_URL      = 'http://localhost:8090'
const KEYCLOAK_BASE_URL = 'http://localhost:8180'
const KEYCLOAK_REALM    = 'piedrazul'
const KEYCLOAK_CLIENT   = 'piedrazul-frontend'

// ── Instancia principal (llamadas al API Gateway) ──────────
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Interceptor de request: adjunta el token JWT en cada petición al gateway
api.interceptors.request.use(config => {
  const token = localStorage.getItem('piedrazul_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptor de response: refresca automáticamente el token si expira (401)
api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config

    // Si es 401 y no hemos reintentado ya
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const refreshToken = localStorage.getItem('piedrazul_refresh')
      if (!refreshToken) {
        // No hay refresh token, redirigir a login
        localStorage.clear()
        window.location.href = '/login'
        return Promise.reject(error)
      }

      try {
        // Usar refresh token para obtener nuevo access token
        const res = await keycloakApi.refresh(refreshToken)
        const newAccessToken = res.data.access_token
        const newRefreshToken = res.data.refresh_token

        // Guardar nuevos tokens
        localStorage.setItem('piedrazul_token', newAccessToken)
        localStorage.setItem('piedrazul_refresh', newRefreshToken)

        // Reintentar la petición original con el nuevo token
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        // Refresh token también expiró, cerrar sesión
        localStorage.clear()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    // Para otros errores, rechazar normalmente
    return Promise.reject(error)
  }
)

// ── Keycloak Auth API ──────────────────────────────────────
export const keycloakApi = {
  /**
   * Autenticación con usuario y contraseña (Resource Owner Password Flow).
   * Devuelve { access_token, refresh_token, expires_in, ... }
   */
  login: (username, password) =>
    axios.post(
      `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`,
      new URLSearchParams({
        grant_type: 'password',
        client_id:  KEYCLOAK_CLIENT,
        username,
        password,
      }),
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    ),

  /**
   * Refresca el access_token usando el refresh_token.
   */
  refresh: (refreshToken) =>
    axios.post(
      `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`,
      new URLSearchParams({
        grant_type:    'refresh_token',
        client_id:     KEYCLOAK_CLIENT,
        refresh_token: refreshToken,
      }),
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    ),

  /**
   * Cierra sesión en Keycloak (invalida el refresh_token).
   */
  logout: (refreshToken) =>
    axios.post(
      `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/logout`,
      new URLSearchParams({
        client_id:     KEYCLOAK_CLIENT,
        refresh_token: refreshToken,
      }),
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    ),
}

// ── Identity Service ───────────────────────────────────────
export const identityApi = {
  getUserById:     (id)       => api.get(`/api/v1/identity/users/${id}`),
  getUserByUsername:(username) => api.get(`/api/v1/identity/users/by-username/${username}`),
  listUsers:       ()         => api.get('/api/v1/identity/users'),
  deactivate:      (id)       => api.patch(`/api/v1/identity/users/${id}/deactivate`),
}

// ── Patient Service ────────────────────────────────────────
export const patientApi = {
  registerWeb: (data) => api.post('/api/v1/register/patient', data),
  getById:     (id)   => api.get(`/api/v1/patients/${id}`),
  getMe:       ()     => api.get('/api/v1/patients/me'),
  listAll:     ()     => api.get('/api/v1/patients'),
}

// ── Medical Staff Service ──────────────────────────────────
export const medicalApi = {
  listDoctors:       ()               => api.get('/api/v1/medical/doctors'),
  getDoctorSchedule: (doctorId)       => {
    const timestamp = Date.now()
    return api.get(`/api/v1/medical/doctors/${doctorId}/schedule?_t=${timestamp}`)
  },
  getAvailability:   (doctorId, date) => {
    const timestamp = Date.now()
    return api.get(`/api/v1/medical/availability?doctorId=${doctorId}&date=${date}&_t=${timestamp}`)
  },
  updateSchedule:    (doctorId, data) =>
    api.put(`/api/v1/medical/doctors/${doctorId}/schedule`, data),
}

// ── Appointment Service ────────────────────────────────────
export const appointmentApi = {
  create:              (data)           => api.post('/api/v1/appointments', data),
  listAll:             ()               => api.get('/api/v1/appointments'),
  listByDoctorAndDate: (doctorId, date) =>
    api.get(`/api/v1/appointments/doctor/${doctorId}/date/${date}`),
  listByDoctor:        (doctorId)       =>
    api.get(`/api/v1/appointments/doctor/${doctorId}/all`),
  listByPatient:       (patientId)      =>
    api.get(`/api/v1/appointments/patient/${patientId}`),
  cancel:              (id)             =>
    api.patch(`/api/v1/appointments/${id}/cancel`),
  markAsAttended:      (id, data)       =>
    api.patch(`/api/v1/appointments/${id}/attend`, data ?? null),
  reschedule:          (id, data)       =>
    api.patch(`/api/v1/appointments/${id}/reschedule`, data),
  getPatientAuthorization: (patientId)  =>
    api.get(`/api/v1/appointments/patient/${patientId}/authorization`),
}

export default api
