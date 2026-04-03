import { useParams, Link } from '@tanstack/react-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { encountersApi } from '@/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { formatDateTime } from '@/lib/utils'
import { ArrowLeft, Play, CheckCircle, Clock, AlertCircle } from 'lucide-react'

export function EncounterDetailPage() {
  const { encounterId } = useParams({ from: '/encounters/$encounterId' })
  const queryClient = useQueryClient()

  const { data: encounter, isLoading: encounterLoading } = useQuery({
    queryKey: ['encounters', encounterId],
    queryFn: () => encountersApi.getById(encounterId),
  })

  const { data: tasks, isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks', 'encounter', encounterId],
    queryFn: () => encountersApi.getTasks(encounterId),
    enabled: !!encounterId,
  })

  const startMutation = useMutation({
    mutationFn: () => encountersApi.start(encounterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounters'] })
    },
  })

  const completeMutation = useMutation({
    mutationFn: () => encountersApi.complete(encounterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounters'] })
    },
  })

  const completeTaskMutation = useMutation({
    mutationFn: (taskId: string) => encountersApi.completeTask(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] })
      queryClient.invalidateQueries({ queryKey: ['encounters'] })
    },
  })

  if (encounterLoading) {
    return <p className="text-muted-foreground">Laddar vardkontakt...</p>
  }

  if (!encounter) {
    return <p className="text-destructive">Vardkontakt hittades ej</p>
  }

  const pendingTasks = tasks?.filter((t) => t.status === 'PENDING' || t.status === 'IN_PROGRESS')
  const completedTasks = tasks?.filter((t) => t.status === 'COMPLETED')

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link to="/encounters">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold tracking-tight">
              {encounter.patientName || 'Okand patient'}
            </h1>
            <Badge
              variant={
                encounter.status === 'IN_PROGRESS' ? 'success' :
                encounter.status === 'COMPLETED' ? 'secondary' :
                encounter.status === 'PLANNED' ? 'warning' :
                'outline'
              }
            >
              {encounter.status === 'IN_PROGRESS' ? 'Pagaende' :
               encounter.status === 'COMPLETED' ? 'Avslutad' :
               encounter.status === 'PLANNED' ? 'Planerad' :
               encounter.status}
            </Badge>
          </div>
          <p className="text-muted-foreground">
            {encounter.chiefComplaint || 'Ingen sokorsak angiven'}
          </p>
        </div>
        <div className="flex gap-2">
          {encounter.status === 'PLANNED' && encounter.readyToStart && (
            <Button onClick={() => startMutation.mutate()} disabled={startMutation.isPending}>
              <Play className="h-4 w-4 mr-2" />
              Starta
            </Button>
          )}
          {encounter.status === 'IN_PROGRESS' && encounter.allTasksCompleted && (
            <Button onClick={() => completeMutation.mutate()} disabled={completeMutation.isPending}>
              <CheckCircle className="h-4 w-4 mr-2" />
              Avsluta
            </Button>
          )}
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Vardkontaktinformation</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Typ</p>
                <p className="font-medium">
                  {encounter.encounterClass === 'OUTPATIENT' ? 'Oppenvard' :
                   encounter.encounterClass === 'INPATIENT' ? 'Slutenvard' :
                   encounter.encounterClass === 'EMERGENCY' ? 'Akut' :
                   encounter.encounterClass}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Enhet</p>
                <p className="font-medium">{encounter.unitName || '-'}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Ansvarig</p>
                <p className="font-medium">{encounter.responsiblePractitionerName || '-'}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Skapad</p>
                <p className="font-medium">{formatDateTime(encounter.createdAt)}</p>
              </div>
              {encounter.startTime && (
                <div>
                  <p className="text-muted-foreground">Startad</p>
                  <p className="font-medium">{formatDateTime(encounter.startTime)}</p>
                </div>
              )}
              {encounter.endTime && (
                <div>
                  <p className="text-muted-foreground">Avslutad</p>
                  <p className="font-medium">{formatDateTime(encounter.endTime)}</p>
                </div>
              )}
            </div>

            <div className="pt-4 border-t flex gap-2">
              {encounter.readyToStart && (
                <Badge variant="info">Redo att starta</Badge>
              )}
              {encounter.allTasksCompleted && (
                <Badge variant="success">Alla uppgifter klara</Badge>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>
              Uppgifter ({pendingTasks?.length ?? 0} kvar)
            </CardTitle>
          </CardHeader>
          <CardContent>
            {tasksLoading ? (
              <p className="text-muted-foreground">Laddar...</p>
            ) : tasks && tasks.length > 0 ? (
              <div className="space-y-3">
                {pendingTasks?.map((task) => (
                  <div
                    key={task.id}
                    className="flex items-center justify-between p-3 rounded-lg border"
                  >
                    <div className="flex items-start gap-3">
                      {task.status === 'IN_PROGRESS' ? (
                        <Clock className="h-5 w-5 text-yellow-500 mt-0.5" />
                      ) : task.blocking ? (
                        <AlertCircle className="h-5 w-5 text-destructive mt-0.5" />
                      ) : (
                        <Clock className="h-5 w-5 text-muted-foreground mt-0.5" />
                      )}
                      <div>
                        <p className="font-medium">{task.title}</p>
                        {task.description && (
                          <p className="text-sm text-muted-foreground">{task.description}</p>
                        )}
                        <div className="flex gap-2 mt-1">
                          <Badge variant={
                            task.priority === 'URGENT' ? 'critical' :
                            task.priority === 'HIGH' ? 'warning' :
                            'outline'
                          }>
                            {task.priority}
                          </Badge>
                          {task.blocking && (
                            <Badge variant="destructive">Blockerande</Badge>
                          )}
                        </div>
                      </div>
                    </div>
                    <Button
                      size="sm"
                      onClick={() => completeTaskMutation.mutate(task.id)}
                      disabled={completeTaskMutation.isPending}
                    >
                      Klar
                    </Button>
                  </div>
                ))}

                {completedTasks && completedTasks.length > 0 && (
                  <div className="pt-4 border-t">
                    <p className="text-sm text-muted-foreground mb-2">
                      Slutforda ({completedTasks.length})
                    </p>
                    {completedTasks.map((task) => (
                      <div
                        key={task.id}
                        className="flex items-center gap-3 p-2 rounded text-muted-foreground"
                      >
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        <span className="line-through">{task.title}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga uppgifter</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
