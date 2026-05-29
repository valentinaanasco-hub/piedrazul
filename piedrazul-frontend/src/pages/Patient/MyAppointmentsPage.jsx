import { useState, useEffect } from 'react'
import PatientLayout from '../../components/PatientLayout'
import { appointmentApi, medicalApi } from '../../api'
import { useAuth } from '../../api/AuthContext'
import { Link } from 'react-router-dom'

const STATUS_STYLES = {
    AGENDADA:   'bg-green-100 text-green-700',
    CONFIRMADA: 'bg-green-100 text-green-700',
    PENDIENTE:  'bg-yellow-100 text-yellow-700',
    CANCELADA:  'bg-red-100 text-red-700',
    ATENDIDA:   'bg-gray-100 text-gray-600',
    REAGENDADA: 'bg-blue-100 text-blue-700',
}

const MONTHS = ['ene','feb','mar','abr','may','jun',
    'jul','ago','sep','oct','nov','dic']

export default function MyAppointmentsPage() {
    const { user }                          = useAuth()
    const [appointments, setAppointments]   = useState([])
    const [doctorNames, setDoctorNames]     = useState({})
    const [loading, setLoading]             = useState(true)
    const [cancelling, setCancelling]       = useState(null)
    const [confirmCancel, setConfirmCancel] = useState(null)

    const loadAppointments = () => {
        setLoading(true)
        appointmentApi.listByPatient(user?.id)
            .then(res => {
                const appts = res.data || []
                setAppointments(appts)

                // Cargar nombres de médicos únicos
                const uniqueDoctorIds = [...new Set(appts.map(a => a.doctorId))]
                return Promise.all(
                    uniqueDoctorIds.map(id =>
                        medicalApi.listDoctors()
                            .then(r => {
                                const doc = r.data?.find(d => d.id === id)
                                return { id, name: doc?.fullName || `Profesional ${id}` }
                            })
                            .catch(() => ({ id, name: `Profesional ${id}` }))
                    )
                )
            })
            .then(names => {
                const map = {}
                names.forEach(n => { map[n.id] = n.name })
                setDoctorNames(map)
            })
            .catch(() => setAppointments([]))
            .finally(() => setLoading(false))
    }

    useEffect(() => { loadAppointments() }, [user])

    const handleCancel = async (id) => {
        setCancelling(id)
        try {
            await appointmentApi.cancel(id)
            setConfirmCancel(null)
            loadAppointments()
        } catch {
            alert('No se pudo cancelar la cita. Intenta de nuevo.')
        } finally {
            setCancelling(null)
        }
    }

    const formatDate = (dateStr) => {
        if (!dateStr) return ''
        const [y, m, d] = dateStr.split('-')
        return `${parseInt(d)} ${MONTHS[parseInt(m) - 1]} ${y}`
    }

    const formatTime = (timeStr) => {
        if (!timeStr) return ''
        return typeof timeStr === 'string' ? timeStr.substring(0, 5) : timeStr
    }

    return (
        <PatientLayout>
            <div className="max-w-3xl mx-auto">

                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Mis Citas</h1>
                    <p className="text-gray-500 text-sm mt-1">Historial y próximas citas médicas</p>
                </div>

                <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">

                    {loading ? (
                        <div className="text-center py-12 text-gray-400 text-sm">Cargando tus citas...</div>
                    ) : appointments.length === 0 ? (
                        <div className="text-center py-12">
                            <p className="text-gray-400 text-sm">No tienes citas registradas</p>
                            <Link to="/patient/schedule"
                                  className="inline-block mt-4 bg-blue-600 text-white rounded-xl
                  px-5 py-2 text-sm font-semibold hover:bg-blue-700 transition-colors">
                                Agendar mi primera cita
                            </Link>
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-50">
                            {appointments.map(apt => (
                                <div key={apt.appointmentId} className="px-6 py-4 hover:bg-gray-50 transition-colors">
                                    <div className="flex items-center gap-4">

                                        {/* Fecha + hora */}
                                        <div className="w-24 shrink-0">
                                            <p className="font-bold text-gray-800 text-sm">{formatTime(apt.startTime)}</p>
                                            <p className="text-gray-400 text-xs mt-0.5">{formatDate(apt.date)}</p>
                                        </div>

                                        {/* Profesional */}
                                        <div className="flex-1 min-w-0">
                                            <p className="text-sm font-medium text-gray-800 truncate">
                                                {doctorNames[apt.doctorId] || `Profesional ${apt.doctorId}`}
                                            </p>
                                            <p className="text-gray-400 text-xs mt-0.5 truncate">
                                                {apt.reason || '—'}
                                            </p>
                                        </div>

                                        {/* Estado */}
                                        <span className={`px-3 py-1 rounded-full text-xs font-semibold shrink-0
                      ${STATUS_STYLES[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                      {apt.status}
                    </span>

                                        {/* Botón cancelar — solo si está AGENDADA */}
                                        {apt.status === 'AGENDADA' && (
                                            confirmCancel === apt.appointmentId ? (
                                                <div className="flex items-center gap-2 shrink-0">
                                                    <span className="text-xs text-gray-500">¿Cancelar?</span>
                                                    <button onClick={() => handleCancel(apt.appointmentId)}
                                                            disabled={cancelling === apt.appointmentId}
                                                            className="text-xs bg-red-500 text-white px-3 py-1 rounded-lg
                              hover:bg-red-600 transition-colors disabled:opacity-50">
                                                        {cancelling === apt.appointmentId ? '...' : 'Sí'}
                                                    </button>
                                                    <button onClick={() => setConfirmCancel(null)}
                                                            className="text-xs border border-gray-200 text-gray-500 px-3 py-1
                              rounded-lg hover:bg-gray-50 transition-colors">
                                                        No
                                                    </button>
                                                </div>
                                            ) : (
                                                <button onClick={() => setConfirmCancel(apt.appointmentId)}
                                                        className="text-xs text-red-500 hover:text-red-700 shrink-0
                            transition-colors font-medium">
                                                    Cancelar
                                                </button>
                                            )
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {!loading && appointments.length > 0 && (
                        <div className="px-6 py-4 border-t border-gray-50 flex justify-between items-center">
                            <p className="text-sm text-gray-400">
                                {appointments.length} cita(s) en total
                            </p>
                            <Link to="/patient/schedule"
                                  className="text-sm text-blue-600 font-medium hover:underline">
                                + Agendar nueva cita
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </PatientLayout>
    )
}