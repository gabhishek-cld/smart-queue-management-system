import api from './axiosConfig'

// --- Auth ---
export const login = (username, password) =>
  api.post('/auth/login', { username, password })

// logout calls the backend to clear the cookie
export const logout = () =>
  api.post('/auth/logout')

// called on every page load to check if a valid cookie exists
export const getMe = () =>
  api.get('/auth/me')

// --- Services & Tickets (public) ---
export const getServices = () => api.get('/services')

export const createTicket = (serviceTypeId, customerName) =>
  api.post('/tickets', { serviceTypeId, customerName })

export const getTicket = (id) => api.get(`/tickets/${id}`)

// --- Counter (staff) ---
export const getCounters = () => api.get('/counters')

export const callNext = (counterId) =>
  api.post(`/counters/${counterId}/next`)

export const markComplete = (counterId) =>
  api.put(`/counters/${counterId}/complete`)

export const markNoShow = (counterId) =>
  api.put(`/counters/${counterId}/no-show`)

export const transferTicket = (counterId, targetServiceTypeId) =>
  api.put(`/counters/${counterId}/transfer`, null, { params: { targetServiceTypeId } })

// --- Admin ---
export const getAnalytics = () => api.get('/admin/analytics')

export const addCounter = (name, staffName) =>
  api.post('/admin/counters', { name, staffName })

export const setCounterStatus = (counterId, status) =>
  api.put(`/admin/counters/${counterId}/status`, null, { params: { status } })

export const deleteCounter = (counterId) =>
  api.delete(`/admin/counters/${counterId}`)