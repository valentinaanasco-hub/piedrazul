import { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import { appointmentApi } from '../../api'

export default function AdminPage() {
  const [parameters, setParameters] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(null)
  const [success, setSuccess] = useState(null)

  useEffect(() => {
    appointmentApi.getParameters()
      .then(res => setParameters(res.data))
      .catch(() => setParameters([]))
      .finally(() => setLoading(false))
  }, [])

  const handleChange = (key, value) => {
    setParameters(prev =>
      prev.map(p => p.key === key ? { ...p, value } : p)
    )
  }

  const handleSave = async (param) => {
    setSaving(param.key)
    try {
      await appointmentApi.updateParameter(param.key, param.value)
      setSuccess(param.key)
      setTimeout(() => setSuccess(null), 2000)
    } catch {
      // Error silencioso — el campo vuelve a su estado
    } finally {
      setSaving(null)
    }
  }

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">

        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Configuración del Sistema</h1>
          <p className="text-gray-500 text-sm mt-1">Parámetros de agendamiento de citas</p>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">

          {loading ? (
            <div className="text-center py-12 text-gray-400">
              <p className="text-sm">Cargando parámetros...</p>
            </div>
          ) : parameters.length === 0 ? (
            <div className="text-center py-12 text-gray-400">
              <p className="text-3xl mb-2">⚙</p>
              <p className="text-sm">No hay parámetros configurados</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-50">
              {parameters.map(param => (
                <div key={param.key} className="px-6 py-5 flex items-center gap-4">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-800">{param.label || param.key}</p>
                    {param.description && (
                      <p className="text-xs text-gray-400 mt-0.5">{param.description}</p>
                    )}
                  </div>
                  <input
                    type="text"
                    value={param.value}
                    onChange={e => handleChange(param.key, e.target.value)}
                    className="w-32 border border-gray-200 rounded-xl px-3 py-2 text-sm text-center focus:outline-none focus:border-blue-500"
                  />
                  <button
                    onClick={() => handleSave(param)}
                    disabled={saving === param.key}
                    className={`px-4 py-2 rounded-xl text-sm font-semibold transition-colors
                      ${success === param.key
                        ? 'bg-green-500 text-white'
                        : 'bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50'
                      }`}>
                    {saving === param.key ? '...' : success === param.key ? '✓' : 'Guardar'}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  )
}
