import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../api/AuthContext'

const Icon = ({ d, d2, circle, rect, line, lines, circles }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
       fill="none" stroke="currentColor" strokeWidth="1.8"
       strokeLinecap="round" strokeLinejoin="round">
    {d  && <path d={d}/>}
    {d2 && <path d={d2}/>}
    {circle  && <circle cx={circle[0]} cy={circle[1]} r={circle[2]}/>}
    {circles && circles.map((c,i) => <circle key={i} cx={c[0]} cy={c[1]} r={c[2]}/>)}
    {rect  && <rect x={rect[0]} y={rect[1]} width={rect[2]} height={rect[3]} rx={rect[4]||0}/>}
    {line  && <line x1={line[0]} y1={line[1]} x2={line[2]} y2={line[3]}/>}
    {lines && lines.map((l,i) => <line key={i} x1={l[0]} y1={l[1]} x2={l[2]} y2={l[3]}/>)}
  </svg>
)

const NavIcon = ({ name }) => {
  if (name === 'dashboard') return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="7" height="7" rx="1.5"/>
      <rect x="14" y="3" width="7" height="7" rx="1.5"/>
      <rect x="3" y="14" width="7" height="7" rx="1.5"/>
      <rect x="14" y="14" width="7" height="7" rx="1.5"/>
    </svg>
  )
  if (name === 'list') return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <line x1="9" y1="6" x2="21" y2="6"/>
      <line x1="9" y1="12" x2="21" y2="12"/>
      <line x1="9" y1="18" x2="21" y2="18"/>
      <line x1="3" y1="6"  x2="4"  y2="6"/>
      <line x1="3" y1="12" x2="4"  y2="12"/>
      <line x1="3" y1="18" x2="4"  y2="18"/>
    </svg>
  )
  if (name === 'plus') return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="9"/>
      <line x1="12" y1="8" x2="12" y2="16"/>
      <line x1="8"  y1="12" x2="16" y2="12"/>
    </svg>
  )
  if (name === 'calendar') return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="4" width="18" height="18" rx="2"/>
      <line x1="16" y1="2" x2="16" y2="6"/>
      <line x1="8"  y1="2" x2="8"  y2="6"/>
      <line x1="3"  y1="10" x2="21" y2="10"/>
    </svg>
  )
  if (name === 'download') return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 3v12"/>
      <path d="M8 11l4 4 4-4"/>
      <path d="M3 17v2a2 2 0 002 2h14a2 2 0 002-2v-2"/>
    </svg>
  )
  if (name === 'settings') return (
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="3"/>
      <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
    </svg>
  )
  return null
}

const NAV_ITEMS = [
  { path: '/dashboard',           label: 'Panel de Citas', icon: 'dashboard', roles: ['AGENDADOR', 'ADMIN'] },
  { path: '/appointments',        label: 'Listar Citas',   icon: 'list',      roles: ['AGENDADOR', 'ADMIN', 'DOCTOR'] },
  { path: '/appointments/new',    label: 'Registrar Cita', icon: 'plus',      roles: ['AGENDADOR'] },
  { path: '/doctor/appointments', label: 'Mis Citas',      icon: 'calendar',  roles: ['DOCTOR'] },
  { path: '/appointments/export', label: 'Exportar Citas', icon: 'download',  roles: ['AGENDADOR', 'ADMIN', 'DOCTOR'] },
  { path: '/admin',               label: 'Configuración',  icon: 'settings',  roles: ['ADMIN'] },
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

  const roleName  = user?.roles?.[0] || ''
  const roleLabel = ROLE_LABELS[roleName] || roleName
  const initials  = user?.fullName
      ? user.fullName.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase()
      : 'U'

  return (
      <div className="flex h-screen bg-gray-50 overflow-hidden">

        {/* --- Sidebar --- */}
        <aside className="w-60 bg-white border-r border-gray-100 flex flex-col shrink-0">

          {/* Logo */}
          <div className="px-5 py-5 flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                   fill="white" stroke="white" strokeWidth="1">
                <path d="M12 21C12 21 3 13.5 3 8a5 5 0 0110 0 5 5 0 0110 0c0 5.5-9 13-9 13z"/>
              </svg>
            </div>
            <div>
              <p className="font-bold text-gray-800 text-sm leading-tight">Piedrazul</p>
              <p className="text-gray-400 text-xs">Citas Médicas</p>
            </div>
          </div>

          {/* Sección nav */}
          <div className="px-4 mt-2">
            <p className="text-gray-400 text-xs font-semibold px-2 pb-2 uppercase tracking-wider">
              {hasRole('DOCTOR') ? 'Médico' : hasRole('AGENDADOR') ? 'Agendador' : 'Administración'}
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
                    <span className={active ? 'text-blue-600' : 'text-gray-400'}>
                      <NavIcon name={item.icon}/>
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
                      className="text-gray-400 hover:text-gray-600 transition-colors shrink-0">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
                     fill="none" stroke="currentColor" strokeWidth="1.8"
                     strokeLinecap="round" strokeLinejoin="round">
                  <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
                  <polyline points="16 17 21 12 16 7"/>
                  <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
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
