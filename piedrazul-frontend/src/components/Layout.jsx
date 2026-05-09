import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../api/AuthContext'

const NAV_ITEMS = [
  { path: '/dashboard',        label: 'Panel principal',  icon: '⊞', roles: ['AGENDADOR', 'DOCTOR', 'ADMIN'] },
  { path: '/appointments',     label: 'Listar citas',     icon: '☰', roles: ['AGENDADOR', 'DOCTOR', 'ADMIN'] },
  { path: '/appointments/new', label: 'Registrar cita',   icon: '⊕', roles: ['AGENDADOR', 'ADMIN'] },
  { path: '/admin',            label: 'Configuración',    icon: '⚙', roles: ['ADMIN'] },
]

export default function Layout({ children }) {
  const { user, logout, hasRole } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const visibleItems = NAV_ITEMS.filter(item =>
    item.roles.some(role => hasRole(role))
  )

  return (
    <div className="flex h-screen bg-gray-50">

      {/* --- Sidebar --- */}
      <aside className="w-60 bg-white border-r border-gray-100 flex flex-col">

        {/* Logo */}
        <div className="px-5 py-5 border-b border-gray-100 flex items-center gap-3">
          <div className="w-9 h-9 bg-blue-600 rounded-lg flex items-center justify-center">
            <span className="text-white text-base">♥</span>
          </div>
          <div>
            <p className="font-bold text-gray-800 text-sm">Piedrazul</p>
            <p className="text-gray-400 text-xs">Citas Médicas</p>
          </div>
        </div>

        {/* Navegación */}
        <nav className="flex-1 py-4 px-3 space-y-1">
          <p className="text-gray-400 text-xs font-semibold px-3 pb-2 uppercase tracking-wider">
            Administración
          </p>
          {visibleItems.map(item => {
            const active = location.pathname === item.path
            return (
              <Link key={item.path} to={item.path}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors
                  ${active
                    ? 'bg-blue-50 text-blue-600 font-semibold border-l-2 border-blue-600'
                    : 'text-gray-600 hover:bg-gray-50'
                  }`}>
                <span>{item.icon}</span>
                {item.label}
              </Link>
            )
          })}
        </nav>

        {/* Usuario */}
        <div className="px-4 py-4 border-t border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-blue-600 rounded-full flex items-center justify-center">
              <span className="text-white text-sm font-bold">
                {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-gray-800 truncate">{user?.fullName}</p>
              <p className="text-xs text-gray-400 truncate">{user?.roles?.[0]}</p>
            </div>
            <button onClick={handleLogout} title="Cerrar sesión"
              className="text-gray-400 hover:text-gray-600 text-lg">
              ⇥
            </button>
          </div>
        </div>
      </aside>

      {/* --- Contenido --- */}
      <main className="flex-1 overflow-auto p-6">
        {children}
      </main>
    </div>
  )
}
