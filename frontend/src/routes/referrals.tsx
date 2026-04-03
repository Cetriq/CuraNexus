import { createFileRoute } from '@tanstack/react-router'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

function ReferralsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Remisser</h1>
        <p className="text-muted-foreground">Hantera remisser och svar</p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Kommer snart</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">
            Remisshantering ar under utveckling.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

export const Route = createFileRoute('/referrals')({
  component: ReferralsPage,
})
