import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../../components/Layout'
import { appointmentApi, medicalApi } from '../../api'

const DAYS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']

export default function AdminPage() {
  const [view, setView] = useState('menu') // 'menu' | 'global' | 'professional'

  return (
      <Layout>
        <div className="max-w-3xl mx-auto">
          <div className="mb-6">
            <p className="text-sm text-gray-400 mb-1">
              Administración / Configuración
              {view === 'global'       && ' / Global'}
              {view === 'professional' && ' / Profesional'}
            </p>
            <h1 className="text-2xl font-bold text-gray-800">
              {view === 'menu'         && 'Configuración del Sistema'}
              {view === 'global'       && 'Configuración Global del Sistema'}
              {view === 'professional' && 'Configuración de Disponibilidad del Profesional'}
            </h1>
            <p className="text-gray-500 text-sm mt-1">
              {view === 'menu'         && 'Seleccione el tipo de configuración que desea administrar.'}
              {view === 'global'       && 'Defina la ventana de tiempo en semanas en la que los pacientes pueden agendar citas.'}
              {view === 'professional' && 'Defina los días, horarios y duración de las citas para cada profesional.'}
            </p>
          </div>

          {view === 'menu'         && <ConfigMenu onSelect={setView} />}
          {view === 'global'       && <GlobalConfig onBack={() => setView('menu')} />}
          {view === 'professional' && <ProfessionalConfig onBack={() => setView('menu')} />}
        </div>
      </Layout>
  )
}

// --- Menú principal de configuración ---
function ConfigMenu({ onSelect }) {
  return (
      <div className="grid grid-cols-2 gap-5">
        <div className="bg-white rounded-2xl border border-gray-100 p-8 flex flex-col">
          <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center text-2xl mb-5">
            🌐
          </div>
          <h2 className="text-lg font-bold text-gray-800 mb-2">Configuración Global</h2>
          <p className="text-gray-500 text-sm flex-1">
            Permite definir parámetros generales del sistema como la ventana de tiempo
            en semanas para agendar citas.
          </p>
          <button onClick={() => onSelect('global')}
                  className="mt-6 w-full bg-blue-600 text-white rounded-xl py-2.5 text-sm font-semibold
            hover:bg-blue-700 transition-colors flex items-center justify-center gap-2">
            Configurar →
          </button>
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 p-8 flex flex-col">
          <div className="w-12 h-12 bg-purple-50 rounded-xl flex items-center justify-center text-2xl mb-5">
            👤
          </div>
          <h2 className="text-lg font-bold text-gray-800 mb-2">Configuración por Profesional</h2>
          <p className="text-gray-500 text-sm flex-1">
            Permite definir días de atención, franja horaria y el intervalo entre
            citas para cada profesional.
          </p>
          <button onClick={() => onSelect('professional')}
                  className="mt-6 w-full bg-blue-600 text-white rounded-xl py-2.5 text-sm font-semibold
            hover:bg-blue-700 transition-colors flex items-center justify-center gap-2">
            Configurar →
          </button>
        </div>
      </div>
  )
}

// --- Configuración Global ---
function GlobalConfig({ onBack }) {
  const [weeks, setWeeks]     = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving]   = useState(false)
  const [saved, setSaved]     = useState(false)
  const [error, setError]     = useState('')

  useEffect(() => {
    appointmentApi.getParameters()
        .then(res => {
          const param = res.data?.find(p => p.key === 'ventana_semanas')
          if (param) setWeeks(param.value)
        })
        .catch(() => {})
        .finally(() => setLoading(false))
  }, [])

  const handleSave = async () => {
    if (!weeks || isNaN(weeks) || parseInt(weeks) < 1) {
      setError('Ingresa un número válido mayor a 0')
      return
    }
    setSaving(true)
    try {
      await appointmentApi.updateParameter('ventana_semanas', weeks)
      setSaved(true)
      setTimeout(() => setSaved(false), 2000)
      setError('')
    } catch {
      setError('Error al guardar. Intenta de nuevo.')
    } finally {
      setSaving(false)
    }
  }

  return (
      <div className="bg-white rounded-2xl border border-gray-100 p-8">
        {loading ? (
            <p className="text-gray-400 text-sm">Cargando configuración...</p>
        ) : (
            <div className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Ventana de tiempo para agendar citas (semanas)
                  <span className="text-red-500 ml-1">*</span>
                </label>
                <input
                    type="number" min="1" max="52"
                    value={weeks}
                    onChange={e => { setWeeks(e.target.value); setError('') }}
                    className={`w-full border rounded-xl px-4 py-3 text-sm focus:outline-none transition-colors
                ${error ? 'border-red-400' : 'border-gray-200 focus:border-blue-500'}`}
                />
                <p className="text-gray-400 text-xs mt-1.5">
                  Define cuántas semanas hacia adelante se pueden reservar citas.
                </p>
                {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
              </div>

              <div className="flex gap-3 pt-2">
                <button onClick={onBack}
                        className="flex-1 border border-gray-200 text-gray-600 rounded-xl py-2.5 text-sm
                font-semibold hover:bg-gray-50 transition-colors">
                  Cancelar
                </button>
                <button onClick={handleSave} disabled={saving}
                        className={`flex-1 rounded-xl py-2.5 text-sm font-semibold transition-colors
                ${saved
                            ? 'bg-green-500 text-white'
                            : 'bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50'
                        }`}>
                  {saving ? 'Guardando...' : saved ? '✓ Guardado' : 'Guardar configuración'}
                </button>
              </div>
            </div>
        )}
      </div>
  )
}

// --- Configuración por Profesional ---
function ProfessionalConfig({ onBack }) {
  const [doctors, setDoctors]       = useState([])
  const [doctorId, setDoctorId]     = useState('')
  const [selectedDays, setSelectedDays] = useState(['Lunes','Martes','Miércoles','Jueves','Viernes'])
  const [startTime, setStartTime]   = useState('08:00')
  const [endTime, setEndTime]       = useState('18:00')
  const [interval, setInterval]     = useState('30')
  const [saving, setSaving]         = useState(false)
  const [saved, setSaved]           = useState(false)
  const [error, setError]           = useState('')

  useEffect(() => {
    medicalApi.listDoctors()
        .then(res => setDoctors(res.data))
        .catch(() => {})
  }, [])

  const toggleDay = (day) => {
    setSelectedDays(prev =>
        prev.includes(day) ? prev.filter(d => d !== day) : [...prev, day]
    )
  }

  const validate = () => {
    if (!doctorId) return 'Selecciona un profesional'
    if (selectedDays.length === 0) return 'Selecciona al menos un día'
    if (!startTime || !endTime) return 'Define la franja horaria'
    if (startTime >= endTime) return 'La hora de inicio debe ser anterior a la hora de fin'
    if (!interval || parseInt(interval) < 5) return 'El intervalo debe ser al menos 5 minutos'
    return ''
  }

  const handleSave = async () => {
    const err = validate()
    if (err) { setError(err); return }
    setSaving(true)
    try {
      await medicalApi.updateSchedule(doctorId, { selectedDays, startTime, endTime, interval })
      setSaved(true)
      setTimeout(() => setSaved(false), 2000)
      setError('')
    } catch {
      setError('Error al guardar. Intenta de nuevo.')
    } finally {
      setSaving(false)
    }
  }

  return (
      <div className="bg-white rounded-2xl border border-gray-100 p-8 space-y-6">

        {/* Profesional */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Profesional <span className="text-red-500">*</span>
          </label>
          <select value={doctorId} onChange={e => { setDoctorId(e.target.value); setError('') }}
                  className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm
            focus:outline-none focus:border-blue-500 transition-colors">
            <option value="">Seleccione un profesional...</option>
            {doctors.map(d => (
                <option key={d.id} value={d.id}>{d.fullName}</option>
            ))}
          </select>
        </div>

        {/* Días de atención */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-3">Días de atención</label>
          <div className="flex flex-wrap gap-3">
            {DAYS.map(day => (
                <label key={day} className="flex items-center gap-2 cursor-pointer">
                  <input
                      type="checkbox"
                      checked={selectedDays.includes(day)}
                      onChange={() => toggleDay(day)}
                      className="w-4 h-4 accent-blue-600 rounded"
                  />
                  <span className="text-sm text-gray-700">{day}</span>
                </label>
            ))}
          </div>
        </div>

        {/* Franja horaria */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-3">
            Franja horaria de atención
          </label>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs text-gray-500 mb-1">
                Hora de inicio <span className="text-red-500">*</span>
              </label>
              <input type="time" value={startTime}
                     onChange={e => setStartTime(e.target.value)}
                     className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                focus:outline-none focus:border-blue-500 transition-colors" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">
                Hora de fin <span className="text-red-500">*</span>
              </label>
              <input type="time" value={endTime}
                     onChange={e => setEndTime(e.target.value)}
                     className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm
                focus:outline-none focus:border-blue-500 transition-colors" />
            </div>
          </div>
        </div>

        {/* Intervalo */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Intervalo entre citas
          </label>
          <div>
            <label className="block text-xs text-gray-500 mb-1">
              Intervalo entre citas (minutos) <span className="text-red-500">*</span>
            </label>
            <input type="number" min="5" max="120" value={interval}
                   onChange={e => setInterval(e.target.value)}
                   className="w-40 border border-gray-200 rounded-xl px-4 py-2.5 text-sm
              focus:outline-none focus:border-blue-500 transition-colors" />
          </div>
        </div>

        {error && <p className="text-red-500 text-sm bg-red-50 rounded-xl py-2 px-3">{error}</p>}

        {/* Botones */}
        <div className="flex gap-3 pt-2">
          <button onClick={onBack}
                  className="flex-1 border border-gray-200 text-gray-600 rounded-xl py-2.5 text-sm
            font-semibold hover:bg-gray-50 transition-colors">
            Cancelar
          </button>
          <button onClick={handleSave} disabled={saving}
                  className={`flex-1 rounded-xl py-2.5 text-sm font-semibold transition-colors
            ${saved
                      ? 'bg-green-500 text-white'
                      : 'bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50'
                  }`}>
            {saving ? 'Guardando...' : saved ? '✓ Guardado' : 'Guardar disponibilidad'}
          </button>
        </div>
      </div>
  )
}