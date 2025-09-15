import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Box } from '@mui/material'

import { useAuthStore } from '@store/authStore'
import { ProtectedRoute } from '@components/auth/ProtectedRoute'
import { Layout } from '@components/layout/Layout'
import { LoadingSpinner } from '@components/common/LoadingSpinner'

// Lazy load pages for better performance
const LoginPage = React.lazy(() => import('@pages/auth/LoginPage'))
const RegisterPage = React.lazy(() => import('@pages/auth/RegisterPage'))
const DashboardPage = React.lazy(() => import('@pages/dashboard/DashboardPage'))
const ProjectsPage = React.lazy(() => import('@pages/projects/ProjectsPage'))
const ProjectDetailPage = React.lazy(() => import('@pages/projects/ProjectDetailPage'))
const QuickstartPage = React.lazy(() => import('@pages/quickstart/QuickstartPage'))
const SettingsPage = React.lazy(() => import('@pages/settings/SettingsPage'))
const NotFoundPage = React.lazy(() => import('@pages/NotFoundPage'))

function App() {
  const { isAuthenticated, isLoading } = useAuthStore()

  if (isLoading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          height: '100vh',
          bgcolor: 'background.default',
        }}
      >
        <LoadingSpinner size={60} />
      </Box>
    )
  }

  return (
    <React.Suspense
      fallback={
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh',
            bgcolor: 'background.default',
          }}
        >
          <LoadingSpinner size={40} />
        </Box>
      }
    >
      <Routes>
        {/* Public routes */}
        <Route
          path="/login"
          element={!isAuthenticated ? <LoginPage /> : <Navigate to="/dashboard" replace />}
        />
        <Route
          path="/register"
          element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/dashboard" replace />}
        />

        {/* Protected routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="projects" element={<ProjectsPage />} />
          <Route path="projects/:projectId" element={<ProjectDetailPage />} />
          <Route path="quickstart" element={<QuickstartPage />} />
          <Route path="quickstart/:projectId" element={<QuickstartPage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>

        {/* 404 route */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </React.Suspense>
  )
}

export default App

