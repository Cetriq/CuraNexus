import { useState } from 'react'
import { useParams, Link } from '@tanstack/react-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { encountersApi, journalApi, patientsApi } from '@/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { formatDateTime } from '@/lib/utils'
import { ArrowLeft, Play, CheckCircle, Clock, AlertCircle, FileText, Plus, Pen, Activity } from 'lucide-react'
import type { NoteType, NoteStatus } from '@/types'

// Mock current user - replace with actual auth context
const CURRENT_USER = {
  id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  name: 'Anna Karlsson',
}

const NOTE_TYPE_LABELS: Record<NoteType, string> = {
  ADMISSION: 'Inskrivning',
  PROGRESS: 'Daganteckning',
  CONSULTATION: 'Konsultation',
  DISCHARGE: 'Utskrivning',
  PROCEDURE: 'Åtgärd',
  NURSING: 'Omvårdnad',
  OTHER: 'Övrigt',
}

const NOTE_STATUS_LABELS: Record<NoteStatus, string> = {
  DRAFT: 'Utkast',
  FINAL: 'Signerad',
  AMENDED: 'Ändrad',
  CANCELLED: 'Makulerad',
}

export function EncounterDetailPage() {
  const { encounterId } = useParams({ from: '/encounters/$encounterId' })
  const queryClient = useQueryClient()
  const [showNoteForm, setShowNoteForm] = useState(false)
  const [noteContent, setNoteContent] = useState('')
  const [noteTitle, setNoteTitle] = useState('')
  const [noteType, setNoteType] = useState<NoteType>('PROGRESS')

  const { data: encounter, isLoading: encounterLoading } = useQuery({
    queryKey: ['encounters', encounterId],
    queryFn: () => encountersApi.getById(encounterId),
  })

  const { data: patient } = useQuery({
    queryKey: ['patients', encounter?.patientId],
    queryFn: () => patientsApi.getById(encounter!.patientId),
    enabled: !!encounter?.patientId,
  })

  const patientName = patient ? `${patient.givenName} ${patient.familyName}` : 'Laddar...'

  const { data: notes, isLoading: notesLoading } = useQuery({
    queryKey: ['notes', 'encounter', encounterId],
    queryFn: () => journalApi.getNotesByEncounter(encounterId),
    enabled: !!encounterId,
  })

  const { data: diagnoses } = useQuery({
    queryKey: ['diagnoses', 'encounter', encounterId],
    queryFn: () => journalApi.getDiagnosesByEncounter(encounterId),
    enabled: !!encounterId,
  })

  const { data: observations } = useQuery({
    queryKey: ['observations', 'encounter', encounterId],
    queryFn: () => journalApi.getObservationsByEncounter(encounterId),
    enabled: !!encounterId,
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

  const createNoteMutation = useMutation({
    mutationFn: () => journalApi.createNote({
      encounterId,
      patientId: encounter!.patientId,
      type: noteType,
      authorId: CURRENT_USER.id,
      authorName: CURRENT_USER.name,
      title: noteTitle || undefined,
      content: noteContent,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
      setShowNoteForm(false)
      setNoteContent('')
      setNoteTitle('')
    },
  })

  const signNoteMutation = useMutation({
    mutationFn: (noteId: string) => journalApi.signNote(noteId, {
      signedById: CURRENT_USER.id,
      signedByName: CURRENT_USER.name,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
    },
  })

  if (encounterLoading) {
    return <p className="text-muted-foreground">Laddar vårdkontakt...</p>
  }

  if (!encounter) {
    return <p className="text-destructive">Vårdkontakt hittades ej</p>
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
              {patientName}
            </h1>
            <Badge
              variant={
                encounter.status === 'IN_PROGRESS' ? 'success' :
                encounter.status === 'COMPLETED' ? 'secondary' :
                encounter.status === 'PLANNED' ? 'warning' :
                'outline'
              }
            >
              {encounter.status === 'IN_PROGRESS' ? 'Pågående' :
               encounter.status === 'COMPLETED' ? 'Avslutad' :
               encounter.status === 'PLANNED' ? 'Planerad' :
               encounter.status}
            </Badge>
          </div>
          <p className="text-muted-foreground">
            {encounter.chiefComplaint || 'Ingen sökorsak angiven'}
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
            <CardTitle>Vårdkontaktinformation</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Typ</p>
                <p className="font-medium">
                  {encounter.encounterClass === 'OUTPATIENT' ? 'Öppenvård' :
                   encounter.encounterClass === 'INPATIENT' ? 'Slutenvård' :
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
                      Slutförda ({completedTasks.length})
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

      {/* Journal section */}
      <div className="grid gap-6 md:grid-cols-3">
        {/* Notes */}
        <Card className="md:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              Anteckningar
            </CardTitle>
            <Button size="sm" onClick={() => setShowNoteForm(!showNoteForm)}>
              <Plus className="h-4 w-4 mr-1" />
              Ny anteckning
            </Button>
          </CardHeader>
          <CardContent className="space-y-4">
            {showNoteForm && (
              <div className="p-4 border rounded-lg space-y-3 bg-muted/50">
                <div className="flex gap-3">
                  <select
                    value={noteType}
                    onChange={(e) => setNoteType(e.target.value as NoteType)}
                    className="px-3 py-2 border rounded-md text-sm"
                  >
                    {Object.entries(NOTE_TYPE_LABELS).map(([value, label]) => (
                      <option key={value} value={value}>{label}</option>
                    ))}
                  </select>
                  <input
                    type="text"
                    placeholder="Rubrik (valfritt)"
                    value={noteTitle}
                    onChange={(e) => setNoteTitle(e.target.value)}
                    className="flex-1 px-3 py-2 border rounded-md text-sm"
                  />
                </div>
                <textarea
                  placeholder="Skriv anteckning..."
                  value={noteContent}
                  onChange={(e) => setNoteContent(e.target.value)}
                  rows={4}
                  className="w-full px-3 py-2 border rounded-md text-sm"
                />
                <div className="flex justify-end gap-2">
                  <Button variant="outline" size="sm" onClick={() => setShowNoteForm(false)}>
                    Avbryt
                  </Button>
                  <Button
                    size="sm"
                    onClick={() => createNoteMutation.mutate()}
                    disabled={!noteContent.trim() || createNoteMutation.isPending}
                  >
                    Spara utkast
                  </Button>
                </div>
              </div>
            )}

            {notesLoading ? (
              <p className="text-muted-foreground">Laddar anteckningar...</p>
            ) : notes && notes.length > 0 ? (
              <div className="space-y-3">
                {notes.map((note) => (
                  <div key={note.id} className="p-4 border rounded-lg">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <div className="flex items-center gap-2">
                          <Badge variant="outline">{NOTE_TYPE_LABELS[note.type]}</Badge>
                          <Badge variant={
                            note.status === 'FINAL' ? 'success' :
                            note.status === 'DRAFT' ? 'warning' :
                            note.status === 'CANCELLED' ? 'destructive' :
                            'secondary'
                          }>
                            {NOTE_STATUS_LABELS[note.status]}
                          </Badge>
                        </div>
                        {note.title && (
                          <p className="font-medium mt-1">{note.title}</p>
                        )}
                      </div>
                      {note.status === 'DRAFT' && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => signNoteMutation.mutate(note.id)}
                          disabled={signNoteMutation.isPending}
                        >
                          <Pen className="h-4 w-4 mr-1" />
                          Signera
                        </Button>
                      )}
                    </div>
                    <p className="text-sm whitespace-pre-wrap">{note.content}</p>
                    <div className="flex items-center gap-4 mt-3 text-xs text-muted-foreground">
                      <span>{note.authorName}</span>
                      <span>{formatDateTime(note.createdAt)}</span>
                      {note.signedByName && (
                        <span>Signerad av {note.signedByName}</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga anteckningar</p>
            )}
          </CardContent>
        </Card>

        {/* Diagnoses & Observations */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Diagnoser</CardTitle>
            </CardHeader>
            <CardContent>
              {diagnoses && diagnoses.length > 0 ? (
                <div className="space-y-2">
                  {diagnoses.map((d) => (
                    <div key={d.id} className="p-2 border rounded text-sm">
                      <div className="flex items-center gap-2">
                        <Badge variant={d.type === 'PRINCIPAL' ? 'default' : 'outline'} className="text-xs">
                          {d.code}
                        </Badge>
                        <span className="text-xs text-muted-foreground">{d.type}</span>
                      </div>
                      <p className="mt-1">{d.displayText}</p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">Inga diagnoser</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2">
                <Activity className="h-4 w-4" />
                Observationer
              </CardTitle>
            </CardHeader>
            <CardContent>
              {observations && observations.length > 0 ? (
                <div className="space-y-2">
                  {observations.map((o) => (
                    <div key={o.id} className={`p-2 border rounded text-sm ${o.isCritical ? 'border-red-500 bg-red-50' : ''}`}>
                      <div className="flex justify-between">
                        <span className="font-medium">{o.displayText}</span>
                        {o.isCritical && (
                          <Badge variant="critical" className="text-xs">Kritisk</Badge>
                        )}
                      </div>
                      <p className="text-lg font-bold">
                        {o.valueNumeric ?? o.valueText}
                        {o.unit && <span className="text-sm font-normal text-muted-foreground ml-1">{o.unit}</span>}
                      </p>
                      {o.referenceRangeLow !== undefined && o.referenceRangeHigh !== undefined && (
                        <p className="text-xs text-muted-foreground">
                          Ref: {o.referenceRangeLow} - {o.referenceRangeHigh}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">Inga observationer</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
