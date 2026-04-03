import { createFileRoute } from '@tanstack/react-router'
import { EncounterListPage } from '@/features/encounters'

export const Route = createFileRoute('/encounters/')({
  component: EncounterListPage,
})
