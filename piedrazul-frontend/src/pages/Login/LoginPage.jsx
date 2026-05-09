import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../api/AuthContext'
import { identityApi } from '../../api'

export default function LoginPage() {
  const navigate      = useNavigate()
  const { login }     = useAuth()

  const [form, setForm]       = useState({ username: '', password: '' })
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)
  const [showPass, setShowPass] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.username || !form.password) {
      setError('Todos los campos son obligatorios')
      return
    }
    setLoading(true)
    try {
      const response = await identityApi.login(form)
      login(response.data)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Credenciales incorrectas')
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
              <span className="text-white text-2xl">♥</span>
            </div>
            <h1 className="text-3xl font-bold text-gray-800">Piedrazul</h1>
            <p className="text-gray-500 mt-1">Citas Médicas</p>
          </div>

          {/* --- Panel --- */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">Iniciar sesión</h2>

            <form onSubmit={handleSubmit} className="space-y-5">

              {/* --- Correo --- */}
              <div>
                <label className="block text-sm text-gray-500 mb-1">
                  Correo o usuario <span className="text-red-500">*</span>
                </label>
                <input type="text" name="username" value={form.username}
                       onChange={handleChange} placeholder="usuario@gmail.com"
                       className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm
                  focus:outline-none focus:border-blue-500 transition-colors" />
              </div>

              {/* --- Contraseña con toggle --- */}
              <div>
                <label className="block text-sm text-gray-500 mb-1">
                  Contraseña <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                      type={showPass ? 'text' : 'password'}
                      name="password" value={form.password}
                      onChange={handleChange} placeholder="••••••••"
                      className="w-full border border-gray-200 rounded-xl px-4 py-3 pr-10 text-sm
                    focus:outline-none focus:border-blue-500 transition-colors"
                  />
                  <button type="button" onClick={() => setShowPass(!showPass)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400
                    hover:text-gray-600 transition-colors text-base select-none">
                    {showPass ? '🙈' : '👁'}
                  </button>
                </div>
              </div>

              {/* --- Recordarme / Olvidé contraseña --- */}
              <div className="flex items-center justify-between">
                <label className="flex items-center gap-2 text-sm text-gray-500 cursor-pointer">
                  <input type="checkbox" className="rounded" />
                  Recordarme
                </label>
                <button type="button" className="text-sm text-blue-600 hover:underline">
                  ¿Olvidaste tu contraseña?
                </button>
              </div>

              {error && <p className="text-red-500 text-sm">{error}</p>}

              <button type="submit" disabled={loading}
                      className="w-full bg-blue-600 text-white rounded-xl py-3 font-semibold text-sm
                hover:bg-blue-700 transition-colors disabled:opacity-50">
                {loading ? 'Iniciando sesión...' : 'Iniciar sesión'}
              </button>
            </form>
          </div>

          {/* --- Footer --- */}
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