import React from 'react'
import { LucideIcon } from 'lucide-react'

interface Props {
  label: string
  value: string | number
  sub?: string
  icon: LucideIcon
  color: 'red' | 'yellow' | 'green' | 'blue'
  pulse?: boolean
}

const colorMap = {
  red:    { bg: 'bg-red-500/10',    border: 'border-red-500/30',    icon: 'text-red-400',    text: 'text-red-400' },
  yellow: { bg: 'bg-yellow-500/10', border: 'border-yellow-500/30', icon: 'text-yellow-400', text: 'text-yellow-400' },
  green:  { bg: 'bg-emerald-500/10',border: 'border-emerald-500/30',icon: 'text-emerald-400',text: 'text-emerald-400' },
  blue:   { bg: 'bg-blue-500/10',   border: 'border-blue-500/30',   icon: 'text-blue-400',   text: 'text-blue-400' },
}

export default function StatCard({ label, value, sub, icon: Icon, color, pulse }: Props) {
  const c = colorMap[color]
  return (
    <div className={`rounded-xl border ${c.border} ${c.bg} p-5 flex items-start gap-4`}>
      <div className={`rounded-lg p-2 ${c.bg} border ${c.border}`}>
        <Icon className={`w-5 h-5 ${c.icon}`} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-xs text-gray-400 uppercase tracking-wider mb-1">{label}</p>
        <div className="flex items-center gap-2">
          <p className={`text-2xl font-bold ${c.text}`}>{value}</p>
          {pulse && (
            <span className="flex h-2 w-2">
              <span className="animate-ping absolute h-2 w-2 rounded-full bg-red-400 opacity-75" />
              <span className="relative rounded-full h-2 w-2 bg-red-500" />
            </span>
          )}
        </div>
        {sub && <p className="text-xs text-gray-500 mt-1">{sub}</p>}
      </div>
    </div>
  )
}
