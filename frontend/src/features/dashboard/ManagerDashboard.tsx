import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { encountersApi, patientsApi } from '@/api'
import {
  BarChart3,
  Users,
  Clock,
  TrendingUp,
} from 'lucide-react'

// Verksamhetschefsdashboard: Fokus på statistik & analys
// KPI-widgets, väntetider, beläggning
export function ManagerDashboard() {
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
      title: 'Totalt antal patienter',
      value: patients?.length ?? 0,
      icon: Users,
      color: 'text-blue-500',
      change: '+12%',
      changeType: 'positive' as const,
    },
    {
      title: 'Aktiva vårdkontakter',
      value: activeEncounters?.length ?? 0,
      icon: BarChart3,
      color: 'text-green-500',
      change: '+5%',
      changeType: 'positive' as const,
    },
    {
      title: 'Genomsnittlig väntetid',
      value: '23 min',
      icon: Clock,
      color: 'text-amber-500',
      change: '-8%',
      changeType: 'positive' as const,
    },
    {
      title: 'Beläggningsgrad',
      value: '78%',
      icon: TrendingUp,
      color: 'text-purple-500',
      change: '+3%',
      changeType: 'neutral' as const,
    },
  ]

  // Simulerad KPI-data
  const weeklyStats = [
    { day: 'Mån', patients: 45, encounters: 38 },
    { day: 'Tis', patients: 52, encounters: 44 },
    { day: 'Ons', patients: 48, encounters: 41 },
    { day: 'Tor', patients: 61, encounters: 55 },
    { day: 'Fre', patients: 42, encounters: 36 },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Verksamhetsöversikt</h1>
        <p className="text-muted-foreground">
          Analys & KPI:er - styr verksamheten
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
              <p className={`text-xs ${
                stat.changeType === 'positive' ? 'text-green-500' :
                'text-muted-foreground'
              }`}>
                {stat.change} från förra veckan
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Veckostatistik</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {weeklyStats.map((day) => (
                <div key={day.day} className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium">{day.day}</span>
                    <span className="text-muted-foreground">
                      {day.patients} patienter / {day.encounters} besök
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <div
                      className="h-2 bg-blue-500 rounded-full"
                      style={{ width: `${(day.patients / 70) * 100}%` }}
                    />
                    <div
                      className="h-2 bg-green-500 rounded-full"
                      style={{ width: `${(day.encounters / 70) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Kvalitetsindikatorer</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-3 rounded-lg border">
                <div>
                  <p className="font-medium">Genomsnittlig vårdtid</p>
                  <p className="text-sm text-muted-foreground">Mål: &lt; 4 timmar</p>
                </div>
                <span className="text-2xl font-bold text-green-500">3.2h</span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg border">
                <div>
                  <p className="font-medium">Patientnöjdhet</p>
                  <p className="text-sm text-muted-foreground">Mål: &gt; 85%</p>
                </div>
                <span className="text-2xl font-bold text-green-500">91%</span>
              </div>
              <div className="flex items-center justify-between p-3 rounded-lg border">
                <div>
                  <p className="font-medium">Återbesök inom 72h</p>
                  <p className="text-sm text-muted-foreground">Mål: &lt; 5%</p>
                </div>
                <span className="text-2xl font-bold text-amber-500">4.8%</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
