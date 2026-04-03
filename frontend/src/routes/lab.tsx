import { createFileRoute } from '@tanstack/react-router'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

function LabPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Labb</h1>
        <p className="text-muted-foreground">Hantera labbbestallningar och resultat</p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Kommer snart</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">
            Labbhantering ar under utveckling.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

export const Route = createFileRoute('/lab')({
  component: LabPage,
})
