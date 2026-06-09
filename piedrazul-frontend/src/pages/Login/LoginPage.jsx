import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../api/AuthContext'
import { keycloakApi } from '../../api'

export default function LoginPage() {
  const navigate    = useNavigate()
  const { login }   = useAuth()

  const [form, setForm]         = useState({ username: '', password: '' })
  const [errors, setErrors]     = useState({})
  const [loading, setLoading]   = useState(false)
  const [showPass, setShowPass] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrors({ ...errors, [e.target.name]: '', general: '' })
  }

  const validate = () => {
    const e = {}
    if (!form.username.trim()) e.username = 'El usuario es obligatorio'
    if (!form.password)        e.password = 'La contraseña es obligatoria'
    return e
  }

  const handleSubmit = async (ev) => {
    ev.preventDefault()
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) { setErrors(newErrors); return }

    setLoading(true)
    try {
      const response = await keycloakApi.login(
        form.username.trim(),
        form.password
      )
      const { access_token, refresh_token } = response.data
      login(access_token, refresh_token)

      // Redirige según rol (extraído del JWT en AuthContext)
      const allRoles = (() => {
        try {
          const payload = JSON.parse(atob(access_token.split('.')[1]))
          const realmRoles = payload.realm_access?.roles ?? payload.roles ?? []
          return realmRoles.map(r => r.toUpperCase())
        } catch { return [] }
      })()

      if (allRoles.includes('PACIENTE')) {
        navigate('/patient/schedule')
      } else if (allRoles.includes('DOCTOR')) {
        navigate('/doctor/appointments')
      } else {
        navigate('/dashboard')
      }
    } catch (err) {
      const status = err.response?.status
      if (status === 401) {
        setErrors({ general: 'Credenciales incorrectas' })
      } else {
        setErrors({ general: err.response?.data?.error_description || 'Error al iniciar sesión' })
      }
    } finally {
      setLoading(false)
    }
  }

  return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4">
        <div className="w-full max-w-md">

          {/* --- Header --- */}
          <div className="text-center mb-8">
            <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="white" stroke="none">
                <path d="M12 21C12 21 3 13.5 3 8a5 5 0 0110 0 5 5 0 0110 0c0 5.5-9 13-9 13z"/>
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-gray-800">Piedrazul</h1>
            <p className="text-gray-500 mt-1">Citas Médicas</p>
          </div>

          {/* --- Panel --- */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Iniciar sesión</h2>

            <form onSubmit={handleSubmit} className="space-y-5">

              <div>
                <label className="block text-sm text-gray-500 mb-1">
                  Correo o usuario <span className="text-red-500">*</span>
                </label>
                <input type="text" name="username" value={form.username}
                       onChange={handleChange} placeholder="usuario@gmail.com"
                       className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
                  ${errors.username ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}
                />
                {errors.username && <p className="text-red-500 text-xs mt-1">{errors.username}</p>}
              </div>

              <div>
                <label className="block text-sm text-gray-500 mb-1">
                  Contraseña <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                      type={showPass ? 'text' : 'password'}
                      name="password" value={form.password}
                      onChange={handleChange} placeholder="••••••••"
                      className={`w-full border rounded-xl px-4 py-3 pr-10 text-sm focus:outline-none transition-colors
                    ${errors.password ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}
                  />
                  <button type="button" onClick={() => setShowPass(!showPass)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400
                    hover:text-gray-600 transition-colors select-none">
                    {showPass
                      ? <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                      : <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    }
                  </button>
                </div>
                {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
              </div>

              <label className="flex items-center gap-2 text-sm text-gray-500 cursor-pointer">
                <input type="checkbox" className="rounded" />
                Recordarme
              </label>

              {errors.general && (
                  <p className="text-red-500 text-sm bg-red-50 rounded-xl py-2 px-3">
                    {errors.general}
                  </p>
              )}

              <button type="submit" disabled={loading}
                      className="w-full bg-blue-600 text-white rounded-xl py-3 font-semibold text-sm
                hover:bg-blue-700 transition-colors disabled:opacity-50">
                {loading ? 'Iniciando sesión...' : 'Iniciar sesión'}
              </button>
            </form>
          </div>

          <p className="text-center text-gray-500 text-sm mt-6">
            ¿No tienes cuenta?{' '}
            <Link to="/register" className="text-blue-600 font-semibold hover:underline">
              Regístrate
            </Link>
          </p>
        </div>
      </div>
  )
}