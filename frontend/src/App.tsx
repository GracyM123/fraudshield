import React, { useEffect, useState, useCallback } from 'react'
import { Shield, Activity, Bell, RefreshCw, Wifi } from 'lucide-react'
import { Transaction, Alert, DashboardStats } from './types'
import { api } from './services/api'
import StatCard from './components/StatCard'
import TransactionFeed from './components/TransactionFeed'
import AlertPanel from './components/AlertPanel'
import Charts from './components/Charts'

type Tab = 'live' | 'flagged' | 'alerts'

export default function App() {
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [flagged, setFlagged] = useState<Transaction[]>([])
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [tab, setTab] = useState<Tab>('live')
  const [loading, setLoading] = useState(true)
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date())
  const [connected, setConnected] = useState(true)

  const refresh = useCallback(async () => {
    try {
      const [txs, flags, alts, st] = await Promise.all([
        api.getTransactions(),
        api.getFlagged(),
        api.getOpenAlerts(),
        api.getStats(),
      ])
      setTransactions(txs)
      setFlagged(flags)
      setAlerts(alts)
      setStats(st)
      setLastUpdated(new Date())
      setConnected(true)
    } catch {
      setConnected(false)
    } finally {
      setLoading(false)
    }
  }, [])

  // Initial load + polling every 3 seconds for live feel
  useEffect(() => {
    refresh()
    const interval = setInterval(refresh, 3000)
    return () => clearInterval(interval)
  }, [refresh])

  const tabList: { key: Tab; label: string; count?: number }[] = [
    { key: 'live',    label: 'Live Feed',    count: transactions.length },
    { key: 'flagged', label: 'Flagged',       count: flagged.length },
    { key: 'alerts',  label: 'Open Alerts',   count: alerts.length },
  ]

  return (
    <div className="min-h-screen bg-gray-950">
      {/* Header */}
      <header className="border-b border-gray-800 bg-gray-900/80 backdrop-blur sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-red-500/10 border border-red-500/30 rounded-lg">
              <Shield className="w-5 h-5 text-red-400" />
            </div>
            <div>
              <h1 className="text-base font-bold text-white tracking-tight">FraudShield</h1>
              <p className="text-xs text-gray-500">Real-Time Transaction Anomaly Detection</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1.5 text-xs">
              <Wifi className={`w-3.5 h-3.5 ${connected ? 'text-emerald-400' : 'text-red-400'}`} />
              <span className={connected ? 'text-emerald-400' : 'text-red-400'}>
                {connected ? 'Live' : 'Disconnected'}
              </span>
            </div>
            <div className="text-xs text-gray-600">
              Updated {lastUpdated.toLocaleTimeString()}
            </div>
            <button
              onClick={refresh}
              className="p-1.5 text-gray-500 hover:text-gray-300 transition-colors"
              title="Refresh"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-6 space-y-6">

        {/* Stats row */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard
            label="Total Transactions"
            value={stats?.totalTransactions.toLocaleString() ?? '—'}
            sub="All time"
            icon={Activity}
            color="blue"
          />
          <StatCard
            label="Flagged"
            value={stats?.flaggedTransactions.toLocaleString() ?? '—'}
            sub={stats ? `${stats.flagRate.toFixed(1)}% flag rate` : ''}
            icon={Shield}
            color="red"
            pulse={!!stats && stats.flaggedTransactions > 0}
          />
          <StatCard
            label="Open Alerts"
            value={stats?.openAlerts.toLocaleString() ?? '—'}
            sub="Awaiting review"
            icon={Bell}
            color="yellow"
          />
          <StatCard
            label="Last 24 Hours"
            value={stats?.last24hTransactions.toLocaleString() ?? '—'}
            sub="Transaction volume"
            icon={Activity}
            color="green"
          />
        </div>

        {/* Charts */}
        <Charts stats={stats} />

        {/* Main panel */}
        <div className="bg-gray-900/60 border border-gray-800 rounded-xl">
          {/* Tabs */}
          <div className="flex border-b border-gray-800">
            {tabList.map(({ key, label, count }) => (
              <button
                key={key}
                onClick={() => setTab(key)}
                className={`flex items-center gap-2 px-5 py-3.5 text-sm font-medium transition-colors border-b-2 -mb-px ${
                  tab === key
                    ? 'border-blue-500 text-blue-400'
                    : 'border-transparent text-gray-500 hover:text-gray-300'
                }`}
              >
                {label}
                {count !== undefined && count > 0 && (
                  <span className={`text-xs rounded-full px-1.5 py-0.5 font-mono ${
                    tab === key ? 'bg-blue-500/20 text-blue-300' : 'bg-gray-800 text-gray-500'
                  }`}>
                    {count}
                  </span>
                )}
              </button>
            ))}
          </div>

          {/* Panel body */}
          <div className="p-5">
            {tab === 'live' && (
              <TransactionFeed transactions={transactions} loading={loading} />
            )}
            {tab === 'flagged' && (
              <TransactionFeed transactions={flagged} loading={loading} />
            )}
            {tab === 'alerts' && (
              <AlertPanel alerts={alerts} onResolved={refresh} />
            )}
          </div>
        </div>

        {/* Footer / tech stack callout */}
        <div className="text-center py-4 border-t border-gray-800/50">
          <p className="text-xs text-gray-600">
            Built with{' '}
            <span className="text-gray-500">Java 17 · Spring Boot 3 · PostgreSQL · React · TypeScript · Docker</span>
            {' '}· Z-Score + Velocity + Structuring + Geography anomaly detection
          </p>
        </div>
      </main>
    </div>
  )
}
