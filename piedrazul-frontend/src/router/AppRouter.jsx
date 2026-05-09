import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../api/AuthContext'
import LoginPage               from '../pages/Login/LoginPage'
import RegisterPage            from '../pages/Register/RegisterPage'
import DashboardPage           from '../pages/Dashboard/DashboardPage'
import AppointmentsPage        from '../pages/Appointments/AppointmentsPage'
import CreateAppointmentPage   from '../pages/Appointments/CreateAppointmentPage'
import AdminPage               from '../pages/Admin/AdminPage'
import ScheduleAppointmentPage from '../pages/Patient/ScheduleAppointmentPage'
import MyAppointmentsPage      from '../pages/Patient/MyAppointmentsPage'
import PatientProfilePage      from '../pages/Patient/PatientProfilePage'

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
          <Route path="/admin" element={
            <PrivateRoute requiredRole="ADMIN"><AdminPage /></PrivateRoute>
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