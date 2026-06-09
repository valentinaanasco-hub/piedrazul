import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import PatientLayout from '../../components/PatientLayout'
import { medicalApi, appointmentApi, patientApi } from '../../api'
import { useAuth } from '../../api/AuthContext'

const STEPS = ['Tipo de servicio', 'Profesional', 'Fecha y Hora', 'Confirmación']

// ── Tipos de servicio hardcodeados (igual que en CreateAppointmentPage) ────────
const SERVICE_TYPES = ['Consulta General', 'Fisioterapia', 'Quiropraxia', 'Terapia Neural']
const SERVICE_TYPE_TO_ENUM = {
    'Consulta General': 'CONSULTA_GENERAL',
    'Fisioterapia':     'FISIOTERAPIA',
    'Quiropraxia':      'QUIROPRAXIA',
    'Terapia Neural':   'TERAPIA_NEURAL',
}
function filterDoctorsByService(service, allDocs) {
    if (!service) return []
    if (service === 'Quiropraxia')      return allDocs.filter(d => d.specialties?.includes('Quiropraxia'))
    if (service === 'Terapia Neural')   return allDocs.filter(d => d.specialties?.includes('Terapia Neural'))
    if (service === 'Consulta General') return allDocs
    return allDocs.filter(d => !d.specialties?.length)  // Fisioterapia
}
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
    const total  = h * 60 + m + minutes
    return `${String(Math.floor(total / 60) % 24).padStart(2,'0')}:${String(total % 60).padStart(2,'0')}`
}

export default function ScheduleAppointmentPage() {
    const navigate  = useNavigate()
    const { user }  = useAuth()

    const [step, setStep]                       = useState(0)
    const [allDoctors, setAllDoctors]           = useState([])
    const [specialties, setSpecialties]         = useState([])
    const [doctors, setDoctors]                 = useState([])
    const [availability, setAvailability]       = useState([])
    const [intervalMinutes, setIntervalMinutes] = useState(30)
    const [loadingSlots, setLoadingSlots]       = useState(false)
    const [loading, setLoading]                 = useState(false)
    const [submitting, setSubmitting]           = useState(false)
    const [success, setSuccess]                 = useState(false)
    const [errorMsg, setErrorMsg]               = useState('')

    const [patientId,             setPatientId]             = useState(null) // integer resuelto via /patients/me
    const [hasActiveAppointment, setHasActiveAppointment] = useState(false)
    const [authorization,        setAuthorization]        = useState(null)  // autorización médica activa

    const [selectedSpecialty, setSelectedSpecialty] = useState(null)
    const [selectedDoctor,    setSelectedDoctor]    = useState(null)
    const [selectedDate,      setSelectedDate]      = useState('')
    const [selectedTime,      setSelectedTime]      = useState('')
    const [reason,            setReason]            = useState('')

    const today = new Date()
    const [calYear,  setCalYear]  = useState(today.getFullYear())
    const [calMonth, setCalMonth] = useState(today.getMonth())

    useEffect(() => {
        if (!user) return
        setLoading(true)

        // Paso 1: resolver el patientId real (integer) vía /patients/me
        // Si falla (404 en paciente recién creado) continuamos con null — igual cargamos médicos
        patientApi.getMe()
            .catch(() => ({ data: null }))
            .then(meRes => {
                const realId = meRes?.data?.id ?? null
                setPatientId(realId)

                // Paso 2: cargar médicos, citas y autorización en paralelo
                return Promise.all([
                    medicalApi.listDoctors(),
                    realId
                        ? appointmentApi.listByPatient(realId).catch(() => ({ data: [] }))
                        : Promise.resolve({ data: [] }),
                    realId
                        ? appointmentApi.getPatientAuthorization(realId).catch(() => null)
                        : Promise.resolve(null),
                ])
            })
            .then(([docsRes, aptsRes, authRes]) => {
                const docs  = docsRes.data || []
                const apts  = aptsRes.data || []
                const auth  = authRes?.status === 200 ? authRes.data : null
                setAllDoctors(docs)
                setAuthorization(auth)

                const activeApt = apts.some(a => a.status === 'AGENDADA' || a.status === 'REAGENDADA')
                setHasActiveAppointment(activeApt)

                // Regla: sin autorización médica → solo Consulta General.
                // Con autorización activa → Consulta General + el servicio autorizado.
                const ENUM_TO_SERVICE = {
                    FISIOTERAPIA:   'Fisioterapia',
                    QUIROPRAXIA:    'Quiropraxia',
                    TERAPIA_NEURAL: 'Terapia Neural',
                }
                let availableServices
                if (auth) {
                    const authorizedName = ENUM_TO_SERVICE[auth.serviceType]
                    availableServices = authorizedName
                        ? ['Consulta General', authorizedName]
                        : ['Consulta General']
                } else {
                    availableServices = ['Consulta General']
                }
                setSpecialties(availableServices.map(name => ({ name })))
            })
            .catch(() => { setAllDoctors([]); setSpecialties([]) })
            .finally(() => setLoading(false))
    }, [user])

    useEffect(() => {
        if (!selectedSpecialty) return

        // Quiropraxia: filtrar además por médicos con horario definido
        if (selectedSpecialty.name === 'Quiropraxia') {
            const matching = filterDoctorsByService('Quiropraxia', allDoctors)
            let cancelled  = false
            Promise.all(matching.map(async d => {
                try {
                    const res = await medicalApi.getDoctorSchedule(d.id)
                    return (res.data && res.data.length > 0) ? d : null
                } catch { return null }
            })).then(list => { if (!cancelled) setDoctors(list.filter(Boolean)) })
            return () => { cancelled = true }
        }

        setDoctors(filterDoctorsByService(selectedSpecialty.name, allDoctors))
    }, [selectedSpecialty, allDoctors])

    useEffect(() => {
        if (!selectedDoctor || !selectedDate) { setAvailability([]); return }
        setLoadingSlots(true)
        Promise.all([
            medicalApi.getAvailability(selectedDoctor.id, selectedDate),
            medicalApi.getDoctorSchedule(selectedDoctor.id),
            appointmentApi.listByDoctorAndDate(selectedDoctor.id, selectedDate).catch(() => ({ data: [] })),
        ]).then(([availRes, schedRes, aptsRes]) => {
            const allSlots    = availRes.data || []
            const bookedTimes = new Set(
                (aptsRes.data || [])
                    .filter(a => a.status !== 'CANCELADA')
                    .map(a => (a.startTime || '').substring(0, 5))
            )
            setAvailability(allSlots.filter(s => !bookedTimes.has(s.substring(0, 5))))
            const schedules = schedRes.data || []
            if (schedules.length > 0) setIntervalMinutes(schedules[0].intervalMinutes || 30)
        }).catch(() => setAvailability([]))
            .finally(() => setLoadingSlots(false))
    }, [selectedDoctor, selectedDate])

    const canNext = () => {
        if (step === 0) return !!selectedSpecialty
        if (step === 1) return !!selectedDoctor
        if (step === 2) return !!selectedDate && !!selectedTime
        if (step === 3) return !!reason.trim()
        return true
    }

    const handleNext = () => { if (canNext()) setStep(s => s + 1) }
    const handleBack = () => {
        setStep(s => s - 1)
        setErrorMsg('')
        if (step === 2) { setSelectedDate(''); setSelectedTime('') }
    }

    const handleConfirm = async () => {
        setSubmitting(true)
        setErrorMsg('')

        // Resolver el ID entero del paciente:
        // 1º el resuelto vía /patients/me
        // 2º el username si es numérico (Keycloak usa la cédula como username)
        const resolvedPatientId = patientId
            ?? (parseInt(user?.username) > 0 ? parseInt(user?.username) : null)

        if (!resolvedPatientId) {
            setErrorMsg('No se pudo identificar tu cuenta de paciente. Por favor contacta al administrador.')
            setSubmitting(false)
            return
        }

        try {
            await appointmentApi.create({
                patientId:   resolvedPatientId,
                doctorId:    selectedDoctor.id,
                doctorName:  selectedDoctor.displayName || selectedDoctor.fullName || `Profesional ${selectedDoctor.id}`,
                serviceType: SERVICE_TYPE_TO_ENUM[selectedSpecialty.name] || 'CONSULTA_GENERAL',
                date:        selectedDate,
                startTime:   selectedTime,
                endTime:     addMinutes(selectedTime, intervalMinutes),
                reason:      reason.trim() || selectedSpecialty.name,
                notes:       '',
            })
            setSuccess(true)
            setTimeout(() => navigate('/patient/appointments'), 2500)
        } catch (err) {
            const msg = err.response?.data?.message || 'Error al confirmar la cita. Intenta de nuevo.'
            setErrorMsg(msg)
        } finally {
            setSubmitting(false)
        }
    }

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
                        <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#22c55e" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                        </div>
                        <h2 className="text-xl font-bold text-gray-800">¡Cita Confirmada!</h2>
                        <p className="text-gray-500 text-sm mt-2">Redirigiendo a tus citas...</p>
                    </div>
                </div>
            </PatientLayout>
        )
    }

    // --- Bloqueo: cita activa existente ---
    if (!loading && hasActiveAppointment) {
        return (
            <PatientLayout>
                <div className="max-w-lg mx-auto">
                    <div className="mb-6">
                        <h1 className="text-2xl font-bold text-gray-800">Agendar Cita</h1>
                    </div>
                    <div className="bg-white rounded-2xl border border-gray-100 p-10 text-center">
                        <div className="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <span className="text-3xl">📅</span>
                        </div>
                        <h2 className="text-lg font-bold text-gray-800 mb-2">Ya tienes una cita pendiente</h2>
                        <p className="text-gray-500 text-sm">
                            Tienes una cita <strong>agendada o reagendada</strong>. Para poder agendar una nueva
                            cita, primero debes esperar a que sea atendida o cancelarla desde <strong>Mis Citas</strong>.
                        </p>
                        <button onClick={() => navigate('/patient/appointments')}
                                className="mt-6 bg-blue-600 text-white rounded-xl px-6 py-2.5 text-sm font-semibold
                                hover:bg-blue-700 transition-colors">
                            Ver mis citas
                        </button>
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

                {!authorization && (
                    <div className="mb-4 bg-blue-50 border border-blue-100 rounded-2xl px-5 py-3 flex items-start gap-3">
                        <span className="text-blue-500 text-lg mt-0.5">ℹ️</span>
                        <p className="text-sm text-blue-700">
                            Para acceder a servicios especializados necesitas que el médico te autorice
                            durante una <strong>Consulta General</strong>.
                        </p>
                    </div>
                )}

                {authorization && (
                    <div className="mb-4 bg-green-50 border border-green-100 rounded-2xl px-5 py-3 flex items-start gap-3">
                        <span className="text-green-500 text-lg mt-0.5">✅</span>
                        <p className="text-sm text-green-700">
                            Tu médico te autorizó para acceder a{' '}
                            <strong>
                                {{ FISIOTERAPIA: 'Fisioterapia', QUIROPRAXIA: 'Quiropraxia', TERAPIA_NEURAL: 'Terapia Neural' }[authorization.serviceType] || authorization.serviceType}
                            </strong>.{' '}
                            La autorización caduca el{' '}
                            {new Date(authorization.expiresAt).toLocaleDateString('es-CO', { day: 'numeric', month: 'long', year: 'numeric' })}.
                        </p>
                    </div>
                )}

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
                                    {idx < step
                                        ? <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                                        : idx + 1}
                                </div>
                                <span className={`text-sm whitespace-nowrap ${idx === step ? 'font-semibold text-gray-800' : 'text-gray-400'}`}>
                                    {label}
                                </span>
                            </div>
                            {idx < STEPS.length - 1 && (
                                <div className={`flex-1 h-px mx-3 ${idx < step ? 'bg-blue-600' : 'bg-gray-200'}`} />
                            )}
                        </div>
                    ))}
                </div>

                <div className="bg-white rounded-2xl border border-gray-100 p-6 min-h-64">

                    {step === 0 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-4">Selecciona un tipo de servicio</h2>
                            {loading ? (
                                <p className="text-gray-400 text-sm text-center py-8">Cargando servicios...</p>
                            ) : specialties.length === 0 ? (
                                <p className="text-gray-400 text-sm text-center py-8">No hay servicios disponibles</p>
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
                                                ${selectedSpecialty?.name === spec.name ? 'text-blue-700' : 'text-gray-800'}`}>
                                                {spec.name}
                                            </p>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}

                    {step === 1 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-1">Profesionales disponibles</h2>
                            <p className="text-gray-400 text-sm mb-4">— {selectedSpecialty?.name}</p>
                            {doctors.length === 0 ? (
                                <p className="text-gray-400 text-sm text-center py-8">No hay profesionales disponibles</p>
                            ) : (
                                <div className="space-y-3">
                                    {doctors.map(doc => {
                                        const displayName = doc.fullName?.trim()
                                            || [doc.firstName, doc.firstSurname].filter(Boolean).join(' ')
                                            || `Profesional ${doc.id}`
                                        const initials = displayName.split(' ').map(w => w[0]).slice(0,2).join('').toUpperCase()
                                        return (
                                            <button key={doc.id} type="button"
                                                    onClick={() => setSelectedDoctor({ ...doc, displayName })}
                                                    className={`w-full flex items-center gap-4 p-4 rounded-2xl border-2 text-left transition-all
                                                    ${selectedDoctor?.id === doc.id ? 'border-blue-600 bg-blue-50' : 'border-gray-100 hover:border-blue-300'}`}>
                                                <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-bold shrink-0">
                                                    {initials}
                                                </div>
                                                <div className="flex-1">
                                                    <p className={`font-semibold text-sm ${selectedDoctor?.id === doc.id ? 'text-blue-700' : 'text-gray-800'}`}>
                                                        {displayName}
                                                    </p>
                                                    <p className="text-gray-400 text-xs mt-0.5">{doc.specialties?.join(', ')}</p>
                                                </div>
                                                {selectedDoctor?.id === doc.id && <span className="text-blue-600 text-lg shrink-0">✓</span>}
                                            </button>
                                        )
                                    })}
                                </div>
                            )}
                        </div>
                    )}

                    {step === 2 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-4">Selecciona fecha y hora</h2>
                            <div className="flex gap-6">
                                <div className="flex-1">
                                    <div className="flex items-center justify-between mb-3">
                                        <button type="button" onClick={() => {
                                            if (calMonth === 0) { setCalYear(y => y-1); setCalMonth(11) }
                                            else setCalMonth(m => m-1)
                                        }} className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500 text-lg">‹</button>
                                        <p className="text-sm font-semibold text-gray-800">{MONTHS[calMonth]} {calYear}</p>
                                        <button type="button" onClick={() => {
                                            if (calMonth === 11) { setCalYear(y => y+1); setCalMonth(0) }
                                            else setCalMonth(m => m+1)
                                        }} className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500 text-lg">›</button>
                                    </div>
                                    <div className="grid grid-cols-7 mb-1">
                                        {DAYS.map(d => <div key={d} className="text-center text-xs text-gray-400 font-medium py-1">{d}</div>)}
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

                                <div className="w-52 shrink-0">
                                    <p className="text-sm font-semibold text-gray-800 mb-1">Horarios disponibles</p>
                                    <p className="text-xs text-gray-400 mb-3">
                                        {selectedDate ? formatDateDisplay(selectedDate) : 'Selecciona una fecha'}
                                    </p>
                                    {!selectedDate ? (
                                        <p className="text-gray-300 text-xs">Selecciona una fecha primero</p>
                                    ) : loadingSlots ? (
                                        <p className="text-gray-400 text-xs">Cargando horarios...</p>
                                    ) : availability.length === 0 ? (
                                        <p className="text-orange-500 text-xs font-medium">No hay horarios disponibles para este día</p>
                                    ) : (
                                        <div>
                                            <select value={selectedTime} onChange={e => setSelectedTime(e.target.value)}
                                                    className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                                                    ${selectedTime ? 'border-blue-500 bg-blue-50 text-blue-700 font-semibold' : 'border-gray-200 focus:border-blue-500'}`}>
                                                <option value="">Seleccionar hora...</option>
                                                {availability.map(slot => <option key={slot} value={slot}>{slot}</option>)}
                                            </select>
                                            {selectedTime && <p className="text-blue-600 text-xs mt-2 font-medium">Hora seleccionada: {selectedTime}</p>}
                                            <p className="text-gray-400 text-xs mt-2">{availability.length} horario(s) libre(s)</p>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {step === 3 && (
                        <div>
                            <h2 className="font-semibold text-gray-800 mb-4">Confirma tu cita</h2>
                            <div className="divide-y divide-gray-50 rounded-2xl border border-gray-100">
                                {[
                                    { label: 'Tipo de servicio', value: selectedSpecialty?.name },
                                    { label: 'Profesional',  value: selectedDoctor?.displayName || selectedDoctor?.fullName },
                                    { label: 'Fecha',        value: formatDateDisplay(selectedDate) },
                                    { label: 'Hora',         value: selectedTime },
                                    { label: 'Paciente',     value: user?.fullName },
                                ].map(row => (
                                    <div key={row.label} className="flex items-center justify-between px-5 py-4">
                                        <span className="text-sm text-gray-400">{row.label}</span>
                                        <span className="text-sm font-semibold text-gray-800">{row.value}</span>
                                    </div>
                                ))}
                            </div>

                            <div className="mt-4">
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Motivo de consulta <span className="text-red-500">*</span>
                                </label>
                                <textarea
                                    value={reason}
                                    onChange={e => setReason(e.target.value)}
                                    rows={3}
                                    placeholder="Describe brevemente el motivo de tu consulta..."
                                    className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                                               focus:outline-none focus:border-blue-500 transition-colors resize-none"
                                />
                            </div>
                            {errorMsg && (
                                <div className="mt-4 bg-red-50 border border-red-100 rounded-2xl px-5 py-3 flex items-start gap-3">
                                    <span className="text-red-500 text-lg mt-0.5">⚠️</span>
                                    <div>
                                        <p className="text-sm font-semibold text-red-700">No se pudo confirmar la cita</p>
                                        <p className="text-sm text-red-600 mt-0.5">{errorMsg}</p>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </div>

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