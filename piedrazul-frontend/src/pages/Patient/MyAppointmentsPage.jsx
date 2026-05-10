import { useState, useEffect } from 'react'
import PatientLayout from '../../components/PatientLayout'
import { appointmentApi } from '../../api'
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

export default function MyAppointmentsPage() {
    const { user }            = useAuth()
    const [appointments, setAppointments] = useState([])
    const [loading, setLoading]           = useState(true)

    useEffect(() => {
        appointmentApi.listByPatient(user?.id)
            .then(res => setAppointments(res.data))
            .catch(() => setAppointments([]))
            .finally(() => setLoading(false))
    }, [user])

    return (
        <PatientLayout>
            <div className="max-w-3xl mx-auto">

                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Mis Citas</h1>
                    <p className="text-gray-500 text-sm mt-1">Historial y próximas citas médicas</p>
                </div>

                <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">

                    {/* Encabezado tabla */}
                    <div className="grid grid-cols-4 px-6 py-3 bg-gray-50 border-b border-gray-100">
                        {['Hora', 'Profesional', 'Especialidad', 'Estado'].map(h => (
                            <p key={h} className="text-xs font-semibold text-gray-400 uppercase tracking-wider">{h}</p>
                        ))}
                    </div>

                    {loading ? (
                        <div className="text-center py-12 text-gray-400 text-sm">
                            Cargando tus citas...
                        </div>
                    ) : appointments.length === 0 ? (
                        <div className="text-center py-12">
                            <p className="text-3xl mb-3">📅</p>
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
                                <div key={apt.id} className="grid grid-cols-4 px-6 py-4 items-center
                  hover:bg-gray-50 transition-colors">
                                    <div>
                                        <p className="font-semibold text-gray-800 text-sm">{apt.startTime}</p>
                                        <p className="text-gray-400 text-xs mt-0.5">
                                            {apt.date
                                                ? new Date(apt.date + 'T00:00:00').toLocaleDateString('es-CO', {
                                                    day: 'numeric', month: 'short', year: 'numeric'
                                                })
                                                : ''}
                                        </p>
                                    </div>
                                    <p className="text-gray-700 text-sm">{apt.doctorName || '—'}</p>
                                    <p className="text-gray-500 text-sm">{apt.reason || '—'}</p>
                                    <span className={`inline-flex px-3 py-1 rounded-full text-xs font-semibold w-fit
                    ${STATUS_STYLES[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                    {apt.status}
                  </span>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Footer */}
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