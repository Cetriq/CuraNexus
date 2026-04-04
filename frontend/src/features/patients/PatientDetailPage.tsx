import { useParams, Link } from '@tanstack/react-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { patientsApi, encountersApi } from '@/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { formatPersonnummer, formatDateTime } from '@/lib/utils'
import { ArrowLeft, Plus, User, Shield, Phone, Mail, MapPin } from 'lucide-react'
import type { CreateEncounterRequest } from '@/types'

export function PatientDetailPage() {
  const { patientId } = useParams({ from: '/patients/$patientId' })
  const queryClient = useQueryClient()

  const { data: patient, isLoading: patientLoading } = useQuery({
    queryKey: ['patients', patientId],
    queryFn: () => patientsApi.getById(patientId),
  })

  const { data: encounters, isLoading: encountersLoading } = useQuery({
    queryKey: ['encounters', 'patient', patientId],
    queryFn: () => encountersApi.getByPatient(patientId),
    enabled: !!patientId,
  })

  const createEncounterMutation = useMutation({
    mutationFn: encountersApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['encounters'] })
    },
  })

  const handleCreateEncounter = () => {
    if (!patient) return
    const data: CreateEncounterRequest = {
      patientId: patient.id,
      patientPersonnummer: patient.personnummer,
      patientName: `${patient.givenName} ${patient.familyName}`,
      encounterClass: 'OUTPATIENT',
      unitId: '00000000-0000-0000-0000-000000000001',
      unitName: 'Mottagning 1',
    }
    createEncounterMutation.mutate(data)
  }

  if (patientLoading) {
    return <p className="text-muted-foreground">Laddar patient...</p>
  }

  if (!patient) {
    return <p className="text-destructive">Patient hittades ej</p>
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link to="/patients">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold tracking-tight">
              {patient.givenName} {patient.familyName}
            </h1>
            {patient.protectedIdentity && (
              <Badge variant="critical">Skyddad identitet</Badge>
            )}
            {patient.deceased && (
              <Badge variant="secondary">Avliden</Badge>
            )}
          </div>
          <p className="text-muted-foreground font-mono">
            {formatPersonnummer(patient.personnummer)}
          </p>
        </div>
        <Button onClick={handleCreateEncounter} disabled={createEncounterMutation.isPending}>
          <Plus className="h-4 w-4 mr-2" />
          Ny vårdkontakt
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              {patient.protectedIdentity ? (
                <Shield className="h-5 w-5 text-destructive" />
              ) : (
                <User className="h-5 w-5" />
              )}
              Patientuppgifter
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Kön</p>
                <p className="font-medium">
                  {patient.gender === 'MALE' ? 'Man' :
                   patient.gender === 'FEMALE' ? 'Kvinna' :
                   patient.gender === 'OTHER' ? 'Annat' : 'Okänt'}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Födelsedatum</p>
                <p className="font-medium">{patient.dateOfBirth || '-'}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Registrerad</p>
                <p className="font-medium">{formatDateTime(patient.createdAt)}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Uppdaterad</p>
                <p className="font-medium">{formatDateTime(patient.updatedAt)}</p>
              </div>
            </div>

            {patient.address && (
              <div className="pt-4 border-t">
                <div className="flex items-start gap-2">
                  <MapPin className="h-4 w-4 mt-0.5 text-muted-foreground" />
                  <div>
                    <p>{patient.address.streetAddress}</p>
                    <p>{patient.address.postalCode} {patient.address.city}</p>
                  </div>
                </div>
              </div>
            )}

            {patient.telecom && patient.telecom.length > 0 && (
              <div className="pt-4 border-t space-y-2">
                {patient.telecom.map((contact, i) => (
                  <div key={i} className="flex items-center gap-2">
                    {contact.system === 'phone' && <Phone className="h-4 w-4 text-muted-foreground" />}
                    {contact.system === 'email' && <Mail className="h-4 w-4 text-muted-foreground" />}
                    <span>{contact.value}</span>
                    {contact.use && (
                      <Badge variant="outline" className="text-xs">
                        {contact.use === 'home' ? 'Hem' :
                         contact.use === 'work' ? 'Arbete' :
                         contact.use === 'mobile' ? 'Mobil' : contact.use}
                      </Badge>
                    )}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Vårdkontakter</CardTitle>
          </CardHeader>
          <CardContent>
            {encountersLoading ? (
              <p className="text-muted-foreground">Laddar...</p>
            ) : encounters && encounters.length > 0 ? (
              <div className="space-y-3">
                {encounters.map((encounter) => (
                  <Link
                    key={encounter.id}
                    to="/encounters/$encounterId"
                    params={{ encounterId: encounter.id }}
                    className="block"
                  >
                    <div className="flex items-center justify-between p-3 rounded-lg border hover:bg-accent transition-colors">
                      <div>
                        <p className="font-medium">
                          {encounter.chiefComplaint || 'Ingen sökorsak'}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {formatDateTime(encounter.createdAt)}
                        </p>
                      </div>
                      <Badge
                        variant={
                          encounter.status === 'IN_PROGRESS' ? 'success' :
                          encounter.status === 'FINISHED' || encounter.status === 'COMPLETED' ? 'secondary' :
                          encounter.status === 'PLANNED' ? 'warning' :
                          'outline'
                        }
                      >
                        {encounter.status === 'IN_PROGRESS' ? 'Pågående' :
                         encounter.status === 'FINISHED' || encounter.status === 'COMPLETED' ? 'Avslutad' :
                         encounter.status === 'PLANNED' ? 'Planerad' :
                         encounter.status === 'CANCELLED' ? 'Makulerad' :
                         encounter.status}
                      </Badge>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga vårdkontakter</p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
