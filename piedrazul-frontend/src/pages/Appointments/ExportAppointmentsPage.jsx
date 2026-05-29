import { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi, patientApi, identityApi } from '../../api'

const STATUSES = ['AGENDADA', 'ATENDIDA', 'CANCELADA', 'REAGENDADA']

export default function ExportAppointmentsPage() {
  const [doctors,       setDoctors]       = useState([])
  const [specialties,   setSpecialties]   = useState([])
  const [appointments,  setAppointments]  = useState([])
  const [patientInfo,   setPatientInfo]   = useState({}) // {id: {name, phone}}
  const [loading,       setLoading]       = useState(false)
  const [toast,         setToast]         = useState(null) // { text, filename }

  const [filters, setFilters] = useState({
    from:      '',
    to:        '',
    status:    '',
    specialty: '',
    doctorId:  '',
  })

  // Cargar médicos y especialidades al montar
  useEffect(() => {
    medicalApi.listDoctors().then(res => {
      const docs = res.data || []
      setDoctors(docs)
      const specSet = new Set()
      docs.forEach(d => d.specialties?.forEach(s => specSet.add(s)))
      setSpecialties([...specSet].sort())
    }).catch(() => {})
  }, [])

  const doctorsBySpecialty = filters.specialty
    ? doctors.filter(d => d.specialties?.includes(filters.specialty))
    : doctors

  const handleFilter = (field, value) => {
    setFilters(prev => ({
      ...prev,
      [field]: value,
      // Resetear doctor si cambia especialidad
      ...(field === 'specialty' ? { doctorId: '' } : {}),
    }))
  }

  // Vista previa: carga y filtra
  const handleSearch = async () => {
    setLoading(true)
    try {
      const res = await appointmentApi.listAll()
      let apts  = res.data || []

      if (filters.from)     apts = apts.filter(a => a.date >= filters.from)
      if (filters.to)       apts = apts.filter(a => a.date <= filters.to)
      if (filters.status)   apts = apts.filter(a => a.status === filters.status)
      if (filters.doctorId) apts = apts.filter(a => a.doctorId === parseInt(filters.doctorId))
      if (filters.specialty) {
        const ids = new Set(
          doctors.filter(d => d.specialties?.includes(filters.specialty)).map(d => d.id)
        )
        apts = apts.filter(a => ids.has(a.doctorId))
      }

      // Ordenar por fecha y hora
      apts.sort((a, b) => a.date.localeCompare(b.date) || a.startTime.localeCompare(b.startTime))
      setAppointments(apts)

      // Resolver info de pacientes únicos
      // Nombre: identity-service (los nombres son @Transient en patient-service)
      // Teléfono: patient-service
      const uniqueIds = [...new Set(apts.map(a => a.patientId).filter(Boolean))]
      const infoMap   = { ...patientInfo }
      await Promise.all(uniqueIds.map(async id => {
        if (infoMap[id]) return
        const [idRes, patRes] = await Promise.allSettled([
          identityApi.getUserById(id),
          patientApi.getById(id),
        ])
        infoMap[id] = {
          name:  idRes.status  === 'fulfilled' ? (idRes.value.data?.fullName || `Paciente ${id}`) : `Paciente ${id}`,
          phone: patRes.status === 'fulfilled' ? (patRes.value.data?.phone   || '—')              : '—',
        }
      }))
      setPatientInfo(infoMap)
    } catch {
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const getDoctorName  = (id) => doctors.find(d => d.id === id)?.fullName     || `Médico ${id}`
  const getPatientName = (id) => patientInfo[id]?.name  || `Paciente ${id}`
  const getPatientPhone= (id) => patientInfo[id]?.phone || '—'
  const formatTime     = (t)  => typeof t === 'string' ? t.substring(0, 5)   : t

  const handleExport = () => {
    if (appointments.length === 0) return

    const headers = ['ID', 'Cédula Paciente', 'Paciente', 'Teléfono', 'Profesional', 'Especialidad', 'Fecha', 'Hora inicio', 'Hora fin', 'Estado', 'Motivo']
    const rows = appointments.map(a => {
      const doc = doctors.find(d => d.id === a.doctorId)
      return [
        a.appointmentId,
        a.patientId,
        `"${getPatientName(a.patientId).replace(/"/g, '""')}"`,
        getPatientPhone(a.patientId),
        `"${getDoctorName(a.doctorId).replace(/"/g, '""')}"`,
        `"${(doc?.specialties?.[0] || '').replace(/"/g, '""')}"`,
        a.date,
        formatTime(a.startTime),
        formatTime(a.endTime),
        a.status,
        `"${(a.reason || '').replace(/"/g, '""')}"`,
      ].join(',')
    })

    const csv      = [headers.join(','), ...rows].join('\n')
    const blob     = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' })
    const url      = URL.createObjectURL(blob)
    const filename = `citas_${filters.from || new Date().toISOString().slice(0, 10)}.csv`

    const a    = document.createElement('a')
    a.href     = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)

    setToast({ text: `${appointments.length} registro(s)`, filename })
    setTimeout(() => setToast(null), 4000)
  }

  return (
    <Layout>
      <div className="max-w-5xl mx-auto">

        {/* Toast */}
        {toast && (
          <div className="fixed top-5 right-5 z-50 flex items-start gap-3 bg-white border border-green-200
                          rounded-2xl shadow-lg px-5 py-3 animate-fade-in">
            <span className="text-green-500 text-lg mt-0.5">✓</span>
            <div>
              <p className="text-sm font-semibold text-gray-800">Archivo exportado exitosamente:</p>
              <p className="text-xs text-gray-500">{toast.text}</p>
              <p className="text-xs text-blue-600 font-medium mt-0.5">{toast.filename}</p>
            </div>
            <button onClick={() => setToast(null)} className="text-gray-300 hover:text-gray-500 ml-2 text-lg leading-none">×</button>
          </div>
        )}

        {/* Header */}
        <div className="mb-6">
          <p className="text-sm text-gray-400 mb-1">Administración / Exportar Citas</p>
          <h1 className="text-2xl font-bold text-gray-800">Exportar Citas</h1>
          <p className="text-gray-500 text-sm mt-1">Filtra y exporta citas médicas en formato CSV</p>
        </div>

        {/* Filtros */}
        <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-5">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">Filtros de exportación</p>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">

            <div>
              <label className="block text-xs text-gray-500 mb-1">Desde</label>
              <input type="date" value={filters.from}
                     onChange={e => handleFilter('from', e.target.value)}
                     className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm
                                focus:outline-none focus:border-blue-500 transition-colors" />
            </div>

            <div>
              <label className="block text-xs text-gray-500 mb-1">Hasta</label>
              <input type="date" value={filters.to}
                     onChange={e => handleFilter('to', e.target.value)}
                     min={filters.from || undefined}
                     className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm
                                focus:outline-none focus:border-blue-500 transition-colors" />
            </div>

            <div>
              <label className="block text-xs text-gray-500 mb-1">Estado</label>
              <select value={filters.status} onChange={e => handleFilter('status', e.target.value)}
                      className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm
                                 focus:outline-none focus:border-blue-500 transition-colors">
                <option value="">Todos</option>
                {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>

            <div>
              <label className="block text-xs text-gray-500 mb-1">Especialidad</label>
              <select value={filters.specialty} onChange={e => handleFilter('specialty', e.target.value)}
                      className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm
                                 focus:outline-none focus:border-blue-500 transition-colors">
                <option value="">Todas</option>
                {specialties.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>

            <div className="col-span-2 md:col-span-4">
              <label className="block text-xs text-gray-500 mb-1">Profesional</label>
              <select value={filters.doctorId} onChange={e => handleFilter('doctorId', e.target.value)}
                      className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm
                                 focus:outline-none focus:border-blue-500 transition-colors">
                <option value="">Todos los profesionales</option>
                {doctorsBySpecialty.map(d => (
                  <option key={d.id} value={d.id}>{d.fullName || `Profesional ${d.id}`}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="mt-4 flex justify-end">
            <button onClick={handleSearch} disabled={loading}
                    className="flex items-center gap-2 bg-gray-800 text-white rounded-xl px-5 py-2.5
                               text-sm font-semibold hover:bg-gray-700 transition-colors disabled:opacity-40">
              {loading ? 'Cargando...' : 'Vista previa'}
            </button>
          </div>
        </div>

        {/* Vista previa */}
        {appointments.length > 0 && (
          <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden mb-5">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-50">
              <div className="flex items-center gap-2 text-sm font-medium text-gray-700">
                Vista previa
              </div>
              <p className="text-xs text-gray-400">
                Se exportarán <span className="font-semibold text-gray-700">{appointments.length}</span> registros
              </p>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-50">
                    {['ID', 'Cédula', 'Paciente', 'Profesional', 'Fecha', 'Hora', 'Estado'].map(h => (
                      <th key={h} className="text-left px-6 py-3 text-gray-400 font-medium text-xs uppercase tracking-wider">
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {appointments.map(apt => (
                    <tr key={apt.appointmentId} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-3 text-gray-400 text-xs font-mono">
                        CIT-{apt.appointmentId}
                      </td>
                      <td className="px-6 py-3 text-gray-500 text-xs font-mono">
                        {apt.patientId}
                      </td>
                      <td className="px-6 py-3 text-gray-800 font-medium">
                        {getPatientName(apt.patientId)}
                      </td>
                      <td className="px-6 py-3 text-gray-600">
                        {getDoctorName(apt.doctorId)}
                      </td>
                      <td className="px-6 py-3 text-gray-600">
                        {apt.date?.split('-').reverse().join('/')}
                      </td>
                      <td className="px-6 py-3 text-gray-600 font-semibold">
                        {formatTime(apt.startTime)}
                      </td>
                      <td className="px-6 py-3">
                        <StatusBadge status={apt.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="px-6 py-3 border-t border-gray-50 text-xs text-gray-400">
              Total: <span className="font-semibold text-gray-700">{appointments.length}</span> cita(s)
            </div>
          </div>
        )}

        {/* Botón exportar */}
        <div className="flex justify-start">
          <button onClick={handleExport} disabled={appointments.length === 0}
                  className="flex items-center gap-2 bg-blue-600 text-white rounded-xl px-6 py-3
                             text-sm font-semibold hover:bg-blue-700 transition-colors disabled:opacity-40
                             disabled:cursor-not-allowed">
            Exportar CSV
          </button>
          {appointments.length === 0 && (
            <p className="ml-4 self-center text-xs text-gray-400">
              Genera una vista previa antes de exportar
            </p>
          )}
        </div>

      </div>
    </Layout>
  )
}

const STATUS_STYLES = {
  AGENDADA:   'bg-green-100 text-green-700',
  ATENDIDA:   'bg-gray-100 text-gray-600',
  CANCELADA:  'bg-red-100 text-red-600',
  REAGENDADA: 'bg-blue-100 text-blue-700',
}

function StatusBadge({ status }) {
  return (
    <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold
      ${STATUS_STYLES[status] || 'bg-gray-100 text-gray-600'}`}>
      {status}
    </span>
  )
}
