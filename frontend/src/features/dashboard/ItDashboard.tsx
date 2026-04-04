import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Server,
  Activity,
  AlertTriangle,
  CheckCircle2,
} from 'lucide-react'

// IT-dashboard: Fokus på struktur & kontroll
// Systemstatus, integrationer, larm
export function ItDashboard() {
  const stats = [
    {
      title: 'Tjänster online',
      value: '18/18',
      icon: Server,
      color: 'text-green-500',
    },
    {
      title: 'API-anrop/min',
      value: 1423,
      icon: Activity,
      color: 'text-blue-500',
    },
    {
      title: 'Aktiva larm',
      value: 0,
      icon: AlertTriangle,
      color: 'text-amber-500',
    },
    {
      title: 'Uptime',
      value: '99.9%',
      icon: CheckCircle2,
      color: 'text-green-500',
    },
  ]

  // Simulerad tjänstestatus
  const services = [
    { name: 'Patient Service', port: 8080, status: 'HEALTHY', latency: '12ms' },
    { name: 'Care-Encounter Service', port: 8081, status: 'HEALTHY', latency: '15ms' },
    { name: 'Journal Service', port: 8082, status: 'HEALTHY', latency: '18ms' },
    { name: 'Task Service', port: 8083, status: 'HEALTHY', latency: '11ms' },
    { name: 'Authorization Service', port: 8084, status: 'HEALTHY', latency: '8ms' },
    { name: 'Integration Gateway', port: 8085, status: 'HEALTHY', latency: '22ms' },
    { name: 'Audit Service', port: 8096, status: 'HEALTHY', latency: '14ms' },
    { name: 'Coding Service', port: 8097, status: 'HEALTHY', latency: '9ms' },
  ]

  // Simulerade integrationer
  const integrations = [
    { name: 'NPÖ (Nationell Patientöversikt)', status: 'CONNECTED', lastSync: '2 min sedan' },
    { name: 'Inera SITHS', status: 'CONNECTED', lastSync: 'Live' },
    { name: 'Läkemedelsverket', status: 'CONNECTED', lastSync: '5 min sedan' },
    { name: 'Försäkringskassan', status: 'CONNECTED', lastSync: '1 timme sedan' },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Systemöversikt</h1>
        <p className="text-muted-foreground">
          Kontroll & struktur - övervaka och förvalta
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
            <CardTitle>Tjänstestatus</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {services.map((service) => (
                <div
                  key={service.name}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{service.name}</p>
                    <p className="text-sm text-muted-foreground font-mono">
                      Port {service.port} - {service.latency}
                    </p>
                  </div>
                  <Badge
                    variant={service.status === 'HEALTHY' ? 'success' : 'critical'}
                  >
                    {service.status === 'HEALTHY' ? 'Online' : 'Offline'}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Externa integrationer</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {integrations.map((integration) => (
                <div
                  key={integration.name}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{integration.name}</p>
                    <p className="text-sm text-muted-foreground">
                      Senaste synk: {integration.lastSync}
                    </p>
                  </div>
                  <Badge
                    variant={integration.status === 'CONNECTED' ? 'success' : 'critical'}
                  >
                    {integration.status === 'CONNECTED' ? 'Ansluten' : 'Frånkopplad'}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
