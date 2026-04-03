import { createFileRoute } from '@tanstack/react-router'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

function MedicationPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Läkemedel</h1>
        <p className="text-muted-foreground">Hantera ordinationer och recept</p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Kommer snart</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">
            Läkemedelshantering är under utveckling.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

export const Route = createFileRoute('/medication')({
  component: MedicationPage,
})
