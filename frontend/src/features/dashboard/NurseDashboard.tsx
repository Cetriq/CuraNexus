import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { encountersApi } from '@/api'
import {
  ClipboardList,
  Users,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react'

// Sjuksköterskadashboard: Fokus på flöde & uppgifter
// Uppgiftslista, triagekö, statusöversikt patienter
export function NurseDashboard() {
  const { data: activeEncounters } = useQuery({
    queryKey: ['encounters', 'active'],
    queryFn: encountersApi.getActive,
  })

  const planned = activeEncounters?.filter(e => e.status === 'PLANNED') ?? []

  const stats = [
    {
      title: 'Aktiva uppgifter',
      value: 0,
      icon: ClipboardList,
      color: 'text-blue-500',
    },
    {
      title: 'Patienter i kö',
      value: planned.length,
      icon: Users,
      color: 'text-amber-500',
    },
    {
      title: 'Akuta ärenden',
      value: 0,
      icon: AlertCircle,
      color: 'text-red-500',
    },
    {
      title: 'Klara idag',
      value: 0,
      icon: CheckCircle2,
      color: 'text-green-500',
    },
  ]

  // Simulerade uppgifter (ersätts med riktig data från task-service)
  const mockTasks = [
    { id: 1, title: 'Kontrollera vitala parametrar', patient: 'Eva Eriksson', priority: 'HIGH', status: 'PENDING' },
    { id: 2, title: 'Administrera läkemedel', patient: 'Karl Persson', priority: 'MEDIUM', status: 'PENDING' },
    { id: 3, title: 'Ta blodprov', patient: 'Anna Lindberg', priority: 'LOW', status: 'PENDING' },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Sjuksköterskevy</h1>
        <p className="text-muted-foreground">
          Uppgifter & flöde - koordinera vården
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
            <CardTitle>Mina uppgifter</CardTitle>
          </CardHeader>
          <CardContent>
            {mockTasks.length > 0 ? (
              <div className="space-y-3">
                {mockTasks.map((task) => (
                  <div
                    key={task.id}
                    className="flex items-center justify-between p-3 rounded-lg border"
                  >
                    <div>
                      <p className="font-medium">{task.title}</p>
                      <p className="text-sm text-muted-foreground">
                        {task.patient}
                      </p>
                    </div>
                    <Badge
                      variant={
                        task.priority === 'HIGH'
                          ? 'critical'
                          : task.priority === 'MEDIUM'
                          ? 'warning'
                          : 'secondary'
                      }
                    >
                      {task.priority === 'HIGH' ? 'Hög' :
                       task.priority === 'MEDIUM' ? 'Medium' : 'Låg'}
                    </Badge>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga uppgifter just nu</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Patientflöde</CardTitle>
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
                      <p className="font-medium">{encounter.patientName || 'Okänd patient'}</p>
                      <p className="text-sm text-muted-foreground">
                        {encounter.chiefComplaint || 'Väntar på triage'}
                      </p>
                    </div>
                    <Badge
                      variant={
                        encounter.status === 'IN_PROGRESS'
                          ? 'success'
                          : 'warning'
                      }
                    >
                      {encounter.status === 'IN_PROGRESS' ? 'Pågår' : 'Väntar'}
                    </Badge>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga patienter i flödet</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
