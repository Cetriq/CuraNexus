import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { encountersApi, patientsApi } from '@/api'
import {
  Users,
  Stethoscope,
  ClipboardCheck,
  AlertTriangle,
} from 'lucide-react'

export function DashboardPage() {
  const { data: patients } = useQuery({
    queryKey: ['patients'],
    queryFn: patientsApi.getAll,
  })

  const { data: activeEncounters } = useQuery({
    queryKey: ['encounters', 'active'],
    queryFn: encountersApi.getActive,
  })

  const stats = [
    {
      title: 'Patienter',
      value: patients?.length ?? 0,
      icon: Users,
      color: 'text-blue-500',
    },
    {
      title: 'Aktiva vardkontakter',
      value: activeEncounters?.filter(e => e.status === 'IN_PROGRESS').length ?? 0,
      icon: Stethoscope,
      color: 'text-green-500',
    },
    {
      title: 'Vantande',
      value: activeEncounters?.filter(e => e.status === 'PLANNED').length ?? 0,
      icon: ClipboardCheck,
      color: 'text-yellow-500',
    },
    {
      title: 'Kritiska',
      value: 0,
      icon: AlertTriangle,
      color: 'text-red-500',
    },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          Oversikt over vardenheten
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
            <CardTitle>Aktiva vardkontakter</CardTitle>
          </CardHeader>
          <CardContent>
            {activeEncounters && activeEncounters.length > 0 ? (
              <div className="space-y-3">
                {activeEncounters.slice(0, 5).map((encounter) => (
                  <div
                    key={encounter.id}
                    className="flex items-center justify-between p-3 rounded-lg border"
                  >
                    <div>
                      <p className="font-medium">{encounter.patientName || 'Okand patient'}</p>
                      <p className="text-sm text-muted-foreground">
                        {encounter.chiefComplaint || 'Ingen sokorsak angiven'}
                      </p>
                    </div>
                    <Badge
                      variant={
                        encounter.status === 'IN_PROGRESS'
                          ? 'success'
                          : encounter.status === 'PLANNED'
                          ? 'warning'
                          : 'secondary'
                      }
                    >
                      {encounter.status === 'IN_PROGRESS' ? 'Pagaende' :
                       encounter.status === 'PLANNED' ? 'Planerad' :
                       encounter.status}
                    </Badge>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga aktiva vardkontakter</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Senaste patienterna</CardTitle>
          </CardHeader>
          <CardContent>
            {patients && patients.length > 0 ? (
              <div className="space-y-3">
                {patients.slice(0, 5).map((patient) => (
                  <div
                    key={patient.id}
                    className="flex items-center justify-between p-3 rounded-lg border"
                  >
                    <div>
                      <p className="font-medium">
                        {patient.givenName} {patient.familyName}
                      </p>
                      <p className="text-sm text-muted-foreground font-mono">
                        {patient.personnummer}
                      </p>
                    </div>
                    {patient.protectedIdentity && (
                      <Badge variant="critical">Skyddad</Badge>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga patienter registrerade</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
