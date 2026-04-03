import { createFileRoute } from '@tanstack/react-router'
import { EncounterDetailPage } from '@/features/encounters'

export const Route = createFileRoute('/encounters/$encounterId')({
  component: EncounterDetailPage,
})
