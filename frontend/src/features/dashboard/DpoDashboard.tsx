import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Shield,
  AlertTriangle,
  Eye,
  FileSearch,
} from 'lucide-react'

// DPO-dashboard: Fokus på risk & spårbarhet
// Senaste åtkomstloggar, avvikelser, samtyckeöversikt
export function DpoDashboard() {
  const stats = [
    {
      title: 'Åtkomstloggar idag',
      value: 1247,
      icon: Eye,
      color: 'text-blue-500',
    },
    {
      title: 'Nödöppningar',
      value: 3,
      icon: AlertTriangle,
      color: 'text-amber-500',
    },
    {
      title: 'Avvikelser',
      value: 0,
      icon: Shield,
      color: 'text-red-500',
    },
    {
      title: 'Granskningar',
      value: 12,
      icon: FileSearch,
      color: 'text-green-500',
    },
  ]

  // Simulerade åtkomstloggar
  const recentAccess = [
    { id: 1, user: 'Anna Karlsson', patient: '19850515-1234', action: 'READ', time: '14:32', reason: 'Vårdkontakt' },
    { id: 2, user: 'Erik Lindgren', patient: '19720823-5678', action: 'READ', time: '14:28', reason: 'Vårdkontakt' },
    { id: 3, user: 'Maria Svensson', patient: '19901102-9012', action: 'UPDATE', time: '14:15', reason: 'Bokning' },
    { id: 4, user: 'Johan Berg', patient: '19650417-3456', action: 'EMERGENCY', time: '13:45', reason: 'Nödöppning' },
    { id: 5, user: 'Lisa Andersson', patient: '19880630-7890', action: 'READ', time: '13:30', reason: 'Kodning' },
  ]

  // Simulerade nödöppningar
  const emergencyAccess = [
    { id: 1, user: 'Johan Berg', patient: '19650417-3456', time: '13:45', reason: 'Akut medicinsk situation', reviewed: false },
    { id: 2, user: 'Anna Karlsson', patient: '19780923-1234', time: '10:22', reason: 'Medvetslös patient', reviewed: true },
    { id: 3, user: 'Erik Lindgren', patient: '19550812-5678', time: '08:15', reason: 'Akut allergi', reviewed: true },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dataskydd & Compliance</h1>
        <p className="text-muted-foreground">
          Spårbarhet & risk - säkerställ regelefterlevnad
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <Card key={stat.title}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Senaste åtkomstloggar</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {recentAccess.map((log) => (
                <div
                  key={log.id}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{log.user}</p>
                    <p className="text-sm text-muted-foreground font-mono">
                      {log.patient} - {log.reason}
                    </p>
                  </div>
                  <div className="text-right">
                    <Badge
                      variant={
                        log.action === 'EMERGENCY'
                          ? 'critical'
                          : log.action === 'UPDATE'
                          ? 'warning'
                          : 'secondary'
                      }
                    >
                      {log.action}
                    </Badge>
                    <p className="text-xs text-muted-foreground mt-1">{log.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Nödöppningar att granska</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {emergencyAccess.map((access) => (
                <div
                  key={access.id}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{access.user}</p>
                    <p className="text-sm text-muted-foreground">
                      {access.reason}
                    </p>
                    <p className="text-xs text-muted-foreground font-mono">
                      Patient: {access.patient}
                    </p>
                  </div>
                  <div className="text-right">
                    <Badge
                      variant={access.reviewed ? 'success' : 'warning'}
                    >
                      {access.reviewed ? 'Granskad' : 'Väntar'}
                    </Badge>
                    <p className="text-xs text-muted-foreground mt-1">{access.time}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
