import { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi } from '../../api'

export default function AppointmentsPage() {
  const [doctors, setDoctors] = useState([])
  const [selectedDoctor, setSelectedDoctor] = useState('')
  const [selectedDate, setSelectedDate] = useState('')
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)

  useEffect(() => {
    medicalApi.listDoctors()
      .then(res => setDoctors(res.data))
      .catch(() => setDoctors([]))
  }, [])

  const handleSearch = async () => {
    if (!selectedDoctor || !selectedDate) return
    setLoading(true)
    setSearched(true)
    try {
      const res = await appointmentApi.listByDoctorAndDate(selectedDoctor, selectedDate)
      setAppointments(res.data)
    } catch {
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const statusColor = {
    AGENDADA:  'bg-blue-100 text-blue-700',
    CANCELADA: 'bg-red-100 text-red-700',
    ATENDIDA:  'bg-green-100 text-green-700',
  }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">

        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Listar Citas</h1>
          <p className="text-gray-500 text-sm mt-1">Consulta las citas por médico y fecha</p>
        </div>

        {/* --- Filtros --- */}
        <div className="bg-white rounded-2xl border border-gray-100 p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

            <div>
              <label className="block text-sm text-gray-500 mb-1">Médico / Terapista</label>
              <select value={selectedDoctor} onChange={e => setSelectedDoctor(e.target.value)}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500">
                <option value="">Seleccionar...</option>
                {doctors.map(d => (
                  <option key={d.id} value={d.id}>{d.fullName}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm text-gray-500 mb-1">Fecha</label>
              <input type="date" value={selectedDate} onChange={e => setSelectedDate(e.target.value)}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500" />
            </div>

            <div className="flex items-end">
              <button onClick={handleSearch} disabled={!selectedDoctor || !selectedDate || loading}
                className="w-full bg-blue-600 text-white rounded-xl py-2.5 text-sm font-semibold hover:bg-blue-700 transition-colors disabled:opacity-40">
                {loading ? 'Buscando...' : 'Buscar'}
              </button>
            </div>
          </div>
        </div>

        {/* --- Resultados --- */}
        {searched && (
          <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-50 flex items-center justify-between">
              <h2 className="font-semibold text-gray-800">Resultados</h2>
              <span className="text-sm text-gray-400">{appointments.length} cita(s)</span>
            </div>

            {appointments.length === 0 ? (
              <div className="text-center py-12 text-gray-400">
                <p className="text-3xl mb-2">📅</p>
                <p className="text-sm">No hay citas para este médico en la fecha seleccionada</p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="text-left px-6 py-3 text-gray-500 font-medium">Hora</th>
                    <th className="text-left px-6 py-3 text-gray-500 font-medium">Paciente</th>
                    <th className="text-left px-6 py-3 text-gray-500 font-medium">Motivo</th>
                    <th className="text-left px-6 py-3 text-gray-500 font-medium">Estado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {appointments.map(apt => (
                    <tr key={apt.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-medium text-gray-800">
                        {apt.startTime} — {apt.endTime}
                      </td>
                      <td className="px-6 py-4 text-gray-600">{apt.patientName}</td>
                      <td className="px-6 py-4 text-gray-500">{apt.reason}</td>
                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusColor[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                          {apt.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </Layout>
  )
}
