import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  FileText,
  FileCode,
  FileBadge,
  CheckCircle2,
} from 'lucide-react'

// Sekreterardashboard: Fokus på dokumentation & struktur
// Kodning, intyg, dokumentationskö
export function SecretaryDashboard() {
  const stats = [
    {
      title: 'Att koda',
      value: 5,
      icon: FileCode,
      color: 'text-blue-500',
    },
    {
      title: 'Väntande intyg',
      value: 3,
      icon: FileBadge,
      color: 'text-amber-500',
    },
    {
      title: 'Diktat att skriva',
      value: 8,
      icon: FileText,
      color: 'text-purple-500',
    },
    {
      title: 'Klara idag',
      value: 12,
      icon: CheckCircle2,
      color: 'text-green-500',
    },
  ]

  // Simulerade kodningsuppgifter
  const codingTasks = [
    { id: 1, patient: 'Eva Eriksson', encounter: 'Akutbesök', date: '2024-01-15', status: 'PENDING' },
    { id: 2, patient: 'Karl Persson', encounter: 'Återbesök', date: '2024-01-15', status: 'PENDING' },
    { id: 3, patient: 'Anna Lindberg', encounter: 'Nybesök', date: '2024-01-14', status: 'PENDING' },
    { id: 4, patient: 'Johan Berg', encounter: 'Kontroll', date: '2024-01-14', status: 'IN_PROGRESS' },
    { id: 5, patient: 'Lisa Svensson', encounter: 'Akutbesök', date: '2024-01-13', status: 'PENDING' },
  ]

  // Simulerade intyg att hantera
  const certificates = [
    { id: 1, patient: 'Maria Andersson', type: 'FK7263', status: 'DRAFT', doctor: 'Dr. Karlsson' },
    { id: 2, patient: 'Per Johansson', type: 'FK7804', status: 'REVIEW', doctor: 'Dr. Lindgren' },
    { id: 3, patient: 'Karin Nilsson', type: 'FK7263', status: 'DRAFT', doctor: 'Dr. Karlsson' },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dokumentation</h1>
        <p className="text-muted-foreground">
          Struktur & kvalitet - kodning och intyg
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
            <CardTitle>Kodningskö</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {codingTasks.map((task) => (
                <div
                  key={task.id}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{task.patient}</p>
                    <p className="text-sm text-muted-foreground">
                      {task.encounter} - {task.date}
                    </p>
                  </div>
                  <Badge
                    variant={task.status === 'IN_PROGRESS' ? 'warning' : 'secondary'}
                  >
                    {task.status === 'IN_PROGRESS' ? 'Pågår' : 'Väntar'}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Intyg att hantera</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {certificates.map((cert) => (
                <div
                  key={cert.id}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{cert.patient}</p>
                    <p className="text-sm text-muted-foreground">
                      {cert.type} - {cert.doctor}
                    </p>
                  </div>
                  <Badge
                    variant={cert.status === 'REVIEW' ? 'warning' : 'secondary'}
                  >
                    {cert.status === 'REVIEW' ? 'Granskning' : 'Utkast'}
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
