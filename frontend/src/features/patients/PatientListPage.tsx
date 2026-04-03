import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { patientsApi } from '@/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { formatPersonnummer } from '@/lib/utils'
import { Plus, Search, User, Shield } from 'lucide-react'
import type { CreatePatientRequest } from '@/types'

export function PatientListPage() {
  const [searchQuery, setSearchQuery] = useState('')
  const [showCreateForm, setShowCreateForm] = useState(false)
  const queryClient = useQueryClient()

  const { data: patients, isLoading } = useQuery({
    queryKey: ['patients'],
    queryFn: patientsApi.getAll,
  })

  const createMutation = useMutation({
    mutationFn: patientsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] })
      setShowCreateForm(false)
    },
  })

  const filteredPatients = patients?.filter((patient) => {
    const query = searchQuery.toLowerCase()
    return (
      patient.givenName.toLowerCase().includes(query) ||
      patient.familyName.toLowerCase().includes(query) ||
      patient.personnummer.includes(query)
    )
  })

  const handleCreate = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const formData = new FormData(e.currentTarget)
    const data: CreatePatientRequest = {
      personnummer: formData.get('personnummer') as string,
      givenName: formData.get('givenName') as string,
      familyName: formData.get('familyName') as string,
      gender: formData.get('gender') as 'MALE' | 'FEMALE' | 'OTHER' | 'UNKNOWN',
    }
    createMutation.mutate(data)
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Patienter</h1>
          <p className="text-muted-foreground">
            Hantera patientregister
          </p>
        </div>
        <Button onClick={() => setShowCreateForm(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Ny patient
        </Button>
      </div>

      {showCreateForm && (
        <Card>
          <CardHeader>
            <CardTitle>Registrera ny patient</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleCreate} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium">Personnummer</label>
                  <Input
                    name="personnummer"
                    placeholder="YYYYMMDD-XXXX"
                    required
                  />
                </div>
                <div>
                  <label className="text-sm font-medium">Kon</label>
                  <select
                    name="gender"
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  >
                    <option value="UNKNOWN">Okant</option>
                    <option value="MALE">Man</option>
                    <option value="FEMALE">Kvinna</option>
                    <option value="OTHER">Annat</option>
                  </select>
                </div>
                <div>
                  <label className="text-sm font-medium">Fornamn</label>
                  <Input name="givenName" placeholder="Fornamn" required />
                </div>
                <div>
                  <label className="text-sm font-medium">Efternamn</label>
                  <Input name="familyName" placeholder="Efternamn" required />
                </div>
              </div>
              <div className="flex gap-2">
                <Button type="submit" disabled={createMutation.isPending}>
                  {createMutation.isPending ? 'Sparar...' : 'Spara'}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setShowCreateForm(false)}
                >
                  Avbryt
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Sok patient..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      {isLoading ? (
        <p className="text-muted-foreground">Laddar...</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredPatients?.map((patient) => (
            <Link
              key={patient.id}
              to="/patients/$patientId"
              params={{ patientId: patient.id }}
            >
              <Card className="cursor-pointer hover:border-primary transition-colors">
                <CardContent className="pt-6">
                  <div className="flex items-start gap-4">
                    <div className="h-12 w-12 rounded-full bg-muted flex items-center justify-center">
                      {patient.protectedIdentity ? (
                        <Shield className="h-6 w-6 text-destructive" />
                      ) : (
                        <User className="h-6 w-6 text-muted-foreground" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="font-semibold truncate">
                          {patient.givenName} {patient.familyName}
                        </p>
                        {patient.protectedIdentity && (
                          <Badge variant="critical" className="shrink-0">
                            Skyddad
                          </Badge>
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground font-mono">
                        {formatPersonnummer(patient.personnummer)}
                      </p>
                      {patient.deceased && (
                        <Badge variant="secondary" className="mt-1">
                          Avliden
                        </Badge>
                      )}
                    </div>
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}

      {filteredPatients?.length === 0 && !isLoading && (
        <p className="text-muted-foreground text-center py-8">
          Inga patienter hittades
        </p>
      )}
    </div>
  )
}
