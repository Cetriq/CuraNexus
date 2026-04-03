import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatPersonnummer(personnummer: string): string {
  if (!personnummer) return ''
  const clean = personnummer.replace(/\D/g, '')
  if (clean.length === 12) {
    return `${clean.slice(0, 8)}-${clean.slice(8)}`
  }
  if (clean.length === 10) {
    return `${clean.slice(0, 6)}-${clean.slice(6)}`
  }
  return personnummer
}

export function formatDate(date: string | Date | null | undefined): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleDateString('sv-SE')
}

export function formatDateTime(date: string | Date | null | undefined): string {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleString('sv-SE')
}
