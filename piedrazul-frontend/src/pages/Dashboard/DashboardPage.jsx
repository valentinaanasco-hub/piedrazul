import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import Layout from '../../components/Layout'
import { patientApi, medicalApi, appointmentApi } from '../../api'
import { useAuth } from '../../api/AuthContext'

export default function DashboardPage() {
  const { user, hasRole } = useAuth()

  return (
    <Layout>
      {hasRole('DOCTOR')     ? <DoctorDashboard    user={user} /> :
       hasRole('AGENDADOR')  ? <AgendadorDashboard user={user} /> :
                               <AdminDashboard />}
    </Layout>
  )
}

// ── Dashboard para DOCTOR ─────────────────────────────────────────────────────
function DoctorDashboard({ user }) {
  const today    = new Date().toISOString().split('T')[0]
  const doctorId = user?.id

  const [todayApts,    setTodayApts]    = useState(null)
  const [nextApt,      setNextApt]      = useState(null)
  const [weekCount,    setWeekCount]    = useState(null)
  const [pendingCount, setPendingCount] = useState(null)

  useEffect(() => {
    if (!doctorId) return

    // Citas de hoy
    appointmentApi.listByDoctorAndDate(doctorId, today)
      .then(res => {
        const apts = res.data || []
        setTodayApts(apts.length)
        const pending = apts.filter(a => a.status === 'AGENDADA' || a.status === 'REAGENDADA')
        setPendingCount(pending.length)
        const next = [...pending].sort((a, b) => a.startTime.localeCompare(b.startTime))
        setNextApt(next[0] || null)
      })
      .catch(() => { setTodayApts(0); setPendingCount(0) })

    // Citas de la semana
    Promise.all(
      getWeekDays().map(d =>
        appointmentApi.listByDoctorAndDate(doctorId, d).catch(() => ({ data: [] }))
      )
    ).then(results => {
      setWeekCount(results.reduce((sum, r) => sum + (r.data || []).length, 0))
    })
  }, [doctorId])

  const formatTime = (t) => typeof t === 'string' ? t.substring(0, 5) : t

  return (
    <div className="max-w-4xl mx-auto">

      {/* Encabezado */}
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">
            Bienvenido, {user?.fullName?.split(' ')[0] || 'Doctor'}
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            {formatDateFull(new Date())} — aquí tienes tu resumen de hoy.
          </p>
        </div>
        <Link to="/doctor/appointments"
              className="bg-blue-600 text-white rounded-xl px-4 py-2.5 text-sm font-medium
                hover:bg-blue-700 transition-colors">
          Ver mis citas
        </Link>
      </div>

      {/* Tarjetas */}
      <div className="grid grid-cols-2 gap-5 mb-8">

        <div className="bg-white rounded-2xl border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl flex items-center justify-center text-xl bg-blue-50">📅</div>
          <div className="flex-1">
            <p className="text-sm text-gray-500">Citas hoy</p>
            <div className="flex items-center justify-between mt-1">
              <p className="text-3xl font-bold text-blue-600">
                {todayApts ?? <span className="text-gray-300 text-xl">...</span>}
              </p>
              <Link to="/doctor/appointments" className="text-sm text-blue-600 hover:underline font-medium">
                Ver todas
              </Link>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl flex items-center justify-center text-xl bg-orange-50">🕐</div>
          <div className="flex-1">
            <p className="text-sm text-gray-500">Pendientes hoy</p>
            <p className="text-3xl font-bold text-orange-500 mt-1">
              {pendingCount ?? <span className="text-gray-300 text-xl">...</span>}
            </p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl flex items-center justify-center text-xl bg-purple-50">📋</div>
          <div className="flex-1">
            <p className="text-sm text-gray-500">Citas esta semana</p>
            <p className="text-3xl font-bold text-purple-600 mt-1">
              {weekCount ?? <span className="text-gray-300 text-xl">...</span>}
            </p>
          </div>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-6 flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl flex items-center justify-center text-xl bg-green-50">🩺</div>
          <div className="flex-1">
            <p className="text-sm text-gray-500">Especialidad</p>
            <div className="mt-1">
              <DoctorSpecialty doctorId={doctorId} />
            </div>
          </div>
        </div>

      </div>

      {/* Próxima cita */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 mb-6">
        <h2 className="text-base font-semibold text-gray-700 mb-4">Próxima cita pendiente hoy</h2>
        {nextApt ? (
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 bg-blue-600 rounded-xl flex items-center justify-center shrink-0">
              <span className="text-white text-base font-bold">{formatTime(nextApt.startTime)}</span>
            </div>
            <div>
              <p className="font-semibold text-gray-800">Paciente #{nextApt.patientId}</p>
              <p className="text-sm text-gray-500 mt-0.5">{nextApt.reason || 'Sin motivo especificado'}</p>
              <span className="inline-block mt-1.5 px-2.5 py-0.5 bg-blue-100 text-blue-700
                text-xs font-semibold rounded-full">{nextApt.status}</span>
            </div>
            <div className="ml-auto">
              <Link to="/doctor/appointments"
                    className="text-sm text-blue-600 hover:underline font-medium">
                Ir a gestionar
              </Link>
            </div>
          </div>
        ) : todayApts === 0 ? (
          <p className="text-sm text-gray-400">No tienes citas programadas para hoy.</p>
        ) : (
          <p className="text-sm text-gray-400">Todas las citas de hoy ya fueron atendidas.</p>
        )}
      </div>

      {/* Acciones rápidas */}
      <div>
        <h2 className="text-base font-semibold text-gray-700 mb-4">Acciones rápidas</h2>
        <div className="grid grid-cols-2 gap-4">
          <Link to="/doctor/appointments"
                className="bg-blue-600 text-white rounded-2xl p-5 hover:bg-blue-700 transition-colors">
            <span className="text-2xl block mb-2">📅</span>
            <p className="font-semibold text-sm">Gestionar Citas</p>
            <p className="text-blue-200 text-xs mt-1">Ver, atender o reagendar</p>
          </Link>
          <Link to="/appointments/new"
                className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow">
            <span className="text-2xl block mb-2">📝</span>
            <p className="font-semibold text-sm text-gray-800">Registrar Cita</p>
            <p className="text-gray-400 text-xs mt-1">Nueva cita para un paciente</p>
          </Link>
        </div>
      </div>
    </div>
  )
}

// Carga y muestra la especialidad del doctor
function DoctorSpecialty({ doctorId }) {
  const [specialty, setSpecialty] = useState(null)
  useEffect(() => {
    if (!doctorId) return
    medicalApi.listDoctors()
      .then(res => {
        const doc = (res.data || []).find(d => d.id === doctorId)
        setSpecialty(doc?.specialties?.[0] || 'Medicina General')
      })
      .catch(() => setSpecialty('Medicina General'))
  }, [doctorId])
  return (
    <p className="text-lg font-bold text-green-600">
      {specialty ?? <span className="text-gray-300 text-base">...</span>}
    </p>
  )
}

// ── Dashboard para AGENDADOR ─────────────────────────────────────────────────
function AgendadorDashboard({ user }) {
  const [allApts,       setAllApts]       = useState(null)   // todas las citas
  const [doctors,       setDoctors]       = useState([])
  const [totalPatients, setTotalPatients] = useState(null)
  const [loading,       setLoading]       = useState(true)

  useEffect(() => {
    Promise.allSettled([
      appointmentApi.listAll(),
      medicalApi.listDoctors(),
      patientApi.listAll(),
    ]).then(([aptsRes, docsRes, patsRes]) => {
      setAllApts(aptsRes.status === 'fulfilled' ? aptsRes.value.data || [] : [])
      setDoctors(docsRes.status === 'fulfilled' ? docsRes.value.data || [] : [])
      setTotalPatients(patsRes.status === 'fulfilled' ? patsRes.value.data?.length ?? 0 : 0)
      setLoading(false)
    })
  }, [])

  const today = new Date().toISOString().split('T')[0]

  // Métricas derivadas de allApts
  const activeApts   = allApts ? allApts.filter(a => a.status === 'AGENDADA' || a.status === 'REAGENDADA') : []
  const todayApts    = allApts ? allApts.filter(a => a.date === today) : []
  const todayPending = todayApts.filter(a => a.status === 'AGENDADA' || a.status === 'REAGENDADA')

  // Distribución por estado
  const statusCount = allApts ? allApts.reduce((acc, a) => {
    acc[a.status] = (acc[a.status] || 0) + 1
    return acc
  }, {}) : {}

  // Próximas 5 citas activas ordenadas por fecha y hora
  const upcomingApts = [...activeApts]
    .sort((a, b) => a.date === b.date
      ? a.startTime.localeCompare(b.startTime)
      : a.date.localeCompare(b.date))
    .slice(0, 5)

  const getDoctorName = (id) => doctors.find(d => d.id === id)?.fullName || `Médico ${id}`

  const formatDate = (dateStr) => {
    if (!dateStr) return '—'
    const [y, m, d] = dateStr.split('-')
    const months = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
    return `${parseInt(d)} ${months[parseInt(m) - 1]}`
  }

  const formatTime = (t) => typeof t === 'string' ? t.substring(0, 5) : t

  const STATUS_BADGE = {
    AGENDADA:    'bg-green-100 text-green-700',
    REAGENDADA:  'bg-blue-100 text-blue-700',
    ATENDIDA:    'bg-gray-100 text-gray-600',
    CANCELADA:   'bg-red-100 text-red-700',
    NO_ASISTIO:  'bg-orange-100 text-orange-700',
  }

  const STATUS_LABEL = {
    AGENDADA:   'Agendada',
    REAGENDADA: 'Reagendada',
    ATENDIDA:   'Atendida',
    CANCELADA:  'Cancelada',
    NO_ASISTIO: 'NO ASISTIO',
  }

  return (
    <div className="max-w-5xl mx-auto">

      {/* Encabezado */}
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">
            Bienvenido, {user?.fullName?.split(' ')[0] || 'Agendador'}
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            {formatDateFull(new Date())}
          </p>
        </div>
        <Link to="/appointments/new"
              className="bg-blue-600 text-white rounded-xl px-4 py-2.5 text-sm font-medium
                hover:bg-blue-700 transition-colors">
          + Registrar cita
        </Link>
      </div>

      {/* Tarjetas de métricas */}
      <div className="grid grid-cols-4 gap-4 mb-6">

        <div className="bg-white rounded-2xl border border-gray-100 p-5">
          <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center text-lg mb-3">📅</div>
          <p className="text-xs text-gray-400 uppercase tracking-wide font-medium">Citas activas</p>
          <p className="text-3xl font-bold text-blue-600 mt-1">
            {loading ? <span className="text-gray-200 text-xl">...</span> : activeApts.length}
          </p>
          <p className="text-xs text-gray-400 mt-1">Agendadas + Reagendadas</p>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-5">
          <div className="w-10 h-10 rounded-xl bg-orange-50 flex items-center justify-center text-lg mb-3">🕐</div>
          <p className="text-xs text-gray-400 uppercase tracking-wide font-medium">Citas hoy</p>
          <p className="text-3xl font-bold text-orange-500 mt-1">
            {loading ? <span className="text-gray-200 text-xl">...</span> : todayApts.length}
          </p>
          <p className="text-xs text-gray-400 mt-1">
            {todayPending.length} pendiente{todayPending.length !== 1 ? 's' : ''}
          </p>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-5">
          <div className="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center text-lg mb-3">👥</div>
          <p className="text-xs text-gray-400 uppercase tracking-wide font-medium">Pacientes</p>
          <p className="text-3xl font-bold text-purple-600 mt-1">
            {loading ? <span className="text-gray-200 text-xl">...</span> : (totalPatients ?? 0)}
          </p>
          <p className="text-xs text-gray-400 mt-1">Registrados en el sistema</p>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-5">
          <div className="w-10 h-10 rounded-xl bg-green-50 flex items-center justify-center text-lg mb-3">🩺</div>
          <p className="text-xs text-gray-400 uppercase tracking-wide font-medium">Médicos</p>
          <p className="text-3xl font-bold text-green-600 mt-1">
            {loading ? <span className="text-gray-200 text-xl">...</span> : doctors.length}
          </p>
          <p className="text-xs text-gray-400 mt-1">Disponibles para agendar</p>
        </div>

      </div>

      <div className="grid grid-cols-3 gap-6 mb-6">

        {/* Próximas citas */}
        <div className="col-span-2 bg-white rounded-2xl border border-gray-100 p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-semibold text-gray-700">Próximas citas agendadas</h2>
            <Link to="/appointments" className="text-xs text-blue-600 hover:underline font-medium">
              Ver todas
            </Link>
          </div>

          {loading ? (
            <p className="text-sm text-gray-300 text-center py-8">Cargando...</p>
          ) : upcomingApts.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-sm text-gray-400">No hay citas activas próximas</p>
              <Link to="/appointments/new"
                    className="text-xs text-blue-600 hover:underline mt-1 inline-block">
                Registrar primera cita
              </Link>
            </div>
          ) : (
            <div className="space-y-2">
              {upcomingApts.map(apt => (
                <div key={apt.appointmentId}
                     className="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 transition-colors">
                  {/* Fecha */}
                  <div className="w-12 text-center shrink-0">
                    <p className="text-xs font-bold text-blue-600">{formatDate(apt.date)}</p>
                    <p className="text-xs text-gray-400">{formatTime(apt.startTime)}</p>
                  </div>
                  {/* Separador */}
                  <div className="w-px h-8 bg-gray-100 shrink-0" />
                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-800 truncate">
                      {getDoctorName(apt.doctorId)}
                    </p>
                    <p className="text-xs text-gray-400 truncate">
                      {apt.reason || 'Sin motivo especificado'}
                    </p>
                  </div>
                  {/* Estado */}
                  <span className={`text-xs px-2.5 py-1 rounded-full font-semibold shrink-0
                    ${STATUS_BADGE[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                    {STATUS_LABEL[apt.status] || apt.status}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Distribución por estado */}
        <div className="bg-white rounded-2xl border border-gray-100 p-5">
          <h2 className="text-sm font-semibold text-gray-700 mb-4">Estado de citas</h2>
          {loading ? (
            <p className="text-sm text-gray-300 text-center py-8">Cargando...</p>
          ) : (
            <div className="space-y-3">
              {[
                { key: 'AGENDADA',   label: 'Agendadas',   color: 'bg-green-500' },
                { key: 'REAGENDADA', label: 'Reagendadas', color: 'bg-blue-500'  },
                { key: 'ATENDIDA',   label: 'Atendidas',   color: 'bg-gray-400'  },
                { key: 'CANCELADA',  label: 'Canceladas',  color: 'bg-red-400'   },
                { key: 'NO_ASISTIO', label: 'NO ASISTIO',  color: 'bg-orange-400'},
              ].map(({ key, label, color }) => {
                const count = statusCount[key] || 0
                const total = allApts?.length || 1
                const pct   = Math.round((count / total) * 100)
                return (
                  <div key={key}>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-gray-600">{label}</span>
                      <span className="font-semibold text-gray-700">{count}</span>
                    </div>
                    <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
                      <div className={`h-full ${color} rounded-full transition-all`}
                           style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                )
              })}
              <p className="text-xs text-gray-400 pt-1 text-right">
                Total: {allApts?.length || 0} citas
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Acciones rápidas */}
      <div>
        <h2 className="text-sm font-semibold text-gray-700 mb-3">Acciones rápidas</h2>
        <div className="grid grid-cols-3 gap-4">
          <Link to="/appointments/new"
                className="bg-blue-600 text-white rounded-2xl p-5 hover:bg-blue-700 transition-colors">
            <span className="text-2xl block mb-2">⊕</span>
            <p className="font-semibold text-sm">Registrar Cita</p>
            <p className="text-blue-200 text-xs mt-1">Nueva cita manual</p>
          </Link>
          <Link to="/appointments"
                className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow">
            <span className="text-2xl block mb-2">☰</span>
            <p className="font-semibold text-sm text-gray-800">Listar Citas</p>
            <p className="text-gray-400 text-xs mt-1">Buscar y filtrar</p>
          </Link>
          <Link to="/appointments/export"
                className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow">
            <span className="text-2xl block mb-2">⬇</span>
            <p className="font-semibold text-sm text-gray-800">Exportar Citas</p>
            <p className="text-gray-400 text-xs mt-1">Descargar en CSV</p>
          </Link>
        </div>
      </div>
    </div>
  )
}

// ── Dashboard para ADMIN ──────────────────────────────────────────────────────
function AdminDashboard() {
  const today = new Date().toISOString().split('T')[0]

  const [stats, setStats] = useState({
    totalPatients:     null,
    appointmentsToday: null,
    professionals:     null,
    pendingAppts:      null,
  })

  useEffect(() => {
    Promise.allSettled([
      patientApi.listAll(),
      medicalApi.listDoctors(),
      appointmentApi.listByDoctorAndDate('', today),
    ]).then(([patients, doctors, appts]) => {
      setStats({
        totalPatients:     patients.status === 'fulfilled' ? patients.value.data.length : '—',
        professionals:     doctors.status  === 'fulfilled' ? doctors.value.data.length  : '—',
        appointmentsToday: appts.status    === 'fulfilled' ? appts.value.data.length    : '—',
        pendingAppts:      appts.status    === 'fulfilled'
          ? appts.value.data.filter(a => a.status === 'AGENDADA').length
          : '—',
      })
    })
  }, [])

  const statCards = [
    { label: 'Total Pacientes',  value: stats.totalPatients,     icon: '👥', color: 'text-blue-600',   bg: 'bg-blue-50'   },
    { label: 'Citas Hoy',        value: stats.appointmentsToday, icon: '📅', color: 'text-blue-600',   bg: 'bg-blue-50',  link: '/appointments' },
    { label: 'Profesionales',    value: stats.professionals,     icon: '🩺', color: 'text-purple-600', bg: 'bg-purple-50' },
    { label: 'Citas Pendientes', value: stats.pendingAppts,      icon: '🕐', color: 'text-orange-500', bg: 'bg-orange-50' },
  ]

  return (
    <div className="max-w-4xl mx-auto">

      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Resumen General</h1>
          <p className="text-gray-500 text-sm mt-1">
            Bienvenido al sistema de gestión médica. Aquí tienes un resumen de hoy.
          </p>
        </div>
        <Link to="/admin"
              className="bg-white border border-gray-200 rounded-xl px-4 py-2.5 text-sm font-medium
                text-gray-700 hover:bg-gray-50 transition-colors">
          Configuración del sistema
        </Link>
      </div>

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
                  <Link to={card.link} className="text-sm text-blue-600 hover:underline font-medium">
                    Ver todas
                  </Link>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-8">
        <h2 className="text-base font-semibold text-gray-700 mb-4">Acciones rápidas</h2>
        <div className="grid grid-cols-3 gap-4">
          <Link to="/appointments/new"
                className="bg-blue-600 text-white rounded-2xl p-5 hover:bg-blue-700 transition-colors">
            <span className="text-2xl block mb-2">⊕</span>
            <p className="font-semibold text-sm">Registrar Cita</p>
            <p className="text-blue-200 text-xs mt-1">Nueva cita manual</p>
          </Link>
          <Link to="/appointments"
                className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow">
            <span className="text-2xl block mb-2">☰</span>
            <p className="font-semibold text-sm text-gray-800">Listar Citas</p>
            <p className="text-gray-400 text-xs mt-1">Ver citas del día</p>
          </Link>
          <Link to="/admin"
                className="bg-white border border-gray-100 rounded-2xl p-5 hover:shadow-md transition-shadow">
            <span className="text-2xl block mb-2">⚙</span>
            <p className="font-semibold text-sm text-gray-800">Configuración</p>
            <p className="text-gray-400 text-xs mt-1">Parámetros del sistema</p>
          </Link>
        </div>
      </div>
    </div>
  )
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function getWeekDays() {
  const days   = []
  const now    = new Date()
  const monday = new Date(now)
  monday.setDate(now.getDate() - ((now.getDay() + 6) % 7))
  for (let i = 0; i < 5; i++) {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    days.push(d.toISOString().split('T')[0])
  }
  return days
}

function formatDateFull(date) {
  const days   = ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado']
  const months = ['enero','febrero','marzo','abril','mayo','junio',
                  'julio','agosto','septiembre','octubre','noviembre','diciembre']
  return `${days[date.getDay()]}, ${date.getDate()} de ${months[date.getMonth()]} de ${date.getFullYear()}`
}
