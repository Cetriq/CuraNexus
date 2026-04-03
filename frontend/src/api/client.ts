// Native fetch-based API client (no axios dependency)

const defaultHeaders: Record<string, string> = {
  'Content-Type': 'application/json',
  // Default user context headers (would normally come from auth)
  'X-User-Id': '00000000-0000-0000-0000-000000000001',
  'X-Unit-Id': '00000000-0000-0000-0000-000000000001',
}

class ApiError extends Error {
  status: number
  statusText: string
  data?: unknown

  constructor(status: number, statusText: string, data?: unknown) {
    super(`API Error: ${status} ${statusText}`)
    this.name = 'ApiError'
    this.status = status
    this.statusText = statusText
    this.data = data
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let data: unknown
    try {
      data = await response.json()
    } catch {
      // Response body is not JSON
    }
    console.error('API Error:', data || response.statusText)
    throw new ApiError(response.status, response.statusText, data)
  }

  // Handle empty responses
  const text = await response.text()
  if (!text) {
    return undefined as T
  }

  return JSON.parse(text) as T
}

export const apiClient = {
  async get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
    const queryString = params
      ? '?' + new URLSearchParams(
          Object.entries(params)
            .filter(([, v]) => v !== undefined && v !== null)
            .map(([k, v]) => [k, String(v)])
        ).toString()
      : ''

    const response = await fetch(url + queryString, {
      method: 'GET',
      headers: defaultHeaders,
    })

    return handleResponse<T>(response)
  },

  async post<T>(url: string, body?: unknown, params?: Record<string, unknown>): Promise<T> {
    const queryString = params
      ? '?' + new URLSearchParams(
          Object.entries(params)
            .filter(([, v]) => v !== undefined && v !== null)
            .map(([k, v]) => [k, String(v)])
        ).toString()
      : ''

    const response = await fetch(url + queryString, {
      method: 'POST',
      headers: defaultHeaders,
      body: body ? JSON.stringify(body) : undefined,
    })

    return handleResponse<T>(response)
  },

  async put<T>(url: string, body?: unknown): Promise<T> {
    const response = await fetch(url, {
      method: 'PUT',
      headers: defaultHeaders,
      body: body ? JSON.stringify(body) : undefined,
    })

    return handleResponse<T>(response)
  },

  async delete<T>(url: string): Promise<T> {
    const response = await fetch(url, {
      method: 'DELETE',
      headers: defaultHeaders,
    })

    return handleResponse<T>(response)
  },
}

export { ApiError }
