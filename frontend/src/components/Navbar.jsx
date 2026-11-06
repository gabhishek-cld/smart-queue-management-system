import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between">
      <div className="flex items-center gap-6">
        <Link to="/" className="text-sm font-semibold text-gray-800 hover:text-blue-600">
          QMS
        </Link>
        <Link to="/display" className="text-sm text-gray-500 hover:text-gray-800">
          Display Board
        </Link>
        {user?.role === 'STAFF' || user?.role === 'ADMIN' ? (
          <Link to="/staff" className="text-sm text-gray-500 hover:text-gray-800">
            Counter
          </Link>
        ) : null}
        {user?.role === 'ADMIN' && (
          <Link to="/admin" className="text-sm text-gray-500 hover:text-gray-800">
            Admin
          </Link>
        )}
      </div>

      <div className="flex items-center gap-4">
        {user ? (
          <>
            <span className="text-sm text-gray-500">
              {user.displayName} &middot;{' '}
              <span className="text-xs uppercase tracking-wide text-gray-400">{user.role}</span>
            </span>
            <button
              onClick={handleLogout}
              className="text-sm text-red-500 hover:text-red-700"
            >
              Sign out
            </button>
          </>
        ) : (
          <Link to="/login" className="text-sm text-blue-600 hover:text-blue-800">
            Sign in
          </Link>
        )}
      </div>
    </nav>
  )
}
