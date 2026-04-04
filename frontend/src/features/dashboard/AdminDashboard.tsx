import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { encountersApi } from '@/api'
import {
  Calendar,
  Clock,
  Users,
  CheckCircle2,
} from 'lucide-react'

// Vårdadministratörsdashboard: Fokus på resurser & optimering
// Bokningsöversikt, kapacitet, köhantering
export function AdminDashboard() {
  const { data: activeEncounters } = useQuery({
    queryKey: ['encounters', 'active'],
    queryFn: encountersApi.getActive,
  })

  const planned = activeEncounters?.filter(e => e.status === 'PLANNED') ?? []

  const stats = [
    {
      title: 'Bokningar idag',
      value: 24,
      icon: Calendar,
      color: 'text-blue-500',
    },
    {
      title: 'Väntande i kö',
      value: planned.length,
      icon: Clock,
      color: 'text-amber-500',
    },
    {
      title: 'Lediga tider',
      value: 8,
      icon: Users,
      color: 'text-green-500',
    },
    {
      title: 'Genomförda',
      value: 16,
      icon: CheckCircle2,
      color: 'text-purple-500',
    },
  ]

  // Simulerade bokningar
  const upcomingBookings = [
    { id: 1, patient: 'Eva Eriksson', time: '14:30', type: 'Återbesök', doctor: 'Dr. Karlsson' },
    { id: 2, patient: 'Karl Persson', time: '15:00', type: 'Nybesök', doctor: 'Dr. Lindgren' },
    { id: 3, patient: 'Anna Lindberg', time: '15:30', type: 'Provtagning', doctor: 'SSK Svensson' },
    { id: 4, patient: 'Johan Berg', time: '16:00', type: 'Kontroll', doctor: 'Dr. Karlsson' },
  ]

  // Simulerad kapacitet
  const resourceCapacity = [
    { resource: 'Mottagningsrum 1', booked: 7, total: 8 },
    { resource: 'Mottagningsrum 2', booked: 5, total: 8 },
    { resource: 'Akutrum', booked: 3, total: 4 },
    { resource: 'Provtagning', booked: 12, total: 16 },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Bokningscentral</h1>
        <p className="text-muted-foreground">
          Resursoptimering - hantera bokningar och kapacitet
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
            <CardTitle>Kommande bokningar</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {upcomingBookings.map((booking) => (
                <div
                  key={booking.id}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{booking.patient}</p>
                    <p className="text-sm text-muted-foreground">
                      {booking.type} - {booking.doctor}
                    </p>
                  </div>
                  <Badge variant="secondary">{booking.time}</Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Resurskapacitet</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {resourceCapacity.map((resource) => {
                const percentage = (resource.booked / resource.total) * 100
                return (
                  <div key={resource.resource} className="space-y-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="font-medium">{resource.resource}</span>
                      <span className="text-muted-foreground">
                        {resource.booked}/{resource.total}
                      </span>
                    </div>
                    <div className="h-2 bg-muted rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          percentage > 90 ? 'bg-red-500' :
                          percentage > 70 ? 'bg-amber-500' :
                          'bg-green-500'
                        }`}
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                  </div>
                )
              })}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
