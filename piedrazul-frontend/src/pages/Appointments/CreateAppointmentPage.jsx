import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi, patientApi, identityApi } from '../../api'
import { useAuth } from '../../api/AuthContext'
import { isHoliday } from '../../utils/colombianHolidays'

const DAYS   = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb']
const MONTHS = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
  'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']

function addMinutes(timeStr, minutes) {
  const [h, m] = timeStr.split(':').map(Number)
  const total  = h * 60 + m + minutes
  return `${String(Math.floor(total / 60) % 24).padStart(2,'0')}:${String(total % 60).padStart(2,'0')}`
}

export default function CreateAppointmentPage() {
  const navigate    = useNavigate()
  const { hasRole } = useAuth()

  const [allDoctors,   setAllDoctors]   = useState([])
  const [specialties,  setSpecialties]  = useState([])
  const [doctors,      setDoctors]      = useState([])
  const [availability,   setAvailability]   = useState([])
  const [scheduleDays,   setScheduleDays]   = useState(null) // Set de días ISO (1=Lun…7=Dom) del médico seleccionado
  const [loadingSlots,   setLoadingSlots]   = useState(false)
  const [loading,      setLoading]      = useState(false)
  const [success,      setSuccess]      = useState(false)
  const [intervalMinutes, setIntervalMinutes] = useState(30)
  const [patientAppointments, setPatientAppointments] = useState([]) // Historial del paciente

  // Búsqueda de paciente
  const [documentId,       setDocumentId]       = useState('')
  const [searchingPatient, setSearchingPatient] = useState(false)
  const [patient,          setPatient]          = useState(null)
  const [patientName,      setPatientName]      = useState('')
  const [patientError,     setPatientError]     = useState('')

  // Selecciones
  const [selectedSpecialty, setSelectedSpecialty] = useState('')
  const [selectedDoctor,    setSelectedDoctor]    = useState('')
  const [selectedDate,      setSelectedDate]      = useState('')
  const [selectedTime,      setSelectedTime]      = useState('')
  const [refreshKey,        setRefreshKey]        = useState(0) // Para forzar recarga

  // Formulario
  const [form, setForm]     = useState({ reason: '', notes: '' })
  const [errors, setErrors] = useState({})

  // Calendario
  const today = new Date()
  const [calYear,  setCalYear]  = useState(today.getFullYear())
  const [calMonth, setCalMonth] = useState(today.getMonth())

  // --- Cargar todos los médicos y extraer especialidades ---
  useEffect(() => {
    medicalApi.listDoctors().then(res => {
      const docs = res.data || []
      setAllDoctors(docs)
      const specSet = new Set()
      docs.forEach(d => d.specialties?.forEach(s => specSet.add(s)))
      setSpecialties([...specSet])
    }).catch(() => {})
  }, [])

  // --- Filtrar médicos por especialidad ---
  useEffect(() => {
    if (!selectedSpecialty) { setDoctors([]); setSelectedDoctor(''); return }
    setDoctors(allDoctors.filter(d => d.specialties?.includes(selectedSpecialty)))
    setSelectedDoctor('')
    setSelectedDate('')
    setSelectedTime('')
    setAvailability([])
  }, [selectedSpecialty, allDoctors])

  // --- Cargar horario del médico al seleccionarlo (para bloquear días en el calendario) ---
  useEffect(() => {
    if (!selectedDoctor) { setScheduleDays(null); return }
    medicalApi.getDoctorSchedule(selectedDoctor).then(res => {
      const s = res.data || []
      // dayOfWeek del backend: 1=Lun … 7=Dom (ISO 8601)
      // getDay() de JS:        0=Dom, 1=Lun … 6=Sáb
      // Conversión: ISO → JS: ISO%7 da 0 para 7(Dom), resto igual
      const jsdays = new Set(s.map(d => d.dayOfWeek % 7))
      setScheduleDays(jsdays)
      if (s.length > 0) setIntervalMinutes(s[0].intervalMinutes || 30)
    }).catch(() => setScheduleDays(null))
  }, [selectedDoctor])

  // --- Cargar slots disponibles para la fecha elegida ---
  useEffect(() => {
    if (!selectedDoctor || !selectedDate) { setAvailability([]); setSelectedTime(''); return }
    setLoadingSlots(true)
    setSelectedTime('')
    medicalApi.getAvailability(selectedDoctor, selectedDate)
      .then(res => setAvailability(res.data || []))
      .catch(() => setAvailability([]))
      .finally(() => setLoadingSlots(false))
  }, [selectedDoctor, selectedDate, refreshKey])

  // --- Buscar paciente ---
  const handleSearchPatient = async () => {
    if (!documentId.trim()) { setPatientError('Ingresa el número de documento'); return }
    setSearchingPatient(true)
    setPatient(null)
    setPatientName('')
    setPatientError('')
    setPatientAppointments([])
    try {
      const [patRes, idRes] = await Promise.all([
        patientApi.getById(documentId.trim()).catch(() => null),
        identityApi.getUserById(documentId.trim()).catch(() => null),
      ])
      if (!patRes && !idRes) throw new Error('No encontrado')
      setPatient(patRes?.data || null)
      setPatientName(idRes?.data?.fullName || `Paciente ${documentId}`)
      
      // Cargar historial de citas del paciente
      try {
        const appointmentsRes = await appointmentApi.listByPatient(documentId.trim())
        setPatientAppointments(appointmentsRes.data || [])
      } catch {
        setPatientAppointments([])
      }
    } catch {
      setPatientError('Paciente no encontrado. Verifique el número de documento.')
    } finally {
      setSearchingPatient(false)
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

  const isDateAvailable = (day) => {
    if (!day) return false
    const d = new Date(calYear, calMonth, day)
    const t = new Date(today.getFullYear(), today.getMonth(), today.getDate())

    if (d < t) return false
    if (isHoliday(d)) return false

    // Si hay médico seleccionado, solo habilitar los días en que trabaja
    if (scheduleDays !== null && !scheduleDays.has(d.getDay())) return false

    return true
  }

  // Verificar si el paciente tiene cita con Medicina General
  const hasMedicinaGeneral = () => {
    for (const apt of patientAppointments) {
      const doctor = allDoctors.find(d => d.id === apt.doctorId)
      if (doctor && doctor.specialties && doctor.specialties.includes('Medicina General')) {
        return true
      }
    }
    return false
  }

  // Filtrar especialidades según regla de negocio
  const getAvailableSpecialties = () => {
    if (patientAppointments.length === 0 || !hasMedicinaGeneral()) {
      // Si no tiene citas o no ha pasado por Medicina General, solo mostrar Medicina General
      return specialties.filter(s => s === 'Medicina General')
    }
    // Si ya pasó por Medicina General, mostrar todas
    return specialties
  }

  // Verificar si el paciente tiene una cita activa (AGENDADA)
  const hasActiveAppointment = () => {
    for (const apt of patientAppointments) {
      if (apt.status === 'AGENDADA') {
        return true
      }
    }
    return false
  }

  const formatDate = (day) =>
      `${calYear}-${String(calMonth + 1).padStart(2,'0')}-${String(day).padStart(2,'0')}`

  const formatDateDisplay = (dateStr) => {
    if (!dateStr) return ''
    const [y, m, d] = dateStr.split('-')
    return `${parseInt(d)} de ${MONTHS[parseInt(m) - 1]} de ${y}`
  }

  const validate = () => {
    const e = {}
    if (!patientName)       e.patient   = 'Busca y selecciona un paciente'
    if (!selectedSpecialty) e.specialty = 'Selecciona una especialidad'
    if (!selectedDoctor)    e.doctorId  = 'Selecciona un profesional'
    if (!selectedDate)      e.date      = 'Selecciona una fecha'
    if (!selectedTime)      e.startTime = 'Selecciona una hora'
    if (!form.reason)       e.reason    = 'Obligatorio'
    
    // Validar que no tenga cita activa
    if (hasActiveAppointment()) {
      e.general = 'Este paciente ya tiene una cita agendada. Debe esperar a que sea atendida o cancelada antes de agendar otra'
    }
    
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) { setErrors(newErrors); return }

    setLoading(true)
    try {
      await appointmentApi.create({
        patientId: parseInt(documentId),
        doctorId:  parseInt(selectedDoctor),
        date:      selectedDate,
        startTime: selectedTime,
        endTime:   addMinutes(selectedTime, intervalMinutes),
        reason:    form.reason,
        notes:     form.notes,
      })
      setSuccess(true)
      setTimeout(() => navigate('/appointments'), 2000)
    } catch (err) {
      setErrors({ general: err.response?.data?.message || 'Error al registrar la cita' })
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
        <Layout>
          <div className="flex items-center justify-center h-full">
            <div className="bg-white rounded-2xl border border-gray-100 p-10 text-center max-w-sm">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#22c55e" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
              <h2 className="text-xl font-bold text-gray-800">¡Cita Registrada!</h2>
              <p className="text-gray-500 text-sm mt-2">Redirigiendo...</p>
            </div>
          </div>
        </Layout>
    )
  }

  return (
      <Layout>
        <div className="max-w-6xl mx-auto">

          <div className="mb-6">
            <p className="text-sm text-gray-400 mb-1">Administración / Registrar Cita</p>
            <h1 className="text-2xl font-bold text-gray-800">Registrar Cita Manual</h1>
            <p className="text-gray-500 text-sm mt-1">Registra una nueva cita médica para un paciente existente</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="flex gap-6">

              {/* ── Columna izquierda ── */}
              <div className="flex-1 space-y-5">

                {/* Paciente */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                  <h2 className="font-semibold text-sm uppercase tracking-wider text-gray-500 mb-4">Paciente</h2>
                  <div className="flex gap-3 items-end">
                    <div className="flex-1">
                      <label className="block text-sm text-gray-500 mb-1">
                        Número de documento <span className="text-red-500">*</span>
                      </label>
                      <input type="text" value={documentId}
                             onChange={e => { setDocumentId(e.target.value); setPatient(null); setPatientName(''); setPatientError('') }}
                             onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), handleSearchPatient())}
                             placeholder="Ej: 1077156530"
                             className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                        ${patientError ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`} />
                    </div>
                    <button type="button" onClick={handleSearchPatient} disabled={searchingPatient}
                            className="bg-blue-600 text-white rounded-xl px-5 py-2.5 text-sm font-semibold
                      hover:bg-blue-700 transition-colors disabled:opacity-50 shrink-0">
                      {searchingPatient ? 'Buscando...' : 'Buscar'}
                    </button>
                  </div>
                  {patientError && <p className="text-red-500 text-xs mt-2">{patientError}</p>}
                  {errors.patient && !patientError && <p className="text-red-500 text-xs mt-2">{errors.patient}</p>}

                  {patientName && patient && (
                      <div className="mt-4 bg-blue-50 rounded-xl p-4 border border-blue-100">
                        <p className="text-xs text-blue-500 font-semibold uppercase tracking-wider mb-2">Paciente encontrado</p>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                          <div><span className="text-gray-400 text-xs">Nombre</span><p className="font-semibold text-gray-800">{patientName}</p></div>
                          <div><span className="text-gray-400 text-xs">Teléfono</span><p className="font-semibold text-gray-800">{patient.phone || '—'}</p></div>
                          <div><span className="text-gray-400 text-xs">Correo</span><p className="font-semibold text-gray-800">{patient.email || '—'}</p></div>
                          <div><span className="text-gray-400 text-xs">Género</span><p className="font-semibold text-gray-800">{patient.gender || '—'}</p></div>
                        </div>
                      </div>
                  )}
                </div>

                {/* Especialidad y Profesional */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                  <h2 className="font-semibold text-sm uppercase tracking-wider text-gray-500 mb-4">Profesional</h2>

                  <div className="mb-4">
                    <label className="block text-sm text-gray-500 mb-1">
                      Especialidad <span className="text-red-500">*</span>
                    </label>
                    <select value={selectedSpecialty}
                            onChange={e => { setSelectedSpecialty(e.target.value); setErrors({...errors, specialty: ''}) }}
                            disabled={!patientName}
                            className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                      disabled:bg-gray-50 disabled:text-gray-400
                      ${errors.specialty ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                      <option value="">
                        {patientName ? 'Seleccionar especialidad...' : 'Primero busca un paciente'}
                      </option>
                      {getAvailableSpecialties().map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                    {errors.specialty && <p className="text-red-500 text-xs mt-1">{errors.specialty}</p>}
                    {patientName && patientAppointments.length === 0 && (
                      <p className="text-blue-600 text-xs mt-1">
                        ℹ️ Como paciente nuevo, debes agendar primero con Medicina General
                      </p>
                    )}
                    {patientName && patientAppointments.length > 0 && !hasMedicinaGeneral() && (
                      <p className="text-orange-600 text-xs mt-1">
                        ℹ️ Debes tener al menos una cita con Medicina General antes de acceder a especialidades
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm text-gray-500 mb-1">
                      Profesional <span className="text-red-500">*</span>
                    </label>
                    <div className="flex gap-2">
                      <select value={selectedDoctor} disabled={!selectedSpecialty}
                              onChange={e => { setSelectedDoctor(e.target.value); setSelectedDate(''); setSelectedTime(''); setErrors({...errors, doctorId: ''}) }}
                              className={`flex-1 border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                        disabled:bg-gray-50 disabled:text-gray-400
                        ${errors.doctorId ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                        <option value="">
                          {selectedSpecialty ? 'Seleccionar profesional...' : 'Primero selecciona una especialidad'}
                        </option>
                        {doctors.map(d => (
                            <option key={d.id} value={d.id}>
                              {d.fullName || `Profesional ${d.id}`}
                            </option>
                        ))}
                      </select>
                      {selectedDoctor && (
                          <button type="button" 
                                  onClick={() => setRefreshKey(prev => prev + 1)}
                                  disabled={loadingSlots}
                                  className="bg-gray-100 text-gray-600 rounded-xl px-4 py-2.5 text-sm font-semibold
                                    hover:bg-gray-200 transition-colors disabled:opacity-50 shrink-0"
                                  title="Actualizar horarios">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>
                          </button>
                      )}
                    </div>
                    {errors.doctorId && <p className="text-red-500 text-xs mt-1">{errors.doctorId}</p>}
                  </div>
                </div>

                {/* Motivo y notas */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                  <h2 className="font-semibold text-sm uppercase tracking-wider text-gray-500 mb-4">Detalles</h2>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">
                        Motivo de consulta <span className="text-red-500">*</span>
                      </label>
                      <textarea name="reason" value={form.reason}
                                onChange={e => { setForm({...form, reason: e.target.value}); setErrors({...errors, reason: ''}) }}
                                rows={3} placeholder="Describe el motivo..."
                                className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none
                        resize-none transition-colors
                        ${errors.reason ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`} />
                      {errors.reason && <p className="text-red-500 text-xs mt-1">{errors.reason}</p>}
                    </div>
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">Notas adicionales</label>
                      <textarea name="notes" value={form.notes}
                                onChange={e => setForm({...form, notes: e.target.value})}
                                rows={3} placeholder="Información adicional (opcional)"
                                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                        focus:outline-none focus:border-blue-500 resize-none transition-colors" />
                    </div>
                  </div>
                </div>

                {errors.general && (
                    <p className="text-red-500 text-sm bg-red-50 rounded-xl py-2 px-4">{errors.general}</p>
                )}

                <button type="submit" disabled={loading}
                        className="w-full bg-blue-600 text-white rounded-xl py-3 font-semibold text-sm
                  hover:bg-blue-700 transition-colors disabled:opacity-50">
                  {loading ? 'Registrando...' : 'Registrar Cita'}
                </button>
              </div>

              {/* ── Columna derecha: calendario + fecha + hora ── */}
              <div className="w-72 shrink-0">
                <div className="bg-white rounded-2xl border border-gray-100 p-5 sticky top-6 space-y-4">
                  <h3 className="font-semibold text-gray-800 text-sm">Fecha y hora</h3>

                  {/* Calendario */}
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <button type="button" onClick={() => {
                        if (calMonth === 0) { setCalYear(calYear - 1); setCalMonth(11) }
                        else setCalMonth(calMonth - 1)
                      }} className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500 text-lg">‹</button>
                      <p className="text-sm font-semibold text-gray-800">{MONTHS[calMonth]} {calYear}</p>
                      <button type="button" onClick={() => {
                        if (calMonth === 11) { setCalYear(calYear + 1); setCalMonth(0) }
                        else setCalMonth(calMonth + 1)
                      }} className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500 text-lg">›</button>
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
                        const available  = isDateAvailable(day)
                        const dateObj    = day ? new Date(calYear, calMonth, day) : null
                        const isHol      = dateObj && isHoliday(dateObj)
                        
                        return (
                            <button key={idx} type="button" disabled={!available}
                                    onClick={() => {
                                      if (available) {
                                        setSelectedDate(dateStr)
                                        setSelectedTime('')
                                        setErrors({...errors, date: ''})
                                      }
                                    }}
                                    title={isHol ? 'Festivo - No disponible' : ''}
                                    className={`h-8 w-8 mx-auto rounded-full text-xs flex items-center
                            justify-center transition-colors
                            ${!day ? 'invisible' : ''}
                            ${isSelected ? 'bg-blue-600 text-white font-semibold' : ''}
                            ${available && !isSelected ? 'hover:bg-blue-50 text-gray-700 cursor-pointer' : ''}
                            ${!available && day && isHol ? 'text-red-300 cursor-not-allowed line-through' : ''}
                            ${!available && day && !isHol ? 'text-gray-300 cursor-not-allowed' : ''}
                          `}>
                              {day}
                            </button>
                        )
                      })}
                    </div>
                    {errors.date && <p className="text-red-500 text-xs mt-1">{errors.date}</p>}
                  </div>

                  {/* Fecha seleccionada */}
                  <div>
                    <label className="block text-xs text-gray-400 mb-1">Fecha seleccionada</label>
                    <div className={`w-full border rounded-xl px-3 py-2 text-sm bg-gray-50
                    ${errors.date ? 'border-red-400' : 'border-gray-200'}`}>
                      {selectedDate
                          ? <span className="text-blue-600 font-medium">{formatDateDisplay(selectedDate)}</span>
                          : <span className="text-gray-400">Sin seleccionar</span>}
                    </div>
                  </div>

                  {/* Dropdown de horarios */}
                  <div>
                    <label className="block text-xs text-gray-400 mb-1">Horarios disponibles</label>
                    {!selectedDate || !selectedDoctor ? (
                        <p className="text-gray-400 text-xs">Selecciona profesional y fecha primero</p>
                    ) : loadingSlots ? (
                        <p className="text-gray-400 text-xs">Cargando...</p>
                    ) : availability.length === 0 ? (
                        <p className="text-orange-500 text-xs font-medium">
                          No hay horarios disponibles para este día
                        </p>
                    ) : (
                        <select value={selectedTime}
                                onChange={e => { setSelectedTime(e.target.value); setErrors({...errors, startTime: ''}) }}
                                className={`w-full border rounded-xl px-3 py-2 text-sm focus:outline-none transition-colors
                        ${selectedTime
                                    ? 'border-blue-500 bg-blue-50 text-blue-700 font-semibold'
                                    : 'border-gray-200 focus:border-blue-500'}`}>
                          <option value="">Seleccionar hora...</option>
                          {availability.map(slot => (
                              <option key={slot} value={slot}>{slot}</option>
                          ))}
                        </select>
                    )}
                  </div>

                  {/* Hora seleccionada */}
                  <div>
                    <label className="block text-xs text-gray-400 mb-1">Hora seleccionada</label>
                    <div className={`w-full border rounded-xl px-3 py-2 text-sm bg-gray-50
                    ${errors.startTime ? 'border-red-400' : 'border-gray-200'}`}>
                      {selectedTime
                          ? <span className="text-blue-600 font-medium">{selectedTime}</span>
                          : <span className="text-gray-400">Sin seleccionar</span>}
                    </div>
                    {errors.startTime && <p className="text-red-500 text-xs mt-1">{errors.startTime}</p>}
                  </div>

                </div>
              </div>

            </div>
          </form>
        </div>
      </Layout>
  )
}