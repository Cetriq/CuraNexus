import {
  Users,
  Stethoscope,
  ClipboardList,
  Calendar,
  Pill,
  FileText,
  FlaskConical,
  LayoutDashboard,
  FileCode,
  FileBadge,
  Shield,
  ScrollText,
  Settings,
  Activity,
  BarChart3,
  type LucideIcon,
} from 'lucide-react'
import type { UserRole } from '@/contexts/UserContext'

export interface NavigationItem {
  name: string
  href: string
  icon: LucideIcon
}

// Alla tillgängliga navigationsalternativ
const allNavigation: Record<string, NavigationItem> = {
  dashboard: { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  patients: { name: 'Patienter', href: '/patients', icon: Users },
  encounters: { name: 'Vårdkontakter', href: '/encounters', icon: Stethoscope },
  tasks: { name: 'Uppgifter', href: '/tasks', icon: ClipboardList },
  booking: { name: 'Bokningar', href: '/booking', icon: Calendar },
  medication: { name: 'Läkemedel', href: '/medication', icon: Pill },
  referrals: { name: 'Remisser', href: '/referrals', icon: FileText },
  lab: { name: 'Labb', href: '/lab', icon: FlaskConical },
  coding: { name: 'Kodning', href: '/coding', icon: FileCode },
  certificates: { name: 'Intyg', href: '/certificates', icon: FileBadge },
  consent: { name: 'Samtycke', href: '/consent', icon: Shield },
  audit: { name: 'Loggranskning', href: '/audit', icon: ScrollText },
  admin: { name: 'Admin', href: '/admin', icon: Settings },
  integration: { name: 'Integration', href: '/integration', icon: Activity },
  analytics: { name: 'Analys', href: '/analytics', icon: BarChart3 },
}

// Navigation per roll baserat på intressenter.md
// Varje roll ser endast de moduler som är relevanta för deras arbete
export const navigationByRole: Record<UserRole, NavigationItem[]> = {
  // Specialistläkare: patient & beslut, snabbhet & överblick
  // Moduler: A2 Vårdkontakt, A3 Journal, A7 Läkemedel, A8 Prov
  LAKARE: [
    allNavigation.dashboard,
    allNavigation.patients,
    allNavigation.encounters,
    allNavigation.medication,
    allNavigation.lab,
    allNavigation.referrals,
  ],

  // Sjuksköterska: flöde & uppgifter
  // Moduler: A2 Vårdkontakt, A4 Triage, A5 Bokning, B1 Uppgifter, A13 Formulär
  SJUKSKOTERSKA: [
    allNavigation.dashboard,
    allNavigation.tasks,
    allNavigation.patients,
    allNavigation.encounters,
    allNavigation.booking,
  ],

  // Vårdadministratör: resurser, optimering
  // Moduler: A5 Bokning, A2 Vårdkontakt, A6 Remiss
  VARDADMIN: [
    allNavigation.dashboard,
    allNavigation.booking,
    allNavigation.encounters,
    allNavigation.referrals,
    allNavigation.patients,
  ],

  // Medicinsk sekreterare: dokumentation, struktur
  // Moduler: A3 Journal, B4 Kodning, B5 Intyg
  SEKRETERARE: [
    allNavigation.dashboard,
    allNavigation.patients,
    allNavigation.encounters,
    allNavigation.coding,
    allNavigation.certificates,
  ],

  // Verksamhetschef: statistik, analys
  // Moduler: D3 Analys, B2 Kapacitet
  CHEF: [
    allNavigation.dashboard,
    allNavigation.analytics,
    allNavigation.patients,
    allNavigation.encounters,
  ],

  // Dataskyddsombud: risk, spårbarhet
  // Moduler: C1 Samtycke, C2 Behörighet, C3 Audit
  DPO: [
    allNavigation.dashboard,
    allNavigation.audit,
    allNavigation.consent,
    allNavigation.patients,
  ],

  // Systemförvaltare IT: struktur, kontroll
  // Moduler: E1 Integration, C4 Masterdata, C5 Admin
  IT: [
    allNavigation.dashboard,
    allNavigation.integration,
    allNavigation.admin,
    allNavigation.audit,
  ],
}

export function getNavigationForRole(role: UserRole): NavigationItem[] {
  return navigationByRole[role] || navigationByRole.LAKARE
}
