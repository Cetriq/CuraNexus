import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { encountersApi, patientsApi } from '@/api'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { formatDateTime } from '@/lib/utils'

export function EncounterListPage() {
  const { data: encounters, isLoading } = useQuery({
    queryKey: ['encounters'],
    queryFn: encountersApi.getAll,
  })

  const { data: patients } = useQuery({
    queryKey: ['patients'],
    queryFn: patientsApi.getAll,
  })

  const patientNames = useMemo(() => {
    const map = new Map<string, string>()
    patients?.forEach(p => {
      map.set(p.id, `${p.givenName} ${p.familyName}`)
    })
    return map
  }, [patients])

  const activeEncounters = encounters?.filter(
    (e) => e.status === 'IN_PROGRESS' || e.status === 'PLANNED'
  )
  const completedEncounters = encounters?.filter(
    (e) => e.status === 'COMPLETED'
  )

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Vårdkontakter</h1>
        <p className="text-muted-foreground">Hantera pågående och avslutade vårdkontakter</p>
      </div>

      {isLoading ? (
        <p className="text-muted-foreground">Laddar...</p>
      ) : (
        <>
          <div>
            <h2 className="text-xl font-semibold mb-4">
              Aktiva ({activeEncounters?.length ?? 0})
            </h2>
            {activeEncounters && activeEncounters.length > 0 ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {activeEncounters.map((encounter) => (
                  <Link
                    key={encounter.id}
                    to="/encounters/$encounterId"
                    params={{ encounterId: encounter.id }}
                  >
                    <Card className="cursor-pointer hover:border-primary transition-colors">
                      <CardContent className="pt-6">
                        <div className="flex items-start justify-between">
                          <div>
                            <p className="font-semibold">
                              {patientNames.get(encounter.patientId) || 'Okänd patient'}
                            </p>
                            <p className="text-sm text-muted-foreground">
                              {encounter.chiefComplaint || 'Ingen sökorsak'}
                            </p>
                            <p className="text-xs text-muted-foreground mt-2">
                              {formatDateTime(encounter.createdAt)}
                            </p>
                          </div>
                          <Badge
                            variant={
                              encounter.status === 'IN_PROGRESS' ? 'success' : 'warning'
                            }
                          >
                            {encounter.status === 'IN_PROGRESS' ? 'Pågående' : 'Planerad'}
                          </Badge>
                        </div>
                      </CardContent>
                    </Card>
                  </Link>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga aktiva vårdkontakter</p>
            )}
          </div>

          <div>
            <h2 className="text-xl font-semibold mb-4">
              Avslutade ({completedEncounters?.length ?? 0})
            </h2>
            {completedEncounters && completedEncounters.length > 0 ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {completedEncounters.slice(0, 6).map((encounter) => (
                  <Link
                    key={encounter.id}
                    to="/encounters/$encounterId"
                    params={{ encounterId: encounter.id }}
                  >
                    <Card className="cursor-pointer hover:border-primary transition-colors opacity-75">
                      <CardContent className="pt-6">
                        <div className="flex items-start justify-between">
                          <div>
                            <p className="font-semibold">
                              {patientNames.get(encounter.patientId) || 'Okänd patient'}
                            </p>
                            <p className="text-sm text-muted-foreground">
                              {encounter.chiefComplaint || 'Ingen sökorsak'}
                            </p>
                            <p className="text-xs text-muted-foreground mt-2">
                              {formatDateTime(encounter.actualEndTime || encounter.updatedAt)}
                            </p>
                          </div>
                          <Badge variant="secondary">Avslutad</Badge>
                        </div>
                      </CardContent>
                    </Card>
                  </Link>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground">Inga avslutade vårdkontakter</p>
            )}
          </div>
        </>
      )}
    </div>
  )
}
