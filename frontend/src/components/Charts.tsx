import React from 'react'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts'
import { DashboardStats } from '../types'

interface Props {
  stats: DashboardStats | null
}

const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: '#ef4444',
  HIGH:     '#f97316',
  MEDIUM:   '#eab308',
  LOW:      '#3b82f6',
}

const TYPE_COLORS = ['#6366f1', '#8b5cf6', '#ec4899', '#14b8a6', '#f59e0b']

const CustomTooltip = ({ active, payload, label }: any) => {
  if (!active || !payload?.length) return null
  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2 text-xs">
      <p className="text-gray-400 mb-1">{label}</p>
      {payload.map((p: any) => (
        <p key={p.name} style={{ color: p.color }}>{p.name}: {p.value}</p>
      ))}
    </div>
  )
}

export default function Charts({ stats }: Props) {
  if (!stats) return null

  const severityData = Object.entries(stats.alertsBySeverity).map(([name, value]) => ({ name, value }))
  const typeData = Object.entries(stats.alertsByType).slice(0, 5).map(([name, value]) => ({
    name: name.replace(/_/g, ' '),
    value
  }))

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {/* Alert severity pie */}
      <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-5">
        <h3 className="text-sm font-semibold text-gray-300 mb-4">Alerts by Severity</h3>
        {severityData.length > 0 ? (
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={severityData}
                cx="50%"
                cy="50%"
                innerRadius={55}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
              >
                {severityData.map((entry) => (
                  <Cell key={entry.name} fill={SEVERITY_COLORS[entry.name] ?? '#6b7280'} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
              <Legend
                formatter={(value) => <span className="text-xs text-gray-400">{value}</span>}
              />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-[200px] flex items-center justify-center text-gray-600 text-sm">
            No alerts yet — system is clean
          </div>
        )}
      </div>

      {/* Alert types bar chart */}
      <div className="bg-gray-900/60 border border-gray-800 rounded-xl p-5">
        <h3 className="text-sm font-semibold text-gray-300 mb-4">Alert Types Distribution</h3>
        {typeData.length > 0 ? (
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={typeData} layout="vertical" margin={{ left: 10 }}>
              <XAxis type="number" tick={{ fill: '#6b7280', fontSize: 11 }} />
              <YAxis
                type="category"
                dataKey="name"
                width={100}
                tick={{ fill: '#9ca3af', fontSize: 10 }}
              />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="value" name="Count" radius={[0, 4, 4, 0]}>
                {typeData.map((_, i) => (
                  <Cell key={i} fill={TYPE_COLORS[i % TYPE_COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-[200px] flex items-center justify-center text-gray-600 text-sm">
            Awaiting data...
          </div>
        )}
      </div>
    </div>
  )
}
