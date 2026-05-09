import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import { medicalApi, appointmentApi } from '../../api'

const DOCUMENT_TYPES = ['CC', 'TI', 'CE', 'PA', 'RC']

export default function CreateAppointmentPage() {
  const navigate = useNavigate()

  const [doctors, setDoctors] = useState([])
  const [availability, setAvailability] = useState([])
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)

  const [form, setForm] = useState({
    // Datos del paciente
    documentId: '',
    userTypeId: '',
    firstName: '',
    middleName: '',
    firstSurname: '',
    lastName: '',
    phone: '',
    gender: '',
    email: '',
    birthDay: '',
    birthMonth: '',
    birthYear: '',
    // Datos de la cita
    doctorId: '',
    date: '',
    startTime: '',
    reason: '',
  })

  const [errors, setErrors] = useState({})

  useEffect(() => {
    medicalApi.listDoctors()
      .then(res => setDoctors(res.data))
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (form.doctorId && form.date) {
      medicalApi.getAvailability(form.doctorId, form.date)
        .then(res => setAvailability(res.data))
        .catch(() => setAvailability([]))
    }
  }, [form.doctorId, form.date])

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
  }

  const validate = () => {
    const e = {}
    if (!form.documentId) e.documentId = 'Obligatorio'
    if (!form.userTypeId) e.userTypeId = 'Obligatorio'
    if (!form.firstName) e.firstName = 'Obligatorio'
    if (!form.firstSurname) e.firstSurname = 'Obligatorio'
    if (!form.phone) e.phone = 'Obligatorio'
    if (!form.gender) e.gender = 'Obligatorio'
    if (!form.doctorId) e.doctorId = 'Obligatorio'
    if (!form.date) e.date = 'Obligatorio'
    if (!form.startTime) e.startTime = 'Obligatorio'
    return e
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    setLoading(true)
    try {
      await appointmentApi.create({
        ...form,
        documentId: parseInt(form.documentId),
      })
      setSuccess(true)
      setTimeout(() => navigate('/appointments'), 2000)
    } catch (err) {
      setErrors({ general: err.response?.data?.message || 'Error al crear la cita' })
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

  return (
    <Layout>
      <div className="max-w-3xl mx-auto">

        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Registrar Cita</h1>
          <p className="text-gray-500 text-sm mt-1">Ingresa los datos del paciente y la cita</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">

          {/* --- Datos del paciente --- */}
          <div className="bg-white rounded-2xl border border-gray-100 p-6">
            <h2 className="font-semibold text-gray-800 mb-4">Datos del Paciente</h2>

            <div className="grid grid-cols-2 gap-4">
              <Field label="Tipo de documento" name="userTypeId" required error={errors.userTypeId}>
                <select name="userTypeId" value={form.userTypeId} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500">
                  <option value="">Seleccionar...</option>
                  {DOCUMENT_TYPES.map(t => <option key={t}>{t}</option>)}
                </select>
              </Field>
              <SimpleField label="Número de documento" name="documentId" required value={form.documentId} onChange={handleChange} error={errors.documentId} />
            </div>

            <div className="grid grid-cols-2 gap-4 mt-4">
              <SimpleField label="Primer nombre" name="firstName" required value={form.firstName} onChange={handleChange} error={errors.firstName} />
              <SimpleField label="Apellido" name="firstSurname" required value={form.firstSurname} onChange={handleChange} error={errors.firstSurname} />
            </div>

            <div className="grid grid-cols-2 gap-4 mt-4">
              <SimpleField label="Segundo nombre" name="middleName" value={form.middleName} onChange={handleChange} />
              <SimpleField label="Segundo apellido" name="lastName" value={form.lastName} onChange={handleChange} />
            </div>

            <div className="grid grid-cols-2 gap-4 mt-4">
              <SimpleField label="Celular" name="phone" required value={form.phone} onChange={handleChange} error={errors.phone} />
              <Field label="Género" name="gender" required error={errors.gender}>
                <select name="gender" value={form.gender} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500">
                  <option value="">Seleccionar...</option>
                  <option>Hombre</option>
                  <option>Mujer</option>
                  <option>Otro</option>
                </select>
              </Field>
            </div>

            <div className="grid grid-cols-2 gap-4 mt-4">
              <SimpleField label="Correo (opcional)" name="email" type="email" value={form.email} onChange={handleChange} />
              <div>
                <label className="block text-sm text-gray-500 mb-1">Fecha de nacimiento (opcional)</label>
                <div className="grid grid-cols-3 gap-1">
                  <input name="birthDay" placeholder="DD" maxLength={2} value={form.birthDay} onChange={handleChange}
                    className="border border-gray-200 rounded-xl px-2 py-2.5 text-sm text-center focus:outline-none focus:border-blue-500" />
                  <input name="birthMonth" placeholder="MM" maxLength={2} value={form.birthMonth} onChange={handleChange}
                    className="border border-gray-200 rounded-xl px-2 py-2.5 text-sm text-center focus:outline-none focus:border-blue-500" />
                  <input name="birthYear" placeholder="AAAA" maxLength={4} value={form.birthYear} onChange={handleChange}
                    className="border border-gray-200 rounded-xl px-2 py-2.5 text-sm text-center focus:outline-none focus:border-blue-500" />
                </div>
              </div>
            </div>
          </div>

          {/* --- Datos de la cita --- */}
          <div className="bg-white rounded-2xl border border-gray-100 p-6">
            <h2 className="font-semibold text-gray-800 mb-4">Datos de la Cita</h2>

            <div className="grid grid-cols-2 gap-4">
              <Field label="Médico / Terapista" name="doctorId" required error={errors.doctorId}>
                <select name="doctorId" value={form.doctorId} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-blue-500">
                  <option value="">Seleccionar...</option>
                  {doctors.map(d => (
                    <option key={d.id} value={d.id}>{d.fullName}</option>
                  ))}
                </select>
              </Field>
              <SimpleField label="Fecha" name="date" required type="date" value={form.date} onChange={handleChange} error={errors.date} />
            </div>

            <div className="mt-4">
              <label className="block text-sm text-gray-500 mb-2">
                Hora disponible <span className="text-red-500">*</span>
              </label>
              {availability.length === 0 ? (
                <p className="text-gray-400 text-sm">
                  {form.doctorId && form.date ? 'No hay franjas disponibles' : 'Selecciona un médico y una fecha'}
                </p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {availability.map(slot => (
                    <button key={slot} type="button"
                      onClick={() => setForm({ ...form, startTime: slot })}
                      className={`px-4 py-2 rounded-xl text-sm font-medium border transition-colors
                        ${form.startTime === slot
                          ? 'bg-blue-600 text-white border-blue-600'
                          : 'bg-white text-gray-600 border-gray-200 hover:border-blue-400'
                        }`}>
                      {slot}
                    </button>
                  ))}
                </div>
              )}
              {errors.startTime && <p className="text-red-500 text-xs mt-1">{errors.startTime}</p>}
            </div>

            <div className="mt-4">
              <SimpleField label="Motivo de la cita" name="reason" value={form.reason} onChange={handleChange} />
            </div>
          </div>

          {errors.general && (
            <p className="text-red-500 text-sm text-center">{errors.general}</p>
          )}

          <div className="flex gap-3">
            <button type="button" onClick={() => navigate('/appointments')}
              className="flex-1 border border-gray-200 text-gray-600 rounded-xl py-3 text-sm font-semibold hover:bg-gray-50 transition-colors">
              Cancelar
            </button>
            <button type="submit" disabled={loading}
              className="flex-1 bg-blue-600 text-white rounded-xl py-3 text-sm font-semibold hover:bg-blue-700 transition-colors disabled:opacity-50">
              {loading ? 'Registrando...' : 'Registrar Cita'}
            </button>
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

function Field({ label, name, required, error, children }) {
  return (
    <div>
      <label className="block text-sm text-gray-500 mb-1">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
      {children}
      {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
    </div>
  )
}
