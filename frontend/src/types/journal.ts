// Note types
export type NoteType =
  | 'ADMISSION'
  | 'PROGRESS'
  | 'CONSULTATION'
  | 'DISCHARGE'
  | 'PROCEDURE'
  | 'NURSING'
  | 'OTHER'

export type NoteStatus = 'DRAFT' | 'FINAL' | 'AMENDED' | 'CANCELLED'

export interface ClinicalNote {
  id: string
  encounterId: string
  patientId: string
  type: NoteType
  title: string
  content: string
  status: NoteStatus
  authorId: string
  authorName: string
  signedById?: string
  signedByName?: string
  signedAt?: string
  createdAt: string
  updatedAt: string
}

export interface CreateNoteRequest {
  encounterId: string
  patientId: string
  type: NoteType
  authorId: string
  authorName: string
  title?: string
  content?: string
}

export interface UpdateNoteRequest {
  title?: string
  content?: string
}

export interface SignNoteRequest {
  signedById: string
  signedByName: string
}

export interface AmendNoteRequest {
  content: string
  amendedById: string
  amendedByName: string
  reason: string
}

// Diagnosis types
export type DiagnosisType = 'PRINCIPAL' | 'SECONDARY' | 'WORKING' | 'DIFFERENTIAL'

export interface Diagnosis {
  id: string
  encounterId: string
  patientId: string
  code: string
  codeSystem: string
  displayText: string
  type: DiagnosisType
  rank?: number
  onsetDate?: string
  resolvedDate?: string
  recordedAt: string
  recordedById: string
}

export interface CreateDiagnosisRequest {
  encounterId: string
  patientId: string
  code: string
  codeSystem: string
  displayText: string
  type: DiagnosisType
  rank?: number
  onsetDate?: string
  recordedById: string
}

// Observation types
export type ObservationCategory =
  | 'VITAL_SIGNS'
  | 'LABORATORY'
  | 'IMAGING'
  | 'ASSESSMENT'
  | 'SOCIAL_HISTORY'
  | 'OTHER'

export interface Observation {
  id: string
  encounterId: string
  patientId: string
  category: ObservationCategory
  code: string
  codeSystem?: string
  displayText: string
  valueNumeric?: number
  valueText?: string
  unit?: string
  referenceRangeLow?: number
  referenceRangeHigh?: number
  interpretation?: string
  isCritical: boolean
  recordedAt: string
  recordedById: string
}

export interface CreateObservationRequest {
  encounterId: string
  patientId: string
  category: ObservationCategory
  code: string
  codeSystem?: string
  displayText: string
  valueNumeric?: number
  valueText?: string
  unit?: string
  referenceRangeLow?: number
  referenceRangeHigh?: number
  interpretation?: string
  isCritical?: boolean
  recordedById: string
}

// Procedure types
export type ProcedureStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export interface Procedure {
  id: string
  encounterId: string
  patientId: string
  code: string
  codeSystem: string
  displayText: string
  status: ProcedureStatus
  performedAt?: string
  performedById?: string
  notes?: string
  recordedAt: string
  recordedById: string
}

export interface CreateProcedureRequest {
  encounterId: string
  patientId: string
  code: string
  codeSystem: string
  displayText: string
  notes?: string
  recordedById: string
}
