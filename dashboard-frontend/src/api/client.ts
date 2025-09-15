import axios from 'axios'
import toast from 'react-hot-toast'

// Create axios instance with base configuration
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add authentication token
apiClient.interceptors.request.use(
  (config) => {
    // Get token from localStorage (Zustand persist storage)
    const authStorage = localStorage.getItem('auth-storage')
    if (authStorage) {
      try {
        const { state } = JSON.parse(authStorage)
        const token = state?.token
        
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
      } catch (error) {
        console.error('Failed to parse auth storage:', error)
      }
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    const { response } = error
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 401:
          // Unauthorized - redirect to login
          if (window.location.pathname !== '/login') {
            localStorage.removeItem('auth-storage')
            window.location.href = '/login'
            toast.error('Session expired. Please log in again.')
          }
          break
          
        case 403:
          // Forbidden
          toast.error('Access denied')
          break
          
        case 404:
          // Not found
          if (data?.message) {
            toast.error(data.message)
          }
          break
          
        case 422:
          // Validation error
          if (data?.errors) {
            const errorMessages = Object.values(data.errors).flat()
            errorMessages.forEach((message: any) => toast.error(message))
          } else if (data?.message) {
            toast.error(data.message)
          }
          break
          
        case 429:
          // Rate limit exceeded
          toast.error('Too many requests. Please try again later.')
          break
          
        case 500:
          // Internal server error
          toast.error('Server error. Please try again later.')
          break
          
        default:
          // Generic error
          if (data?.message) {
            toast.error(data.message)
          } else {
            toast.error('An unexpected error occurred')
          }
      }
    } else if (error.code === 'ECONNABORTED') {
      // Request timeout
      toast.error('Request timed out. Please try again.')
    } else if (error.message === 'Network Error') {
      // Network error
      toast.error('Network error. Please check your connection.')
    } else {
      // Unknown error
      toast.error('An unexpected error occurred')
    }
    
    return Promise.reject(error)
  }
)

// API response types
export interface ApiResponse<T = any> {
  data: T
  message?: string
  success: boolean
}

export interface ApiError {
  message: string
  errors?: Record<string, string[]>
  code?: string
}

// Utility functions for API calls
export const handleApiError = (error: any): string => {
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  if (error.message) {
    return error.message
  }
  return 'An unexpected error occurred'
}

export const createFormData = (data: Record<string, any>): FormData => {
  const formData = new FormData()
  
  Object.entries(data).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      if (value instanceof File) {
        formData.append(key, value)
      } else if (Array.isArray(value)) {
        value.forEach((item, index) => {
          formData.append(`${key}[${index}]`, item)
        })
      } else {
        formData.append(key, String(value))
      }
    }
  })
  
  return formData
}

export default apiClient

