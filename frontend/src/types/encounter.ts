export type EncounterStatus =
  | 'PLANNED'
  | 'IN_PROGRESS'
  | 'ON_HOLD'
  | 'COMPLETED'
  | 'CANCELLED'

export type EncounterClass =
  | 'INPATIENT'
  | 'OUTPATIENT'
  | 'EMERGENCY'
  | 'HOME'
  | 'VIRTUAL'

export interface Encounter {
  id: string
  patientId: string
  patientPersonnummer?: string
  patientName?: string
  status: EncounterStatus
  encounterClass: EncounterClass
  unitId: string
  unitHsaId?: string
  unitName?: string
  responsiblePractitionerId?: string
  responsiblePractitionerHsaId?: string
  responsiblePractitionerName?: string
  chiefComplaint?: string
  startTime?: string
  endTime?: string
  readyToStart: boolean
  allTasksCompleted: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateEncounterRequest {
  patientId: string
  patientPersonnummer?: string
  patientName?: string
  encounterClass: EncounterClass
  unitId: string
  unitHsaId?: string
  unitName?: string
  responsiblePractitionerId?: string
  responsiblePractitionerHsaId?: string
  responsiblePractitionerName?: string
  chiefComplaint?: string
}

export interface EncounterTask {
  id: string
  encounterId: string
  taskType: string
  title: string
  description?: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'BLOCKED'
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
  blocking: boolean
  dueAt?: string
  completedAt?: string
  assigneeId?: string
  assigneeName?: string
  notes?: string
  createdAt: string
}
