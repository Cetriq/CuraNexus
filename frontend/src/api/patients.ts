import { apiClient } from './client'
import type { Patient, CreatePatientRequest, PatientSearchResult } from '@/types'

const BASE_URL = '/api/patient/patients'

export const patientsApi = {
  getAll: async (): Promise<Patient[]> => {
    const result = await apiClient.get<PatientSearchResult>(BASE_URL)
    return result.content
  },

  getById: async (id: string): Promise<Patient> => {
    return apiClient.get<Patient>(`${BASE_URL}/${id}`)
  },

  getByPersonnummer: async (personnummer: string): Promise<Patient> => {
    return apiClient.get<Patient>(`${BASE_URL}/personnummer/${personnummer}`)
  },

  search: async (query: string, page = 0, size = 20): Promise<PatientSearchResult> => {
    return apiClient.get<PatientSearchResult>(`${BASE_URL}/search`, { query, page, size })
  },

  create: async (data: CreatePatientRequest): Promise<Patient> => {
    return apiClient.post<Patient>(BASE_URL, data)
  },

  update: async (id: string, data: Partial<CreatePatientRequest>): Promise<Patient> => {
    return apiClient.put<Patient>(`${BASE_URL}/${id}`, data)
  },
}
