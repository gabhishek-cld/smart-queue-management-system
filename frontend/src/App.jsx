import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Navbar from './components/Navbar'
import PrivateRoute from './components/PrivateRoute'
import CustomerKiosk from './pages/CustomerKiosk'
import LoginPage from './pages/LoginPage'
import StaffCounter from './pages/StaffCounter'
import AdminDashboard from './pages/AdminDashboard'
import DisplayBoard from './pages/DisplayBoard'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="max-w-4xl mx-auto px-4 py-8">
          <Routes>
            <Route path="/"        element={<CustomerKiosk />} />
            <Route path="/display" element={<DisplayBoard />} />
            <Route path="/login"   element={<LoginPage />} />
            <Route
              path="/staff"
              element={
                <PrivateRoute role="STAFF">
                  <StaffCounter />
                </PrivateRoute>
              }
            />
            <Route
              path="/admin"
              element={
                <PrivateRoute role="ADMIN">
                  <AdminDashboard />
                </PrivateRoute>
              }
            />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  )
}
