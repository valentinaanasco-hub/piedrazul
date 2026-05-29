import { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import { appointmentApi, identityApi, medicalApi } from '../../api'
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

// ── Modal de cambio de estado ─────────────────────────────────────────────────
function StatusModal({ appointment, onClose, onConfirm, updating }) {
  const [newStatus, setNewStatus] = useState('')

  const options = [
    { value: 'ATENDIDA', label: 'Marcar como Atendida' },
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

        <div className="space-y-2 mb-6">
          {options.map(opt => (
            <label key={opt.value}
                   className={`flex items-center gap-3 border rounded-xl px-4 py-3 cursor-pointer transition-colors
                     ${newStatus === opt.value ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'}`}>
              <input type="radio" name="status" value={opt.value}
                     checked={newStatus === opt.value}
                     onChange={() => setNewStatus(opt.value)}
                     className="accent-blue-600" />
              <span className="text-sm font-medium text-gray-700">{opt.label}</span>
            </label>
          ))}
        </div>

        <div className="flex gap-3">
          <button onClick={onClose}
                  className="flex-1 border border-gray-200 text-gray-600 rounded-xl py-2.5 text-sm font-semibold hover:bg-gray-50">
            Cancelar
          </button>
          <button onClick={() => onConfirm(appointment.appointmentId, newStatus)}
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
function RescheduleModal({ appointment, doctorId, onClose, onSuccess }) {
  const [newDate,          setNewDate]          = useState('')
  const [slots,            setSlots]            = useState([])
  const [selectedTime,     setSelectedTime]     = useState('')   // "HH:MM"
  const [intervalMinutes,  setIntervalMinutes]  = useState(30)
  const [loading,          setLoading]          = useState(false)
  const [saving,           setSaving]           = useState(false)
  const [error,            setError]            = useState('')

  // Fecha mínima: mañana
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const minDate = tomorrow.toISOString().split('T')[0]

  useEffect(() => {
    if (!newDate) { setSlots([]); setSelectedTime(''); return }
    setLoading(true)
    setSelectedTime('')
    Promise.all([
      medicalApi.getAvailability(doctorId, newDate),
      medicalApi.getDoctorSchedule(doctorId),
    ]).then(([availRes, schedRes]) => {
      // Solo los slots disponibles
      setSlots((availRes.data || []).filter(s => s.available))
      const scheds = schedRes.data || []
      if (scheds.length > 0) setIntervalMinutes(scheds[0].intervalMinutes || 30)
    }).catch(() => setSlots([]))
      .finally(() => setLoading(false))
  }, [newDate, doctorId])

  const handleConfirm = async () => {
    if (!newDate || !selectedTime) { setError('Selecciona una fecha y un horario'); return }
    setSaving(true)
    setError('')
    try {
      await appointmentApi.reschedule(appointment.appointmentId, {
        newDate,
        newStartTime: selectedTime,
        newEndTime:   addMinutes(selectedTime, intervalMinutes),
      })
      onSuccess()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al reagendar. Intenta de nuevo.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl">
        <h3 className="text-lg font-bold text-gray-800 mb-1">Reagendar cita</h3>
        <p className="text-sm text-gray-500 mb-5">
          Paciente: <span className="font-medium text-gray-700">{appointment.patientName}</span>
          {' '}&mdash; Motivo: <span className="font-medium text-gray-700">{appointment.reason || '—'}</span>
        </p>

        {/* Fecha */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Nueva fecha <span className="text-red-500">*</span>
          </label>
          <input type="date" value={newDate} min={minDate}
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
                          onClick={() => setSelectedTime(s.time?.substring(0,5))}
                          className={`py-2 rounded-xl text-sm font-medium border transition-colors
                            ${selectedTime === s.time?.substring(0,5)
                              ? 'bg-blue-600 text-white border-blue-600'
                              : 'border-gray-200 text-gray-700 hover:border-blue-400 hover:bg-blue-50'
                            }`}>
                    {s.time?.substring(0,5)}
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

// ── Página principal ──────────────────────────────────────────────────────────
export default function DoctorAppointmentsPage() {
  const { user } = useAuth()
  const [selectedDate,    setSelectedDate]    = useState(new Date().toISOString().split('T')[0])
  const [appointments,    setAppointments]    = useState([])
  const [loading,         setLoading]         = useState(false)
  const [updating,        setUpdating]        = useState(false)
  const [statusModal,     setStatusModal]     = useState(null)   // appointment object
  const [rescheduleModal, setRescheduleModal] = useState(null)   // appointment object

  // El doctorId es el user.id del médico (ej: 1000000002)
  const doctorId = user?.id

  useEffect(() => {
    if (doctorId && selectedDate) loadAppointments()
  }, [doctorId, selectedDate])

  const loadAppointments = async () => {
    if (!doctorId) return
    setLoading(true)
    try {
      const res  = await appointmentApi.listByDoctorAndDate(doctorId, selectedDate)
      const appts = res.data || []

      // Enriquecer con nombre del paciente
      const enriched = await Promise.all(
        appts.map(async apt => {
          try {
            const pr = await identityApi.getUserById(apt.patientId)
            return { ...apt, patientName: pr.data?.fullName || `Paciente ${apt.patientId}` }
          } catch {
            return { ...apt, patientName: `Paciente ${apt.patientId}` }
          }
        })
      )
      setAppointments(enriched)
    } catch {
      setAppointments([])
    } finally {
      setLoading(false)
    }
  }

  const handleStatusConfirm = async (appointmentId, newStatus) => {
    setUpdating(true)
    try {
      if (newStatus === 'ATENDIDA') await appointmentApi.markAsAttended(appointmentId)
      else if (newStatus === 'CANCELADA') await appointmentApi.cancel(appointmentId)
      setStatusModal(null)
      await loadAppointments()
    } catch (err) {
      alert('Error al actualizar el estado: ' + (err.response?.data?.message || err.message))
    } finally {
      setUpdating(false)
    }
  }

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
              {appointments.map(apt => (
                <div key={apt.appointmentId} className="px-6 py-5 hover:bg-gray-50 transition-colors">
                  <div className="flex items-start justify-between gap-4">

                    {/* Info de la cita */}
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
                        <span className="text-gray-400">Motivo:</span>{' '}
                        {apt.reason || '—'}
                      </p>
                      {apt.notes && (
                        <p className="text-sm text-gray-400 mt-1 italic">{apt.notes}</p>
                      )}
                    </div>

                    {/* Acciones — solo si está AGENDADA o REAGENDADA */}
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
        </div>
      </div>

      {/* Modal cambio de estado */}
      {statusModal && (
        <StatusModal
          appointment={statusModal}
          updating={updating}
          onClose={() => setStatusModal(null)}
          onConfirm={handleStatusConfirm}
        />
      )}

      {/* Modal reagendamiento */}
      {rescheduleModal && (
        <RescheduleModal
          appointment={rescheduleModal}
          doctorId={doctorId}
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
