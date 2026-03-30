import React from 'react'
import { Alert } from '../types'
import { formatDistanceToNow } from 'date-fns'
import { CheckCheck, AlertTriangle, Flame, Info, Zap } from 'lucide-react'
import { api } from '../services/api'

interface Props {
  alerts: Alert[]
  onResolved: () => void
}

const severityConfig = {
  CRITICAL: { icon: Flame,         color: 'text-red-400',    bg: 'bg-red-500/10 border-red-500/30' },
  HIGH:     { icon: AlertTriangle, color: 'text-orange-400', bg: 'bg-orange-500/10 border-orange-500/30' },
  MEDIUM:   { icon: Zap,           color: 'text-yellow-400', bg: 'bg-yellow-500/10 border-yellow-500/30' },
  LOW:      { icon: Info,          color: 'text-blue-400',   bg: 'bg-blue-500/10 border-blue-500/30' },
}

const typeLabel: Record<string, string> = {
  VELOCITY_ABUSE: '⚡ Velocity',
  STRUCTURING: '🏦 Structuring',
  GEOGRAPHY_RISK: '🌍 Geography',
  STATISTICAL_OUTLIER: '📊 Z-Score',
  SUSPICIOUS_ACTIVITY: '⚠️ Suspicious',
}

export default function AlertPanel({ alerts, onResolved }: Props) {
  const handleResolve = async (id: string) => {
    await api.resolveAlert(id)
    onResolved()
  }

  if (alerts.length === 0) return (
    <div className="flex flex-col items-center justify-center py-12 text-gray-600">
      <CheckCheck className="w-10 h-10 mb-3" />
      <p className="text-sm">No open alerts</p>
    </div>
  )

  return (
    <div className="space-y-3 max-h-[520px] overflow-y-auto scrollbar-thin pr-1">
      {alerts.map(alert => {
        const cfg = severityConfig[alert.severity] ?? severityConfig.LOW
        const Icon = cfg.icon
        return (
          <div key={alert.id} className={`rounded-lg border p-3.5 ${cfg.bg}`}>
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-start gap-2.5 min-w-0">
                <Icon className={`w-4 h-4 mt-0.5 shrink-0 ${cfg.color}`} />
                <div className="min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className={`text-xs font-bold ${cfg.color}`}>{alert.severity}</span>
                    <span className="text-xs text-gray-500">
                      {typeLabel[alert.alertType] ?? alert.alertType}
                    </span>
                    <span className="text-xs font-mono text-gray-600">{alert.accountId}</span>
                  </div>
                  <p className="text-xs text-gray-400 mt-1 leading-relaxed line-clamp-2">
                    {alert.description}
                  </p>
                  <p className="text-xs text-gray-600 mt-1">
                    {formatDistanceToNow(new Date(alert.createdAt), { addSuffix: true })}
                    {' · '}
                    <span className="font-mono">Risk {Math.round(alert.riskScore * 100)}%</span>
                  </p>
                </div>
              </div>
              {alert.status === 'OPEN' && (
                <button
                  onClick={() => handleResolve(alert.id)}
                  className="shrink-0 text-xs text-gray-500 hover:text-emerald-400 border border-gray-700 hover:border-emerald-500/50 rounded px-2 py-1 transition-colors"
                >
                  Resolve
                </button>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}
