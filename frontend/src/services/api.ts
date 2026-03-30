import { Transaction, Alert, DashboardStats } from '../types'

const BASE = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api`
  : '/api'

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`)
  if (!res.ok) throw new Error(`API error ${res.status}`)
  return res.json()
}

async function patch<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, { method: 'PATCH' })
  if (!res.ok) throw new Error(`API error ${res.status}`)
  return res.json()
}

export const api = {
  getTransactions: () => get<Transaction[]>('/transactions'),
  getFlagged:      () => get<Transaction[]>('/transactions/flagged'),
  getAlerts:       () => get<Alert[]>('/alerts'),
  getOpenAlerts:   () => get<Alert[]>('/alerts/open'),
  getStats:        () => get<DashboardStats>('/stats'),
  resolveAlert:    (id: string) => patch<Alert>(`/alerts/${id}/resolve`),
}
