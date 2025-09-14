import React from 'react'
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  IconButton,
  Paper,
} from '@mui/material'
import {
  Add as AddIcon,
  TrendingUp as TrendingUpIcon,
  People as PeopleIcon,
  AccessTime as AccessTimeIcon,
  VideoCall as VideoCallIcon,
  Settings as SettingsIcon,
  Launch as LaunchIcon,
} from '@mui/icons-material'
import { useNavigate } from 'react-router-dom'
import { useQuery } from 'react-query'

import { projectsApi } from '@api/projectsApi'
import { useAuthStore } from '@store/authStore'
import { LoadingSpinner } from '@components/common/LoadingSpinner'
import { ErrorMessage } from '@components/common/ErrorMessage'
import { UsageChart } from '@components/dashboard/UsageChart'
import { ProjectCard } from '@components/projects/ProjectCard'
import { StatsCard } from '@components/dashboard/StatsCard'

const DashboardPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuthStore()

  // Fetch dashboard data
  const {
    data: dashboardData,
    isLoading,
    error,
    refetch,
  } = useQuery(['dashboard'], projectsApi.getDashboardStats, {
    staleTime: 2 * 60 * 1000, // 2 minutes
  })

  const {
    data: recentProjects,
    isLoading: projectsLoading,
  } = useQuery(['projects', 'recent'], () => projectsApi.getProjects({ limit: 6 }), {
    staleTime: 5 * 60 * 1000, // 5 minutes
  })

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <LoadingSpinner size={60} />
      </Box>
    )
  }

  if (error) {
    return (
      <ErrorMessage
        title="Failed to load dashboard"
        message="Unable to fetch dashboard data. Please try again."
        onRetry={refetch}
      />
    )
  }

  const stats = dashboardData?.data || {}
  const projects = recentProjects?.data?.projects || []

  return (
    <Box sx={{ p: 3 }}>
      {/* Welcome Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Welcome back, {user?.firstName}! 👋
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Here's what's happening with your RTC projects today.
        </Typography>
      </Box>

      {/* Quick Actions */}
      <Paper sx={{ p: 2, mb: 4, bgcolor: 'primary.main', color: 'primary.contrastText' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="h6" gutterBottom>
              Ready to build something amazing?
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.9 }}>
              Create a new project and start integrating real-time communication in minutes.
            </Typography>
          </Box>
          <Button
            variant="contained"
            color="secondary"
            startIcon={<AddIcon />}
            onClick={() => navigate('/projects?create=true')}
            sx={{ ml: 2 }}
          >
            New Project
          </Button>
        </Box>
      </Paper>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Total Projects"
            value={stats.totalProjects || 0}
            icon={<VideoCallIcon />}
            color="primary"
            trend={stats.projectsGrowth}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Minutes Used"
            value={stats.totalMinutes || 0}
            icon={<AccessTimeIcon />}
            color="success"
            trend={stats.minutesGrowth}
            format="minutes"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Peak Users"
            value={stats.peakConcurrentUsers || 0}
            icon={<PeopleIcon />}
            color="info"
            trend={stats.usersGrowth}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Total Sessions"
            value={stats.totalSessions || 0}
            icon={<TrendingUpIcon />}
            color="warning"
            trend={stats.sessionsGrowth}
          />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Usage Chart */}
        <Grid item xs={12} lg={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Typography variant="h6">
                  Usage Overview
                </Typography>
                <Button
                  size="small"
                  endIcon={<LaunchIcon />}
                  onClick={() => navigate('/analytics')}
                >
                  View Details
                </Button>
              </Box>
              <UsageChart data={stats.usageHistory || []} />
            </CardContent>
          </Card>
        </Grid>

        {/* Quick Stats */}
        <Grid item xs={12} lg={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Quick Stats
              </Typography>
              
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Active Projects
                  </Typography>
                  <Chip 
                    label={stats.activeProjects || 0} 
                    color="success" 
                    size="small" 
                  />
                </Box>
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    This Month
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {stats.thisMonthMinutes || 0} minutes
                  </Typography>
                </Box>
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Avg Session Duration
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {stats.avgSessionDuration || 0} min
                  </Typography>
                </Box>
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Success Rate
                  </Typography>
                  <Typography variant="body2" fontWeight="medium" color="success.main">
                    {stats.successRate || 0}%
                  </Typography>
                </Box>
              </Box>

              <Button
                fullWidth
                variant="outlined"
                startIcon={<SettingsIcon />}
                sx={{ mt: 2 }}
                onClick={() => navigate('/settings')}
              >
                Account Settings
              </Button>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Projects */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Typography variant="h6">
                  Recent Projects
                </Typography>
                <Button
                  endIcon={<LaunchIcon />}
                  onClick={() => navigate('/projects')}
                >
                  View All Projects
                </Button>
              </Box>

              {projectsLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                  <LoadingSpinner size={40} />
                </Box>
              ) : projects.length === 0 ? (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <VideoCallIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                  <Typography variant="h6" color="text.secondary" gutterBottom>
                    No projects yet
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Create your first project to start building with Tasawwur RTC
                  </Typography>
                  <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => navigate('/projects?create=true')}
                  >
                    Create Project
                  </Button>
                </Box>
              ) : (
                <Grid container spacing={2}>
                  {projects.map((project: any) => (
                    <Grid item xs={12} sm={6} lg={4} key={project.id}>
                      <ProjectCard
                        project={project}
                        onView={() => navigate(`/projects/${project.id}`)}
                        onQuickstart={() => navigate(`/quickstart/${project.id}`)}
                        compact
                      />
                    </Grid>
                  ))}
                </Grid>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}

export default DashboardPage
