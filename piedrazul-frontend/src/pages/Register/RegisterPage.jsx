import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { patientApi } from '../../api'

const DOCUMENT_TYPES = [
  { value: 'CC', label: 'Cédula de ciudadanía' },
  { value: 'TI', label: 'Tarjeta de identidad' },
  { value: 'CE', label: 'Cédula de extranjería' },
  { value: 'PA', label: 'Pasaporte' },
  { value: 'RC', label: 'Registro civil' },
]

export default function RegisterPage() {
  const navigate = useNavigate()

  const [form, setForm] = useState({
    documentId: '',
    userTypeId: '',
    firstName: '',
    middleName: '',
    firstSurname: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phone: '',
    gender: '',
    birthDay: '',
    birthMonth: '',
    birthYear: '',
  })

  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
  }

  const validate = () => {
    const newErrors = {}
    if (!form.documentId) newErrors.documentId = 'Obligatorio'
    if (!form.userTypeId) newErrors.userTypeId = 'Obligatorio'
    if (!form.firstName) newErrors.firstName = 'Obligatorio'
    if (!form.firstSurname) newErrors.firstSurname = 'Obligatorio'
    if (!form.email) newErrors.email = 'Obligatorio'
    else if (!/^[\w._%+\-]+@[\w.\-]+\.[a-zA-Z]{2,}$/.test(form.email))
      newErrors.email = 'Formato inválido'
    if (!form.password) newErrors.password = 'Obligatorio'
    else if (form.password.length < 8) newErrors.password = 'Mínimo 8 caracteres'
    if (form.password !== form.confirmPassword)
      newErrors.confirmPassword = 'No coinciden'
    if (!form.phone) newErrors.phone = 'Obligatorio'
    else if (!/^\d{7,10}$/.test(form.phone)) newErrors.phone = '7 a 10 dígitos'
    if (!form.gender) newErrors.gender = 'Obligatorio'
    return newErrors
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
      await patientApi.registerWeb({
        ...form,
        documentId: parseInt(form.documentId),
      })
      setSuccess(true)
      setTimeout(() => navigate('/login'), 2500)
    } catch (err) {
      setErrors({ general: err.response?.data?.message || 'Error al registrarse' })
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-10 text-center max-w-sm">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-green-500 text-3xl">✓</span>
          </div>
          <h2 className="text-xl font-bold text-gray-800">¡Cuenta Creada Exitosamente!</h2>
          <p className="text-gray-500 text-sm mt-2">Redirigiendo al login...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-lg">

        {/* --- Header --- */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <span className="text-white text-2xl">♥</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-800">Crear cuenta</h1>
          <p className="text-gray-500 text-sm mt-1">Regístrate para agendar tus citas médicas en Piedrazul</p>
        </div>

        {/* --- Panel --- */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <form onSubmit={handleSubmit} className="space-y-4">

            {/* --- Nombres --- */}
            <div className="grid grid-cols-2 gap-4">
              <Field label="Primer nombre" name="firstName" required value={form.firstName} onChange={handleChange} error={errors.firstName} />
              <Field label="Apellido" name="firstSurname" required value={form.firstSurname} onChange={handleChange} error={errors.firstSurname} />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Field label="Segundo nombre" name="middleName" value={form.middleName} onChange={handleChange} />
              <Field label="Segundo apellido" name="lastName" value={form.lastName} onChange={handleChange} />
            </div>

            {/* --- Contacto --- */}
            <div className="grid grid-cols-2 gap-4">
              <Field label="Correo electrónico" name="email" required type="email" value={form.email} onChange={handleChange} error={errors.email} />
              <Field label="Teléfono" name="phone" required value={form.phone} onChange={handleChange} error={errors.phone} />
            </div>

            {/* --- Fecha y género --- */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-gray-500 mb-1">
                  Fecha de nacimiento
                </label>
                <div className="grid grid-cols-3 gap-1">
                  <input name="birthDay" placeholder="DD" maxLength={2} value={form.birthDay} onChange={handleChange}
                    className="border border-gray-200 rounded-xl px-2 py-3 text-sm text-center focus:outline-none focus:border-blue-500" />
                  <input name="birthMonth" placeholder="MM" maxLength={2} value={form.birthMonth} onChange={handleChange}
                    className="border border-gray-200 rounded-xl px-2 py-3 text-sm text-center focus:outline-none focus:border-blue-500" />
                  <input name="birthYear" placeholder="AAAA" maxLength={4} value={form.birthYear} onChange={handleChange}
                    className="border border-gray-200 rounded-xl px-2 py-3 text-sm text-center focus:outline-none focus:border-blue-500" />
                </div>
              </div>

              <div>
                <label className="block text-sm text-gray-500 mb-1">
                  Género <span className="text-red-500">*</span>
                </label>
                <select name="gender" value={form.gender} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-blue-500">
                  <option value="">Seleccionar...</option>
                  <option>Hombre</option>
                  <option>Mujer</option>
                  <option>Otro</option>
                </select>
                {errors.gender && <p className="text-red-500 text-xs mt-1">{errors.gender}</p>}
              </div>
            </div>

            {/* --- Documento --- */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm text-gray-500 mb-1">
                  Tipo de documento <span className="text-red-500">*</span>
                </label>
                <select name="userTypeId" value={form.userTypeId} onChange={handleChange}
                  className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-blue-500">
                  <option value="">Seleccionar...</option>
                  {DOCUMENT_TYPES.map(d => (
                    <option key={d.value} value={d.value}>{d.label}</option>
                  ))}
                </select>
                {errors.userTypeId && <p className="text-red-500 text-xs mt-1">{errors.userTypeId}</p>}
              </div>
              <Field label="Número de documento" name="documentId" required value={form.documentId} onChange={handleChange} error={errors.documentId} />
            </div>

            {/* --- Contraseña --- */}
            <div className="grid grid-cols-2 gap-4">
              <Field label="Contraseña" name="password" required type="password" value={form.password} onChange={handleChange} error={errors.password} />
              <Field label="Confirmar contraseña" name="confirmPassword" required type="password" value={form.confirmPassword} onChange={handleChange} error={errors.confirmPassword} />
            </div>

            {errors.general && (
              <p className="text-red-500 text-sm text-center">{errors.general}</p>
            )}

            <button type="submit" disabled={loading}
              className="w-full bg-blue-600 text-white rounded-xl py-3 font-semibold text-sm hover:bg-blue-700 transition-colors disabled:opacity-50 mt-2">
              {loading ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>
          </form>
        </div>

        <p className="text-center text-gray-500 text-sm mt-6">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="text-blue-600 font-semibold hover:underline">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}

// --- Componente reutilizable de campo ---
function Field({ label, name, value, onChange, error, required, type = 'text' }) {
  return (
    <div>
      <label className="block text-sm text-gray-500 mb-1">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
      <input
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
          ${error ? 'border-red-400 focus:border-red-500' : 'border-gray-200 focus:border-blue-500'}`}
      />
      {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
    </div>
  )
}
