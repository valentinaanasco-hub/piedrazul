import { useState, useEffect } from 'react'
import PatientLayout from '../../components/PatientLayout'
import { appointmentApi, medicalApi, patientApi } from '../../api'
import { useAuth } from '../../api/AuthContext'
import { Link } from 'react-router-dom'

const STATUS_STYLES = {
    AGENDADA:   'bg-green-100 text-green-700',
    CONFIRMADA: 'bg-green-100 text-green-700',
    PENDIENTE:  'bg-yellow-100 text-yellow-700',
    CANCELADA:  'bg-red-100 text-red-700',
    ATENDIDA:   'bg-gray-100 text-gray-600',
    REAGENDADA: 'bg-blue-100 text-blue-700',
}

const MONTHS      = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
const MONTHS_FULL = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
    'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']
const DAYS        = ['Dom','Lun','Mar','Mié','Jue','Vie','Sáb']

// --- Mini calendario para reagendamiento ---
function RescheduleModal({ appointment, doctorName, onClose, onConfirm }) {
    const today = new Date()
    const [calYear,  setCalYear]  = useState(today.getFullYear())
    const [calMonth, setCalMonth] = useState(today.getMonth())
    const [selectedDate, setSelectedDate] = useState('')
    const [selectedTime, setSelectedTime] = useState('')
    const [availability, setAvailability] = useState([])
    const [loadingSlots, setLoadingSlots] = useState(false)
    const [submitting,   setSubmitting]   = useState(false)
    const [error,        setError]        = useState('')

    useEffect(() => {
        if (!selectedDate) { setAvailability([]); setSelectedTime(''); return }
        setLoadingSlots(true)
        medicalApi.getAvailability(appointment.doctorId, selectedDate)
            .then(res => setAvailability(res.data || []))
            .catch(() => setAvailability([]))
            .finally(() => setLoadingSlots(false))
    }, [selectedDate, appointment.doctorId])

    const buildCalendar = () => {
        const firstDay    = new Date(calYear, calMonth, 1).getDay()
        const daysInMonth = new Date(calYear, calMonth + 1, 0).getDate()
        const cells = []
        for (let i = 0; i < firstDay; i++) cells.push(null)
        for (let d = 1; d <= daysInMonth; d++) cells.push(d)
        return cells
    }

    const formatDate = (day) =>
        `${calYear}-${String(calMonth + 1).padStart(2,'0')}-${String(day).padStart(2,'0')}`

    const isAvailable = (day) => {
        if (!day) return false
        const d = new Date(calYear, calMonth, day)
        const t = new Date(today.getFullYear(), today.getMonth(), today.getDate())
        return d > t // solo fechas futuras
    }

    const handleConfirm = async () => {
        if (!selectedDate || !selectedTime) { setError('Selecciona fecha y hora'); return }
        setSubmitting(true)
        setError('')
        try {
            // Calcular endTime sumando la diferencia original
            const [sh, sm] = appointment.startTime.split(':').map(Number)
            const [eh, em] = appointment.endTime.split(':').map(Number)
            const duration = (eh * 60 + em) - (sh * 60 + sm)
            const [nh, nm] = selectedTime.split(':').map(Number)
            const newEndTotal = nh * 60 + nm + duration
            const newEndTime  = `${String(Math.floor(newEndTotal / 60) % 24).padStart(2,'0')}:${String(newEndTotal % 60).padStart(2,'0')}`

            await onConfirm(appointment.appointmentId, {
                newDoctorId:  appointment.doctorId,
                doctorName,
                serviceType:  appointment.serviceType,
                newDate:      selectedDate,
                newStartTime: selectedTime,
                newEndTime,
            })
        } catch (err) {
            setError(err.response?.data?.message || 'No se pudo reagendar. Intenta de nuevo.')
            setSubmitting(false)
        }
    }

    return (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-2xl w-full max-w-md shadow-xl">
                <div className="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
                    <div>
                        <h3 className="font-bold text-gray-800">Reagendar cita</h3>
                        <p className="text-sm text-gray-400 mt-0.5">{doctorName}</p>
                    </div>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
                </div>

                <div className="px-6 py-5 space-y-4">
                    {/* Calendario */}
                    <div>
                        <div className="flex items-center justify-between mb-3">
                            <button type="button" onClick={() => {
                                if (calMonth === 0) { setCalYear(y => y-1); setCalMonth(11) }
                                else setCalMonth(m => m-1)
                            }} className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500">‹</button>
                            <p className="text-sm font-semibold text-gray-800">{MONTHS_FULL[calMonth]} {calYear}</p>
                            <button type="button" onClick={() => {
                                if (calMonth === 11) { setCalYear(y => y+1); setCalMonth(0) }
                                else setCalMonth(m => m+1)
                            }} className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500">›</button>
                        </div>
                        <div className="grid grid-cols-7 mb-1">
                            {DAYS.map(d => (
                                <div key={d} className="text-center text-xs text-gray-400 font-medium py-1">{d}</div>
                            ))}
                        </div>
                        <div className="grid grid-cols-7 gap-0.5">
                            {buildCalendar().map((day, idx) => {
                                const dateStr    = day ? formatDate(day) : ''
                                const isSelected = dateStr === selectedDate
                                const available  = isAvailable(day)
                                return (
                                    <button key={idx} type="button" disabled={!available}
                                            onClick={() => { if (available) { setSelectedDate(dateStr); setSelectedTime('') } }}
                                            className={`h-8 w-8 mx-auto rounded-full text-xs flex items-center justify-center transition-colors
                                            ${!day ? 'invisible' : ''}
                                            ${isSelected ? 'bg-blue-600 text-white font-semibold' : ''}
                                            ${available && !isSelected ? 'hover:bg-blue-100 text-gray-700 cursor-pointer' : ''}
                                            ${!available && day ? 'text-gray-300 cursor-not-allowed' : ''}`}>
                                        {day}
                                    </button>
                                )
                            })}
                        </div>
                    </div>

                    {/* Horarios */}
                    {selectedDate && (
                        <div>
                            <label className="block text-xs text-gray-500 mb-1">Hora disponible</label>
                            {loadingSlots ? (
                                <p className="text-gray-400 text-xs">Cargando horarios...</p>
                            ) : availability.length === 0 ? (
                                <p className="text-orange-500 text-xs font-medium">No hay horarios disponibles para este día</p>
                            ) : (
                                <select value={selectedTime} onChange={e => setSelectedTime(e.target.value)}
                                        className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                                        ${selectedTime ? 'border-blue-500 bg-blue-50 text-blue-700 font-semibold' : 'border-gray-200 focus:border-blue-500'}`}>
                                    <option value="">Seleccionar hora...</option>
                                    {availability.map(slot => (
                                        <option key={slot} value={slot}>{slot}</option>
                                    ))}
                                </select>
                            )}
                        </div>
                    )}

                    {error && (
                        <div className="bg-red-50 border border-red-100 rounded-xl px-4 py-3">
                            <p className="text-sm text-red-600">{error}</p>
                        </div>
                    )}
                </div>

                <div className="px-6 py-4 border-t border-gray-100 flex gap-3 justify-end">
                    <button onClick={onClose}
                            className="text-sm text-gray-500 border border-gray-200 rounded-xl px-5 py-2 hover:bg-gray-50 transition-colors">
                        Cancelar
                    </button>
                    <button onClick={handleConfirm} disabled={submitting || !selectedDate || !selectedTime}
                            className="text-sm bg-blue-600 text-white rounded-xl px-5 py-2 font-semibold
                            hover:bg-blue-700 transition-colors disabled:opacity-40">
                        {submitting ? 'Reagendando...' : 'Confirmar reagendamiento'}
                    </button>
                </div>
            </div>
        </div>
    )
}

export default function MyAppointmentsPage() {
    const { user }                              = useAuth()
    const [appointments, setAppointments]       = useState([])
    const [doctorNames,  setDoctorNames]        = useState({})
    const [loading,      setLoading]            = useState(true)
    const [cancelling,   setCancelling]         = useState(null)
    const [confirmCancel, setConfirmCancel]     = useState(null)

    const loadAppointments = () => {
        setLoading(true)
        // Resolver el ID entero del paciente vía /patients/me antes de buscar citas
        patientApi.getMe()
            .then(meRes => meRes.data?.id)
            .catch(() => user?.id)            // fallback al id del token si el servicio falla
            .then(patientId => appointmentApi.listByPatient(patientId))
            .then(res => {
                const appts = res.data || []
                setAppointments(appts)
                const uniqueDoctorIds = [...new Set(appts.map(a => a.doctorId))]
                return Promise.all(
                    uniqueDoctorIds.map(id =>
                        medicalApi.listDoctors()
                            .then(r => {
                                const doc = r.data?.find(d => d.id === id)
                                return { id, name: doc?.fullName || `Profesional ${id}` }
                            })
                            .catch(() => ({ id, name: `Profesional ${id}` }))
                    )
                )
            })
            .then(names => {
                const map = {}
                names.forEach(n => { map[n.id] = n.name })
                setDoctorNames(map)
            })
            .catch(() => setAppointments([]))
            .finally(() => setLoading(false))
    }

    useEffect(() => { loadAppointments() }, [user])

    const handleCancel = async (id) => {
        setCancelling(id)
        try {
            await appointmentApi.cancel(id)
            setConfirmCancel(null)
            loadAppointments()
        } catch {
            alert('No se pudo cancelar la cita. Intenta de nuevo.')
        } finally {
            setCancelling(null)
        }
    }

    const formatDate = (dateStr) => {
        if (!dateStr) return ''
        const [y, m, d] = dateStr.split('-')
        return `${parseInt(d)} ${MONTHS[parseInt(m) - 1]} ${y}`
    }

    const formatTime = (timeStr) => {
        if (!timeStr) return ''
        return typeof timeStr === 'string' ? timeStr.substring(0, 5) : timeStr
    }

    return (
        <PatientLayout>
            <div className="max-w-3xl mx-auto">

                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Mis Citas</h1>
                    <p className="text-gray-500 text-sm mt-1">Historial y próximas citas médicas</p>
                </div>

                <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">

                    {loading ? (
                        <div className="text-center py-12 text-gray-400 text-sm">Cargando tus citas...</div>
                    ) : appointments.length === 0 ? (
                        <div className="text-center py-12">
                            <p className="text-gray-400 text-sm">No tienes citas registradas</p>
                            <Link to="/patient/schedule"
                                  className="inline-block mt-4 bg-blue-600 text-white rounded-xl
                                    px-5 py-2 text-sm font-semibold hover:bg-blue-700 transition-colors">
                                Agendar mi primera cita
                            </Link>
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-50">
                            {appointments.map(apt => (
                                <div key={apt.appointmentId} className="px-6 py-4 hover:bg-gray-50 transition-colors">
                                    <div className="flex items-center gap-4">

                                        {/* Fecha + hora */}
                                        <div className="w-24 shrink-0">
                                            <p className="font-bold text-gray-800 text-sm">{formatTime(apt.startTime)}</p>
                                            <p className="text-gray-400 text-xs mt-0.5">{formatDate(apt.date)}</p>
                                        </div>

                                        {/* Profesional */}
                                        <div className="flex-1 min-w-0">
                                            <p className="text-sm font-medium text-gray-800 truncate">
                                                {doctorNames[apt.doctorId] || `Profesional ${apt.doctorId}`}
                                            </p>
                                            <p className="text-gray-400 text-xs mt-0.5 truncate">
                                                {apt.reason || '—'}
                                            </p>
                                        </div>

                                        {/* Estado */}
                                        <span className={`px-3 py-1 rounded-full text-xs font-semibold shrink-0
                                            ${STATUS_STYLES[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                                            {apt.status}
                                        </span>

                                        {/* Acciones — solo si está AGENDADA o REAGENDADA */}
                                        {(apt.status === 'AGENDADA' || apt.status === 'REAGENDADA') && (
                                            <div className="flex items-center gap-2 shrink-0">
                                                {/* Botón cancelar */}
                                                {confirmCancel === apt.appointmentId ? (
                                                    <div className="flex items-center gap-1">
                                                        <span className="text-xs text-gray-500">¿Cancelar?</span>
                                                        <button onClick={() => handleCancel(apt.appointmentId)}
                                                                disabled={cancelling === apt.appointmentId}
                                                                className="text-xs bg-red-500 text-white px-3 py-1 rounded-lg
                                                                hover:bg-red-600 transition-colors disabled:opacity-50">
                                                            {cancelling === apt.appointmentId ? '...' : 'Sí'}
                                                        </button>
                                                        <button onClick={() => setConfirmCancel(null)}
                                                                className="text-xs border border-gray-200 text-gray-500 px-3 py-1
                                                                rounded-lg hover:bg-gray-50 transition-colors">
                                                            No
                                                        </button>
                                                    </div>
                                                ) : (
                                                    <button onClick={() => setConfirmCancel(apt.appointmentId)}
                                                            className="text-xs text-red-500 hover:text-red-700
                                                            transition-colors font-medium">
                                                        Cancelar
                                                    </button>
                                                )}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {!loading && appointments.length > 0 && (
                        <div className="px-6 py-4 border-t border-gray-50 flex justify-between items-center">
                            <p className="text-sm text-gray-400">
                                {appointments.length} cita(s) en total
                            </p>
                            <Link to="/patient/schedule"
                                  className="text-sm text-blue-600 font-medium hover:underline">
                                + Agendar nueva cita
                            </Link>
                        </div>
                    )}
                </div>
            </div>

        </PatientLayout>
    )
}