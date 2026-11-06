import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { getCounters, getServices, callNext, markComplete, markNoShow, transferTicket } from '../api'

const STATUS_BADGE = {
  WAITING:   'bg-yellow-100 text-yellow-700',
  CALLED:    'bg-blue-100 text-blue-700',
  COMPLETED: 'bg-green-100 text-green-700',
  NO_SHOW:   'bg-gray-100 text-gray-500',
}

export default function StaffCounter() {
  const { user } = useAuth()
  const [counters, setCounters]     = useState([])
  const [services, setServices]     = useState([])
  const [counterId, setCounterId]   = useState('')
  const [current, setCurrent]       = useState(null)
  const [loading, setLoading]       = useState(false)
  const [message, setMessage]       = useState('')
  const [transferTo, setTransferTo] = useState('')

  useEffect(() => {
    getCounters()
      .then(r => setCounters(Array.isArray(r.data) ? r.data : []))
      .catch(err => {
        console.error('Failed to load counters', err);
        setCounters([]);
      })

    getServices()
      .then(r => setServices(Array.isArray(r.data) ? r.data : []))
      .catch(err => {
        console.error('Failed to load services', err);
        setServices([]);
      })
  }, [])

  function flash(msg) {
    setMessage(msg)
    setTimeout(() => setMessage(''), 3000)
  }

  async function act(fn, successMsg) {
    if (!counterId) return flash('Please select your counter first.')
    setLoading(true)
    try {
      const res = await fn(counterId)
      setCurrent(res.data)
      flash(successMsg)
    } catch (e) {
      flash(e.response?.data?.error || 'Something went wrong.')
    } finally {
      setLoading(false)
    }
  }

  async function handleTransfer() {
    if (!transferTo) return flash('Select a target service type.')
    setLoading(true)
    try {
      const res = await transferTicket(counterId, transferTo)
      setCurrent(null)
      flash(`Ticket transferred to ${res.data.serviceTypeName}`)
    } catch (e) {
      flash(e.response?.data?.error || 'Transfer failed.')
    } finally {
      setLoading(false)
    }
  }

  const activeCounters = Array.isArray(counters) ? counters : [];
  const activeServices = Array.isArray(services) ? services : [];

  return (
    <div className="max-w-lg mx-auto">
      <h1 className="text-xl font-semibold text-gray-800 mb-1">Counter Interface</h1>
      <p className="text-sm text-gray-500 mb-6">Signed in as {user?.displayName}</p>

      {/* Counter selector */}
      <div className="mb-6">
        <label className="block text-sm text-gray-600 mb-1">Your counter</label>
        <select
          value={counterId}
          onChange={e => setCounterId(e.target.value)}
          className="border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400 w-full"
        >
          <option value="">Select counter…</option>
          {activeCounters
            .filter(c => c.status !== 'OFFLINE')
            .map(c => (
              <option key={c.id} value={c.id}>
                {c.name} {c.staffName ? `— ${c.staffName}` : ''}
              </option>
            ))}
        </select>
      </div>

      {message && (
        <div className="mb-4 text-sm text-blue-700 bg-blue-50 border border-blue-200 rounded px-3 py-2">
          {message}
        </div>
      )}

      {/* Current ticket card */}
      {current ? (
        <div className="border border-gray-200 rounded-lg p-5 bg-white mb-6">
          <div className="flex items-start justify-between mb-3">
            <div>
              <p className="text-3xl font-bold text-gray-800">{current.tokenNumber}</p>
              <p className="text-sm text-gray-500">{current.serviceTypeName}</p>
              {current.customerName && (
                <p className="text-xs text-gray-400 mt-0.5">{current.customerName}</p>
              )}
            </div>
            <span className={`text-xs px-2 py-1 rounded-full font-medium ${STATUS_BADGE[current.status] || 'bg-gray-100 text-gray-600'}`}>
              {current.status}
            </span>
          </div>

          {/* Transfer section */}
          <div className="mt-4 pt-4 border-t border-gray-100">
            <p className="text-xs text-gray-500 mb-2">Transfer to another service</p>
            <div className="flex gap-2">
              <select
                value={transferTo}
                onChange={e => setTransferTo(e.target.value)}
                className="flex-1 border border-gray-300 rounded px-2 py-1.5 text-xs focus:outline-none focus:ring-1 focus:ring-blue-400"
              >
                <option value="">Select service…</option>
                {activeServices.map(s => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
              <button
                onClick={handleTransfer}
                disabled={loading}
                className="text-xs border border-gray-300 rounded px-3 py-1.5 hover:bg-gray-50 disabled:opacity-40"
              >
                Transfer
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="border border-dashed border-gray-200 rounded-lg p-8 text-center text-sm text-gray-400 mb-6">
          No active ticket
        </div>
      )}

      {/* Action buttons */}
      <div className="flex gap-3">
        <button
          onClick={() => act(callNext, 'Next customer called.')}
          disabled={loading || !counterId}
          className="flex-1 bg-blue-600 text-white text-sm rounded px-4 py-2.5 hover:bg-blue-700 disabled:opacity-40"
        >
          Call Next
        </button>
        <button
          onClick={() => act(markComplete, 'Ticket marked as completed.')}
          disabled={loading || !current || current.status !== 'CALLED'}
          className="flex-1 bg-green-600 text-white text-sm rounded px-4 py-2.5 hover:bg-green-700 disabled:opacity-40"
        >
          Complete
        </button>
        <button
          onClick={() => act(markNoShow, 'Marked as no-show.')}
          disabled={loading || !current || current.status !== 'CALLED'}
          className="flex-1 border border-gray-300 text-gray-600 text-sm rounded px-4 py-2.5 hover:bg-gray-50 disabled:opacity-40"
        >
          No Show
        </button>
      </div>
    </div>
  )
}