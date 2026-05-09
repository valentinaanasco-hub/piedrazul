import Layout from '../../components/Layout'
import { useAuth } from '../../api/AuthContext'
import { Link } from 'react-router-dom'

export default function DashboardPage() {
  const { user } = useAuth()

  const cards = [
    { title: 'Listar Citas',    desc: 'Consulta las citas por médico y fecha',  icon: '☰', path: '/appointments',     color: 'bg-blue-50 text-blue-600' },
    { title: 'Registrar Cita', desc: 'Crea una nueva cita para un paciente',    icon: '⊕', path: '/appointments/new', color: 'bg-green-50 text-green-600' },
    { title: 'Configuración',  desc: 'Ajusta los parámetros del sistema',       icon: '⚙', path: '/admin',            color: 'bg-purple-50 text-purple-600' },
  ]

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">

        {/* --- Bienvenida --- */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-800">
            Bienvenido, {user?.fullName?.split(' ')[0]} 👋
          </h1>
          <p className="text-gray-500 mt-1">¿Qué deseas hacer hoy?</p>
        </div>

        {/* --- Cards --- */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {cards.map(card => (
            <Link key={card.path} to={card.path}
              className="bg-white rounded-2xl border border-gray-100 p-6 hover:shadow-md transition-shadow group">
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-4 ${card.color}`}>
                {card.icon}
              </div>
              <h3 className="font-semibold text-gray-800 group-hover:text-blue-600 transition-colors">
                {card.title}
              </h3>
              <p className="text-gray-400 text-sm mt-1">{card.desc}</p>
            </Link>
          ))}
        </div>
      </div>
    </Layout>
  )
}
