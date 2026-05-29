import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../api/AuthContext'
import LoginPage                      from '../pages/Login/LoginPage'
import RegisterPage                   from '../pages/Register/RegisterPage'
import DashboardPage                  from '../pages/Dashboard/DashboardPage'
import AppointmentsPage               from '../pages/Appointments/AppointmentsPage'
import CreateAppointmentPage          from '../pages/Appointments/CreateAppointmentPage'
import DoctorAppointmentsPage         from '../pages/Appointments/DoctorAppointmentsPage'
import ExportAppointmentsPage         from '../pages/Appointments/ExportAppointmentsPage'
import AdminPage                      from '../pages/Admin/AdminPage'
import ConfigurationPage              from '../pages/Admin/ConfigurationPage'
import GlobalConfigurationPage        from '../pages/Admin/GlobalConfigurationPage'
import ProfessionalConfigurationPage  from '../pages/Admin/ProfessionalConfigurationPage'
import ScheduleAppointmentPage        from '../pages/Patient/ScheduleAppointmentPage'
import MyAppointmentsPage             from '../pages/Patient/MyAppointmentsPage'
import PatientProfilePage             from '../pages/Patient/PatientProfilePage'

function PrivateRoute({ children, requiredRole }) {
  const { isAuthenticated, hasRole } = useAuth()
  if (!isAuthenticated()) return <Navigate to="/login" replace />
  if (requiredRole && !hasRole(requiredRole)) return <Navigate to="/dashboard" replace />
  return children
}

export default function AppRouter() {
  return (
      <BrowserRouter>
        <Routes>
          {/* --- Públicas --- */}
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/"         element={<Navigate to="/login" replace />} />

          {/* --- Personal (agendador, doctor, admin) --- */}
          <Route path="/dashboard" element={
            <PrivateRoute><DashboardPage /></PrivateRoute>
          } />
          <Route path="/appointments" element={
            <PrivateRoute><AppointmentsPage /></PrivateRoute>
          } />
          <Route path="/appointments/new" element={
            <PrivateRoute><CreateAppointmentPage /></PrivateRoute>
          } />
          <Route path="/doctor/appointments" element={
            <PrivateRoute requiredRole="DOCTOR"><DoctorAppointmentsPage /></PrivateRoute>
          } />
          <Route path="/appointments/export" element={
            <PrivateRoute><ExportAppointmentsPage /></PrivateRoute>
          } />
          <Route path="/admin" element={
            <PrivateRoute requiredRole="ADMIN"><AdminPage /></PrivateRoute>
          } />
          <Route path="/admin/configuration" element={
            <PrivateRoute requiredRole="ADMIN"><ConfigurationPage /></PrivateRoute>
          } />
          <Route path="/admin/configuration/global" element={
            <PrivateRoute requiredRole="ADMIN"><GlobalConfigurationPage /></PrivateRoute>
          } />
          <Route path="/admin/configuration/professional" element={
            <PrivateRoute requiredRole="ADMIN"><ProfessionalConfigurationPage /></PrivateRoute>
          } />

          {/* --- Paciente --- */}
          <Route path="/patient/schedule" element={
            <PrivateRoute requiredRole="PACIENTE">
              <ScheduleAppointmentPage />
            </PrivateRoute>
          } />
          <Route path="/patient/appointments" element={
            <PrivateRoute requiredRole="PACIENTE">
              <MyAppointmentsPage />
            </PrivateRoute>
          } />
          <Route path="/patient/profile" element={
            <PrivateRoute requiredRole="PACIENTE">
              <PatientProfilePage />
            </PrivateRoute>
          } />

          {/* --- Redirección post-login según rol --- */}
          <Route path="/home" element={<HomeRedirect />} />
        </Routes>
      </BrowserRouter>
  )
}

function HomeRedirect() {
  const { hasRole } = useAuth()
  if (hasRole('PACIENTE')) return <Navigate to="/patient/schedule" replace />
  return <Navigate to="/dashboard" replace />
}