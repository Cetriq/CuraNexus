import { apiClient } from './client'
import type {
  ClinicalNote,
  CreateNoteRequest,
  UpdateNoteRequest,
  SignNoteRequest,
  AmendNoteRequest,
  Diagnosis,
  CreateDiagnosisRequest,
  Observation,
  CreateObservationRequest,
  Procedure,
  CreateProcedureRequest,
} from '@/types'

const BASE_URL = '/api/journal'

export const journalApi = {
  // Notes
  createNote: async (data: CreateNoteRequest): Promise<ClinicalNote> => {
    return apiClient.post<ClinicalNote>(`${BASE_URL}/notes`, data)
  },

  getNote: async (noteId: string): Promise<ClinicalNote> => {
    return apiClient.get<ClinicalNote>(`${BASE_URL}/notes/${noteId}`)
  },

  getNotesByEncounter: async (encounterId: string): Promise<ClinicalNote[]> => {
    return apiClient.get<ClinicalNote[]>(`${BASE_URL}/encounters/${encounterId}/notes`)
  },

  getNotesByPatient: async (patientId: string): Promise<ClinicalNote[]> => {
    return apiClient.get<ClinicalNote[]>(`${BASE_URL}/patients/${patientId}/notes`)
  },

  updateNote: async (noteId: string, data: UpdateNoteRequest): Promise<ClinicalNote> => {
    return apiClient.put<ClinicalNote>(`${BASE_URL}/notes/${noteId}`, data)
  },

  signNote: async (noteId: string, data: SignNoteRequest): Promise<ClinicalNote> => {
    return apiClient.post<ClinicalNote>(`${BASE_URL}/notes/${noteId}/sign`, data)
  },

  amendNote: async (noteId: string, data: AmendNoteRequest): Promise<ClinicalNote> => {
    return apiClient.post<ClinicalNote>(`${BASE_URL}/notes/${noteId}/amend`, data)
  },

  cancelNote: async (noteId: string, reason: string): Promise<ClinicalNote> => {
    return apiClient.post<ClinicalNote>(`${BASE_URL}/notes/${noteId}/cancel`, { reason })
  },

  // Diagnoses
  createDiagnosis: async (data: CreateDiagnosisRequest): Promise<Diagnosis> => {
    return apiClient.post<Diagnosis>(`${BASE_URL}/diagnoses`, data)
  },

  getDiagnosis: async (diagnosisId: string): Promise<Diagnosis> => {
    return apiClient.get<Diagnosis>(`${BASE_URL}/diagnoses/${diagnosisId}`)
  },

  getDiagnosesByEncounter: async (encounterId: string): Promise<Diagnosis[]> => {
    return apiClient.get<Diagnosis[]>(`${BASE_URL}/encounters/${encounterId}/diagnoses`)
  },

  getDiagnosesByPatient: async (patientId: string): Promise<Diagnosis[]> => {
    return apiClient.get<Diagnosis[]>(`${BASE_URL}/patients/${patientId}/diagnoses`)
  },

  resolveDiagnosis: async (diagnosisId: string, resolvedDate: string): Promise<Diagnosis> => {
    return apiClient.post<Diagnosis>(`${BASE_URL}/diagnoses/${diagnosisId}/resolve`, { resolvedDate })
  },

  // Observations
  createObservation: async (data: CreateObservationRequest): Promise<Observation> => {
    return apiClient.post<Observation>(`${BASE_URL}/observations`, data)
  },

  getObservation: async (observationId: string): Promise<Observation> => {
    return apiClient.get<Observation>(`${BASE_URL}/observations/${observationId}`)
  },

  getObservationsByEncounter: async (encounterId: string): Promise<Observation[]> => {
    return apiClient.get<Observation[]>(`${BASE_URL}/encounters/${encounterId}/observations`)
  },

  getObservationsByPatient: async (patientId: string, category?: string): Promise<Observation[]> => {
    const params = category ? { category } : {}
    return apiClient.get<Observation[]>(`${BASE_URL}/patients/${patientId}/observations`, params)
  },

  getCriticalObservations: async (patientId: string): Promise<Observation[]> => {
    return apiClient.get<Observation[]>(`${BASE_URL}/patients/${patientId}/observations/critical`)
  },

  // Procedures
  createProcedure: async (data: CreateProcedureRequest): Promise<Procedure> => {
    return apiClient.post<Procedure>(`${BASE_URL}/procedures`, data)
  },

  getProcedure: async (procedureId: string): Promise<Procedure> => {
    return apiClient.get<Procedure>(`${BASE_URL}/procedures/${procedureId}`)
  },

  getProceduresByEncounter: async (encounterId: string): Promise<Procedure[]> => {
    return apiClient.get<Procedure[]>(`${BASE_URL}/encounters/${encounterId}/procedures`)
  },

  getProceduresByPatient: async (patientId: string): Promise<Procedure[]> => {
    return apiClient.get<Procedure[]>(`${BASE_URL}/patients/${patientId}/procedures`)
  },

  startProcedure: async (procedureId: string, performedById: string): Promise<Procedure> => {
    return apiClient.post<Procedure>(`${BASE_URL}/procedures/${procedureId}/start`, { performedById })
  },

  completeProcedure: async (procedureId: string, performedById: string, notes?: string): Promise<Procedure> => {
    return apiClient.post<Procedure>(`${BASE_URL}/procedures/${procedureId}/complete`, { performedById, notes })
  },

  cancelProcedure: async (procedureId: string, reason: string): Promise<Procedure> => {
    return apiClient.post<Procedure>(`${BASE_URL}/procedures/${procedureId}/cancel`, { reason })
  },
}
