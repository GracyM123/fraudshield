export interface Transaction {
  id: string
  accountId: string
  amount: number
  currency: string
  merchantName: string
  merchantCategory: string
  countryCode: string
  city: string
  transactionType: string
  flagged: boolean
  riskScore: number
  flagReasons: string | null
  createdAt: string
}

export interface Alert {
  id: string
  transactionId: string
  accountId: string
  alertType: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
  description: string
  riskScore: number
  status: 'OPEN' | 'RESOLVED'
  createdAt: string
}

export interface DashboardStats {
  totalTransactions: number
  flaggedTransactions: number
  openAlerts: number
  last24hTransactions: number
  flagRate: number
  alertsBySeverity: Record<string, number>
  alertsByType: Record<string, number>
}
