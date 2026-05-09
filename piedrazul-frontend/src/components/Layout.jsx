import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../api/AuthContext'

const NAV_ITEMS = [
  { path: '/dashboard',        label: 'Panel de Citas',  icon: '⊞', roles: ['AGENDADOR', 'DOCTOR', 'ADMIN'] },
  { path: '/appointments',     label: 'Listar Citas',    icon: '☰', roles: ['AGENDADOR', 'DOCTOR', 'ADMIN'] },
  { path: '/appointments/new', label: 'Registrar Cita',  icon: '⊕', roles: ['AGENDADOR', 'ADMIN'] },
  { path: '/admin',            label: 'Configuración',   icon: '⚙', roles: ['ADMIN'] },
]

const ROLE_LABELS = {
  ADMIN:     'Administrador',
  AGENDADOR: 'Agendador',
  DOCTOR:    'Profesional',
  PACIENTE:  'Paciente',
}

export default function Layout({ children }) {
  const { user, logout, hasRole } = useAuth()
  const location  = useLocation()
  const navigate  = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const visibleItems = NAV_ITEMS.filter(item =>
      item.roles.some(role => hasRole(role))
  )

  const roleName = user?.roles?.[0] || ''
  const roleLabel = ROLE_LABELS[roleName] || roleName
  const initials = user?.fullName
      ? user.fullName.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
      : 'U'

  return (
      <div className="flex h-screen bg-gray-50 overflow-hidden">

        {/* --- Sidebar --- */}
        <aside className="w-60 bg-white border-r border-gray-100 flex flex-col shrink-0">

          {/* Logo */}
          <div className="px-5 py-5 flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
              <span className="text-white text-lg">♥</span>
            </div>
            <div>
              <p className="font-bold text-gray-800 text-sm leading-tight">Piedrazul</p>
              <p className="text-gray-400 text-xs">Citas Médicas</p>
            </div>
          </div>

          {/* Sección nav */}
          <div className="px-4 mt-2">
            <p className="text-gray-400 text-xs font-semibold px-2 pb-2 uppercase tracking-wider">
              Administración
            </p>
          </div>

          {/* Navegación */}
          <nav className="flex-1 px-3 space-y-0.5 overflow-y-auto">
            {visibleItems.map(item => {
              const active = location.pathname === item.path
              return (
                  <Link key={item.path} to={item.path}
                        className={`flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-colors
                  ${active
                            ? 'bg-blue-50 text-blue-600 font-semibold'
                            : 'text-gray-600 hover:bg-gray-50 hover:text-gray-800'
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
                  {user?.fullName || 'Usuario'}
                </p>
                <span className="inline-block text-xs bg-blue-100 text-blue-600 rounded-full
                px-2 py-0.5 font-medium mt-0.5">
                {roleLabel}
              </span>
              </div>
              <button onClick={handleLogout} title="Cerrar sesión"
                      className="text-gray-400 hover:text-gray-600 text-xl transition-colors shrink-0">
                ⇥
              </button>
            </div>
          </div>
        </aside>

        {/* --- Contenido principal --- */}
        <main className="flex-1 overflow-auto p-8">
          {children}
        </main>
      </div>
  )
}