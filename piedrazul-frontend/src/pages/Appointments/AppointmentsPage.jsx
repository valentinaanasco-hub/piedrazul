import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import Layout from '../../components/Layout'
import { appointmentApi, patientApi, identityApi, medicalApi } from '../../api'
import { useAuth } from '../../api/AuthContext'

const STATUS_STYLES = {
  AGENDADA:   'bg-green-100 text-green-700',
  CONFIRMADA: 'bg-green-100 text-green-700',
  PENDIENTE:  'bg-yellow-100 text-yellow-700',
  CANCELADA:  'bg-red-100 text-red-700',
  ATENDIDA:   'bg-gray-100 text-gray-600',
  REAGENDADA: 'bg-blue-100 text-blue-700',
}

const SERVICE_TYPE_LABELS = {
  CONSULTA_GENERAL: 'Consulta General',
  FISIOTERAPIA:     'Fisioterapia',
  QUIROPRAXIA:      'Quiropraxia',
  TERAPIA_NEURAL:   'Terapia Neural',
}

const STATUS_LABELS = {
  AGENDADA:   'Agendada',
  REAGENDADA: 'Reagendada',
  ATENDIDA:   'Atendida',
  CANCELADA:  'Cancelada',
  PENDIENTE:  'Pendiente',
  CONFIRMADA: 'Confirmada',
}

const PAGE_SIZE = 10
const COLS = 7

export default function AppointmentsPage() {
  const { user, hasRole } = useAuth()
  const isDoctor = hasRole('DOCTOR')

  const [allDoctors,        setAllDoctors]        = useState([])
  const [selectedDate,      setSelectedDate]      = useState('')
  const [selectedDoctor,    setSelectedDoctor]    = useState('')
  const [appointments,      setAppointments]      = useState([])
  const [patientCache,      setPatientCache]      = useState({})
  const [loading,           setLoading]           = useState(false)
  const [currentPage,       setCurrentPage]       = useState(1)
  const [filterServiceType, setFilterServiceType] = useState('')
  const [filterStatus,      setFilterStatus]      = useState('')

  // Cargar médicos al montar; si es DOCTOR, pre-seleccionarlo
  useEffect(() => {
    medicalApi.listDoctors()
      .then(res => setAllDoctors(res.data || []))
      .catch(() => {})
    if (isDoctor && user?.id) setSelectedDoctor(String(user.id))
  }, [])

  // Auto-cargar citas cuando fecha Y médico estén seleccionados
  useEffect(() => {
    if (!selectedDate || !selectedDoctor) {
      setAppointments([])
      return
    }
    setLoading(true)
    setFilterServiceType('')
    setFilterStatus('')
    setCurrentPage(1)

    appointmentApi.listByDoctorAndDate(parseInt(selectedDoctor), selectedDate)
      .then(async res => {
        const apts = res.data || []
        setAppointments(apts)

        // Cargar nombres de pacientes
        const uniqueIds = [...new Set(apts.map(a => a.patientId).filter(Boolean))]
        const cache     = { ...patientCache }
        await Promise.all(uniqueIds.map(async id => {
          if (cache[id]) return
          try {
            const [idRes, patRes] = await Promise.all([
              identityApi.getUserById(id).catch(() => null),
              patientApi.getById(id).catch(() => null),
            ])
            cache[id] = {
              name:  idRes?.data?.fullName || patRes?.data?.fullName || `Paciente ${id}`,
              phone: patRes?.data?.phone   || '—',
            }
          } catch {
            cache[id] = { name: `Paciente ${id}`, phone: '—' }
          }
        }))
        setPatientCache(cache)
      })
      .catch(() => setAppointments([]))
      .finally(() => setLoading(false))
  }, [selectedDate, selectedDoctor])

  // Filtrado client-side (tipo de servicio + estado)
  const filtered = appointments.filter(a => {
    const matchService = !filterServiceType || a.serviceType === filterServiceType
    const matchStatus  = !filterStatus      || a.status      === filterStatus
    return matchService && matchStatus
  })

  // Paginación
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE)
  const paginated  = filtered.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)

  const formatTime = (t) => typeof t === 'string' ? t.substring(0, 5) : t
  const formatDate = (dateStr) => {
    if (!dateStr) return '—'
    const [y, m, d] = dateStr.split('-')
    return `${d}/${m}/${y}`
  }

  const hasResults = !!(selectedDate && selectedDoctor)

  return (
      <Layout>
        <div className="max-w-6xl mx-auto">

          <div className="mb-6">
            <p className="text-sm text-gray-400 mb-1">Administración / Listado de Citas</p>
            <h1 className="text-2xl font-bold text-gray-800">Listado de Citas</h1>
            <p className="text-gray-500 text-sm mt-1">Consulta las citas por fecha, médico y tipo de servicio.</p>
          </div>

          {/* Panel de filtros */}
          <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-6">
            <div className="flex gap-4 items-end flex-wrap">

              {/* Fecha */}
              <div className="flex-1 min-w-[140px] max-w-[180px]">
                <label className="block text-sm text-gray-500 mb-1">Fecha</label>
                <input type="date" value={selectedDate}
                       onChange={e => {
                         setSelectedDate(e.target.value)
                         if (!isDoctor) setSelectedDoctor('')
                         setAppointments([])
                       }}
                       className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                                  focus:outline-none focus:border-blue-500 transition-colors" />
              </div>

              {/* Médico — oculto para DOCTOR (ya está pre-seleccionado) */}
              {!isDoctor && (
                <div className="flex-1 min-w-[160px] max-w-xs">
                  <label className="block text-sm text-gray-500 mb-1">Médico</label>
                  <select value={selectedDoctor}
                          disabled={!selectedDate}
                          onChange={e => setSelectedDoctor(e.target.value)}
                          className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                                     focus:outline-none focus:border-blue-500 transition-colors bg-white
                                     disabled:bg-gray-50 disabled:text-gray-400">
                    <option value="">
                      {selectedDate ? 'Selecciona un médico...' : 'Primero selecciona una fecha'}
                    </option>
                    {allDoctors.map(d => (
                      <option key={d.id} value={d.id}>
                        {d.fullName || `Profesional ${d.id}`}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {/* Tipo de cita */}
              <div className="flex-1 min-w-[150px] max-w-xs">
                <label className="block text-sm text-gray-500 mb-1">Tipo de cita</label>
                <select value={filterServiceType}
                        disabled={!hasResults || loading}
                        onChange={e => { setFilterServiceType(e.target.value); setCurrentPage(1) }}
                        className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                                   focus:outline-none focus:border-blue-500 transition-colors bg-white
                                   disabled:bg-gray-50 disabled:text-gray-400">
                  <option value="">Todos los tipos</option>
                  {Object.entries(SERVICE_TYPE_LABELS).map(([val, label]) => (
                    <option key={val} value={val}>{label}</option>
                  ))}
                </select>
              </div>

              {/* Estado */}
              <div className="flex-1 min-w-[140px] max-w-xs">
                <label className="block text-sm text-gray-500 mb-1">Estado</label>
                <select value={filterStatus}
                        disabled={!hasResults || loading}
                        onChange={e => { setFilterStatus(e.target.value); setCurrentPage(1) }}
                        className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                                   focus:outline-none focus:border-blue-500 transition-colors bg-white
                                   disabled:bg-gray-50 disabled:text-gray-400">
                  <option value="">Todos los estados</option>
                  {Object.entries(STATUS_LABELS).map(([val, label]) => (
                    <option key={val} value={val}>{label}</option>
                  ))}
                </select>
              </div>

            </div>
          </div>

          {/* Tabla */}
          {hasResults ? (
              <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
                <table className="w-full text-sm">
                  <thead>
                  <tr className="border-b border-gray-50">
                    {['Hora', 'Nombre del paciente', 'Teléfono', 'Médico', 'Tipo de cita', 'Motivo', 'Estado'].map(h => (
                        <th key={h} className="text-left px-4 py-4 text-gray-400 font-medium
                                               text-xs uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                  {loading ? (
                      <tr>
                        <td colSpan={COLS} className="text-center py-12 text-gray-400 text-sm">
                          Cargando citas...
                        </td>
                      </tr>
                  ) : paginated.length === 0 ? (
                      <tr>
                        <td colSpan={COLS} className="text-center py-12">
                          <p className="text-3xl mb-2">📅</p>
                          <p className="text-gray-400 text-sm">
                            No hay citas para el {formatDate(selectedDate)}
                            {filterServiceType || filterStatus ? ' con los filtros aplicados' : ''}
                          </p>
                        </td>
                      </tr>
                  ) : (
                      paginated.map(apt => {
                        const info = patientCache[apt.patientId] || {}
                        return (
                            <tr key={apt.appointmentId || apt.id}
                                className="hover:bg-gray-50 transition-colors">
                              <td className="px-4 py-4 font-semibold text-gray-800">
                                {formatTime(apt.startTime)}
                              </td>
                              <td className="px-4 py-4 text-gray-700">
                                {info.name || `Paciente ${apt.patientId}`}
                              </td>
                              <td className="px-4 py-4 text-gray-500">
                                {info.phone || '—'}
                              </td>
                              <td className="px-4 py-4 text-gray-700">
                                {apt.doctorName || '—'}
                              </td>
                              <td className="px-4 py-4 text-gray-700">
                                {SERVICE_TYPE_LABELS[apt.serviceType] || apt.serviceType || '—'}
                              </td>
                              <td className="px-4 py-4 text-gray-500">
                                {apt.reason || '—'}
                              </td>
                              <td className="px-4 py-4">
                                <span className={`px-3 py-1 rounded-full text-xs font-semibold
                                    ${STATUS_STYLES[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                                  {STATUS_LABELS[apt.status] || apt.status}
                                </span>
                              </td>
                            </tr>
                        )
                      })
                  )}
                  </tbody>
                </table>

                {/* Footer con paginación */}
                {!loading && filtered.length > 0 && (
                    <div className="px-6 py-4 border-t border-gray-50 flex items-center justify-between">
                      <p className="text-sm text-gray-400">
                        Mostrando{' '}
                        <span className="font-semibold text-gray-700">
                          {(currentPage - 1) * PAGE_SIZE + 1}–{Math.min(currentPage * PAGE_SIZE, filtered.length)}
                        </span>{' '}
                        de{' '}
                        <span className="font-semibold text-gray-700">{filtered.length}</span> cita(s)
                        {(filterServiceType || filterStatus) && filtered.length !== appointments.length && (
                          <span className="text-gray-400"> (filtradas de {appointments.length})</span>
                        )}
                      </p>

                      {totalPages > 1 && (
                          <div className="flex items-center gap-1">
                            <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                                    disabled={currentPage === 1}
                                    className="w-8 h-8 flex items-center justify-center rounded-lg
                                               border border-gray-200 text-gray-500 hover:bg-gray-50
                                               disabled:opacity-30 transition-colors text-sm">
                              ‹
                            </button>
                            {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                                <button key={page} onClick={() => setCurrentPage(page)}
                                        className={`w-8 h-8 flex items-center justify-center rounded-lg
                                                    text-xs font-medium transition-colors
                                                    ${currentPage === page
                                            ? 'bg-blue-600 text-white'
                                            : 'border border-gray-200 text-gray-500 hover:bg-gray-50'}`}>
                                  {page}
                                </button>
                            ))}
                            <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                                    disabled={currentPage === totalPages}
                                    className="w-8 h-8 flex items-center justify-center rounded-lg
                                               border border-gray-200 text-gray-500 hover:bg-gray-50
                                               disabled:opacity-30 transition-colors text-sm">
                              ›
                            </button>
                          </div>
                      )}

                      {!isDoctor && (
                        <Link to="/appointments/new"
                              className="text-sm text-blue-600 hover:underline font-medium">
                          + Registrar nueva cita
                        </Link>
                      )}
                    </div>
                )}
              </div>
          ) : (
              <div className="text-center py-16 text-gray-400">
                <p className="text-4xl mb-3">📅</p>
                <p className="text-sm">
                  {!selectedDate
                    ? 'Selecciona una fecha para empezar'
                    : 'Selecciona un médico para ver sus citas'}
                </p>
              </div>
          )}
        </div>
      </Layout>
  )
}
