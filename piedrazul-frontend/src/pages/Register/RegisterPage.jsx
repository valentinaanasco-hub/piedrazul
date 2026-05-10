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
    birthDate: '',
  })

  const [errors, setErrors]           = useState({})
  const [loading, setLoading]         = useState(false)
  const [success, setSuccess]         = useState(false)
  const [showPass, setShowPass]       = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '' })
  }

  const validate = () => {
    const e = {}
    const onlyLetters = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/

    if (!form.firstName.trim())
      e.firstName = 'El primer nombre es obligatorio'
    else if (!onlyLetters.test(form.firstName.trim()))
      e.firstName = 'Solo se permiten letras'

    if (!form.firstSurname.trim())
      e.firstSurname = 'El primer apellido es obligatorio'
    else if (!onlyLetters.test(form.firstSurname.trim()))
      e.firstSurname = 'Solo se permiten letras'

    if (form.middleName && !onlyLetters.test(form.middleName.trim()))
      e.middleName = 'Solo se permiten letras'

    if (form.lastName && !onlyLetters.test(form.lastName.trim()))
      e.lastName = 'Solo se permiten letras'

    if (!form.email.trim())
      e.email = 'El correo es obligatorio'
    else if (!/^[\w._%+\-]+@[\w.\-]+\.[a-zA-Z]{2,}$/.test(form.email))
      e.email = 'Formato de correo inválido'

    if (!form.phone.trim())
      e.phone = 'El teléfono es obligatorio'
    else if (!/^\d{7,10}$/.test(form.phone))
      e.phone = 'Debe tener entre 7 y 10 dígitos'

    if (!form.gender)
      e.gender = 'El género es obligatorio'

    if (!form.userTypeId)
      e.userTypeId = 'El tipo de documento es obligatorio'

    if (!form.documentId.trim())
      e.documentId = 'El número de documento es obligatorio'
    else if (!/^\d{6,15}$/.test(form.documentId))
      e.documentId = 'Debe tener entre 6 y 15 dígitos'

    if (!form.password)
      e.password = 'La contraseña es obligatoria'
    else if (form.password.length < 8)
      e.password = 'Mínimo 8 caracteres'
    else if (!/\d/.test(form.password))
      e.password = 'Debe contener al menos un número'

    if (!form.confirmPassword)
      e.confirmPassword = 'Confirma tu contraseña'
    else if (form.password !== form.confirmPassword)
      e.confirmPassword = 'Las contraseñas no coinciden'

    if (!form.birthDate) {
      e.birthDate = 'La fecha de nacimiento es obligatoria'
    } else {
      const birth = new Date(form.birthDate)
      const now   = new Date()
      const minAge = new Date(now.getFullYear() - 100, now.getMonth(), now.getDate())
      const maxAge = new Date(now.getFullYear() - 1,   now.getMonth(), now.getDate())
      if (birth > maxAge)
        e.birthDate = 'Debes tener al menos 1 año de edad'
      else if (birth < minAge)
        e.birthDate = 'Fecha de nacimiento inválida'
    }

    return e
  }

  const handleSubmit = async (ev) => {
    ev.preventDefault()
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) { setErrors(newErrors); return }

    // Separar la fecha en día, mes y año para el backend
    let birthDay = '', birthMonth = '', birthYear = ''
    if (form.birthDate) {
      const [y, m, d] = form.birthDate.split('-')
      birthDay   = d
      birthMonth = m
      birthYear  = y
    }

    setLoading(true)
    try {
      await patientApi.registerWeb({
        documentId:   parseInt(form.documentId),
        userTypeId:   form.userTypeId,
        firstName:    form.firstName.trim(),
        middleName:   form.middleName.trim(),
        firstSurname: form.firstSurname.trim(),
        lastName:     form.lastName.trim(),
        email:        form.email.trim(),
        password:     form.password,
        phone:        form.phone.trim(),
        gender:       form.gender,
        birthDay,
        birthMonth,
        birthYear,
      })
      setSuccess(true)
      setTimeout(() => navigate('/login'), 2500)
    } catch (err) {
      setErrors({ general: err.response?.data?.message || 'Error al registrarse. Intenta de nuevo.' })
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-10 text-center max-w-sm w-full mx-4">
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
            <p className="text-gray-500 text-sm mt-1">
              Regístrate para agendar tus citas médicas en Piedrazul
            </p>
          </div>

          {/* --- Panel --- */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
            <form onSubmit={handleSubmit} className="space-y-4">

              {/* --- Fila 1: Primer nombre | Segundo nombre --- */}
              <div className="grid grid-cols-2 gap-4">
                <Field label="Primer nombre" name="firstName" required
                       value={form.firstName} onChange={handleChange} error={errors.firstName} />
                <Field label="Segundo nombre" name="middleName"
                       value={form.middleName} onChange={handleChange} error={errors.middleName} />
              </div>

              {/* --- Fila 2: Primer apellido | Segundo apellido --- */}
              <div className="grid grid-cols-2 gap-4">
                <Field label="Primer apellido" name="firstSurname" required
                       value={form.firstSurname} onChange={handleChange} error={errors.firstSurname} />
                <Field label="Segundo apellido" name="lastName"
                       value={form.lastName} onChange={handleChange} error={errors.lastName} />
              </div>

              {/* --- Fila 3: Correo | Teléfono --- */}
              <div className="grid grid-cols-2 gap-4">
                <Field label="Correo electrónico" name="email" required type="email"
                       value={form.email} onChange={handleChange} error={errors.email} />
                <Field label="Teléfono" name="phone" required
                       value={form.phone} onChange={handleChange} error={errors.phone} />
              </div>

              {/* --- Fila 4: Fecha de nacimiento | Género --- */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-500 mb-1">
                    Fecha de nacimiento <span className="text-red-500">*</span>
                  </label>
                  <input
                      type="date"
                      name="birthDate"
                      value={form.birthDate}
                      onChange={handleChange}
                      max={new Date().toISOString().split('T')[0]}
                      className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
                    ${errors.birthDate ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}
                  />
                  {errors.birthDate && <p className="text-red-500 text-xs mt-1">{errors.birthDate}</p>}
                </div>

                <div>
                  <label className="block text-sm text-gray-500 mb-1">
                    Género <span className="text-red-500">*</span>
                  </label>
                  <select name="gender" value={form.gender} onChange={handleChange}
                          className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
                    ${errors.gender ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                    <option value="">Seleccionar...</option>
                    <option>Hombre</option>
                    <option>Mujer</option>
                    <option>Otro</option>
                  </select>
                  {errors.gender && <p className="text-red-500 text-xs mt-1">{errors.gender}</p>}
                </div>
              </div>

              {/* --- Fila 5: Tipo de documento | Número de documento --- */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-500 mb-1">
                    Tipo de documento <span className="text-red-500">*</span>
                  </label>
                  <select name="userTypeId" value={form.userTypeId} onChange={handleChange}
                          className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
                    ${errors.userTypeId ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}>
                    <option value="">Seleccionar...</option>
                    {DOCUMENT_TYPES.map(d => (
                        <option key={d.value} value={d.value}>{d.label}</option>
                    ))}
                  </select>
                  {errors.userTypeId && <p className="text-red-500 text-xs mt-1">{errors.userTypeId}</p>}
                </div>
                <Field label="Número de documento" name="documentId" required
                       value={form.documentId} onChange={handleChange} error={errors.documentId} />
              </div>

              {/* --- Fila 6: Contraseña | Confirmar contraseña --- */}
              <div className="grid grid-cols-2 gap-4">
                <PasswordField
                    label="Contraseña" name="password" required
                    value={form.password} onChange={handleChange}
                    error={errors.password}
                    show={showPass} onToggle={() => setShowPass(!showPass)}
                />
                <PasswordField
                    label="Confirmar contraseña" name="confirmPassword" required
                    value={form.confirmPassword} onChange={handleChange}
                    error={errors.confirmPassword}
                    show={showConfirm} onToggle={() => setShowConfirm(!showConfirm)}
                />
              </div>

              {errors.general && (
                  <p className="text-red-500 text-sm text-center bg-red-50 rounded-xl py-2 px-3">
                    {errors.general}
                  </p>
              )}

              <button type="submit" disabled={loading}
                      className="w-full bg-blue-600 text-white rounded-xl py-3 font-semibold text-sm
                hover:bg-blue-700 transition-colors disabled:opacity-50 mt-2">
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

// --- Campo de texto estándar ---
function Field({ label, name, value, onChange, error, required, type = 'text' }) {
  return (
      <div>
        <label className="block text-sm text-gray-500 mb-1">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
        <input type={type} name={name} value={value} onChange={onChange}
               className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
          ${error ? 'border-red-400 focus:border-red-500' : 'border-gray-200 focus:border-blue-500'}`} />
        {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
      </div>
  )
}

// --- Campo de contraseña con toggle ---
function PasswordField({ label, name, value, onChange, error, required, show, onToggle }) {
  return (
      <div>
        <label className="block text-sm text-gray-500 mb-1">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
        <div className="relative">
          <input
              type={show ? 'text' : 'password'}
              name={name} value={value} onChange={onChange}
              className={`w-full border rounded-xl px-4 py-3 pr-10 text-sm focus:outline-none transition-colors
            ${error ? 'border-red-400 focus:border-red-500' : 'border-gray-200 focus:border-blue-500'}`}
          />
          <button type="button" onClick={onToggle}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400
            hover:text-gray-600 transition-colors text-base select-none">
            {show ? '🙈' : '👁'}
          </button>
        </div>
        {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
      </div>
  )
}