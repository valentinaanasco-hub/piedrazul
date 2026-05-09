import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi } from '../../api'

const DOCUMENT_TYPES = ['CC', 'TI', 'CE', 'PA', 'RC']
const DAYS = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb']
const MONTHS = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
  'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']

export default function CreateAppointmentPage() {
  const navigate = useNavigate()

  const [doctors, setDoctors]           = useState([])
  const [availability, setAvailability] = useState([])
  const [loading, setLoading]           = useState(false)
  const [success, setSuccess]           = useState(false)

  // Calendario
  const today    = new Date()
  const [calYear,  setCalYear]  = useState(today.getFullYear())
  const [calMonth, setCalMonth] = useState(today.getMonth())
  const [selectedDate, setSelectedDate] = useState('')

  const [form, setForm] = useState({
    documentId: '', userTypeId: '', firstName: '', middleName: '',
    firstSurname: '', lastName: '', phone: '', gender: '',
    email: '', birthDay: '', birthMonth: '', birthYear: '',
    doctorId: '', startTime: '', reason: '', notes: '',
  })
  const [errors, setErrors] = useState({})

  useEffect(() => {
    medicalApi.listDoctors()
        .then(res => setDoctors(res.data))
        .catch(() => {})
  }, [])

  useEffect(() => {
    if (form.doctorId && selectedDate) {
      medicalApi.getAvailability(form.doctorId, selectedDate)
          .then(res => setAvailability(res.data))
          .catch(() => setAvailability([]))
    } else {
      setAvailability([])
    }
  }, [form.doctorId, selectedDate])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
  }

  const handleDateSelect = (dateStr) => {
    setSelectedDate(dateStr)
    setForm({ ...form, startTime: '' })
  }

  // Construir días del mes para el calendario
  const buildCalendar = () => {
    const firstDay = new Date(calYear, calMonth, 1).getDay()
    const daysInMonth = new Date(calYear, calMonth + 1, 0).getDate()
    const cells = []
    for (let i = 0; i < firstDay; i++) cells.push(null)
    for (let d = 1; d <= daysInMonth; d++) cells.push(d)
    return cells
  }

  const prevMonth = () => {
    if (calMonth === 0) { setCalYear(calYear - 1); setCalMonth(11) }
    else setCalMonth(calMonth - 1)
  }

  const nextMonth = () => {
    if (calMonth === 11) { setCalYear(calYear + 1); setCalMonth(0) }
    else setCalMonth(calMonth + 1)
  }

  const isDateAvailable = (day) => {
    if (!day) return false
    const d = new Date(calYear, calMonth, day)
    return d >= today
  }

  const formatDate = (day) =>
      `${calYear}-${String(calMonth + 1).padStart(2,'0')}-${String(day).padStart(2,'0')}`

  const validate = () => {
    const e = {}
    if (!form.documentId)   e.documentId   = 'Obligatorio'
    if (!form.userTypeId)   e.userTypeId   = 'Obligatorio'
    if (!form.firstName)    e.firstName    = 'Obligatorio'
    if (!form.firstSurname) e.firstSurname = 'Obligatorio'
    if (!form.phone)        e.phone        = 'Obligatorio'
    if (!form.gender)       e.gender       = 'Obligatorio'
    if (!form.doctorId)     e.doctorId     = 'Obligatorio'
    if (!selectedDate)      e.date         = 'Selecciona una fecha'
    if (!form.startTime)    e.startTime    = 'Selecciona una hora'
    if (!form.reason)       e.reason       = 'Obligatorio'
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) { setErrors(newErrors); return }

    setLoading(true)
    try {
      await appointmentApi.create({
        ...form,
        date: selectedDate,
        documentId: parseInt(form.documentId),
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
                <span className="text-green-500 text-3xl">✓</span>
              </div>
              <h2 className="text-xl font-bold text-gray-800">¡Cita Registrada!</h2>
              <p className="text-gray-500 text-sm mt-2">Redirigiendo...</p>
            </div>
          </div>
        </Layout>
    )
  }

  const calCells = buildCalendar()

  return (
      <Layout>
        <div className="max-w-6xl mx-auto">

          {/* --- Breadcrumb + título --- */}
          <div className="mb-6">
            <p className="text-sm text-gray-400 mb-1">Administración / Registrar Cita</p>
            <h1 className="text-2xl font-bold text-gray-800">Registrar Cita Manual</h1>
            <p className="text-gray-500 text-sm mt-1">Registra una nueva cita médica para un paciente</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="flex gap-6">

              {/* --- Columna izquierda: formulario --- */}
              <div className="flex-1 space-y-5">
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                  <h2 className="font-semibold text-gray-800 mb-4 text-sm uppercase tracking-wider text-gray-500">
                    Datos de la cita
                  </h2>

                  {/* Paciente y Profesional */}
                  <div className="grid grid-cols-2 gap-4 mb-4">
                    <SimpleField label="Número de documento" name="documentId" required
                                 value={form.documentId} onChange={handleChange} error={errors.documentId} />
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">
                        Profesional <span className="text-red-500">*</span>
                      </label>
                      <select name="doctorId" value={form.doctorId} onChange={handleChange}
                              className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                        ${errors.doctorId ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                        <option value="">Seleccionar profesional...</option>
                        {doctors.map(d => (
                            <option key={d.id} value={d.id}>{d.fullName}</option>
                        ))}
                      </select>
                      {errors.doctorId && <p className="text-red-500 text-xs mt-1">{errors.doctorId}</p>}
                    </div>
                  </div>

                  {/* Hora seleccionada */}
                  <div className="grid grid-cols-2 gap-4 mb-4">
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">
                        Fecha <span className="text-red-500">*</span>
                      </label>
                      <div className={`w-full border rounded-xl px-4 py-2.5 text-sm bg-gray-50
                      ${errors.date ? 'border-red-400' : 'border-gray-200'}`}>
                        {selectedDate || <span className="text-gray-400">Selecciona en el calendario →</span>}
                      </div>
                      {errors.date && <p className="text-red-500 text-xs mt-1">{errors.date}</p>}
                    </div>
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">
                        Hora <span className="text-red-500">*</span>
                      </label>
                      <select name="startTime" value={form.startTime}
                              onChange={e => { setForm({...form, startTime: e.target.value}); setErrors({...errors, startTime: ''}) }}
                              className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                        ${errors.startTime ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                        <option value="">--:--</option>
                        {availability.map(slot => (
                            <option key={slot} value={slot}>{slot}</option>
                        ))}
                      </select>
                      {errors.startTime && <p className="text-red-500 text-xs mt-1">{errors.startTime}</p>}
                      {availability.length > 0 && (
                          <p className="text-gray-400 text-xs mt-1">{availability.length} horarios disponibles</p>
                      )}
                    </div>
                  </div>

                  {/* Motivo y notas */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">
                        Motivo de consulta <span className="text-red-500">*</span>
                      </label>
                      <textarea name="reason" value={form.reason} onChange={handleChange} rows={3}
                                placeholder="Describe el motivo de la consulta..."
                                className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none
                        resize-none transition-colors
                        ${errors.reason ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`} />
                      {errors.reason && <p className="text-red-500 text-xs mt-1">{errors.reason}</p>}
                    </div>
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">Notas adicionales</label>
                      <textarea name="notes" value={form.notes} onChange={handleChange} rows={3}
                                placeholder="Información adicional (opcional)"
                                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                        focus:outline-none focus:border-blue-500 resize-none transition-colors" />
                    </div>
                  </div>
                </div>

                {/* --- Datos del paciente --- */}
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                  <h2 className="font-semibold text-sm uppercase tracking-wider text-gray-500 mb-4">
                    Datos del Paciente
                  </h2>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">
                        Tipo de documento <span className="text-red-500">*</span>
                      </label>
                      <select name="userTypeId" value={form.userTypeId} onChange={handleChange}
                              className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                        ${errors.userTypeId ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                        <option value="">Seleccionar...</option>
                        {DOCUMENT_TYPES.map(t => <option key={t}>{t}</option>)}
                      </select>
                      {errors.userTypeId && <p className="text-red-500 text-xs mt-1">{errors.userTypeId}</p>}
                    </div>
                    <SimpleField label="Primer nombre" name="firstName" required
                                 value={form.firstName} onChange={handleChange} error={errors.firstName} />
                  </div>

                  <div className="grid grid-cols-2 gap-4 mt-4">
                    <SimpleField label="Segundo nombre" name="middleName"
                                 value={form.middleName} onChange={handleChange} />
                    <SimpleField label="Primer apellido" name="firstSurname" required
                                 value={form.firstSurname} onChange={handleChange} error={errors.firstSurname} />
                  </div>

                  <div className="grid grid-cols-2 gap-4 mt-4">
                    <SimpleField label="Segundo apellido" name="lastName"
                                 value={form.lastName} onChange={handleChange} />
                    <SimpleField label="Celular" name="phone" required
                                 value={form.phone} onChange={handleChange} error={errors.phone} />
                  </div>

                  <div className="grid grid-cols-2 gap-4 mt-4">
                    <div>
                      <label className="block text-sm text-gray-500 mb-1">
                        Género <span className="text-red-500">*</span>
                      </label>
                      <select name="gender" value={form.gender} onChange={handleChange}
                              className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
                        ${errors.gender ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                        <option value="">Seleccionar...</option>
                        <option>Hombre</option>
                        <option>Mujer</option>
                        <option>Otro</option>
                      </select>
                      {errors.gender && <p className="text-red-500 text-xs mt-1">{errors.gender}</p>}
                    </div>
                    <SimpleField label="Correo (opcional)" name="email" type="email"
                                 value={form.email} onChange={handleChange} />
                  </div>

                  <div className="mt-4">
                    <label className="block text-sm text-gray-500 mb-1">Fecha de nacimiento (opcional)</label>
                    <div className="grid grid-cols-3 gap-2">
                      <input name="birthDay" placeholder="DD" maxLength={2}
                             value={form.birthDay} onChange={handleChange}
                             className="border border-gray-200 rounded-xl px-3 py-2.5 text-sm text-center
                        focus:outline-none focus:border-blue-500" />
                      <input name="birthMonth" placeholder="MM" maxLength={2}
                             value={form.birthMonth} onChange={handleChange}
                             className="border border-gray-200 rounded-xl px-3 py-2.5 text-sm text-center
                        focus:outline-none focus:border-blue-500" />
                      <input name="birthYear" placeholder="AAAA" maxLength={4}
                             value={form.birthYear} onChange={handleChange}
                             className="border border-gray-200 rounded-xl px-3 py-2.5 text-sm text-center
                        focus:outline-none focus:border-blue-500" />
                    </div>
                  </div>
                </div>

                {errors.general && (
                    <p className="text-red-500 text-sm bg-red-50 rounded-xl py-2 px-4">
                      {errors.general}
                    </p>
                )}

                <button type="submit" disabled={loading}
                        className="w-full bg-blue-600 text-white rounded-xl py-3 font-semibold text-sm
                  hover:bg-blue-700 transition-colors disabled:opacity-50">
                  {loading ? 'Registrando...' : 'Registrar Cita'}
                </button>
              </div>

              {/* --- Columna derecha: calendario de disponibilidad --- */}
              <div className="w-72 shrink-0">
                <div className="bg-white rounded-2xl border border-gray-100 p-5 sticky top-6">
                  <h3 className="font-semibold text-gray-800 mb-4 text-sm">Vista de disponibilidad</h3>

                  {/* Navegación del mes */}
                  <div className="flex items-center justify-between mb-3">
                    <button type="button" onClick={prevMonth}
                            className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500">
                      ‹
                    </button>
                    <p className="text-sm font-semibold text-gray-800">
                      {MONTHS[calMonth]} {calYear}
                    </p>
                    <button type="button" onClick={nextMonth}
                            className="w-7 h-7 flex items-center justify-center rounded-lg hover:bg-gray-100 text-gray-500">
                      ›
                    </button>
                  </div>

                  {/* Encabezado días */}
                  <div className="grid grid-cols-7 mb-1">
                    {DAYS.map(d => (
                        <div key={d} className="text-center text-xs text-gray-400 font-medium py-1">
                          {d}
                        </div>
                    ))}
                  </div>

                  {/* Días */}
                  <div className="grid grid-cols-7 gap-0.5">
                    {calCells.map((day, idx) => {
                      const dateStr  = day ? formatDate(day) : ''
                      const isSelected = dateStr === selectedDate
                      const available  = isDateAvailable(day)

                      return (
                          <button
                              key={idx} type="button"
                              disabled={!available}
                              onClick={() => available && handleDateSelect(dateStr)}
                              className={`h-8 w-8 mx-auto rounded-full text-xs flex items-center justify-center transition-colors
                          ${!day ? 'invisible' : ''}
                          ${isSelected ? 'bg-blue-600 text-white font-semibold' : ''}
                          ${available && !isSelected ? 'hover:bg-blue-50 text-gray-700 cursor-pointer' : ''}
                          ${!available && day ? 'text-gray-300 cursor-not-allowed' : ''}
                        `}>
                            {day}
                            {available && !isSelected && (
                                <span className="absolute mt-5 w-1 h-1 bg-blue-400 rounded-full" />
                            )}
                          </button>
                      )
                    })}
                  </div>

                  {/* Leyenda */}
                  <div className="flex items-center gap-4 mt-4 pt-4 border-t border-gray-50">
                    <div className="flex items-center gap-1.5">
                      <div className="w-2.5 h-2.5 rounded-full bg-blue-400" />
                      <span className="text-xs text-gray-500">Con disponibilidad</span>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <div className="w-2.5 h-2.5 rounded-full bg-gray-200" />
                      <span className="text-xs text-gray-500">Sin disponibilidad</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </div>
      </Layout>
  )
}

function SimpleField({ label, name, value, onChange, error, required, type = 'text' }) {
  return (
      <div>
        <label className="block text-sm text-gray-500 mb-1">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
        <input type={type} name={name} value={value} onChange={onChange}
               className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none transition-colors
          ${error ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`} />
        {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
      </div>
  )
}