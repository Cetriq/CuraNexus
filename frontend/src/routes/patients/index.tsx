import { createFileRoute } from '@tanstack/react-router'
import { PatientListPage } from '@/features/patients'

export const Route = createFileRoute('/patients/')({
  component: PatientListPage,
})
