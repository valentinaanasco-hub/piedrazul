import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import PatientLayout from '../../components/PatientLayout'
import { medicalApi, appointmentApi } from '../../api'
import { useAuth } from '../../api/AuthContext'

const STEPS = ['Especialidad', 'Profesional', 'Fecha y Hora', 'Confirmación']

const DAYS  = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb']
const MONTHS = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
    'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']

// Iconos por especialidad
const SPECIALTY_ICONS = {
    default:          '🩺',
    'medicina general': '🩺',
    'odontología':    '🦷',
    'psicología':     '🧠',
    'fisioterapia':   '💪',
    'nutrición':      '🥗',
    'dermatología':   '✨',
    'pediatría':      '👶',
    'cardiología':    '❤️',
    'oftalmología':   '👁️',
}

function getIcon(name) {
    return SPECIALTY_ICONS[name?.toLowerCase()] || SPECIALTY_ICONS.default
}

export default function ScheduleAppointmentPage() {
    const navigate  = useNavigate()
    const { user }  = useAuth()

    const [step, setStep]                 = useState(0)
    const [specialties, setSpecialties]   = useState([])
    const [doctors, setDoctors]           = useState([])
    const [availability, setAvailability] = useState([])
    const [loading, setLoading]           = useState(false)
    const [submitting, setSubmitting]     = useState(false)
    const [success, setSuccess]           = useState(false)

    // Selecciones del paciente
    const [selectedSpecialty, setSelectedSpecialty] = useState(null)
    const [selectedDoctor,    setSelectedDoctor]    = useState(null)
    const [selectedDate,      setSelectedDate]      = useState('')
    const [selectedTime,      setSelectedTime]      = useState('')

    // Calendario
    const today = new Date()
    const [calYear,  setCalYear]  = useState(today.getFullYear())
    const [calMonth, setCalMonth] = useState(today.getMonth())

    // --- Cargar especialidades ---
    useEffect(() => {
        setLoading(true)
        // Obtenemos especialidades únicas desde los médicos
        medicalApi.listDoctors()
            .then(res => {
                const allSpecialties = []
                res.data.forEach(doctor => {
                    doctor.specialties?.forEach(spec => {
                        if (!allSpecialties.find(s => s.name === spec)) {
                            allSpecialties.push({ name: spec })
                        }
                    })
                })
                setSpecialties(allSpecialties)
            })
            .catch(() => setSpecialties([]))
            .finally(() => setLoading(false))
    }, [])

    // --- Cargar médicos por especialidad ---
    useEffect(() => {
        if (!selectedSpecialty) return
        setLoading(true)
        medicalApi.listDoctors()
            .then(res => {
                const filtered = res.data.filter(d =>
                    d.specialties?.includes(selectedSpecialty.name)
                )
                setDoctors(filtered)
            })
            .catch(() => setDoctors([]))
            .finally(() => setLoading(false))
    }, [selectedSpecialty])

    // --- Cargar disponibilidad ---
    useEffect(() => {
        if (!selectedDoctor || !selectedDate) return
        medicalApi.getAvailability(selectedDoctor.id, selectedDate)
            .then(res => setAvailability(res.data))
            .catch(() => setAvailability([]))
    }, [selectedDoctor, selectedDate])

    // --- Navegación entre pasos ---
    const canNext = () => {
        if (step === 0) return !!selectedSpecialty
        if (step === 1) return !!selectedDoctor
        if (step === 2) return !!selectedDate && !!selectedTime
        return true
    }

    const handleNext = () => { if (canNext()) setStep(s => s + 1) }
    const handleBack = () => { setStep(s => s - 1) }

    // --- Confirmar cita ---
    const handleConfirm = async () => {
        setSubmitting(true)
        try {
            await appointmentApi.create({
                documentId: user?.id,
                doctorId:   selectedDoctor.id,
                date:       selectedDate,
                startTime:  selectedTime,
                reason:     selectedSpecialty.name,
            })
            setSuccess(true)
            setTimeout(() => navigate('/patient/appointments'), 2500)
        } catch {
            alert('Error al confirmar la cita. Intenta de nuevo.')
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
        return d >= today
    }

    if (success) {
        return (
            <PatientLayout>
                <div className="flex items-center justify-center h-full">
                    <div className="bg-white rounded-2xl border border-gray-100 p-10 text-center max-w-sm">
                        <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
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

                {/* --- Título --- */}
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Agendar Cita</h1>
                    <p className="text-gray-500 text-sm mt-1">Sigue los pasos para agendar tu cita médica</p>
                </div>

                {/* --- Stepper --- */}
                <div className="flex items-center gap-2 mb-8">
                    {STEPS.map((label, idx) => (
                        <div key={idx} className="flex items-center gap-2 flex-1">
                            <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0
                ${idx < step  ? 'bg-blue-600 text-white' : ''}
                ${idx === step ? 'bg-blue-600 text-white ring-4 ring-blue-100' : ''}
                ${idx > step  ? 'bg-gray-100 text-gray-400' : ''}`}>
                                {idx < step ? '✓' : idx + 1}
                            </div>
                            <span className={`text-sm ${idx === step ? 'font-semibold text-gray-800' : 'text-gray-400'}`}>
                {label}
              </span>
                            {idx < STEPS.length - 1 && (
                                <div className={`flex-1 h-px ${idx < step ? 'bg-blue-600' : 'bg-gray-200'}`} />
                            )}
                        </div>
                    ))}
                </div>

                {/* --- Contenido por paso --- */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6 min-h-64">

                    {/* Paso 1: Especialidad */}
                    {step === 0 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-4">Selecciona una especialidad</h2>
                            {loading ? (
                                <p className="text-gray-400 text-sm text-center py-8">Cargando especialidades...</p>
                            ) : specialties.length === 0 ? (
                                <p className="text-gray-400 text-sm text-center py-8">
                                    No hay especialidades disponibles por el momento
                                </p>
                            ) : (
                                <div className="grid grid-cols-3 gap-4">
                                    {specialties.map(spec => (
                                        <button key={spec.name} type="button"
                                                onClick={() => setSelectedSpecialty(spec)}
                                                className={`p-5 rounded-2xl border-2 text-left transition-all
                        ${selectedSpecialty?.name === spec.name
                                                    ? 'border-blue-600 bg-blue-50'
                                                    : 'border-gray-100 hover:border-blue-300 hover:bg-gray-50'
                                                }`}>
                                            <div className="text-2xl mb-3">{getIcon(spec.name)}</div>
                                            <p className={`font-semibold text-sm
                        ${selectedSpecialty?.name === spec.name ? 'text-blue-700' : 'text-gray-800'}`}>
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
                            {loading ? (
                                <p className="text-gray-400 text-sm text-center py-8">Cargando profesionales...</p>
                            ) : doctors.length === 0 ? (
                                <p className="text-gray-400 text-sm text-center py-8">
                                    No hay profesionales disponibles para esta especialidad
                                </p>
                            ) : (
                                <div className="space-y-3">
                                    {doctors.map(doc => {
                                        const initials = doc.fullName
                                            ? doc.fullName.split(' ').map(w => w[0]).slice(0,2).join('').toUpperCase()
                                            : 'DR'
                                        return (
                                            <button key={doc.id} type="button"
                                                    onClick={() => setSelectedDoctor(doc)}
                                                    className={`w-full flex items-center gap-4 p-4 rounded-2xl border-2 text-left transition-all
                          ${selectedDoctor?.id === doc.id
                                                        ? 'border-blue-600 bg-blue-50'
                                                        : 'border-gray-100 hover:border-blue-300'
                                                    }`}>
                                                <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center
                          justify-center text-white text-sm font-bold shrink-0">
                                                    {initials}
                                                </div>
                                                <div className="flex-1">
                                                    <p className={`font-semibold text-sm
                            ${selectedDoctor?.id === doc.id ? 'text-blue-700' : 'text-gray-800'}`}>
                                                        {doc.fullName}
                                                    </p>
                                                    <p className="text-gray-400 text-xs mt-0.5">
                                                        {doc.specialties?.join(', ')}
                                                    </p>
                                                </div>
                                                {selectedDoctor?.id === doc.id && (
                                                    <span className="text-blue-600 text-lg">✓</span>
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
                                        <button type="button"
                                                onClick={() => {
                                                    if (calMonth === 0) { setCalYear(y => y-1); setCalMonth(11) }
                                                    else setCalMonth(m => m-1)
                                                }}
                                                className="w-7 h-7 flex items-center justify-center rounded-lg
                        hover:bg-gray-100 text-gray-500">‹</button>
                                        <p className="text-sm font-semibold text-gray-800">
                                            {MONTHS[calMonth]} {calYear}
                                        </p>
                                        <button type="button"
                                                onClick={() => {
                                                    if (calMonth === 11) { setCalYear(y => y+1); setCalMonth(0) }
                                                    else setCalMonth(m => m+1)
                                                }}
                                                className="w-7 h-7 flex items-center justify-center rounded-lg
                        hover:bg-gray-100 text-gray-500">›</button>
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
                                                <button key={idx} type="button"
                                                        disabled={!available}
                                                        onClick={() => { if (available) { setSelectedDate(dateStr); setSelectedTime('') } }}
                                                        className={`h-8 w-8 mx-auto rounded-full text-xs flex items-center
                            justify-center transition-colors
                            ${!day ? 'invisible' : ''}
                            ${isSelected ? 'bg-blue-600 text-white font-semibold' : ''}
                            ${available && !isSelected ? 'hover:bg-blue-50 text-gray-700 cursor-pointer' : ''}
                            ${!available && day ? 'text-gray-300 cursor-not-allowed' : ''}
                          `}>
                                                    {day}
                                                </button>
                                            )
                                        })}
                                    </div>
                                </div>

                                {/* Horarios disponibles */}
                                <div className="w-48 shrink-0">
                                    <p className="text-sm font-semibold text-gray-800 mb-1">Horarios disponibles</p>
                                    <p className="text-xs text-gray-400 mb-3">
                                        {selectedDate || 'Selecciona una fecha'}
                                    </p>
                                    {availability.length === 0 ? (
                                        <p className="text-gray-300 text-xs">
                                            {selectedDate
                                                ? 'Sin horarios disponibles'
                                                : 'Selecciona una fecha primero'}
                                        </p>
                                    ) : (
                                        <div className="space-y-2">
                                            {availability.map(slot => (
                                                <button key={slot} type="button"
                                                        onClick={() => setSelectedTime(slot)}
                                                        className={`w-full py-2 rounded-xl text-sm font-medium border transition-colors
                            ${selectedTime === slot
                                                            ? 'bg-blue-600 text-white border-blue-600'
                                                            : 'bg-white text-gray-700 border-gray-200 hover:border-blue-400'
                                                        }`}>
                                                    {slot}
                                                </button>
                                            ))}
                                        </div>
                                    )}

                                    {/* Leyenda */}
                                    <div className="mt-4 space-y-1.5">
                                        <div className="flex items-center gap-2">
                                            <div className="w-3 h-3 rounded-full bg-blue-600" />
                                            <span className="text-xs text-gray-500">Seleccionado</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <div className="w-3 h-3 rounded-full border border-gray-300" />
                                            <span className="text-xs text-gray-500">Disponible</span>
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
                            <div className="divide-y divide-gray-50">
                                {[
                                    { label: 'Especialidad', value: selectedSpecialty?.name },
                                    { label: 'Profesional',  value: selectedDoctor?.fullName },
                                    { label: 'Fecha',        value: selectedDate
                                            ? new Date(selectedDate + 'T00:00:00').toLocaleDateString('es-CO', {
                                                day: 'numeric', month: 'long', year: 'numeric'
                                            })
                                            : '' },
                                    { label: 'Hora',         value: selectedTime },
                                ].map(row => (
                                    <div key={row.label} className="flex items-center justify-between py-4">
                                        <span className="text-sm text-gray-400">{row.label}</span>
                                        <span className="text-sm font-semibold text-gray-800">{row.value}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* --- Navegación --- */}
                <div className="flex items-center justify-between mt-6">
                    <button type="button" onClick={handleBack} disabled={step === 0}
                            className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-700
              disabled:opacity-30 transition-colors">
                        ← Anterior
                    </button>

                    {step < 3 ? (
                        <button type="button" onClick={handleNext} disabled={!canNext()}
                                className="flex items-center gap-2 bg-blue-600 text-white rounded-xl
                px-6 py-2.5 text-sm font-semibold hover:bg-blue-700 transition-colors
                disabled:opacity-40">
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