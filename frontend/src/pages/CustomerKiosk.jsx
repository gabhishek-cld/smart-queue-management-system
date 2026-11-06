import { useState, useEffect } from 'react'
import { getServices, createTicket } from '../api'

export default function CustomerKiosk() {
  const [services, setServices]       = useState([])
  const [selected, setSelected]       = useState(null)
  const [customerName, setCustomerName] = useState('')
  const [ticket, setTicket]           = useState(null)
  const [loading, setLoading]         = useState(false)
  const [error, setError]             = useState('')

  useEffect(() => {
    getServices().then(res => setServices(res.data)).catch(() => setError('Failed to load services.'))
  }, [])

  async function handleGetTicket() {
    if (!selected) return
    setLoading(true)
    setError('')
    try {
      const res = await createTicket(selected.id, customerName || null)
      setTicket(res.data)
    } catch {
      setError('Could not generate ticket. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  function reset() {
    setTicket(null)
    setSelected(null)
    setCustomerName('')
  }

  // Show the issued ticket
  if (ticket) {
    return (
      <div className="max-w-sm mx-auto mt-16 text-center">
        <p className="text-sm text-gray-500 mb-2">Your token number</p>
        <div className="text-6xl font-bold text-blue-600 mb-4">{ticket.tokenNumber}</div>
        <p className="text-sm text-gray-700 mb-1">{ticket.serviceTypeName}</p>
        <p className="text-sm text-gray-500">
          {ticket.positionInQueue === 0
            ? 'You are next!'
            : `${ticket.positionInQueue} person(s) ahead of you`}
        </p>
        <p className="text-sm text-gray-500 mb-8">
          Estimated wait: <span className="font-medium">{ticket.estimatedWaitMinutes} min</span>
        </p>
        {ticket.customerName && (
          <p className="text-sm text-gray-400 mb-4">Checked in as: {ticket.customerName}</p>
        )}
        <button
          onClick={reset}
          className="text-sm text-blue-600 hover:underline"
        >
          ← Get another ticket
        </button>
      </div>
    )
  }

  return (
    <div className="max-w-lg mx-auto">
      <h1 className="text-xl font-semibold text-gray-800 mb-2">Welcome</h1>
      <p className="text-sm text-gray-500 mb-6">Select the service you need and we will issue you a token.</p>

      {error && <p className="text-sm text-red-500 mb-4">{error}</p>}

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-6">
        {services.map(service => (
          <button
            key={service.id}
            onClick={() => setSelected(service)}
            className={`text-left border rounded-lg px-4 py-3 transition-all ${
              selected?.id === service.id
                ? 'border-blue-500 bg-blue-50'
                : 'border-gray-200 bg-white hover:border-gray-400'
            }`}
          >
            <p className="text-sm font-medium text-gray-800">{service.name}</p>
            <p className="text-xs text-gray-400 mt-1">
              ~{service.avgServiceMinutes} min avg
              {service.priorityWeight >= 8 && (
                <span className="ml-2 text-amber-500 font-medium">Priority</span>
              )}
            </p>
          </button>
        ))}
      </div>

      {/* Optional virtual check-in name */}
      <div className="mb-4">
        <label className="block text-sm text-gray-600 mb-1">
          Your name <span className="text-gray-400">(optional)</span>
        </label>
        <input
          type="text"
          value={customerName}
          onChange={e => setCustomerName(e.target.value)}
          placeholder="Virtual check-in"
          className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-400"
        />
      </div>

      <button
        onClick={handleGetTicket}
        disabled={!selected || loading}
        className="w-full bg-blue-600 text-white text-sm rounded px-4 py-2.5 hover:bg-blue-700 disabled:opacity-40"
      >
        {loading ? 'Issuing ticket…' : 'Get Ticket'}
      </button>
    </div>
  )
}
