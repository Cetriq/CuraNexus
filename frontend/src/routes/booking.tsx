import { createFileRoute } from '@tanstack/react-router'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

function BookingPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Bokningar</h1>
        <p className="text-muted-foreground">Hantera bokningar och kalendrar</p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Kommer snart</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">
            Bokningshantering ar under utveckling.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

export const Route = createFileRoute('/booking')({
  component: BookingPage,
})
