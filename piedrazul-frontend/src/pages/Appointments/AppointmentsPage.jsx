import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi, patientApi, identityApi } from '../../api'

const STATUS_STYLES = {
  AGENDADA:    'bg-green-100 text-green-700',
  CONFIRMADA:  'bg-green-100 text-green-700',
  PENDIENTE:   'bg-yellow-100 text-yellow-700',
  CANCELADA:   'bg-red-100 text-red-700',
  ATENDIDA:    'bg-gray-100 text-gray-600',
  REAGENDADA:  'bg-blue-100 text-blue-700',
  NO_ASISTIO:  'bg-orange-100 text-orange-700',
}

const STATUS_LABELS = {
  AGENDADA:   'Agendada',
  REAGENDADA: 'Reagendada',
  ATENDIDA:   'Atendida',
  CANCELADA:  'Cancelada',
  NO_ASISTIO: 'No Asistio',
}

export default function AppointmentsPage() {
  const [doctors,        setDoctors]        = useState([])
  const [selectedDoctor, setSelectedDoctor] = useState('')
  const [selectedDate,   setSelectedDate]   = useState('')
  const [selectedStatus, setSelectedStatus] = useState('')
  const [appointments,   setAppointments]   = useState([])
  const [patientCache,   setPatientCache]   = useState({})
  const [loading,        setLoading]        = useState(false)
  const [searched,       setSearched]       = useState(false)

  const STATUSES = ['AGENDADA', 'REAGENDADA', 'ATENDIDA', 'CANCELADA', 'NO_ASISTIO']

  useEffect(() => {
    medicalApi.listDoctors()
        .then(res => setDoctors(res.data))
        .catch(() => setDoctors([]))
  }, [])

  const handleSearch = async () => {
    setLoading(true)
    setSearched(true)
    try {
      let apts = []

      if (selectedDate) {
        const res = await appointmentApi.listByDoctorAndDate(selectedDoctor, selectedDate)
        apts = res.data || []
      } else {
        const res = await appointmentApi.listAll()
        apts = res.data || []

        if (selectedDoctor) {
          apts = apts.filter(apt => apt.doctorId === parseInt(selectedDoctor))
        }

        // Si no hay filtro de estado, mostrar solo activas por defecto
        if (selectedStatus) {
          apts = apts.filter(apt => apt.status === selectedStatus)
        } else {
          apts = apts.filter(apt => apt.status === 'AGENDADA' || apt.status === 'REAGENDADA')
        }
      }

      // Filtro de estado cuando hay fecha seleccionada
      if (selectedDate && selectedStatus) {
        apts = apts.filter(apt => apt.status === selectedStatus)
      }

      setAppointments(apts)

      // Nombre (identity-service) + teléfono (patient-service) en paralelo
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
            name:  idRes?.data?.fullName || `Paciente ${id}`,
            phone: patRes?.data?.phone   || '—',
          }
        } catch {
          cache[id] = { name: `Paciente ${id}`, phone: '—' }
        }
      }))

      setPatientCache(cache)
    } catch {
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const formatTime = (t) => typeof t === 'string' ? t.substring(0, 5) : t

  const formatDate = (dateStr) => {
    if (!dateStr) return '—'
    const [y, m, d] = dateStr.split('-')
    return `${d}/${m}/${y}`
  }

  return (
      <Layout>
        <div className="max-w-5xl mx-auto">

          <div className="mb-6">
            <p className="text-sm text-gray-400 mb-1">Administración / Listado de Citas</p>
            <h1 className="text-2xl font-bold text-gray-800">Listado de Citas</h1>
            <p className="text-gray-500 text-sm mt-1">Busca y filtra todas las citas médicas programadas.</p>
          </div>

          {/* Filtros */}
          <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-6">
            <div className="grid grid-cols-3 gap-4 mb-4">
              <div>
                <label className="block text-sm text-gray-500 mb-1">Profesional</label>
                <select value={selectedDoctor} onChange={e => setSelectedDoctor(e.target.value)}
                        className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                  focus:outline-none focus:border-blue-500 transition-colors">
                  <option value="">Todos los profesionales</option>
                  {doctors.map(d => (
                      <option key={d.id} value={d.id}>
                        {d.fullName || `Profesional ${d.id}`}
                      </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-500 mb-1">Fecha</label>
                <input type="date" value={selectedDate}
                       onChange={e => setSelectedDate(e.target.value)}
                       className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                  focus:outline-none focus:border-blue-500 transition-colors" />
              </div>
              <div>
                <label className="block text-sm text-gray-500 mb-1">Estado</label>
                <select value={selectedStatus} onChange={e => setSelectedStatus(e.target.value)}
                        className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                  focus:outline-none focus:border-blue-500 transition-colors">
                  <option value="">Todos los estados</option>
                  {STATUSES.map(s => (
                      <option key={s} value={s}>{STATUS_LABELS[s] || s}</option>
                  ))}
                </select>
              </div>
            </div>
            <div className="flex justify-end">
              <button onClick={handleSearch} disabled={loading}
                      className="bg-blue-600 text-white rounded-xl px-6 py-2.5
                text-sm font-semibold hover:bg-blue-700 transition-colors disabled:opacity-40">
                Buscar
              </button>
            </div>
          </div>

          {/* Tabla */}
          {searched && (
              <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
                <table className="w-full text-sm">
                  <thead>
                  <tr className="border-b border-gray-50">
                    {['Fecha', 'Hora', 'Nombre del paciente', 'Médico', 'Teléfono de contacto', 'Tipo de cita', 'Estado'].map(h => (
                        <th key={h} className="text-left px-6 py-4 text-gray-400 font-medium
                      text-xs uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                  {loading ? (
                      <tr>
                        <td colSpan={7} className="text-center py-12 text-gray-400 text-sm">
                          Buscando citas...
                        </td>
                      </tr>
                  ) : appointments.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="text-center py-12">
                          <p className="text-gray-400 text-sm">No hay citas para los filtros seleccionados</p>
                        </td>
                      </tr>
                  ) : (
                      appointments.map(apt => {
                        const info   = patientCache[apt.patientId] || {}
                        const doctor = doctors.find(d => d.id === apt.doctorId)
                        return (
                            <tr key={apt.appointmentId || apt.id} className="hover:bg-gray-50 transition-colors">
                              <td className="px-6 py-4 text-gray-700">
                                {formatDate(apt.date)}
                              </td>
                              <td className="px-6 py-4 font-semibold text-gray-800">
                                {formatTime(apt.startTime)}
                              </td>
                              <td className="px-6 py-4 text-gray-700">
                                {info.name || `Paciente ${apt.patientId}`}
                              </td>
                              <td className="px-6 py-4 text-gray-700">
                                {doctor?.fullName || `Médico ${apt.doctorId}`}
                              </td>
                              <td className="px-6 py-4 text-gray-500">
                                {info.phone || '—'}
                              </td>
                              <td className="px-6 py-4 text-gray-500">{apt.reason || 'General'}</td>
                              <td className="px-6 py-4">
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

                {!loading && searched && appointments.length > 0 && (
                    <div className="px-6 py-4 border-t border-gray-50 flex items-center justify-between">
                      <p className="text-sm text-gray-400">
                        Total: <span className="font-semibold text-gray-700">{appointments.length}</span> cita(s)
                      </p>
                      <Link to="/appointments/new"
                            className="text-sm text-blue-600 hover:underline font-medium">
                        + Registrar nueva cita
                      </Link>
                    </div>
                )}
              </div>
          )}

          {!searched && (
              <div className="text-center py-16 text-gray-400">
                <p className="text-sm">Presiona Buscar para ver todas las citas agendadas</p>
                <p className="text-xs mt-2">O selecciona una fecha para filtrar por día específico</p>
              </div>
          )}
        </div>
      </Layout>
  )
}
