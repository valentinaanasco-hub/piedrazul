import { createContext, useContext, useState, useCallback } from 'react'
import { keycloakApi } from './index'

const AuthContext = createContext(null)

const APP_ROLES = ['ADMIN', 'DOCTOR', 'PACIENTE', 'AGENDADOR']

/** Decodifica el payload de un JWT (sin verificar firma), respetando UTF-8 */
function decodeJwt(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    // atob devuelve una "binary string" (1 char = 1 byte); reconstruimos UTF-8
    // para que acentos/ñ (María, Pérez, González) no salgan como "MarÃ­a".
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(json)
  } catch {
    return null
  }
}

/** Construye el objeto de usuario desde los claims del JWT de Keycloak */
function buildUserFromJwt(accessToken) {
  const claims = decodeJwt(accessToken)
  if (!claims) return null

  const allRoles = claims.realm_access?.roles ?? claims.roles ?? []
  const roles = allRoles.filter(r => APP_ROLES.includes(r))

  return {
    id:       claims.userId ?? claims.sub,   // ID en BD (claim custom) o UUID de Keycloak
    keycloakId: claims.sub,
    username: claims.preferred_username ?? claims.email ?? '',
    fullName: [claims.given_name, claims.family_name].filter(Boolean).join(' ')
              || claims.preferred_username
              || '',
    email:    claims.email ?? '',
    roles,
    state:    'ACTIVO',
  }
}

function loadPersistedSession() {
  try {
    const token   = localStorage.getItem('piedrazul_token')
    const refresh = localStorage.getItem('piedrazul_refresh')
    if (!token) return { user: null, token: null, refreshToken: null }

    const user = buildUserFromJwt(token)
    // Comprueba si el token ya expiró
    const claims = decodeJwt(token)
    if (claims?.exp && Date.now() / 1000 > claims.exp) {
      return { user: null, token: null, refreshToken: refresh }
    }

    return { user, token, refreshToken: refresh }
  } catch {
    return { user: null, token: null, refreshToken: null }
  }
}

export function AuthProvider({ children }) {
  const initial = loadPersistedSession()

  const [user,         setUser]         = useState(initial.user)
  const [accessToken,  setAccessToken]  = useState(initial.token)
  const [refreshToken, setRefreshToken] = useState(initial.refreshToken)

  const login = useCallback((newAccessToken, newRefreshToken) => {
    const userData = buildUserFromJwt(newAccessToken)
    setUser(userData)
    setAccessToken(newAccessToken)
    setRefreshToken(newRefreshToken)
    localStorage.setItem('piedrazul_token',   newAccessToken)
    localStorage.setItem('piedrazul_refresh', newRefreshToken)
  }, [])

  const logout = useCallback(async () => {
    try {
      if (refreshToken) await keycloakApi.logout(refreshToken)
    } catch { /* si falla el logout en Keycloak, igual limpiamos local */ }
    setUser(null)
    setAccessToken(null)
    setRefreshToken(null)
    localStorage.removeItem('piedrazul_token')
    localStorage.removeItem('piedrazul_refresh')
    localStorage.removeItem('piedrazul_user')
  }, [refreshToken])

  const refreshSession = useCallback(async () => {
    if (!refreshToken) return false
    try {
      const res = await keycloakApi.refresh(refreshToken)
      login(res.data.access_token, res.data.refresh_token)
      return true
    } catch {
      await logout()
      return false
    }
  }, [refreshToken, login, logout])

  const isAuthenticated = () => !!user && !!accessToken

  const hasRole = (role) =>
    user?.roles?.some(r => r.toLowerCase() === role.toLowerCase()) ?? false

  return (
    <AuthContext.Provider value={{
      user,
      accessToken,
      login,
      logout,
      refreshSession,
      isAuthenticated,
      hasRole,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
