import { apiClient, ApiResponse } from './client'
import { User } from '@store/authStore'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  user: User
  token: string
  expiresAt: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  companyName?: string
}

export interface RegisterResponse {
  user: User
  token: string
  expiresAt: string
}

export const authApi = {
  /**
   * Login with email and password
   */
  login: (email: string, password: string) => {
    return apiClient.post<ApiResponse<LoginResponse>>('/auth/login', {
      email,
      password,
    })
  },

  /**
   * Register a new user account
   */
  register: (userData: RegisterRequest) => {
    return apiClient.post<ApiResponse<RegisterResponse>>('/auth/register', userData)
  },

  /**
   * Get current user information
   */
  getCurrentUser: () => {
    return apiClient.get<ApiResponse<User>>('/auth/me')
  },

  /**
   * Refresh authentication token
   */
  refreshToken: () => {
    return apiClient.post<ApiResponse<{ token: string; expiresAt: string }>>('/auth/refresh')
  },

  /**
   * Logout (revoke token)
   */
  logout: () => {
    return apiClient.post<ApiResponse<{}>>('/auth/logout')
  },

  /**
   * Request password reset
   */
  requestPasswordReset: (email: string) => {
    return apiClient.post<ApiResponse<{}>>('/auth/password-reset/request', {
      email,
    })
  },

  /**
   * Reset password with token
   */
  resetPassword: (token: string, newPassword: string) => {
    return apiClient.post<ApiResponse<{}>>('/auth/password-reset/confirm', {
      token,
      password: newPassword,
    })
  },

  /**
   * Verify email address
   */
  verifyEmail: (token: string) => {
    return apiClient.post<ApiResponse<{}>>('/auth/verify-email', {
      token,
    })
  },

  /**
   * Resend email verification
   */
  resendEmailVerification: () => {
    return apiClient.post<ApiResponse<{}>>('/auth/verify-email/resend')
  },

  /**
   * Update user profile
   */
  updateProfile: (userData: Partial<User>) => {
    return apiClient.put<ApiResponse<User>>('/auth/profile', userData)
  },

  /**
   * Change password
   */
  changePassword: (currentPassword: string, newPassword: string) => {
    return apiClient.put<ApiResponse<{}>>('/auth/password', {
      currentPassword,
      newPassword,
    })
  },

  /**
   * Delete user account
   */
  deleteAccount: (password: string) => {
    return apiClient.delete<ApiResponse<{}>>('/auth/account', {
      data: { password },
    })
  },
}

