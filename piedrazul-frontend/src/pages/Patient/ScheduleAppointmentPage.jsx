import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import PatientLayout from '../../components/PatientLayout'
import { medicalApi, appointmentApi } from '../../api'
import { useAuth } from '../../api/AuthContext'

const STEPS = ['Especialidad', 'Profesional', 'Fecha y Hora', 'Confirmación']
const DAYS   = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb']
const MONTHS = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
    'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']

const SPECIALTY_ICONS = {
    default:            '🩺',
    'medicina general': '🩺',
    'fisioterapia':     '💪',
    'terapia neural':   '🧠',
    'quiropraxia':      '🦴',
}

function getIcon(name) {
    return SPECIALTY_ICONS[name?.toLowerCase()] || SPECIALTY_ICONS.default
}

function addMinutes(timeStr, minutes) {
    const [h, m] = timeStr.split(':').map(Number)
    const total = h * 60 + m + minutes
    const hh = String(Math.floor(total / 60) % 24).padStart(2, '0')
    const mm = String(total % 60).padStart(2, '0')
    return `${hh}:${mm}`
}

export default function ScheduleAppointmentPage() {
    const navigate = useNavigate()
    const { user } = useAuth()

    const [step, setStep]                       = useState(0)
    const [allDoctors, setAllDoctors]           = useState([])
    const [specialties, setSpecialties]         = useState([])
    const [doctors, setDoctors]                 = useState([])
    const [availability, setAvailability]       = useState([])
    const [occupiedSlots, setOccupiedSlots]     = useState([])
    const [intervalMinutes, setIntervalMinutes] = useState(30)
    const [loading, setLoading]                 = useState(false)
    const [submitting, setSubmitting]           = useState(false)
    const [success, setSuccess]                 = useState(false)

    const [selectedSpecialty, setSelectedSpecialty] = useState(null)
    const [selectedDoctor,    setSelectedDoctor]    = useState(null)
    const [selectedDate,      setSelectedDate]      = useState('')
    const [selectedTime,      setSelectedTime]      = useState('')

    const today = new Date()
    const [calYear,  setCalYear]  = useState(today.getFullYear())
    const [calMonth, setCalMonth] = useState(today.getMonth())

    // --- Cargar médicos y especialidades ---
    useEffect(() => {
        setLoading(true)
        medicalApi.listDoctors()
            .then(res => {
                const docs = res.data || []
                setAllDoctors(docs)
                const specSet = new Set()
                docs.forEach(d => d.specialties?.forEach(s => specSet.add(s)))
                setSpecialties([...specSet].map(name => ({ name })))
            })
            .catch(() => { setAllDoctors([]); setSpecialties([]) })
            .finally(() => setLoading(false))
    }, [])

    // --- Filtrar médicos por especialidad ---
    useEffect(() => {
        if (!selectedSpecialty) return
        setDoctors(allDoctors.filter(d => d.specialties?.includes(selectedSpecialty.name)))
    }, [selectedSpecialty, allDoctors])

    // --- Cargar disponibilidad + citas ocupadas ---
    useEffect(() => {
        if (!selectedDoctor || !selectedDate) { setAvailability([]); setOccupiedSlots([]); return }

        // Cargamos en paralelo: slots disponibles + citas ya agendadas
        Promise.all([
            medicalApi.getAvailability(selectedDoctor.id, selectedDate),
            appointmentApi.listByDoctorAndDate(selectedDoctor.id, selectedDate),
            medicalApi.getDoctorSchedule(selectedDoctor.id),
        ]).then(([availRes, apptRes, schedRes]) => {
            const allSlots  = availRes.data || []
            const appts     = apptRes.data  || []
            const schedules = schedRes.data || []

            // Obtener intervalo del primer horario disponible
            if (schedules.length > 0) {
                setIntervalMinutes(schedules[0].intervalMinutes || 30)
            }

            // Extraer horas ya ocupadas de las citas existentes (solo AGENDADA)
            const occupied = appts
                .filter(a => a.status === 'AGENDADA')
                .map(a => typeof a.startTime === 'string'
                    ? a.startTime.substring(0, 5)  // "10:00:00" → "10:00"
                    : a.startTime)

            setOccupiedSlots(occupied)

            // Filtrar slots que no estén ocupados
            const free = allSlots.filter(slot => !occupied.includes(slot))
            setAvailability(free)
        }).catch(() => { setAvailability([]); setOccupiedSlots([]) })
    }, [selectedDoctor, selectedDate])

    const canNext = () => {
        if (step === 0) return !!selectedSpecialty
        if (step === 1) return !!selectedDoctor
        if (step === 2) return !!selectedDate && !!selectedTime
        return true
    }

    const handleNext = () => { if (canNext()) setStep(s => s + 1) }
    const handleBack = () => {
        setStep(s => s - 1)
        if (step === 2) { setSelectedDate(''); setSelectedTime('') }
    }

    const handleConfirm = async () => {
        setSubmitting(true)
        try {
            const endTime = addMinutes(selectedTime, intervalMinutes)
            await appointmentApi.create({
                patientId: user?.id,
                doctorId:  selectedDoctor.id,
                date:      selectedDate,
                startTime: selectedTime,
                endTime,
                reason:    selectedSpecialty.name,
                notes:     '',
            })
            setSuccess(true)
            setTimeout(() => navigate('/patient/appointments'), 2500)
        } catch (err) {
            const msg = err.response?.data?.message || 'Error al confirmar la cita'
            alert(msg)
        } finally {
            setSubmitting(false)
        }
    }

    // --- Calendario ---
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
        return d >= t
    }

    const formatDateDisplay = (dateStr) => {
        if (!dateStr) return ''
        const [y, m, d] = dateStr.split('-')
        return `${parseInt(d)} de ${MONTHS[parseInt(m) - 1]} de ${y}`
    }

    if (success) {
        return (
            <PatientLayout>
                <div className="flex items-center justify-center h-full">
                    <div className="bg-white rounded-2xl border border-gray-100 p-10 text-center max-w-sm">
                        <div className="w-16 h-16 bg-green-100 rounded-full flex items-center
              justify-center mx-auto mb-4">
                            <span className="text-green-500 text-3xl">✓</span>
                        </div>
                        <h2 className="text-xl font-bold text-gray-800">¡Cita Confirmada!</h2>
                        <p className="text-gray-500 text-sm mt-2">Redirigiendo a tus citas...</p>
                    </div>
                </div>
            </PatientLayout>
        )
    }

    return (
        <PatientLayout>
            <div className="max-w-3xl mx-auto">

                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Agendar Cita</h1>
                    <p className="text-gray-500 text-sm mt-1">Sigue los pasos para agendar tu cita médica</p>
                </div>

                {/* Stepper */}
                <div className="flex items-center mb-8">
                    {STEPS.map((label, idx) => (
                        <div key={idx} className="flex items-center flex-1">
                            <div className="flex items-center gap-2">
                                <div className={`w-7 h-7 rounded-full flex items-center justify-center
                  text-xs font-bold shrink-0 transition-all
                  ${idx < step   ? 'bg-blue-600 text-white' : ''}
                  ${idx === step ? 'bg-blue-600 text-white ring-4 ring-blue-100' : ''}
                  ${idx > step   ? 'bg-gray-100 text-gray-400' : ''}`}>
                                    {idx < step ? '✓' : idx + 1}
                                </div>
                                <span className={`text-sm whitespace-nowrap
                  ${idx === step ? 'font-semibold text-gray-800' : 'text-gray-400'}`}>
                  {label}
                </span>
                            </div>
                            {idx < STEPS.length - 1 && (
                                <div className={`flex-1 h-px mx-3 ${idx < step ? 'bg-blue-600' : 'bg-gray-200'}`} />
                            )}
                        </div>
                    ))}
                </div>

                {/* Contenido */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6 min-h-64">

                    {/* Paso 1: Especialidad */}
                    {step === 0 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-4">Selecciona una especialidad</h2>
                            {loading ? (
                                <p className="text-gray-400 text-sm text-center py-8">Cargando especialidades...</p>
                            ) : specialties.length === 0 ? (
                                <p className="text-gray-400 text-sm text-center py-8">
                                    No hay especialidades disponibles
                                </p>
                            ) : (
                                <div className="grid grid-cols-3 gap-4">
                                    {specialties.map(spec => (
                                        <button key={spec.name} type="button"
                                                onClick={() => setSelectedSpecialty(spec)}
                                                className={`p-5 rounded-2xl border-2 text-left transition-all
                        ${selectedSpecialty?.name === spec.name
                                                    ? 'border-blue-600 bg-blue-50'
                                                    : 'border-gray-100 hover:border-blue-300 hover:bg-gray-50'}`}>
                                            <div className="text-2xl mb-3">{getIcon(spec.name)}</div>
                                            <p className={`font-semibold text-sm
                        ${selectedSpecialty?.name === spec.name
                                                ? 'text-blue-700' : 'text-gray-800'}`}>
                                                {spec.name}
                                            </p>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}

                    {/* Paso 2: Profesional */}
                    {step === 1 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-1">Profesionales disponibles</h2>
                            <p className="text-gray-400 text-sm mb-4">— {selectedSpecialty?.name}</p>
                            {doctors.length === 0 ? (
                                <p className="text-gray-400 text-sm text-center py-8">
                                    No hay profesionales disponibles
                                </p>
                            ) : (
                                <div className="space-y-3">
                                    {doctors.map(doc => {
                                        const displayName = doc.fullName?.trim()
                                            || [doc.firstName, doc.firstSurname].filter(Boolean).join(' ')
                                            || `Profesional ${doc.id}`
                                        const initials = displayName.split(' ').map(w => w[0])
                                            .slice(0, 2).join('').toUpperCase()
                                        return (
                                            <button key={doc.id} type="button"
                                                    onClick={() => setSelectedDoctor({ ...doc, displayName })}
                                                    className={`w-full flex items-center gap-4 p-4 rounded-2xl border-2
                          text-left transition-all
                          ${selectedDoctor?.id === doc.id
                                                        ? 'border-blue-600 bg-blue-50'
                                                        : 'border-gray-100 hover:border-blue-300'}`}>
                                                <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center
                          justify-center text-white text-sm font-bold shrink-0">
                                                    {initials}
                                                </div>
                                                <div className="flex-1">
                                                    <p className={`font-semibold text-sm
                            ${selectedDoctor?.id === doc.id
                                                        ? 'text-blue-700' : 'text-gray-800'}`}>
                                                        {displayName}
                                                    </p>
                                                    <p className="text-gray-400 text-xs mt-0.5">
                                                        {doc.specialties?.join(', ')}
                                                    </p>
                                                </div>
                                                {selectedDoctor?.id === doc.id && (
                                                    <span className="text-blue-600 text-lg shrink-0">✓</span>
                                                )}
                                            </button>
                                        )
                                    })}
                                </div>
                            )}
                        </div>
                    )}

                    {/* Paso 3: Fecha y hora */}
                    {step === 2 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-4">Selecciona fecha y hora</h2>
                            <div className="flex gap-6">

                                {/* Calendario */}
                                <div className="flex-1">
                                    <div className="flex items-center justify-between mb-3">
                                        <button type="button" onClick={() => {
                                            if (calMonth === 0) { setCalYear(y => y-1); setCalMonth(11) }
                                            else setCalMonth(m => m-1)
                                        }} className="w-7 h-7 flex items-center justify-center rounded-lg
                      hover:bg-gray-100 text-gray-500 text-lg">‹</button>
                                        <p className="text-sm font-semibold text-gray-800">
                                            {MONTHS[calMonth]} {calYear}
                                        </p>
                                        <button type="button" onClick={() => {
                                            if (calMonth === 11) { setCalYear(y => y+1); setCalMonth(0) }
                                            else setCalMonth(m => m+1)
                                        }} className="w-7 h-7 flex items-center justify-center rounded-lg
                      hover:bg-gray-100 text-gray-500 text-lg">›</button>
                                    </div>

                                    <div className="grid grid-cols-7 mb-1">
                                        {DAYS.map(d => (
                                            <div key={d} className="text-center text-xs text-gray-400 font-medium py-1">
                                                {d}
                                            </div>
                                        ))}
                                    </div>

                                    <div className="grid grid-cols-7 gap-0.5">
                                        {buildCalendar().map((day, idx) => {
                                            const dateStr    = day ? formatDate(day) : ''
                                            const isSelected = dateStr === selectedDate
                                            const available  = isAvailable(day)
                                            return (
                                                <button key={idx} type="button" disabled={!available}
                                                        onClick={() => {
                                                            if (available) { setSelectedDate(dateStr); setSelectedTime('') }
                                                        }}
                                                        className={`h-8 w-8 mx-auto rounded-full text-xs flex items-center
                            justify-center transition-colors
                            ${!day ? 'invisible' : ''}
                            ${isSelected ? 'bg-blue-600 text-white font-semibold' : ''}
                            ${available && !isSelected
                                                            ? 'hover:bg-blue-100 text-gray-700 cursor-pointer' : ''}
                            ${!available && day ? 'text-gray-300 cursor-not-allowed' : ''}
                          `}>
                                                    {day}
                                                </button>
                                            )
                                        })}
                                    </div>
                                </div>

                                {/* Horarios */}
                                <div className="w-52 shrink-0">
                                    <p className="text-sm font-semibold text-gray-800 mb-1">Horarios disponibles</p>
                                    <p className="text-xs text-gray-400 mb-3">
                                        {selectedDate ? formatDateDisplay(selectedDate) : 'Selecciona una fecha'}
                                    </p>

                                    {!selectedDate ? (
                                        <p className="text-gray-300 text-xs">← Selecciona una fecha primero</p>
                                    ) : availability.length === 0 ? (
                                        <div>
                                            <p className="text-orange-500 text-xs font-medium">
                                                No hay horarios disponibles para este día
                                            </p>
                                            <p className="text-gray-400 text-xs mt-1">
                                                Todos los horarios están ocupados o el profesional no atiende este día.
                                            </p>
                                        </div>
                                    ) : (
                                        <div>
                                            <select value={selectedTime}
                                                    onChange={e => setSelectedTime(e.target.value)}
                                                    className={`w-full border rounded-xl px-4 py-2.5 text-sm
                          focus:outline-none transition-colors
                          ${selectedTime
                                                        ? 'border-blue-500 bg-blue-50 text-blue-700 font-semibold'
                                                        : 'border-gray-200 focus:border-blue-500'}`}>
                                                <option value="">Seleccionar hora...</option>
                                                {availability.map(slot => (
                                                    <option key={slot} value={slot}>{slot}</option>
                                                ))}
                                            </select>
                                            {selectedTime && (
                                                <p className="text-blue-600 text-xs mt-2 font-medium">
                                                    ✓ Hora seleccionada: {selectedTime}
                                                </p>
                                            )}
                                            <p className="text-gray-400 text-xs mt-2">
                                                {availability.length} horario(s) libre(s)
                                            </p>
                                        </div>
                                    )}

                                    <div className="mt-6 space-y-1.5">
                                        <div className="flex items-center gap-2">
                                            <div className="w-3 h-3 rounded-full bg-blue-600" />
                                            <span className="text-xs text-gray-500">Seleccionado</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <div className="w-3 h-3 rounded-full border border-gray-300" />
                                            <span className="text-xs text-gray-500">Sin disponibilidad</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Paso 4: Confirmación */}
                    {step === 3 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-4">Confirma tu cita</h2>
                            <div className="divide-y divide-gray-50 rounded-2xl border border-gray-100">
                                {[
                                    { label: 'Especialidad', value: selectedSpecialty?.name },
                                    { label: 'Profesional',  value: selectedDoctor?.displayName
                                            || selectedDoctor?.fullName
                                            || `Profesional ${selectedDoctor?.id}` },
                                    { label: 'Fecha',        value: formatDateDisplay(selectedDate) },
                                    { label: 'Hora',         value: selectedTime },  // solo hora de inicio
                                    { label: 'Paciente',     value: user?.fullName },
                                ].map(row => (
                                    <div key={row.label} className="flex items-center justify-between px-5 py-4">
                                        <span className="text-sm text-gray-400">{row.label}</span>
                                        <span className="text-sm font-semibold text-gray-800">{row.value}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* Navegación */}
                <div className="flex items-center justify-between mt-6">
                    <button type="button" onClick={handleBack} disabled={step === 0}
                            className="text-sm text-gray-500 hover:text-gray-700 disabled:opacity-30 transition-colors">
                        ← Anterior
                    </button>
                    {step < 3 ? (
                        <button type="button" onClick={handleNext} disabled={!canNext()}
                                className="bg-blue-600 text-white rounded-xl px-6 py-2.5 text-sm font-semibold
                hover:bg-blue-700 transition-colors disabled:opacity-40">
                            Siguiente →
                        </button>
                    ) : (
                        <button type="button" onClick={handleConfirm} disabled={submitting}
                                className="bg-blue-600 text-white rounded-xl px-8 py-2.5 text-sm font-semibold
                hover:bg-blue-700 transition-colors disabled:opacity-50">
                            {submitting ? 'Confirmando...' : 'Confirmar Cita'}
                        </button>
                    )}
                </div>
            </div>
        </PatientLayout>
    )
}