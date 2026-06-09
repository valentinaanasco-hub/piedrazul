import { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import { appointmentApi, identityApi, medicalApi, patientApi } from '../../api'
import configurationService from '../../api/configurationService'
import { useAuth } from '../../api/AuthContext'

function addMinutes(timeStr, minutes) {
  const [h, m] = timeStr.split(':').map(Number)
  const total = h * 60 + m + minutes
  return `${String(Math.floor(total / 60) % 24).padStart(2,'0')}:${String(total % 60).padStart(2,'0')}`
}

const STATUS_COLORS = {
  AGENDADA:   'bg-blue-100 text-blue-700',
  REAGENDADA: 'bg-purple-100 text-purple-700',
  ATENDIDA:   'bg-green-100 text-green-700',
  CANCELADA:  'bg-red-100 text-red-700',
}

const STATUS_LABELS = {
  AGENDADA:   'Agendada',
  REAGENDADA: 'Reagendada',
  ATENDIDA:   'Atendida',
  CANCELADA:  'Cancelada',
}

const MONTHS = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']

// ── Mapeo entre el enum del backend y el nombre visible ──────────────────────
const SERVICE_TYPES = ['Consulta General', 'Fisioterapia', 'Quiropraxia', 'Terapia Neural']

const SERVICE_TYPE_TO_ENUM = {
  'Consulta General': 'CONSULTA_GENERAL',
  'Fisioterapia':     'FISIOTERAPIA',
  'Quiropraxia':      'QUIROPRAXIA',
  'Terapia Neural':   'TERAPIA_NEURAL',
}

const ENUM_TO_SERVICE_TYPE = {
  CONSULTA_GENERAL: 'Consulta General',
  FISIOTERAPIA:     'Fisioterapia',
  QUIROPRAXIA:      'Quiropraxia',
  TERAPIA_NEURAL:   'Terapia Neural',
}

// Devuelve los doctores que pueden atender el servicio indicado
function filterDoctorsByService(service, allDocs) {
  if (!service) return []
  if (service === 'Quiropraxia')      return allDocs.filter(d => d.specialties?.includes('Quiropraxia'))
  if (service === 'Terapia Neural')   return allDocs.filter(d => d.specialties?.includes('Terapia Neural'))
  if (service === 'Consulta General') return allDocs
  // Fisioterapia → doctores sin especialidad
  return allDocs.filter(d => !d.specialties?.length)
}

// Servicios especializados que el médico puede autorizar (excluye Consulta General)
const AUTHORIZED_SERVICE_OPTIONS = [
  { value: 'FISIOTERAPIA',   label: 'Fisioterapia' },
  { value: 'QUIROPRAXIA',    label: 'Quiropraxia' },
  { value: 'TERAPIA_NEURAL', label: 'Terapia Neural' },
]

// ── Modal de cambio de estado ─────────────────────────────────────────────────
function StatusModal({ appointment, onClose, onConfirm, updating, error }) {
  const [newStatus,          setNewStatus]          = useState('')
  const [authorizedService,  setAuthorizedService]  = useState('')

  const today   = new Date().toISOString().split('T')[0]
  const isToday = appointment.date === today

  const options = [
    ...(isToday ? [{ value: 'ATENDIDA', label: 'Marcar como Atendida' }] : []),
    { value: 'CANCELADA', label: 'Cancelar cita' },
  ]

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl p-6 w-full max-w-sm shadow-xl">
        <h3 className="text-lg font-bold text-gray-800 mb-1">Cambiar estado</h3>
        <p className="text-sm text-gray-500 mb-5">
          Cita de <span className="font-medium text-gray-700">{appointment.patientName}</span>
          {' '}&mdash; {appointment.startTime?.substring(0,5)}
        </p>

        <div className="space-y-2 mb-4">
          {options.map(opt => (
            <label key={opt.value}
                   className={`flex items-center gap-3 border rounded-xl px-4 py-3 cursor-pointer transition-colors
                     ${newStatus === opt.value ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'}`}>
              <input type="radio" name="status" value={opt.value}
                     checked={newStatus === opt.value}
                     onChange={() => { setNewStatus(opt.value); setAuthorizedService('') }}
                     className="accent-blue-600" />
              <span className="text-sm font-medium text-gray-700">{opt.label}</span>
            </label>
          ))}
        </div>

        {/* Autorización de servicio: solo visible al marcar como atendida */}
        {newStatus === 'ATENDIDA' && (
          <div className="mb-5 bg-blue-50 border border-blue-100 rounded-xl p-4">
            <p className="text-xs font-semibold text-blue-700 mb-2">
              Autorización para próxima cita <span className="font-normal text-blue-500">(opcional)</span>
            </p>
            <p className="text-xs text-blue-600 mb-3">
              Puedes autorizar al paciente para acceder a un servicio especializado en su siguiente cita.
              Sin autorización, solo podrá agendar Consulta General.
            </p>
            <select
              value={authorizedService}
              onChange={e => setAuthorizedService(e.target.value)}
              className="w-full border border-blue-200 bg-white rounded-xl px-3 py-2 text-sm
                         focus:outline-none focus:border-blue-500 transition-colors">
              <option value="">Sin autorización adicional</option>
              {AUTHORIZED_SERVICE_OPTIONS.map(opt => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
            {authorizedService && (
              <p className="text-xs text-blue-600 mt-2 font-medium">
                ✓ Paciente autorizado para:{' '}
                {AUTHORIZED_SERVICE_OPTIONS.find(o => o.value === authorizedService)?.label}
              </p>
            )}
          </div>
        )}

        {/* Error inline — reemplaza el alert() del navegador */}
        {error && (
          <div className="mb-4 flex items-start gap-2 bg-red-50 border border-red-200 rounded-xl px-4 py-3">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                 fill="none" stroke="#ef4444" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                 className="shrink-0 mt-0.5">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <div className="flex gap-3">
          <button onClick={onClose}
                  className="flex-1 border border-gray-200 text-gray-600 rounded-xl py-2.5 text-sm font-semibold hover:bg-gray-50">
            Cancelar
          </button>
          <button onClick={() => onConfirm(appointment.appointmentId, newStatus, authorizedService || null)}
                  disabled={!newStatus || updating}
                  className="flex-1 bg-blue-600 text-white rounded-xl py-2.5 text-sm font-semibold
                    hover:bg-blue-700 disabled:opacity-40 transition-colors">
            {updating ? 'Guardando...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── Modal de reagendamiento ───────────────────────────────────────────────────
function RescheduleModal({ appointment, onClose, onSuccess, windowWeeks }) {
  const [allDoctors,      setAllDoctors]      = useState([])
  const [doctors,         setDoctors]         = useState([])
  // Preseleccionar el servicio actual de la cita
  const [specialty,       setSpecialty]       = useState(ENUM_TO_SERVICE_TYPE[appointment.serviceType] || '')
  const [doctorId,        setDoctorId]        = useState(appointment.doctorId)
  const [newDate,         setNewDate]         = useState('')
  const [slots,           setSlots]           = useState([])
  const [selectedTime,    setSelectedTime]    = useState('')
  const [intervalMinutes, setIntervalMinutes] = useState(30)
  const [loading,         setLoading]         = useState(false)
  const [saving,          setSaving]          = useState(false)
  const [error,           setError]           = useState('')
  const [success,         setSuccess]         = useState(false)

  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const minDate = tomorrow.toISOString().split('T')[0]

  const maxDateObj = new Date()
  maxDateObj.setDate(maxDateObj.getDate() + (windowWeeks ?? 4) * 7)
  const maxDate = maxDateObj.toISOString().split('T')[0]

  // Cargar todos los profesionales
  useEffect(() => {
    medicalApi.listDoctors().then(res => {
      setAllDoctors(res.data || [])
    }).catch(() => {})
  }, [])

  // Filtrar profesionales cuando cambia el servicio o la lista
  useEffect(() => {
    if (!specialty || !allDoctors.length) { setDoctors([]); return }

    if (specialty === 'Quiropraxia') {
      const matching = filterDoctorsByService('Quiropraxia', allDoctors)
      let cancelled = false
      Promise.all(matching.map(async d => {
        try {
          const r = await medicalApi.getDoctorSchedule(d.id)
          return (r.data && r.data.length > 0) ? d : null
        } catch { return null }
      })).then(list => {
        if (cancelled) return
        const avail = list.filter(Boolean)
        setDoctors(avail)
        if (!avail.some(d => d.id === Number(doctorId))) setDoctorId(avail[0]?.id || '')
      })
      return () => { cancelled = true }
    }

    const matching = filterDoctorsByService(specialty, allDoctors)
    setDoctors(matching)
    // Mantener el doctor actual si sigue siendo válido para el servicio
    if (!matching.some(d => d.id === Number(doctorId))) setDoctorId(matching[0]?.id || '')
  }, [specialty, allDoctors])

  // Cargar horarios disponibles (filtrados por citas ya agendadas en esa fecha)
  useEffect(() => {
    if (!doctorId || !newDate) { setSlots([]); setSelectedTime(''); return }
    setLoading(true)
    setSelectedTime('')
    Promise.all([
      medicalApi.getAvailability(doctorId, newDate),
      medicalApi.getDoctorSchedule(doctorId),
      appointmentApi.listByDoctorAndDate(doctorId, newDate).catch(() => ({ data: [] })),
    ]).then(([availRes, schedRes, aptsRes]) => {
      const allSlots    = availRes.data || []
      // Excluir horarios ya ocupados (ignorar la propia cita que se está reagendando)
      const bookedTimes = new Set(
        (aptsRes.data || [])
          .filter(a => a.status !== 'CANCELADA' && a.appointmentId !== appointment.appointmentId)
          .map(a => (a.startTime || '').substring(0, 5))
      )
      setSlots(allSlots.filter(s => !bookedTimes.has(s.substring(0, 5))))
      const scheds = schedRes.data || []
      if (scheds.length > 0) setIntervalMinutes(scheds[0].intervalMinutes || 30)
    }).catch(() => setSlots([]))
      .finally(() => setLoading(false))
  }, [doctorId, newDate])

  const handleConfirm = async () => {
    if (!doctorId)                 { setError('Selecciona un profesional'); return }
    if (!newDate || !selectedTime) { setError('Selecciona una fecha y un horario'); return }

    const selectedDoc = allDoctors.find(d => d.id === parseInt(doctorId))

    setSaving(true)
    setError('')
    try {
      await appointmentApi.reschedule(appointment.appointmentId, {
        newDoctorId:  parseInt(doctorId),
        doctorName:   selectedDoc?.fullName || appointment.doctorName,
        serviceType:  SERVICE_TYPE_TO_ENUM[specialty],
        newDate,
        newStartTime: selectedTime,
        newEndTime:   addMinutes(selectedTime, intervalMinutes),
      })
      setSuccess(true)
      setTimeout(() => onSuccess(), 1600)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al reagendar. Intenta de nuevo.')
    } finally {
      setSaving(false)
    }
  }

  if (success) {
    return (
      <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-2xl p-8 w-full max-w-sm shadow-xl text-center">
          <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#22c55e" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
          </div>
          <h3 className="text-lg font-bold text-gray-800">¡Cita reagendada!</h3>
          <p className="text-gray-500 text-sm mt-1">La cita se actualizó correctamente.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl">
        <h3 className="text-lg font-bold text-gray-800 mb-1">Reagendar cita</h3>
        <p className="text-sm text-gray-500 mb-5">
          Paciente: <span className="font-medium text-gray-700">{appointment.patientName}</span>
          {' '}<span className="text-gray-400 text-xs font-mono">({appointment.patientId})</span>
          {' '}&mdash; Motivo: <span className="font-medium text-gray-700">{appointment.reason || '—'}</span>
        </p>

        {/* Servicio */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Servicio <span className="text-red-500">*</span>
          </label>
          <select value={specialty}
                  onChange={e => { setSpecialty(e.target.value); setNewDate(''); setSelectedTime(''); setError('') }}
                  className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                    focus:outline-none focus:border-blue-500 transition-colors">
            <option value="">Seleccionar servicio...</option>
            {SERVICE_TYPES.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>

        {/* Profesional */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Profesional <span className="text-red-500">*</span>
          </label>
          <select value={doctorId} disabled={!specialty}
                  onChange={e => { setDoctorId(e.target.value); setNewDate(''); setSelectedTime(''); setError('') }}
                  className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                    focus:outline-none focus:border-blue-500 transition-colors disabled:bg-gray-50 disabled:text-gray-400">
            <option value="">{specialty ? 'Seleccionar profesional...' : 'Primero elige un servicio'}</option>
            {doctors.map(d => (
                <option key={d.id} value={d.id}>{d.fullName || `Profesional ${d.id}`}</option>
            ))}
          </select>
          {specialty === 'Quiropraxia' && doctors.length === 0 && (
              <p className="text-orange-600 text-xs mt-1">
                ⚠️ No hay especialistas en Quiropraxia disponibles (activos y con horario).
              </p>
          )}
        </div>

        {/* Fecha */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Nueva fecha <span className="text-red-500">*</span>
          </label>
          <input type="date" value={newDate} min={minDate} max={maxDate}
                 onChange={e => { setNewDate(e.target.value); setError('') }}
                 className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                   focus:outline-none focus:border-blue-500 transition-colors" />
        </div>

        {/* Horarios disponibles */}
        {newDate && (
          <div className="mb-5">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Horario disponible <span className="text-red-500">*</span>
            </label>
            {loading ? (
              <p className="text-sm text-gray-400 py-4 text-center">Cargando horarios...</p>
            ) : slots.length === 0 ? (
              <p className="text-sm text-gray-400 py-4 text-center">Sin horarios disponibles para esta fecha</p>
            ) : (
              <div className="grid grid-cols-4 gap-2 max-h-48 overflow-y-auto pr-1">
                {slots.map((s, i) => (
                  <button key={i}
                          onClick={() => setSelectedTime(s.substring(0,5))}
                          className={`py-2 rounded-xl text-sm font-medium border transition-colors
                            ${selectedTime === s.substring(0,5)
                              ? 'bg-blue-600 text-white border-blue-600'
                              : 'border-gray-200 text-gray-700 hover:border-blue-400 hover:bg-blue-50'
                            }`}>
                    {s.substring(0,5)}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {error && <p className="text-red-500 text-sm mb-4 bg-red-50 rounded-xl px-3 py-2">{error}</p>}

        <div className="flex gap-3">
          <button onClick={onClose}
                  className="flex-1 border border-gray-200 text-gray-600 rounded-xl py-2.5 text-sm font-semibold hover:bg-gray-50">
            Cancelar
          </button>
          <button onClick={handleConfirm} disabled={saving || !selectedTime}
                  className="flex-1 bg-blue-600 text-white rounded-xl py-2.5 text-sm font-semibold
                    hover:bg-blue-700 disabled:opacity-40 transition-colors">
            {saving ? 'Reagendando...' : 'Confirmar'}
          </button>
        </div>
      </div>
    </div>
  )
}

const PAGE_SIZE = 5

// ── Página principal ──────────────────────────────────────────────────────────
export default function DoctorAppointmentsPage() {
  const { user } = useAuth()
  const [selectedDate,    setSelectedDate]    = useState(new Date().toISOString().split('T')[0])
  const [appointments,    setAppointments]    = useState([])
  const [loading,         setLoading]         = useState(false)
  const [updating,        setUpdating]        = useState(false)
  const [statusModal,     setStatusModal]     = useState(null)
  const [statusError,     setStatusError]     = useState('')
  const [rescheduleModal, setRescheduleModal] = useState(null)
  const [currentPage,     setCurrentPage]     = useState(1)

  const doctorId = user?.id

  const [windowWeeks, setWindowWeeks] = useState(4)

  // Cargar ventana de reagendamiento desde configuración del sistema
  useEffect(() => {
    configurationService.getGlobalConfiguration()
      .then(data => { if (data?.weeks) setWindowWeeks(data.weeks) })
      .catch(() => {}) // fallback al valor por defecto (4 semanas)
  }, [])

  useEffect(() => {
    if (doctorId && selectedDate) loadAppointments()
  }, [doctorId, selectedDate])

  const loadAppointments = async () => {
    if (!doctorId) return
    setLoading(true)
    try {
      const res   = await appointmentApi.listByDoctorAndDate(doctorId, selectedDate)
      const appts = res.data || []

      // Enriquecer con nombre del paciente — intenta identity service primero,
      // luego patient service como fallback (ambos tienen fullName)
      const enriched = await Promise.all(
        appts.map(async apt => {
          try {
            const [idRes, patRes] = await Promise.all([
              identityApi.getUserById(apt.patientId).catch(() => null),
              patientApi.getById(apt.patientId).catch(() => null),
            ])
            const patientName = idRes?.data?.fullName
                             || patRes?.data?.fullName
                             || `Paciente ${apt.patientId}`
            return { ...apt, patientName }
          } catch {
            return { ...apt, patientName: `Paciente ${apt.patientId}` }
          }
        })
      )
      setAppointments(enriched)
      setCurrentPage(1)
    } catch {
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const handleStatusConfirm = async (appointmentId, newStatus, authorizedService) => {
    setStatusError('')
    setUpdating(true)
    try {
      if (newStatus === 'ATENDIDA') {
        const body = authorizedService
          ? { authorizedServiceType: authorizedService }
          : null
        await appointmentApi.markAsAttended(appointmentId, body)
      } else if (newStatus === 'CANCELADA') {
        await appointmentApi.cancel(appointmentId)
      }
      setStatusModal(null)
      await loadAppointments()
    } catch (err) {
      setStatusError(err.response?.data?.message || err.message || 'Error al actualizar el estado')
    } finally {
      setUpdating(false)
    }
  }

  const totalPages = Math.ceil(appointments.length / PAGE_SIZE)
  const paginated  = appointments.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)

  const formatDate = (dateStr) => {
    const [y, m, d] = dateStr.split('-')
    return `${parseInt(d)} de ${MONTHS[parseInt(m) - 1]} de ${y}`
  }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">

        <div className="mb-6">
          <p className="text-sm text-gray-400 mb-1">Mis Citas</p>
          <h1 className="text-2xl font-bold text-gray-800">Gestión de Citas</h1>
          <p className="text-gray-500 text-sm mt-1">
            Visualiza y actualiza el estado de tus citas médicas
          </p>
        </div>

        {/* Selector de fecha */}
        <div className="bg-white rounded-2xl border border-gray-100 p-5 mb-6 flex items-end gap-4">
          <div>
            <label className="block text-sm text-gray-500 mb-1">Seleccionar fecha</label>
            <input type="date" value={selectedDate}
                   onChange={e => setSelectedDate(e.target.value)}
                   className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                     focus:outline-none focus:border-blue-500 transition-colors" />
          </div>
          <p className="text-sm text-gray-400 pb-1">
            {appointments.length > 0
              ? `${appointments.length} cita(s) encontrada(s)`
              : ''}
          </p>
        </div>

        {/* Lista de citas */}
        <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-50">
            <h2 className="font-semibold text-gray-800">
              Citas del {formatDate(selectedDate)}
            </h2>
          </div>

          {loading ? (
            <p className="text-gray-400 text-sm text-center py-12">Cargando citas...</p>
          ) : appointments.length === 0 ? (
            <p className="text-gray-400 text-sm text-center py-12">
              No hay citas programadas para esta fecha
            </p>
          ) : (
            <div className="divide-y divide-gray-50">
              {paginated.map(apt => (
                <div key={apt.appointmentId} className="px-6 py-5 hover:bg-gray-50 transition-colors">
                  <div className="flex items-start justify-between gap-4">

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3 mb-2 flex-wrap">
                        <span className="text-base font-semibold text-gray-800">
                          {apt.startTime?.substring(0,5)} – {apt.endTime?.substring(0,5)}
                        </span>
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold
                          ${STATUS_COLORS[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                          {STATUS_LABELS[apt.status] || apt.status}
                        </span>
                      </div>

                      <p className="text-sm text-gray-700 mb-1">
                        <span className="text-gray-400">Paciente:</span>{' '}
                        <span className="font-medium">{apt.patientName}</span>
                      </p>
                      <p className="text-sm text-gray-500">
                        <span className="text-gray-400">Tipo:</span>{' '}
                        {ENUM_TO_SERVICE_TYPE[apt.serviceType] || apt.serviceType || '—'}
                      </p>
                      <p className="text-sm text-gray-500">
                        <span className="text-gray-400">Motivo:</span>{' '}
                        {apt.reason || '—'}
                      </p>
                      {apt.notes && (
                        <p className="text-sm text-gray-400 mt-1 italic">{apt.notes}</p>
                      )}
                    </div>

                    {(apt.status === 'AGENDADA' || apt.status === 'REAGENDADA') && (
                      <div className="flex flex-col gap-2 shrink-0">
                        <button
                          onClick={() => setStatusModal(apt)}
                          className="border border-gray-200 text-gray-700 rounded-xl px-4 py-2
                            text-sm font-medium hover:bg-gray-100 transition-colors">
                          Cambiar estado
                        </button>
                        <button
                          onClick={() => setRescheduleModal(apt)}
                          className="border border-blue-200 text-blue-600 rounded-xl px-4 py-2
                            text-sm font-medium hover:bg-blue-50 transition-colors">
                          Reagendar
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {!loading && totalPages > 1 && (
            <div className="px-6 py-4 border-t border-gray-50 flex items-center justify-between">
              <p className="text-sm text-gray-400">
                Mostrando{' '}
                <span className="font-semibold text-gray-700">
                  {(currentPage - 1) * PAGE_SIZE + 1}–{Math.min(currentPage * PAGE_SIZE, appointments.length)}
                </span>{' '}
                de <span className="font-semibold text-gray-700">{appointments.length}</span> cita(s)
              </p>
              <div className="flex items-center gap-1">
                <button onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                        disabled={currentPage === 1}
                        className="w-8 h-8 flex items-center justify-center rounded-lg border
                          border-gray-200 text-gray-500 hover:bg-gray-50 disabled:opacity-30 text-sm">
                  ‹
                </button>
                {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                  <button key={page} onClick={() => setCurrentPage(page)}
                          className={`w-8 h-8 flex items-center justify-center rounded-lg text-xs font-medium
                            ${currentPage === page
                              ? 'bg-blue-600 text-white'
                              : 'border border-gray-200 text-gray-500 hover:bg-gray-50'}`}>
                    {page}
                  </button>
                ))}
                <button onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                        disabled={currentPage === totalPages}
                        className="w-8 h-8 flex items-center justify-center rounded-lg border
                          border-gray-200 text-gray-500 hover:bg-gray-50 disabled:opacity-30 text-sm">
                  ›
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {statusModal && (
        <StatusModal
          appointment={statusModal}
          updating={updating}
          error={statusError}
          onClose={() => { setStatusModal(null); setStatusError('') }}
          onConfirm={handleStatusConfirm}
        />
      )}

      {rescheduleModal && (
        <RescheduleModal
          appointment={rescheduleModal}
          windowWeeks={windowWeeks}
          onClose={() => setRescheduleModal(null)}
          onSuccess={() => {
            setRescheduleModal(null)
            loadAppointments()
          }}
        />
      )}
    </Layout>
  )
}
