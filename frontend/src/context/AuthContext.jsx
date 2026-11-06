import { createContext, useContext, useState, useEffect } from 'react'
import api from '../api/axiosConfig'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/auth/me')
      .then(r => setUser(r.data))
      .catch(() => setUser(null)) 
      .finally(() => setLoading(false))
  }, [])

  function login(userData) {
    setUser(userData)
  }

  function logout() {
    api.post('/auth/logout').finally(() => setUser(null))
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}