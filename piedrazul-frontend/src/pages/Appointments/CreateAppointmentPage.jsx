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

// ── Modal de registro rápido de paciente nuevo ────────────────────────────────
function RegisterPatientModal({ documentId: initialDocumentId, onClose, onSuccess }) {
  const [form, setForm] = useState({
    documentId:      initialDocumentId || '',
    userTypeId:      'CC',
    firstName:       '',
    middleName:      '',
    firstSurname:    '',
    lastName:        '',
    phone:           '',
    gender:          '',
    email:           '',
    password:        '',
    confirmPassword: '',
    birthDay:        '',
    birthMonth:      '',
    birthYear:       '',
  })
  const [fieldErrors, setFieldErrors] = useState({})
  const [saving,      setSaving]      = useState(false)
  const [globalError, setGlobalError] = useState('')

  const set = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }))
    setFieldErrors(prev => ({ ...prev, [field]: '' }))
  }

  const validate = () => {
    const e = {}
    if (!form.documentId.trim())   e.documentId   = 'La cédula es obligatoria'
    if (!form.firstName.trim())    e.firstName    = 'Obligatorio'
    if (!form.firstSurname.trim()) e.firstSurname = 'Obligatorio'
    if (!form.phone.trim())        e.phone        = 'Obligatorio'
    if (!form.gender)              e.gender       = 'Selecciona un género'

    // Validación de fecha de nacimiento
    if (!form.birthDay || !form.birthMonth || !form.birthYear) {
      e.birthDate = 'La fecha de nacimiento es obligatoria'
    } else {
      const day   = parseInt(form.birthDay)
      const month = parseInt(form.birthMonth)
      const year  = parseInt(form.birthYear)
      const currentYear = new Date().getFullYear()
      if (year < 1900 || year > currentYear) {
        e.birthDate = `El año debe estar entre 1900 y ${currentYear}`
      } else if (month < 1 || month > 12) {
        e.birthDate = 'El mes debe estar entre 01 y 12'
      } else if (day < 1 || day > 31) {
        e.birthDate = 'El día debe estar entre 01 y 31'
      } else {
        const fecha = new Date(year, month - 1, day)
        if (fecha > new Date()) {
          e.birthDate = 'La fecha de nacimiento no puede ser en el futuro'
        } else if (fecha.getDate() !== day || fecha.getMonth() !== month - 1) {
          e.birthDate = 'Fecha inválida (verifica día y mes)'
        }
      }
    }

    // Contraseña: si se ingresa, debe confirmarse y coincidir
    const hasPassword = !!form.password
    if (hasPassword && !form.confirmPassword)
      e.confirmPassword = 'Confirma la contraseña'
    if (hasPassword && form.confirmPassword && form.password !== form.confirmPassword)
      e.confirmPassword = 'Las contraseñas no coinciden'
    return e
  }

  // Clase base para inputs — rojo si hay error en ese campo
  const cls = (field) =>
    `w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-blue-500 transition-colors
     ${fieldErrors[field] ? 'border-red-400 bg-red-50' : 'border-gray-200'}`

  const handleSubmit = async () => {
    const e = validate()
    setFieldErrors(e)
    if (Object.keys(e).length > 0) return

    setSaving(true)
    setGlobalError('')
    try {
      const emailFinal    = form.email.trim()    || `${form.documentId}@piedrazul.com`
      const passwordFinal = form.password        || form.documentId

      await patientApi.registerWeb({
        documentId:   parseInt(form.documentId),
        userTypeId:   form.userTypeId,
        firstName:    form.firstName.trim(),
        middleName:   form.middleName.trim()   || null,
        firstSurname: form.firstSurname.trim(),
        lastName:     form.lastName.trim()     || null,
        email:        emailFinal,
        password:     passwordFinal,
        phone:        form.phone.trim(),
        gender:       form.gender,
        birthDay:     form.birthDay,
        birthMonth:   form.birthMonth,
        birthYear:    form.birthYear,
      })
      localStorage.setItem(`piedrazul_typeId_${form.documentId}`, form.userTypeId)
      onSuccess(form.documentId)
    } catch (err) {
      setGlobalError(err.response?.data?.message || 'Error al registrar el paciente.')
    } finally {
      setSaving(false)
    }
  }

  return (
      <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-2xl w-full max-w-lg shadow-xl">
          <div className="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
            <div>
              <h3 className="font-bold text-gray-800">Registrar nuevo paciente</h3>
              <p className="text-sm text-gray-400 mt-0.5">Completa los datos del paciente</p>
            </div>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
          </div>

          <div className="px-6 py-5 space-y-3 max-h-[70vh] overflow-y-auto">
            <div className="grid grid-cols-2 gap-3">

              {/* Tipo de documento + Cédula */}
              <div>
                <label className="block text-xs text-gray-500 mb-1">Tipo de documento <span className="text-red-500">*</span></label>
                <select value={form.userTypeId} onChange={e => set('userTypeId', e.target.value)}
                        className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-blue-500">
                  {['CC','TI','CE','PA','RC'].map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Número de documento <span className="text-red-500">*</span></label>
                <input value={form.documentId}
                       onChange={e => set('documentId', e.target.value.replace(/\D/g, ''))}
                       placeholder="Ej: 1077156530"
                       className={cls('documentId')} />
                {fieldErrors.documentId
                  ? <p className="text-red-500 text-xs mt-1">{fieldErrors.documentId}</p>
                  : <p className="text-gray-400 text-xs mt-1">Solo números, sin puntos ni espacios</p>}
              </div>

              {/* Género */}
              <div className="col-span-2">
                <label className="block text-xs text-gray-500 mb-1">Género <span className="text-red-500">*</span></label>
                <select value={form.gender} onChange={e => set('gender', e.target.value)}
                        className={`w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:border-blue-500
                          ${fieldErrors.gender ? 'border-red-400' : 'border-gray-200'}`}>
                  <option value="">Seleccione un género</option>
                  {['Hombre','Mujer','Otro'].map(g => <option key={g} value={g}>{g}</option>)}
                </select>
                {fieldErrors.gender && <p className="text-red-500 text-xs mt-1">{fieldErrors.gender}</p>}
              </div>

              {/* Nombres */}
              <div>
                <label className="block text-xs text-gray-500 mb-1">Primer nombre <span className="text-red-500">*</span></label>
                <input value={form.firstName} onChange={e => set('firstName', e.target.value)}
                       placeholder="Ej: Juan"
                       className={cls('firstName')} />
                {fieldErrors.firstName && <p className="text-red-500 text-xs mt-1">{fieldErrors.firstName}</p>}
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Segundo nombre</label>
                <input value={form.middleName} onChange={e => set('middleName', e.target.value)}
                       placeholder="Ej: Carlos (opcional)"
                       className={cls('middleName')} />
              </div>

              {/* Apellidos */}
              <div>
                <label className="block text-xs text-gray-500 mb-1">Primer apellido <span className="text-red-500">*</span></label>
                <input value={form.firstSurname} onChange={e => set('firstSurname', e.target.value)}
                       placeholder="Ej: Pérez"
                       className={cls('firstSurname')} />
                {fieldErrors.firstSurname && <p className="text-red-500 text-xs mt-1">{fieldErrors.firstSurname}</p>}
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Segundo apellido</label>
                <input value={form.lastName} onChange={e => set('lastName', e.target.value)}
                       placeholder="Ej: García (opcional)"
                       className={cls('lastName')} />
              </div>

              {/* Contacto */}
              <div>
                <label className="block text-xs text-gray-500 mb-1">Teléfono <span className="text-red-500">*</span></label>
                <input value={form.phone} onChange={e => set('phone', e.target.value)}
                       placeholder="Ej: 3001234567"
                       className={cls('phone')} />
                {fieldErrors.phone
                  ? <p className="text-red-500 text-xs mt-1">{fieldErrors.phone}</p>
                  : <p className="text-gray-400 text-xs mt-1">10 dígitos, sin espacios ni guiones</p>}
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Correo <span className="text-gray-400">(opcional)</span></label>
                <input type="email" value={form.email} onChange={e => set('email', e.target.value)}
                       placeholder="Ej: paciente@correo.com"
                       className={cls('email')} />
                {fieldErrors.email && <p className="text-red-500 text-xs mt-1">{fieldErrors.email}</p>}
              </div>

              {/* Contraseña + confirmación */}
              <div>
                <label className="block text-xs text-gray-500 mb-1">Contraseña <span className="text-gray-400">(opcional)</span></label>
                <input type="password" value={form.password} onChange={e => set('password', e.target.value)}
                       placeholder="Mín. 8 caracteres"
                       className={cls('password')} />
                {fieldErrors.password && <p className="text-red-500 text-xs mt-1">{fieldErrors.password}</p>}
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Confirmar contraseña <span className="text-gray-400">(opcional)</span></label>
                <input type="password" value={form.confirmPassword} onChange={e => set('confirmPassword', e.target.value)}
                       placeholder="Repite la contraseña"
                       className={cls('confirmPassword')} />
                {fieldErrors.confirmPassword && <p className="text-red-500 text-xs mt-1">{fieldErrors.confirmPassword}</p>}
              </div>

              {/* Fecha de nacimiento */}
              <div className="col-span-2">
                <label className="block text-xs text-gray-500 mb-1">
                  Fecha de nacimiento <span className="text-red-500">*</span>
                  <span className="text-gray-400 ml-1">(DD / MM / AAAA)</span>
                </label>
                <div className="flex gap-2">
                  <input placeholder="DD" maxLength={2} value={form.birthDay}
                         onChange={e => set('birthDay', e.target.value.replace(/\D/g,''))}
                         className={`w-16 border rounded-xl px-2 py-2 text-sm text-center focus:outline-none focus:border-blue-500
                           ${fieldErrors.birthDate ? 'border-red-400 bg-red-50' : 'border-gray-200'}`} />
                  <input placeholder="MM" maxLength={2} value={form.birthMonth}
                         onChange={e => set('birthMonth', e.target.value.replace(/\D/g,''))}
                         className={`w-16 border rounded-xl px-2 py-2 text-sm text-center focus:outline-none focus:border-blue-500
                           ${fieldErrors.birthDate ? 'border-red-400 bg-red-50' : 'border-gray-200'}`} />
                  <input placeholder="AAAA" maxLength={4} value={form.birthYear}
                         onChange={e => set('birthYear', e.target.value.replace(/\D/g,''))}
                         className={`flex-1 border rounded-xl px-2 py-2 text-sm text-center focus:outline-none focus:border-blue-500
                           ${fieldErrors.birthDate ? 'border-red-400 bg-red-50' : 'border-gray-200'}`} />
                </div>
                {fieldErrors.birthDate && <p className="text-red-500 text-xs mt-1">{fieldErrors.birthDate}</p>}
              </div>

            </div>

            {globalError && (
                <div className="bg-red-50 border border-red-100 rounded-xl px-4 py-3">
                  <p className="text-sm text-red-600">{globalError}</p>
                </div>
            )}
          </div>

          <div className="px-6 py-4 border-t border-gray-100 flex gap-3 justify-end">
            <button onClick={onClose}
                    className="text-sm text-gray-500 border border-gray-200 rounded-xl px-5 py-2 hover:bg-gray-50 transition-colors">
              Cancelar
            </button>
            <button onClick={handleSubmit} disabled={saving}
                    className="text-sm bg-blue-600 text-white rounded-xl px-5 py-2 font-semibold
              hover:bg-blue-700 transition-colors disabled:opacity-40">
              {saving ? 'Registrando...' : 'Registrar paciente'}
            </button>
          </div>
        </div>
      </div>
  )
}

// ─────────────────────────────────────────────────────────────────────────────

// Tipos de servicio hardcodeados (igual que en ExportAppointmentsPage / DoctorAppointmentsPage)
const SERVICE_TYPES = ['Consulta General', 'Fisioterapia', 'Quiropraxia', 'Terapia Neural']

const SERVICE_TYPE_TO_ENUM = {
  'Consulta General': 'CONSULTA_GENERAL',
  'Fisioterapia':     'FISIOTERAPIA',
  'Quiropraxia':      'QUIROPRAXIA',
  'Terapia Neural':   'TERAPIA_NEURAL',
}

// Quiropraxia / Terapia Neural → doctores con esa especialidad
// Consulta General → todos los doctores
// Fisioterapia → doctores sin especialidad asignada
function filterDoctorsByService(service, allDocs) {
  if (!service) return []
  if (service === 'Quiropraxia')      return allDocs.filter(d => d.specialties?.includes('Quiropraxia'))
  if (service === 'Terapia Neural')   return allDocs.filter(d => d.specialties?.includes('Terapia Neural'))
  if (service === 'Consulta General') return allDocs
  return allDocs.filter(d => !d.specialties?.length)
}

export default function CreateAppointmentPage() {
  const navigate    = useNavigate()
  const { hasRole } = useAuth()

  const [allDoctors,          setAllDoctors]          = useState([])
  const [doctors,             setDoctors]             = useState([])
  const [availability,        setAvailability]        = useState([])
  const [scheduleDays,        setScheduleDays]        = useState(null)
  const [loadingSlots,        setLoadingSlots]        = useState(false)
  const [loading,             setLoading]             = useState(false)
  const [success,             setSuccess]             = useState(false)
  const [intervalMinutes,     setIntervalMinutes]     = useState(30)
  const [patientAppointments,  setPatientAppointments]  = useState([])
  const [patientAuthorization, setPatientAuthorization] = useState(null) // autorización médica activa del paciente
  const [showRegisterModal,    setShowRegisterModal]    = useState(false)

  // Búsqueda de paciente
  const [documentId,       setDocumentId]       = useState('')
  const [searchingPatient, setSearchingPatient] = useState(false)
  const [patient,          setPatient]          = useState(null)
  const [patientName,      setPatientName]      = useState('')
  const [patientError,     setPatientError]     = useState('')

  // Selecciones
  const [selectedServiceType, setselectedServiceType] = useState('')
  const [selectedDoctor,    setSelectedDoctor]    = useState('')
  const [selectedDate,      setSelectedDate]      = useState('')
  const [selectedTime,      setSelectedTime]      = useState('')
  const [refreshKey,        setRefreshKey]        = useState(0)

  const [form, setForm]     = useState({ reason: '', notes: '' })
  const [errors, setErrors] = useState({})

  const today = new Date()
  const [calYear,  setCalYear]  = useState(today.getFullYear())
  const [calMonth, setCalMonth] = useState(today.getMonth())

  // --- Cargar médicos ---
  useEffect(() => {
    medicalApi.listDoctors()
      .then(res => setAllDoctors(res.data || []))
      .catch(() => {})
  }, [])

  // --- Filtrar médicos por especialidad ---
  useEffect(() => {
    setSelectedDoctor('')
    setSelectedDate('')
    setSelectedTime('')
    setAvailability([])

    if (!selectedServiceType) { setDoctors([]); return }

    // Quiropraxia: filtrar además por médicos con horario definido
    if (selectedServiceType === 'Quiropraxia') {
      const matching = filterDoctorsByService('Quiropraxia', allDoctors)
      let cancelled = false
      Promise.all(matching.map(async d => {
        try {
          const res = await medicalApi.getDoctorSchedule(d.id)
          return (res.data && res.data.length > 0) ? d : null
        } catch { return null }
      })).then(list => { if (!cancelled) setDoctors(list.filter(Boolean)) })
         .catch(() => {})
      return () => { cancelled = true }
    }

    setDoctors(filterDoctorsByService(selectedServiceType, allDoctors))
  }, [selectedServiceType, allDoctors])

  // --- Cargar horario del médico ---
  useEffect(() => {
    if (!selectedDoctor) { setScheduleDays(null); return }
    medicalApi.getDoctorSchedule(selectedDoctor).then(res => {
      const s = res.data || []
      const jsdays = new Set(s.map(d => d.dayOfWeek % 7))
      setScheduleDays(jsdays)
      if (s.length > 0) setIntervalMinutes(s[0].intervalMinutes || 30)
    }).catch(() => setScheduleDays(null))
  }, [selectedDoctor])

  // --- Cargar slots disponibles (filtrados por citas ya agendadas) ---
  useEffect(() => {
    if (!selectedDoctor || !selectedDate) { setAvailability([]); setSelectedTime(''); return }
    setLoadingSlots(true)
    setSelectedTime('')
    Promise.all([
      medicalApi.getAvailability(selectedDoctor, selectedDate),
      appointmentApi.listByDoctorAndDate(selectedDoctor, selectedDate).catch(() => ({ data: [] })),
    ])
    .then(([slotsRes, aptsRes]) => {
      const allSlots    = slotsRes.data || []
      // Horarios ya ocupados: citas que no están canceladas
      const bookedTimes = new Set(
        (aptsRes.data || [])
          .filter(a => a.status !== 'CANCELADA')
          .map(a => (a.startTime || '').substring(0, 5))
      )
      setAvailability(allSlots.filter(s => !bookedTimes.has(s.substring(0, 5))))
    })
    .catch(() => setAvailability([]))
    .finally(() => setLoadingSlots(false))
  }, [selectedDoctor, selectedDate, refreshKey])

  // --- Buscar paciente ---
  const handleSearchPatient = async (overrideId) => {
    const id = String(overrideId || documentId || '')
    if (!id.trim()) { setPatientError('Ingresa el número de documento'); return }
    if (overrideId) setDocumentId(overrideId)
    setSearchingPatient(true)
    setPatient(null)
    setPatientName('')
    setPatientError('')
    setPatientAppointments([])
    setPatientAuthorization(null)
    try {
      const [patRes, idRes] = await Promise.all([
        patientApi.getById(id.trim()).catch(() => null),
        identityApi.getUserById(id.trim()).catch(() => null),
      ])
      if (!patRes && !idRes) throw new Error('No encontrado')
      setPatient(patRes?.data || null)
      setPatientName(idRes?.data?.fullName || patRes?.data?.fullName || `Paciente ${id}`)
      try {
        const appointmentsRes = await appointmentApi.listByPatient(id.trim())
        setPatientAppointments(appointmentsRes.data || [])
      } catch {
        setPatientAppointments([])
      }
      try {
        const authRes = await appointmentApi.getPatientAuthorization(parseInt(id.trim()))
        setPatientAuthorization(authRes?.status === 200 ? authRes.data : null)
      } catch {
        setPatientAuthorization(null)
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
    if (scheduleDays !== null && !scheduleDays.has(d.getDay())) return false
    return true
  }

  // Sin autorización médica activa → solo Consulta General.
  // Con autorización → Consulta General + el servicio autorizado.
  const ENUM_TO_SERVICE_NAME = {
    FISIOTERAPIA:   'Fisioterapia',
    QUIROPRAXIA:    'Quiropraxia',
    TERAPIA_NEURAL: 'Terapia Neural',
  }
  const getAvailableSpecialties = () => {
    if (patientAuthorization) {
      const authorizedName = ENUM_TO_SERVICE_NAME[patientAuthorization.serviceType]
      return authorizedName ? ['Consulta General', authorizedName] : ['Consulta General']
    }
    return ['Consulta General']
  }

  const hasActiveAppointment = () =>
      patientAppointments.some(apt => apt.status === 'AGENDADA')

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
    if (!selectedServiceType) e.specialty = 'Selecciona un tipo de servicio'
    if (!selectedDoctor)    e.doctorId  = 'Selecciona un profesional'
    if (!selectedDate)      e.date      = 'Selecciona una fecha'
    if (!selectedTime)      e.startTime = 'Selecciona una hora'
    if (!form.reason)       e.reason    = 'Obligatorio'
    if (hasActiveAppointment()) {
      e.general = 'Este paciente ya tiene una cita agendada. Debe esperar a que sea atendida o cancelada antes de agendar otra'
    }
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const newErrors = validate()
    if(Object.keys(newErrors).length > 0){ setErrors((newErrors)); return}
    const doctor = allDoctors.find(d => d.id === parseInt(selectedDoctor));
    const doctorName = doctor?.fullName;
    const serviceType = SERVICE_TYPE_TO_ENUM[selectedServiceType] || selectedServiceType.replace(' ', '_').toUpperCase()

    setLoading(true);
    try {
      await appointmentApi.create({
        patientId: parseInt(documentId),
        doctorId:  parseInt(selectedDoctor),
        doctorName: doctorName,
        serviceType: serviceType,
        date:      selectedDate,
        startTime: selectedTime,
        endTime:   addMinutes(selectedTime, intervalMinutes),
        reason:    form.reason,
        notes:     form.notes,
      })
      setSuccess(true)
      setRefreshKey(prev => prev + 1)       // refresca disponibilidad en caso de volver
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
                    <button type="button" onClick={() => handleSearchPatient()} disabled={searchingPatient}
                            className="bg-blue-600 text-white rounded-xl px-5 py-2.5 text-sm font-semibold
                      hover:bg-blue-700 transition-colors disabled:opacity-50 shrink-0">
                      {searchingPatient ? 'Buscando...' : 'Buscar'}
                    </button>
                  </div>

                  {/* Mensajes de error de búsqueda */}
                  {patientError && <p className="text-red-500 text-xs mt-2">{patientError}</p>}
                  {errors.patient && !patientError && <p className="text-red-500 text-xs mt-2">{errors.patient}</p>}

                  {/* Registrar nuevo paciente — siempre visible hasta encontrar uno */}
                  {!patient && (
                      <div className="mt-4 flex items-center justify-between gap-3 bg-blue-50 border border-blue-100 rounded-xl px-4 py-3">
                        <div>
                          <p className="text-sm font-medium text-gray-700">¿El paciente no está registrado?</p>
                          <p className="text-xs text-gray-500">Créalo aquí mismo para poder agendar su cita.</p>
                        </div>
                        <button type="button" onClick={() => setShowRegisterModal(true)}
                                className="bg-blue-600 text-white rounded-xl px-4 py-2 text-sm font-semibold
                          hover:bg-blue-700 transition-colors shrink-0 whitespace-nowrap">
                          + Registrar paciente nuevo
                        </button>
                      </div>
                  )}

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

                {/* Tipo de servicio */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                  <h2 className="font-semibold text-sm uppercase tracking-wider text-gray-500 mb-4">Tipo de Servicio</h2>
                  <label className="block text-sm text-gray-500 mb-1">
                    Tipo de servicio <span className="text-red-500">*</span>
                  </label>
                  <select value={selectedServiceType}
                          onChange={e => { setselectedServiceType(e.target.value); setErrors({...errors, specialty: ''}) }}
                          disabled={!patientName}
                          className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                    disabled:bg-gray-50 disabled:text-gray-400
                    ${errors.specialty ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                    <option value="">
                      {patientName ? 'Seleccionar tipo de servicio...' : 'Primero busca un paciente'}
                    </option>
                    {getAvailableSpecialties().map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                  {errors.specialty && <p className="text-red-500 text-xs mt-1">{errors.specialty}</p>}
                  {patientName && !patientAuthorization && (
                      <p className="text-blue-600 text-xs mt-1">
                        ℹ️ Sin autorización médica activa, solo puede agendar Consulta General
                      </p>
                  )}
                  {patientName && patientAuthorization && (
                      <p className="text-green-600 text-xs mt-1">
                        ✅ Autorizado para{' '}
                        {ENUM_TO_SERVICE_NAME[patientAuthorization.serviceType] || patientAuthorization.serviceType}
                      </p>
                  )}
                </div>

                {/* Profesional */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                  <h2 className="font-semibold text-sm uppercase tracking-wider text-gray-500 mb-4">Profesional</h2>

                  <div>
                    <label className="block text-sm text-gray-500 mb-1">
                      Profesional <span className="text-red-500">*</span>
                    </label>
                    <div className="flex gap-2">
                      <select value={selectedDoctor} disabled={!selectedServiceType}
                              onChange={e => { setSelectedDoctor(e.target.value); setSelectedDate(''); setSelectedTime(''); setErrors({...errors, doctorId: ''}) }}
                              className={`flex-1 border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                        disabled:bg-gray-50 disabled:text-gray-400
                        ${errors.doctorId ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                        <option value="">
                          {selectedServiceType ? 'Seleccionar profesional...' : 'Primero selecciona un tipo de servicio'}
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
                    {selectedServiceType === 'Quiropraxia' && doctors.length === 0 && (
                        <p className="text-orange-600 text-xs mt-1">
                          ⚠️ No hay especialistas en Quiropraxia disponibles (activos y con horario definido).
                        </p>
                    )}
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

        {/* Modal de registro de paciente nuevo */}
        {showRegisterModal && (
            <RegisterPatientModal
                documentId={documentId}
                onClose={() => setShowRegisterModal(false)}
                onSuccess={(newDocId) => {
                  setShowRegisterModal(false)
                  handleSearchPatient(newDocId)
                }}
            />
        )}

      </Layout>
  )
}