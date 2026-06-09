import { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi, patientApi, identityApi } from '../../api'
import { useAuth } from '../../api/AuthContext'

const STATUSES = ['AGENDADA', 'ATENDIDA', 'CANCELADA', 'REAGENDADA']

const SERVICE_TYPE_LABELS = {
  CONSULTA_GENERAL: 'Consulta General',
  FISIOTERAPIA:     'Fisioterapia',
  QUIROPRAXIA:      'Quiropraxia',
  TERAPIA_NEURAL:   'Terapia Neural',
}

export default function ExportAppointmentsPage() {
  const { user, hasRole } = useAuth()
  const isDoctor = hasRole('DOCTOR')

  const [doctors,       setDoctors]       = useState([])
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

  const [currentPage, setCurrentPage] = useState(1)
  const PAGE_SIZE = 10

  // Cargar médicos al montar
  useEffect(() => {
    medicalApi.listDoctors().then(res => {
      setDoctors(res.data || [])
    }).catch(() => {})
  }, [])

  const doctorsBySpecialty = doctors

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
      // Doctor: solo sus citas via endpoint propio; Admin/Agendador: todas
      const res = isDoctor
          ? await appointmentApi.listByDoctor(user?.id)
          : await appointmentApi.listAll()
      let apts  = res.data || []

      if (filters.from)     apts = apts.filter(a => a.date >= filters.from)
      if (filters.to)       apts = apts.filter(a => a.date <= filters.to)
      if (filters.status)   apts = apts.filter(a => a.status === filters.status)
      if (!isDoctor && filters.doctorId) apts = apts.filter(a => a.doctorId === parseInt(filters.doctorId))
      if (filters.specialty) apts = apts.filter(a => a.serviceType === filters.specialty)

      // Ordenar por fecha y hora
      apts.sort((a, b) => a.date.localeCompare(b.date) || a.startTime.localeCompare(b.startTime))
      setAppointments(apts)
      setCurrentPage(1)

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
        `"${(SERVICE_TYPE_LABELS[a.serviceType] || a.serviceType || '').replace(/"/g, '""')}"`,
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
              <label className="block text-xs text-gray-500 mb-1">Tipo de servicio</label>
              <select value={filters.specialty} onChange={e => handleFilter('specialty', e.target.value)}
                      className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm
                                 focus:outline-none focus:border-blue-500 transition-colors">
                <option value="">Todos</option>
                {Object.entries(SERVICE_TYPE_LABELS).map(([val, label]) => (
                  <option key={val} value={val}>{label}</option>
                ))}
              </select>
            </div>

            {!isDoctor && (
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
            )}
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
        {appointments.length > 0 && (() => {
          const totalPages  = Math.ceil(appointments.length / PAGE_SIZE)
          const paginated   = appointments.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)

          return (
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
                    {paginated.map(apt => (
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

              {/* Footer: total + paginación */}
              <div className="px-6 py-3 border-t border-gray-50 flex items-center justify-between">
                <p className="text-xs text-gray-400">
                  Total: <span className="font-semibold text-gray-700">{appointments.length}</span> cita(s)
                  &nbsp;·&nbsp;
                  Página <span className="font-semibold text-gray-700">{currentPage}</span> de <span className="font-semibold text-gray-700">{totalPages}</span>
                </p>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => setCurrentPage(1)}
                    disabled={currentPage === 1}
                    className="px-2 py-1 rounded-lg text-xs text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed"
                  >«</button>
                  <button
                    onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                    disabled={currentPage === 1}
                    className="px-2 py-1 rounded-lg text-xs text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed"
                  >‹</button>

                  {Array.from({ length: totalPages }, (_, i) => i + 1)
                    .filter(p => p === 1 || p === totalPages || Math.abs(p - currentPage) <= 1)
                    .reduce((acc, p, idx, arr) => {
                      if (idx > 0 && p - arr[idx - 1] > 1) acc.push('...')
                      acc.push(p)
                      return acc
                    }, [])
                    .map((p, idx) =>
                      p === '...'
                        ? <span key={`ellipsis-${idx}`} className="px-2 py-1 text-xs text-gray-400">…</span>
                        : <button
                            key={p}
                            onClick={() => setCurrentPage(p)}
                            className={`px-2.5 py-1 rounded-lg text-xs font-medium transition-colors
                              ${currentPage === p
                                ? 'bg-blue-600 text-white'
                                : 'text-gray-500 hover:bg-gray-100'}`}
                          >{p}</button>
                    )}

                  <button
                    onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                    disabled={currentPage === totalPages}
                    className="px-2 py-1 rounded-lg text-xs text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed"
                  >›</button>
                  <button
                    onClick={() => setCurrentPage(totalPages)}
                    disabled={currentPage === totalPages}
                    className="px-2 py-1 rounded-lg text-xs text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed"
                  >»</button>
                </div>
              </div>
            </div>
          )
        })()}

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
