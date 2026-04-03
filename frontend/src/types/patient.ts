export interface Patient {
  id: string
  personnummer: string
  givenName: string
  familyName: string
  displayName?: string
  dateOfBirth?: string
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'UNKNOWN'
  protectedIdentity: boolean
  deceased: boolean
  deceasedDate?: string
  address?: Address
  telecom?: ContactPoint[]
  createdAt: string
  updatedAt: string
}

export interface Address {
  streetAddress?: string
  postalCode?: string
  city?: string
  country?: string
}

export interface ContactPoint {
  system: 'phone' | 'email' | 'sms'
  value: string
  use?: 'home' | 'work' | 'mobile'
}

export interface CreatePatientRequest {
  personnummer: string
  givenName: string
  familyName: string
  dateOfBirth?: string
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'UNKNOWN'
  protectedIdentity?: boolean
  address?: Address
  telecom?: ContactPoint[]
}

export interface PatientSearchResult {
  content: Patient[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
