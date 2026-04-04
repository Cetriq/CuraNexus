import { useUser } from '@/contexts/UserContext'
import { DoctorDashboard } from './DoctorDashboard'
import { NurseDashboard } from './NurseDashboard'
import { ManagerDashboard } from './ManagerDashboard'
import { DpoDashboard } from './DpoDashboard'
import { AdminDashboard } from './AdminDashboard'
import { SecretaryDashboard } from './SecretaryDashboard'
import { ItDashboard } from './ItDashboard'

// Huvuddashboard som renderar rollspecifik vy
// Baserat på intressenter.md: "Samma system – olika verkligheter"
export function DashboardPage() {
  const { user } = useUser()

  // Returnera rollspecifik dashboard
  switch (user.role) {
    case 'LAKARE':
      return <DoctorDashboard />
    case 'SJUKSKOTERSKA':
      return <NurseDashboard />
    case 'VARDADMIN':
      return <AdminDashboard />
    case 'SEKRETERARE':
      return <SecretaryDashboard />
    case 'CHEF':
      return <ManagerDashboard />
    case 'DPO':
      return <DpoDashboard />
    case 'IT':
      return <ItDashboard />
    default:
      return <DoctorDashboard />
  }
}
