import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('piedrazul_user')
    return saved ? JSON.parse(saved) : null
  })

  const login = (userData) => {
    setUser(userData)
    localStorage.setItem('piedrazul_user', JSON.stringify(userData))
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem('piedrazul_user')
  }

  const isAuthenticated = () => !!user

  const hasRole = (role) =>
    user?.roles?.some((r) => r.toLowerCase() === role.toLowerCase())

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated, hasRole }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
