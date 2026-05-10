import axios from 'axios'

const API_BASE_URL = 'http://localhost:8090'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// --- Identity Service ---
export const identityApi = {
  login:       (credentials) => api.post('/api/v1/identity/login', credentials),
  getUserById: (id)          => api.get(`/api/v1/identity/users/${id}`),
}

// --- Patient Service ---
export const patientApi = {
  registerWeb: (data) => api.post('/api/v1/register/patient', data),
  getById:     (id)   => api.get(`/api/v1/patients/${id}`),
  listAll:     ()     => api.get('/api/v1/patients'),
}

// --- Medical Staff Service ---
export const medicalApi = {
  listDoctors:       ()               => api.get('/api/v1/medical/doctors'),
  getDoctorSchedule: (doctorId)       => api.get(`/api/v1/medical/doctors/${doctorId}/schedule`),
  getAvailability:   (doctorId, date) =>
      api.get(`/api/v1/medical/availability?doctorId=${doctorId}&date=${date}`),
  updateSchedule:    (doctorId, data) =>
      api.put(`/api/v1/medical/doctors/${doctorId}/schedule`, data),
}

// --- Appointment Service ---
export const appointmentApi = {
  create:              (data)             => api.post('/api/v1/appointments', data),
  listByDoctorAndDate: (doctorId, date)   =>
      api.get(`/api/v1/appointments/doctor/${doctorId}/date/${date}`),
  listByPatient:       (patientId)        =>
      api.get(`/api/v1/appointments/patient/${patientId}`),
  cancel:              (id)               =>
      api.patch(`/api/v1/appointments/${id}/cancel`),
  getParameters:       ()                 => api.get('/api/v1/appointments/parameters'),
  updateParameter:     (key, value)       =>
      api.put(`/api/v1/appointments/parameters/${key}`, { value }),
}

export default api