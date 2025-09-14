import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { authApi } from '@api/authApi'
import toast from 'react-hot-toast'

export interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  companyName?: string
  emailVerified: boolean
  createdAt: string
  updatedAt: string
}

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (userData: RegisterData) => Promise<void>
  logout: () => void
  refreshUser: () => Promise<void>
  setLoading: (loading: boolean) => void
}

interface RegisterData {
  email: string
  password: string
  firstName: string
  lastName: string
  companyName?: string
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,

      setLoading: (loading: boolean) => {
        set({ isLoading: loading })
      },

      login: async (email: string, password: string) => {
        try {
          set({ isLoading: true })
          
          const response = await authApi.login(email, password)
          const { user, token } = response.data
          
          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
          })
          
          toast.success(`Welcome back, ${user.firstName}!`)
        } catch (error: any) {
          set({ isLoading: false })
          
          const message = error.response?.data?.message || 'Login failed'
          toast.error(message)
          throw error
        }
      },

      register: async (userData: RegisterData) => {
        try {
          set({ isLoading: true })
          
          const response = await authApi.register(userData)
          const { user, token } = response.data
          
          set({
            user,
            token,
            isAuthenticated: true,
            isLoading: false,
          })
          
          toast.success(`Welcome to Tasawwur RTC, ${user.firstName}!`)
        } catch (error: any) {
          set({ isLoading: false })
          
          const message = error.response?.data?.message || 'Registration failed'
          toast.error(message)
          throw error
        }
      },

      logout: () => {
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
        })
        
        toast.success('Logged out successfully')
      },

      refreshUser: async () => {
        try {
          const { token } = get()
          if (!token) {
            throw new Error('No token available')
          }
          
          const response = await authApi.getCurrentUser()
          const user = response.data
          
          set({ user })
        } catch (error: any) {
          console.error('Failed to refresh user:', error)
          
          // If token is invalid, logout
          if (error.response?.status === 401) {
            get().logout()
          }
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          // Set loading to false after rehydration
          state.isLoading = false
          
          // Refresh user data if authenticated
          if (state.isAuthenticated && state.token) {
            state.refreshUser()
          }
        }
      },
    }
  )
)
