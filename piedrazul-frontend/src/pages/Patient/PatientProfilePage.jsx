import PatientLayout from '../../components/PatientLayout'
import { useAuth } from '../../api/AuthContext'

export default function PatientProfilePage() {
    const { user } = useAuth()

    const initials = user?.fullName
        ? user.fullName.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
        : 'P'

    const fields = [
        { label: 'Nombre completo', value: user?.fullName },
        { label: 'Correo',          value: user?.username },
        { label: 'Tipo de documento', value: user?.userTypeId },
        { label: 'Número de documento', value: user?.id },
    ]

    return (
        <PatientLayout>
            <div className="max-w-lg mx-auto">

                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-800">Mi Perfil</h1>
                    <p className="text-gray-500 text-sm mt-1">Información de tu cuenta</p>
                </div>

                {/* Avatar */}
                <div className="bg-white rounded-2xl border border-gray-100 p-8 text-center mb-5">
                    <div className="w-20 h-20 bg-blue-600 rounded-full flex items-center justify-center
            mx-auto mb-4">
                        <span className="text-white text-2xl font-bold">{initials}</span>
                    </div>
                    <h2 className="text-lg font-bold text-gray-800">{user?.fullName}</h2>
                    <span className="inline-block mt-1 text-xs bg-blue-100 text-blue-600 rounded-full
            px-3 py-1 font-medium">
            Paciente
          </span>
                </div>

                {/* Datos */}
                <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
                    <div className="divide-y divide-gray-50">
                        {fields.map(field => (
                            <div key={field.label} className="flex items-center justify-between px-6 py-4">
                                <span className="text-sm text-gray-400">{field.label}</span>
                                <span className="text-sm font-semibold text-gray-800">
                  {field.value || '—'}
                </span>
                            </div>
                        ))}
                    </div>
                </div>

                <p className="text-center text-gray-400 text-xs mt-6">
                    Para actualizar tu información contacta al personal de la clínica.
                </p>
            </div>
        </PatientLayout>
    )
}