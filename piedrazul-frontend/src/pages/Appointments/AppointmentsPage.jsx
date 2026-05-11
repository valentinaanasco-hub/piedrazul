import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi, patientApi } from '../../api'

const STATUS_STYLES = {
  AGENDADA:   'bg-green-100 text-green-700',
  CONFIRMADA: 'bg-green-100 text-green-700',
  PENDIENTE:  'bg-yellow-100 text-yellow-700',
  CANCELADA:  'bg-red-100 text-red-700',
  ATENDIDA:   'bg-gray-100 text-gray-600',
  REAGENDADA: 'bg-blue-100 text-blue-700',
}

export default function AppointmentsPage() {
  const [doctors,        setDoctors]        = useState([])
  const [selectedDoctor, setSelectedDoctor] = useState('')
  const [selectedDate,   setSelectedDate]   = useState('')
  const [appointments,   setAppointments]   = useState([])
  const [patientInfo,    setPatientInfo]    = useState({}) // cache: {patientId: {name, phone}}
  const [loading,        setLoading]        = useState(false)
  const [searched,       setSearched]       = useState(false)

  useEffect(() => {
    medicalApi.listDoctors()
        .then(res => setDoctors(res.data))
        .catch(() => setDoctors([]))
  }, [])

  const handleSearch = async () => {
    if (!selectedDate) return
    setLoading(true)
    setSearched(true)
    try {
      const res  = await appointmentApi.listByDoctorAndDate(selectedDoctor, selectedDate)
      const apts = res.data || []
      setAppointments(apts)

      // Cargar info de pacientes únicos
      const uniqueIds = [...new Set(apts.map(a => a.patientId).filter(Boolean))]
      const infoMap   = { ...patientInfo }

      await Promise.all(uniqueIds.map(async id => {
        if (infoMap[id]) return
        try {
          const pr = await patientApi.getById(id)
          infoMap[id] = {
            name:  pr.data?.fullName || `Paciente ${id}`,
            phone: pr.data?.phone    || '—',
          }
        } catch {
          infoMap[id] = { name: `Paciente ${id}`, phone: '—' }
        }
      }))

      setPatientInfo(infoMap)
    } catch {
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const formatTime = (t) => typeof t === 'string' ? t.substring(0, 5) : t

  return (
      <Layout>
        <div className="max-w-5xl mx-auto">

          <div className="mb-6">
            <p className="text-sm text-gray-400 mb-1">Administración / Listado de Citas</p>
            <h1 className="text-2xl font-bold text-gray-800">Listado de Citas</h1>
            <p className="text-gray-500 text-sm mt-1">
              Busca y filtra todas las citas médicas programadas.
            </p>
          </div>

          {/* Filtros */}
          <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-6">
            <div className="flex gap-4 items-end">
              <div className="flex-1">
                <label className="block text-sm text-gray-500 mb-1">Profesional o terapista</label>
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

              <div className="flex-1">
                <label className="block text-sm text-gray-500 mb-1">Fecha</label>
                <input type="date" value={selectedDate}
                       onChange={e => setSelectedDate(e.target.value)}
                       className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                  focus:outline-none focus:border-blue-500 transition-colors" />
              </div>

              <button onClick={handleSearch} disabled={!selectedDate || loading}
                      className="flex items-center gap-2 bg-blue-600 text-white rounded-xl px-6 py-2.5
                text-sm font-semibold hover:bg-blue-700 transition-colors disabled:opacity-40">
                🔍 Buscar
              </button>
            </div>
          </div>

          {/* Tabla */}
          {searched && (
              <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
                <table className="w-full text-sm">
                  <thead>
                  <tr className="border-b border-gray-50">
                    {['Hora', 'Nombre del paciente', 'Teléfono de contacto', 'Tipo de cita', 'Estado'].map(h => (
                        <th key={h} className="text-left px-6 py-4 text-gray-400 font-medium
                      text-xs uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-50">
                  {loading ? (
                      <tr>
                        <td colSpan={5} className="text-center py-12 text-gray-400 text-sm">
                          Buscando citas...
                        </td>
                      </tr>
                  ) : appointments.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="text-center py-12">
                          <p className="text-3xl mb-2">📅</p>
                          <p className="text-gray-400 text-sm">No hay citas para los filtros seleccionados</p>
                        </td>
                      </tr>
                  ) : (
                      appointments.map(apt => {
                        const info = patientInfo[apt.patientId] || {}
                        return (
                            <tr key={apt.appointmentId || apt.id} className="hover:bg-gray-50 transition-colors">
                              <td className="px-6 py-4 font-semibold text-gray-800">
                                {formatTime(apt.startTime)}
                              </td>
                              <td className="px-6 py-4 text-gray-700">
                                {info.name || apt.patientName || `Paciente ${apt.patientId}`}
                              </td>
                              <td className="px-6 py-4 text-gray-500">
                                {info.phone || apt.patientPhone || '—'}
                              </td>
                              <td className="px-6 py-4 text-gray-500">{apt.reason || 'General'}</td>
                              <td className="px-6 py-4">
                          <span className={`px-3 py-1 rounded-full text-xs font-semibold
                            ${STATUS_STYLES[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                            {apt.status}
                          </span>
                              </td>
                            </tr>
                        )
                      })
                  )}
                  </tbody>
                </table>

                {!loading && searched && (
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
                <p className="text-4xl mb-3">🔍</p>
                <p className="text-sm">Selecciona una fecha y presiona Buscar</p>
              </div>
          )}
        </div>
      </Layout>
  )
}