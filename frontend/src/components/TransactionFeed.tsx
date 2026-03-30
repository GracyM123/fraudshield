import React from 'react'
import { Transaction } from '../types'
import RiskBadge from './RiskBadge'
import { formatDistanceToNow } from 'date-fns'
import { ShieldAlert, CheckCircle } from 'lucide-react'

interface Props {
  transactions: Transaction[]
  loading: boolean
}

export default function TransactionFeed({ transactions, loading }: Props) {
  if (loading) return (
    <div className="space-y-2">
      {Array.from({ length: 8 }).map((_, i) => (
        <div key={i} className="h-12 rounded-lg bg-gray-800/50 animate-pulse" />
      ))}
    </div>
  )

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-xs text-gray-500 uppercase tracking-wider border-b border-gray-800">
            <th className="text-left py-2 pr-4">Account</th>
            <th className="text-right pr-4">Amount</th>
            <th className="text-left pr-4 hidden md:table-cell">Merchant</th>
            <th className="text-left pr-4 hidden lg:table-cell">Country</th>
            <th className="text-left pr-4">Risk</th>
            <th className="text-left">Time</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-800/50">
          {transactions.map(tx => (
            <tr
              key={tx.id}
              className={`group transition-colors ${
                tx.flagged ? 'bg-red-500/5 hover:bg-red-500/10' : 'hover:bg-gray-800/30'
              }`}
            >
              <td className="py-2.5 pr-4">
                <div className="flex items-center gap-2">
                  {tx.flagged
                    ? <ShieldAlert className="w-3.5 h-3.5 text-red-400 shrink-0" />
                    : <CheckCircle className="w-3.5 h-3.5 text-emerald-500/50 shrink-0" />
                  }
                  <span className="font-mono text-xs text-gray-300">{tx.accountId}</span>
                </div>
              </td>
              <td className="py-2.5 pr-4 text-right">
                <span className={`font-mono font-semibold ${tx.flagged ? 'text-red-300' : 'text-gray-200'}`}>
                  ${Number(tx.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </span>
              </td>
              <td className="py-2.5 pr-4 hidden md:table-cell">
                <span className="text-gray-400 truncate max-w-[120px] block">{tx.merchantName}</span>
              </td>
              <td className="py-2.5 pr-4 hidden lg:table-cell">
                <span className="text-xs font-mono bg-gray-800 text-gray-400 px-1.5 py-0.5 rounded">
                  {tx.countryCode}
                </span>
              </td>
              <td className="py-2.5 pr-4">
                {tx.flagged
                  ? <RiskBadge score={tx.riskScore} />
                  : <span className="text-xs text-gray-600">—</span>
                }
              </td>
              <td className="py-2.5 text-xs text-gray-500">
                {formatDistanceToNow(new Date(tx.createdAt), { addSuffix: true })}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
