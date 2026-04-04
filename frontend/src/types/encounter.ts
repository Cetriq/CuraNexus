export type EncounterStatus =
  | 'PLANNED'
  | 'ARRIVED'
  | 'TRIAGED'
  | 'IN_PROGRESS'
  | 'ON_HOLD'
  | 'FINISHED'
  | 'COMPLETED'
  | 'CANCELLED'

export type EncounterClass =
  | 'INPATIENT'
  | 'OUTPATIENT'
  | 'EMERGENCY'
  | 'HOME'
  | 'VIRTUAL'

export type EncounterType =
  | 'ROUTINE'
  | 'EMERGENCY'
  | 'URGENT'
  | 'FOLLOW_UP'

export type EncounterPriority =
  | 'IMMEDIATE'
  | 'URGENT'
  | 'ASAP'
  | 'ROUTINE'

export type ReasonType =
  | 'CHIEF_COMPLAINT'
  | 'DIAGNOSIS'
  | 'ADMISSION_DIAGNOSIS'
  | 'REFERRAL_REASON'
  | 'ADMISSION_REASON'

export interface EncounterReason {
  id: string
  type: ReasonType
  code?: string
  codeSystem?: string
  displayText?: string
  isPrimary: boolean
}

export interface Encounter {
  id: string
  patientId: string
  status: EncounterStatus
  encounterClass: EncounterClass
  type?: EncounterType
  priority?: EncounterPriority
  serviceType?: string
  responsibleUnitId?: string
  responsiblePractitionerId?: string
  plannedStartTime?: string
  plannedEndTime?: string
  actualStartTime?: string
  actualEndTime?: string
  createdAt: string
  updatedAt: string
  reasons?: EncounterReason[]
  chiefComplaint?: string
  // Legacy fields for UI compatibility
  startTime?: string
  endTime?: string
  readyToStart?: boolean
  allTasksCompleted?: boolean
  patientName?: string
  unitName?: string
  responsiblePractitionerName?: string
}

export interface CreateEncounterRequest {
  patientId: string
  patientPersonnummer?: string
  patientName?: string
  encounterClass: EncounterClass
  type?: EncounterType
  priority?: EncounterPriority
  serviceType?: string
  responsibleUnitId?: string
  responsiblePractitionerId?: string
  plannedStartTime?: string
  plannedEndTime?: string
  // Legacy fields for UI compatibility
  unitId?: string
  unitName?: string
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
