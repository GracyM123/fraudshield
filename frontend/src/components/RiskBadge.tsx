import React from 'react'

interface Props {
  score: number
  size?: 'sm' | 'md'
}

export default function RiskBadge({ score, size = 'sm' }: Props) {
  const pct = Math.round(score * 100)
  const color = pct >= 75 ? 'text-red-400 bg-red-500/15 border-red-500/30'
              : pct >= 55 ? 'text-orange-400 bg-orange-500/15 border-orange-500/30'
              : pct >= 35 ? 'text-yellow-400 bg-yellow-500/15 border-yellow-500/30'
              :              'text-emerald-400 bg-emerald-500/15 border-emerald-500/30'

  const label = pct >= 75 ? 'CRITICAL' : pct >= 55 ? 'HIGH' : pct >= 35 ? 'MEDIUM' : 'LOW'
  const px = size === 'md' ? 'px-3 py-1 text-sm' : 'px-2 py-0.5 text-xs'

  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border font-mono font-semibold ${color} ${px}`}>
      <span className="w-1.5 h-1.5 rounded-full bg-current" />
      {label} {pct}%
    </span>
  )
}
