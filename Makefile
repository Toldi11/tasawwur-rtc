# Tasawwur RTC - Development and Deployment Makefile

.PHONY: help dev build test clean deploy docs

# Default target
help:
	@echo "Tasawwur RTC - Real-Time Communication Platform"
	@echo ""
	@echo "Available commands:"
	@echo "  dev          - Start development environment"
	@echo "  build        - Build all components"
	@echo "  test         - Run all tests"
	@echo "  clean        - Clean build artifacts"
	@echo "  deploy       - Deploy to production"
	@echo "  docs         - Build and serve documentation"
	@echo "  android      - Build Android SDK"
	@echo "  backend      - Build backend services"
	@echo "  frontend     - Build frontend dashboard"
	@echo "  k8s-dev      - Deploy to development Kubernetes"
	@echo "  k8s-prod     - Deploy to production Kubernetes"
	@echo ""

# Development environment
dev:
	@echo "🚀 Starting Tasawwur RTC development environment..."
	docker-compose up -d postgres redis turn-server
	@echo "⏳ Waiting for services to be ready..."
	sleep 10
	docker-compose up rest-api-server signaling-server dashboard docs

# Build all components
build: android backend frontend docs-build
	@echo "✅ All components built successfully"

# Android SDK
android:
	@echo "🔨 Building Android SDK..."
	cd sdk-android && ./gradlew assembleRelease
	cd sdk-android && ./gradlew publishToMavenLocal
	@echo "✅ Android SDK built and published locally"

# Backend services
backend:
	@echo "🔨 Building backend services..."
	cd backend-services/rest-api-server && mvn clean package -DskipTests
	cd backend-services/signaling-server && mvn clean package -DskipTests
	@echo "✅ Backend services built"

# Frontend dashboard
frontend:
	@echo "🔨 Building frontend dashboard..."
	cd dashboard-frontend && npm ci && npm run build
	@echo "✅ Frontend dashboard built"

# Documentation
docs-build:
	@echo "📚 Building documentation..."
	cd docs && npm ci && npm run build
	@echo "✅ Documentation built"

docs:
	@echo "📚 Starting documentation server..."
	cd docs && npm ci && npm start

# Testing
test: test-android test-backend test-frontend
	@echo "✅ All tests completed"

test-android:
	@echo "🧪 Running Android SDK tests..."
	cd sdk-android && ./gradlew test

test-backend:
	@echo "🧪 Running backend tests..."
	cd backend-services/rest-api-server && mvn test
	cd backend-services/signaling-server && mvn test

test-frontend:
	@echo "🧪 Running frontend tests..."
	cd dashboard-frontend && npm test

# Integration tests
test-integration:
	@echo "🧪 Running integration tests..."
	docker-compose -f docker-compose.test.yml up --abort-on-container-exit
	docker-compose -f docker-compose.test.yml down

# Clean build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	cd sdk-android && ./gradlew clean
	cd backend-services/rest-api-server && mvn clean
	cd backend-services/signaling-server && mvn clean
	cd dashboard-frontend && rm -rf dist node_modules
	cd docs && rm -rf build node_modules
	docker system prune -f
	@echo "✅ Clean completed"

# Docker images
docker-build:
	@echo "🐳 Building Docker images..."
	docker build -t tasawwur-rtc/rest-api-server:1.0.0 backend-services/rest-api-server
	docker build -t tasawwur-rtc/signaling-server:1.0.0 backend-services/signaling-server
	docker build -t tasawwur-rtc/dashboard:1.0.0 dashboard-frontend
	docker build -t tasawwur-rtc/docs:1.0.0 docs
	@echo "✅ Docker images built"

# Kubernetes deployment
k8s-dev:
	@echo "☸️  Deploying to development Kubernetes..."
	kubectl apply -f infra-k8s/base/namespace.yaml
	kubectl apply -f infra-k8s/base/
	kubectl apply -f infra-k8s/overlays/dev/
	@echo "✅ Deployed to development environment"

k8s-prod:
	@echo "☸️  Deploying to production Kubernetes..."
	kubectl apply -f infra-k8s/base/namespace.yaml
	kubectl apply -f infra-k8s/base/
	kubectl apply -f infra-k8s/overlays/prod/
	@echo "✅ Deployed to production environment"

# Performance testing
perf-test:
	@echo "⚡ Running performance tests..."
	cd tests/performance && ./run-load-test.sh
	@echo "📊 Performance test results available in tests/performance/results/"

# Security scanning
security:
	@echo "🔒 Running security scans..."
	cd sdk-android && ./gradlew dependencyCheckAnalyze
	cd backend-services/rest-api-server && mvn org.owasp:dependency-check-maven:check
	cd backend-services/signaling-server && mvn org.owasp:dependency-check-maven:check
	cd dashboard-frontend && npm audit
	@echo "✅ Security scans completed"

# Code quality
quality:
	@echo "📊 Running code quality checks..."
	cd sdk-android && ./gradlew spotbugsMain
	cd backend-services/rest-api-server && mvn spotbugs:check
	cd backend-services/signaling-server && mvn spotbugs:check
	cd dashboard-frontend && npm run lint
	@echo "✅ Code quality checks completed"

# Release preparation
release: clean build test security quality
	@echo "🚀 Preparing release..."
	@echo "✅ Release preparation completed"
	@echo ""
	@echo "📋 Release Checklist:"
	@echo "  - [ ] Update version numbers"
	@echo "  - [ ] Update CHANGELOG.md"
	@echo "  - [ ] Create Git tag"
	@echo "  - [ ] Build and push Docker images"
	@echo "  - [ ] Deploy to staging"
	@echo "  - [ ] Run smoke tests"
	@echo "  - [ ] Deploy to production"
	@echo "  - [ ] Update documentation"

# Monitoring
logs:
	@echo "📋 Showing application logs..."
	docker-compose logs -f rest-api-server signaling-server dashboard

monitor:
	@echo "📊 Opening monitoring dashboard..."
	@echo "Prometheus: http://localhost:9090"
	@echo "Grafana: http://localhost:3002 (admin/admin)"
	@echo "Application: http://localhost:3000"

# Database operations
db-migrate:
	@echo "🗃️  Running database migrations..."
	cd backend-services/rest-api-server && mvn flyway:migrate

db-reset:
	@echo "🗃️  Resetting database..."
	docker-compose exec postgres psql -U postgres -c "DROP DATABASE IF EXISTS tasawwur_rtc_dev;"
	docker-compose exec postgres psql -U postgres -c "CREATE DATABASE tasawwur_rtc_dev;"
	$(MAKE) db-migrate

# Backup and restore
backup:
	@echo "💾 Creating backup..."
	mkdir -p backups
	docker-compose exec postgres pg_dump -U postgres tasawwur_rtc_dev > backups/backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "✅ Backup created in backups/"

# Quick start for new developers
setup:
	@echo "🎯 Setting up Tasawwur RTC for development..."
	@echo "1. Installing dependencies..."
	cd dashboard-frontend && npm ci
	cd docs && npm ci
	@echo "2. Starting services..."
	docker-compose up -d postgres redis
	@echo "3. Running migrations..."
	sleep 10
	$(MAKE) db-migrate
	@echo "4. Building components..."
	$(MAKE) backend
	@echo ""
	@echo "✅ Setup completed! Run 'make dev' to start development environment."
	@echo ""
	@echo "🌟 Next steps:"
	@echo "  - Visit http://localhost:3000 for the dashboard"
	@echo "  - Visit http://localhost:3001 for documentation"
	@echo "  - Check backend health at http://localhost:8081/actuator/health"
	@echo "  - WebSocket signaling at ws://localhost:8080/ws"

# Status check
status:
	@echo "📊 Tasawwur RTC Status"
	@echo "====================="
	@echo ""
	@echo "🐳 Docker Services:"
	@docker-compose ps
	@echo ""
	@echo "☸️  Kubernetes Services:"
	@kubectl get pods -n tasawwur-rtc 2>/dev/null || echo "  Not deployed to Kubernetes"
	@echo ""
	@echo "🌐 Service URLs:"
	@echo "  Dashboard:     http://localhost:3000"
	@echo "  Documentation: http://localhost:3001"
	@echo "  REST API:      http://localhost:8081"
	@echo "  Signaling:     ws://localhost:8080/ws"
	@echo "  Prometheus:    http://localhost:9090"
	@echo "  Grafana:       http://localhost:3002"

