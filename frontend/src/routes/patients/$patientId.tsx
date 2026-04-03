import { createFileRoute } from '@tanstack/react-router'
import { PatientDetailPage } from '@/features/patients'

export const Route = createFileRoute('/patients/$patientId')({
  component: PatientDetailPage,
})
