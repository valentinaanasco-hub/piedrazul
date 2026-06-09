import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import { patientApi, medicalApi, appointmentApi } from '../../api'
import { useAuth } from '../../api/AuthContext'

export default function DashboardPage() {
  const { user, hasRole } = useAuth()
  const navigate    = useNavigate()

  const [stats, setStats] = useState({
    totalPatients:     null,
    appointmentsToday: null,
    professionals:     null,
    pendingAppts:      null,
  })

  useEffect(() => {
    // Esperar a que el usuario esté cargado antes de actuar
    if (!user) return

    // Redirigir antes de hacer cualquier llamada si el rol no corresponde
    if (hasRole('DOCTOR'))   { navigate('/doctor/appointments', { replace: true }); return }
    if (hasRole('PACIENTE')) { navigate('/patient/schedule',    { replace: true }); return }

    // Solo llega aquí ADMIN / AGENDADOR
    const today = new Date().toISOString().split('T')[0]
    Promise.allSettled([
      patientApi.listAll(),
      medicalApi.listDoctors(),
      appointmentApi.listAll(),
    ]).then(([patients, doctors, appts]) => {
      const todayApts = appts.status === 'fulfilled'
          ? (appts.value.data || []).filter(a => a.date === today)
          : []
      setStats({
        totalPatients:     patients.status === 'fulfilled' ? patients.value.data.length : '—',
        professionals:     doctors.status  === 'fulfilled' ? doctors.value.data.length  : '—',
        appointmentsToday: appts.status    === 'fulfilled' ? todayApts.length           : '—',
        pendingAppts:      appts.status    === 'fulfilled'
            ? todayApts.filter(a => a.status === 'AGENDADA').length
            : '—',
      })
    })
  }, [user])

  const statCards = [
    { label: 'Total Pacientes',   value: stats.totalPatients,     icon: '👥', color: 'text-blue-600',   bg: 'bg-blue-50'   },
    { label: 'Citas Hoy',         value: stats.appointmentsToday, icon: '📅', color: 'text-blue-600',   bg: 'bg-blue-50',  link: '/appointments' },
    { label: 'Profesionales',     value: stats.professionals,     icon: '🩺', color: 'text-purple-600', bg: 'bg-purple-50' },
    { label: 'Citas Pendientes',  value: stats.pendingAppts,      icon: '🕐', color: 'text-orange-500', bg: 'bg-orange-50' },
  ]

  return (
      <Layout>
        <div className="max-w-4xl mx-auto">

          {/* --- Encabezado --- */}
          <div className="flex items-start justify-between mb-8">
            <div>
              <h1 className="text-2xl font-bold text-gray-800">Resumen General</h1>
              <p className="text-gray-500 text-sm mt-1">
                Bienvenido al sistema de gestión médica. Aquí tienes un resumen de hoy.
              </p>
            </div>
          </div>

          {/* --- Tarjetas de estadísticas --- */}
          <div className="grid grid-cols-2 gap-5">
            {statCards.map((card) => (
                <div key={card.label}
                     className="bg-white rounded-2xl border border-gray-100 p-6 flex items-center gap-4">
                  <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-xl ${card.bg}`}>
                    {card.icon}
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-gray-500">{card.label}</p>
                    <div className="flex items-center justify-between mt-1">
                      <p className={`text-3xl font-bold ${card.color}`}>
                        {card.value ?? <span className="text-gray-300 text-xl">...</span>}
                      </p>
                      {card.link && (
                          <Link to={card.link}
                                className="text-sm text-blue-600 hover:underline font-medium">
                            Ver todas
                          </Link>
                      )}
                    </div>
                  </div>
                </div>
            ))}
          </div>

          {/* --- Accesos rápidos --- */}
          <div className="mt-8">
            <h2 className="text-base font-semibold text-gray-700 mb-4">Acciones rápidas</h2>
            <div className="grid grid-cols-3 gap-4">
              <Link to="/appointments/new"
                    className="bg-blue-600 text-white rounded-2xl p-5 hover:bg-blue-700 transition-colors group">
                <span className="text-2xl block mb-2">⊕</span>
                <p className="font-semibold text-sm">Registrar Cita</p>
                <p className="text-blue-200 text-xs mt-1">Nueva cita manual</p>
              </Link>
              <Link to="/appointments"
                    className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow group">
                <span className="text-2xl block mb-2">☰</span>
                <p className="font-semibold text-sm text-gray-800">Listar Citas</p>
                <p className="text-gray-400 text-xs mt-1">Ver citas del día</p>
              </Link>
              {hasRole('ADMIN') ? (
                <Link to="/admin"
                      className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow group">
                  <span className="text-2xl block mb-2">⚙</span>
                  <p className="font-semibold text-sm text-gray-800">Configuración</p>
                  <p className="text-gray-400 text-xs mt-1">Parámetros del sistema</p>
                </Link>
              ) : (
                <Link to="/appointments/export"
                      className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow group">
                  <span className="text-2xl block mb-2">⬇</span>
                  <p className="font-semibold text-sm text-gray-800">Exportar Citas</p>
                  <p className="text-gray-400 text-xs mt-1">Descargar reporte</p>
                </Link>
              )}
            </div>
          </div>
        </div>
      </Layout>
  )
}