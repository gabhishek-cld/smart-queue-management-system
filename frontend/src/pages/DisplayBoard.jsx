import { useState, useEffect, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import { getCounters } from '../api'

export default function DisplayBoard() {
  const [counters, setCounters]   = useState([])
  const [events, setEvents]       = useState([])
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)

  useEffect(() => {
    getCounters()
      .then(r => {
         setCounters(Array.isArray(r.data) ? r.data : [])
      })
      .catch(() => setCounters([]))
  }, [])

  useEffect(() => {
    const client = new Client({
      brokerURL: `ws://${location.host}/ws`,

      onConnect: () => {
        setConnected(true)
        client.subscribe('/topic/board', (msg) => {
          let event
          try {
            event = JSON.parse(msg.body)
          } catch {
            console.warn('Received unparseable WebSocket message:', msg.body)
            return
          }

          setCounters(prev =>
            prev.map(c =>
              c.name === event.counterName
                ? { ...c, currentToken: event.tokenNumber, currentService: event.serviceTypeName }
                : c
            )
          )

          setEvents(prev => [event, ...prev].slice(0, 10))
        })
      },

      onDisconnect: () => setConnected(false),

      onStompError: (frame) => {
        console.error('STOMP broker error:', frame.headers['message'], frame.body)
      },

      reconnectDelay: 5000,
    })

    client.activate()
    clientRef.current = client
    return () => client.deactivate()
  }, [])

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-gray-800">Display Board</h1>
        <span className={`text-xs px-2 py-1 rounded-full ${connected ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
          {connected ? 'Live' : 'Connecting…'}
        </span>
      </div>

      {/* One card per active counter */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        {counters.filter(c => c.status !== 'OFFLINE').map(counter => (
          <div
            key={counter.id}
            className="border border-gray-200 bg-white rounded-lg px-5 py-4 text-center"
          >
            <p className="text-xs text-gray-400 uppercase tracking-wider mb-2">{counter.name}</p>
            {counter.currentToken ? (
              <>
                <p className="text-4xl font-bold text-blue-600">{counter.currentToken}</p>
                <p className="text-xs text-gray-500 mt-1">{counter.currentService || ''}</p>
              </>
            ) : (
              <p className="text-2xl text-gray-300 font-light">—</p>
            )}
            {counter.status === 'PAUSED' && (
              <p className="text-xs mt-2 text-amber-500">Paused</p>
            )}
          </div>
        ))}
      </div>

      {events.length > 0 && (
        <div>
          <h2 className="text-sm font-medium text-gray-600 mb-3">Recent calls</h2>
          <div className="flex flex-col gap-2">
            {events.map((e, i) => (
              <div
                key={i}
                className={`flex items-center justify-between border rounded px-4 py-2 text-sm ${
                  i === 0 ? 'bg-blue-50 border-blue-200' : 'bg-white border-gray-100'
                }`}
              >
                <span className="font-semibold text-gray-800">{e.tokenNumber}</span>
                <span className="text-gray-500">{e.serviceTypeName}</span>
                <span className="text-gray-400">{e.counterName}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {events.length === 0 && (
        <p className="text-sm text-gray-400 text-center mt-16">
          Waiting for tokens to be called…
        </p>
      )}
    </div>
  )
}