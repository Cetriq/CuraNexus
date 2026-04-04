import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'

// Roller baserat på intressenter.md
export type UserRole =
  | 'LAKARE'           // Specialistläkare - patient & beslut, snabbhet & överblick
  | 'SJUKSKOTERSKA'    // Sjuksköterska - flöde & uppgifter
  | 'VARDADMIN'        // Vårdadministratör - resurser, optimering
  | 'SEKRETERARE'      // Medicinsk sekreterare - dokumentation, struktur
  | 'CHEF'             // Verksamhetschef - statistik, analys
  | 'DPO'              // Dataskyddsombud - risk, spårbarhet
  | 'IT'               // Systemförvaltare IT - struktur, kontroll

export interface User {
  id: string
  name: string
  initials: string
  hsaId: string
  role: UserRole
  unit: string
}

// Rollmetadata för UI
export const roleMetadata: Record<UserRole, { label: string; description: string; color: string }> = {
  LAKARE: { label: 'Läkare', description: 'Specialistläkare', color: 'bg-blue-500' },
  SJUKSKOTERSKA: { label: 'Sjuksköterska', description: 'Koordinator', color: 'bg-green-500' },
  VARDADMIN: { label: 'Vårdadmin', description: 'Flödesoptimerare', color: 'bg-purple-500' },
  SEKRETERARE: { label: 'Sekreterare', description: 'Medicinsk sekreterare', color: 'bg-amber-500' },
  CHEF: { label: 'Chef', description: 'Verksamhetschef', color: 'bg-indigo-500' },
  DPO: { label: 'DPO', description: 'Dataskyddsombud', color: 'bg-red-500' },
  IT: { label: 'IT', description: 'Systemförvaltare', color: 'bg-slate-500' },
}

// Demo-användare per roll
const demoUsers: Record<UserRole, User> = {
  LAKARE: {
    id: '1',
    name: 'Anna Karlsson',
    initials: 'AK',
    hsaId: 'SE2321000016-1234',
    role: 'LAKARE',
    unit: 'Akutmottagningen',
  },
  SJUKSKOTERSKA: {
    id: '2',
    name: 'Erik Lindgren',
    initials: 'EL',
    hsaId: 'SE2321000016-2345',
    role: 'SJUKSKOTERSKA',
    unit: 'Akutmottagningen',
  },
  VARDADMIN: {
    id: '3',
    name: 'Maria Svensson',
    initials: 'MS',
    hsaId: 'SE2321000016-3456',
    role: 'VARDADMIN',
    unit: 'Bokningscentralen',
  },
  SEKRETERARE: {
    id: '4',
    name: 'Lisa Andersson',
    initials: 'LA',
    hsaId: 'SE2321000016-4567',
    role: 'SEKRETERARE',
    unit: 'Medicinkliniken',
  },
  CHEF: {
    id: '5',
    name: 'Per Johansson',
    initials: 'PJ',
    hsaId: 'SE2321000016-5678',
    role: 'CHEF',
    unit: 'Medicinkliniken',
  },
  DPO: {
    id: '6',
    name: 'Karin Nilsson',
    initials: 'KN',
    hsaId: 'SE2321000016-6789',
    role: 'DPO',
    unit: 'Juridik & Compliance',
  },
  IT: {
    id: '7',
    name: 'Johan Berg',
    initials: 'JB',
    hsaId: 'SE2321000016-7890',
    role: 'IT',
    unit: 'IT-avdelningen',
  },
}

interface UserContextType {
  user: User
  setRole: (role: UserRole) => void
  availableRoles: UserRole[]
}

const UserContext = createContext<UserContextType | undefined>(undefined)

const STORAGE_KEY = 'curanexus-user-role'

export function UserProvider({ children }: { children: ReactNode }) {
  const [role, setRoleState] = useState<UserRole>(() => {
    // Hämta sparad roll från localStorage
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved && saved in demoUsers) {
        return saved as UserRole
      }
    }
    return 'LAKARE' // Default: Läkare
  })

  const user = demoUsers[role]

  const setRole = (newRole: UserRole) => {
    setRoleState(newRole)
    localStorage.setItem(STORAGE_KEY, newRole)
  }

  // Spara roll vid ändring
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, role)
  }, [role])

  const availableRoles: UserRole[] = [
    'LAKARE',
    'SJUKSKOTERSKA',
    'VARDADMIN',
    'SEKRETERARE',
    'CHEF',
    'DPO',
    'IT',
  ]

  return (
    <UserContext.Provider value={{ user, setRole, availableRoles }}>
      {children}
    </UserContext.Provider>
  )
}

export function useUser() {
  const context = useContext(UserContext)
  if (context === undefined) {
    throw new Error('useUser must be used within a UserProvider')
  }
  return context
}
