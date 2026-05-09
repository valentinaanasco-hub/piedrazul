import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../api/AuthContext'
import LoginPage from '../pages/Login/LoginPage'
import RegisterPage from '../pages/Register/RegisterPage'
import DashboardPage from '../pages/Dashboard/DashboardPage'
import AppointmentsPage from '../pages/Appointments/AppointmentsPage'
import CreateAppointmentPage from '../pages/Appointments/CreateAppointmentPage'
import AdminPage from '../pages/Admin/AdminPage'

// --- Ruta protegida: redirige al login si no está autenticado ---
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
        {/* --- Rutas públicas --- */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* --- Rutas protegidas --- */}
        <Route path="/dashboard" element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        } />

        {/* RF1 — Listar citas */}
        <Route path="/appointments" element={
          <PrivateRoute>
            <AppointmentsPage />
          </PrivateRoute>
        } />

        {/* RF2 — Crear cita */}
        <Route path="/appointments/new" element={
          <PrivateRoute>
            <CreateAppointmentPage />
          </PrivateRoute>
        } />

        {/* RF4 — Configuración admin */}
        <Route path="/admin" element={
          <PrivateRoute requiredRole="ADMIN">
            <AdminPage />
          </PrivateRoute>
        } />
      </Routes>
    </BrowserRouter>
  )
}
