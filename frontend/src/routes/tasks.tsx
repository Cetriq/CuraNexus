import { createFileRoute } from '@tanstack/react-router'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

function TasksPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Uppgifter</h1>
        <p className="text-muted-foreground">Hantera uppgifter och arbetsfloden</p>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Kommer snart</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">
            Uppgiftshantering ar under utveckling.
          </p>
        </CardContent>
      </Card>
    </div>
  )
}

export const Route = createFileRoute('/tasks')({
  component: TasksPage,
})
