import { useQuery } from '@tanstack/react-query'
import { useMemo } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { encountersApi, patientsApi } from '@/api'
import {
  Users,
  Stethoscope,
  FileWarning,
  FlaskConical,
} from 'lucide-react'

// Läkardashboard: Fokus på snabbhet & överblick
// "Mina patienter idag", väntande labbsvar, osignerade anteckningar
export function DoctorDashboard() {
  const { data: activeEncounters } = useQuery({
    queryKey: ['encounters', 'active'],
    queryFn: encountersApi.getActive,
  })

  const { data: patients } = useQuery({
    queryKey: ['patients'],
    queryFn: patientsApi.getAll,
  })

  // Create a map of patientId -> patient name
  const patientNames = useMemo(() => {
    const map = new Map<string, string>()
    patients?.forEach(p => {
      map.set(p.id, `${p.givenName} ${p.familyName}`)
    })
    return map
  }, [patients])

  const inProgress = activeEncounters?.filter(e => e.status === 'IN_PROGRESS') ?? []
  const planned = activeEncounters?.filter(e => e.status === 'PLANNED') ?? []

  const stats = [
    {
      title: 'Mina patienter idag',
      value: inProgress.length,
      icon: Users,
      color: 'text-blue-500',
    },
    {
      title: 'Pågående besök',
      value: inProgress.length,
      icon: Stethoscope,
      color: 'text-green-500',
    },
    {
      title: 'Osignerade anteckningar',
      value: 0,
      icon: FileWarning,
      color: 'text-amber-500',
    },
    {
      title: 'Väntande labbsvar',
      value: 0,
      icon: FlaskConical,
      color: 'text-purple-500',
    },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Läkarvy</h1>
        <p className="text-muted-foreground">
          Snabb överblick - patient & beslut
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
            <CardTitle>Pågående vårdkontakter</CardTitle>
          </CardHeader>
          <CardContent>
            {inProgress.length > 0 ? (
              <div className="space-y-3">
                {inProgress.slice(0, 5).map((encounter) => (
                  <div
                    key={encounter.id}
                    className="flex items-center justify-between p-3 rounded-lg border"
                  >
                    <div>
                      <p className="font-medium">{patientNames.get(encounter.patientId) || 'Okänd patient'}</p>
                      <p className="text-sm text-muted-foreground">
                        {encounter.chiefComplaint || 'Ingen sökorsak angiven'}
                      </p>
                    </div>
                    <Badge variant="success">Pågående</Badge>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga pågående vårdkontakter</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Planerade besök</CardTitle>
          </CardHeader>
          <CardContent>
            {planned.length > 0 ? (
              <div className="space-y-3">
                {planned.slice(0, 5).map((encounter) => (
                  <div
                    key={encounter.id}
                    className="flex items-center justify-between p-3 rounded-lg border"
                  >
                    <div>
                      <p className="font-medium">{patientNames.get(encounter.patientId) || 'Okänd patient'}</p>
                      <p className="text-sm text-muted-foreground">
                        {encounter.chiefComplaint || 'Ingen sökorsak angiven'}
                      </p>
                    </div>
                    <Badge variant="warning">Planerad</Badge>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga planerade besök</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
