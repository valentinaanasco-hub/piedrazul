import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../api/AuthContext'

const NAV_ITEMS = [
    { path: '/patient/schedule',     label: 'Agendar Cita', icon: '📅' },
    { path: '/patient/appointments', label: 'Mis Citas',    icon: '☰'  },
    { path: '/patient/profile',      label: 'Mi Perfil',    icon: '👤' },
]

export default function PatientLayout({ children }) {
    const { user, logout } = useAuth()
    const location = useLocation()
    const navigate = useNavigate()

    const handleLogout = () => { logout(); navigate('/login') }

    const initials = user?.fullName
        ? user.fullName.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
        : 'P'

    return (
        <div className="flex h-screen bg-gray-50 overflow-hidden">

            {/* --- Sidebar --- */}
            <aside className="w-60 bg-white border-r border-gray-100 flex flex-col shrink-0">

                {/* Logo */}
                <div className="px-5 py-5 flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="white" stroke="none">
                            <path d="M12 21C12 21 3 13.5 3 8a5 5 0 0110 0 5 5 0 0110 0c0 5.5-9 13-9 13z"/>
                        </svg>
                    </div>
                    <div>
                        <p className="font-bold text-gray-800 text-sm leading-tight">Piedrazul</p>
                        <p className="text-gray-400 text-xs">Citas Médicas</p>
                    </div>
                </div>

                {/* Sección */}
                <div className="px-4 mt-2">
                    <p className="text-gray-400 text-xs font-semibold px-2 pb-2 uppercase tracking-wider">
                        Paciente
                    </p>
                </div>

                {/* Navegación */}
                <nav className="flex-1 px-3 space-y-0.5 overflow-y-auto">
                    {NAV_ITEMS.map(item => {
                        const active = location.pathname === item.path
                        return (
                            <Link key={item.path} to={item.path}
                                  className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-colors
                  ${active
                                      ? 'bg-blue-50 text-blue-600 font-semibold'
                                      : 'text-gray-600 hover:bg-gray-50'
                                  }`}>
                <span className={`text-base ${active ? 'text-blue-600' : 'text-gray-400'}`}>
                    {item.icon}
                </span>
                                {item.label}
                            </Link>
                        )
                    })}
                </nav>

                {/* Usuario */}
                <div className="px-4 py-4 border-t border-gray-100">
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 bg-blue-600 rounded-full flex items-center justify-center shrink-0">
                            <span className="text-white text-sm font-bold">{initials}</span>
                        </div>
                        <div className="flex-1 min-w-0">
                            <p className="text-sm font-semibold text-gray-800 truncate">
                                {user?.fullName || 'Paciente'}
                            </p>
                            <span className="inline-block text-xs bg-blue-100 text-blue-600 rounded-full
                px-2 py-0.5 font-medium mt-0.5">
                Paciente
              </span>
                        </div>
                        <button onClick={handleLogout} title="Cerrar sesión"
                                className="text-gray-400 hover:text-gray-600 transition-colors shrink-0">
                            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
                                 fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
                                <polyline points="16 17 21 12 16 7"/>
                                <line x1="21" y1="12" x2="9" y2="12"/>
                            </svg>
                        </button>
                    </div>
                </div>
            </aside>

            {/* --- Contenido --- */}
            <main className="flex-1 overflow-auto p-8">
                {children}
            </main>
        </div>
    )
}