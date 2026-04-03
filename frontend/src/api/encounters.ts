import { apiClient } from './client'
import type { Encounter, CreateEncounterRequest, EncounterTask } from '@/types'

interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

const BASE_URL = '/api/encounter/encounters'

export const encountersApi = {
  getAll: async (): Promise<Encounter[]> => {
    const result = await apiClient.get<PageResponse<Encounter>>(BASE_URL)
    return result.content
  },

  getById: async (id: string): Promise<Encounter> => {
    return apiClient.get<Encounter>(`${BASE_URL}/${id}`)
  },

  getByPatient: async (patientId: string): Promise<Encounter[]> => {
    return apiClient.get<Encounter[]>(`${BASE_URL}/patient/${patientId}`)
  },

  getActive: async (): Promise<Encounter[]> => {
    return apiClient.get<Encounter[]>(`${BASE_URL}/active`)
  },

  create: async (data: CreateEncounterRequest): Promise<Encounter> => {
    return apiClient.post<Encounter>(BASE_URL, data)
  },

  start: async (id: string): Promise<Encounter> => {
    return apiClient.post<Encounter>(`${BASE_URL}/${id}/start`)
  },

  complete: async (id: string): Promise<Encounter> => {
    return apiClient.post<Encounter>(`${BASE_URL}/${id}/complete`)
  },

  cancel: async (id: string, reason: string): Promise<Encounter> => {
    return apiClient.post<Encounter>(`${BASE_URL}/${id}/cancel`, null, { reason })
  },

  getTasks: async (encounterId: string): Promise<EncounterTask[]> => {
    return apiClient.get<EncounterTask[]>(`/api/task/encounters/${encounterId}/tasks`)
  },

  completeTask: async (taskId: string, notes?: string): Promise<EncounterTask> => {
    return apiClient.post<EncounterTask>(`/api/task/tasks/${taskId}/complete`, null, { notes })
  },
}
