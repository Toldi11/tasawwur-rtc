# Contributing to Tasawwur RTC

Thank you for your interest in contributing to Tasawwur RTC! This document provides guidelines and information for contributors.

## 🎯 Project Vision

Tasawwur RTC aims to be the most developer-friendly real-time communication platform, delivering:
- **Sub-200ms glass-to-glass latency**
- **<5MB SDK footprint**
- **<15% CPU usage** on mid-range devices
- **10-minute, 15-line** integration experience

## 🚀 Getting Started

### Prerequisites

- **Android Development**: Android Studio 4.2+, Java 17+, Kotlin 1.8+
- **Backend Development**: Java 17, Maven 3.8+, Docker, PostgreSQL
- **Frontend Development**: Node.js 18+, npm 9+, TypeScript 5+
- **Infrastructure**: Kubernetes, Docker, Helm (optional)

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/tasawwur-rtc/tasawwur-rtc.git
   cd tasawwur-rtc
   ```

2. **Quick setup for new developers**
   ```bash
   make setup
   make dev
   ```

3. **Verify the setup**
   - Dashboard: http://localhost:3000
   - Documentation: http://localhost:3001
   - REST API: http://localhost:8081/actuator/health
   - Signaling: ws://localhost:8080/ws

## 📁 Repository Structure

```
tasawwur-rtc/
├── sdk-android/           # Android SDK (Kotlin + C++ + WebRTC)
├── backend-services/      # Microservices (Spring Boot)
│   ├── signaling-server/  # WebSocket signaling server
│   └── rest-api-server/   # REST API for auth & projects
├── dashboard-frontend/    # React TypeScript dashboard
├── docs/                  # Docusaurus documentation
├── infra-k8s/            # Kubernetes manifests
├── tests/                # Integration and E2E tests
└── scripts/              # Build and deployment scripts
```

## 🛠️ Development Workflow

### 1. Creating Issues

Before contributing, please:
- Check existing issues to avoid duplicates
- Use issue templates for bug reports and feature requests
- Provide detailed information and reproduction steps

### 2. Branch Naming Convention

- `feature/description` - New features
- `fix/description` - Bug fixes
- `docs/description` - Documentation updates
- `refactor/description` - Code refactoring
- `test/description` - Test improvements

### 3. Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description

[optional body]

[optional footer]
```

Examples:
```
feat(sdk): add hardware acceleration support
fix(signaling): resolve WebSocket connection timeout
docs(quickstart): update Android integration guide
```

### 4. Pull Request Process

1. **Fork and create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow coding standards (see below)
   - Add tests for new functionality
   - Update documentation if needed

3. **Test your changes**
   ```bash
   make test
   make test-integration
   ```

4. **Create a pull request**
   - Use the PR template
   - Link related issues
   - Provide clear description and testing instructions

5. **Code review process**
   - Address reviewer feedback
   - Ensure CI passes
   - Maintain clean commit history

## 📋 Coding Standards

### Android SDK (Kotlin/C++)

- **Kotlin**: Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **C++**: Follow [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)
- **Documentation**: Use KDoc for Kotlin, Doxygen for C++
- **Testing**: Unit tests with JUnit 5, integration tests with Espresso

```kotlin
/**
 * Example of proper KDoc documentation.
 * 
 * @param channelName The name of the channel to join
 * @return Result indicating success or failure
 */
suspend fun joinChannel(channelName: String): Result<Unit> {
    // Implementation
}
```

### Backend Services (Java)

- **Style**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **Documentation**: Use Javadoc for all public APIs
- **Testing**: Unit tests with JUnit 5, integration tests with TestContainers
- **Architecture**: Follow Clean Architecture principles

```java
/**
 * Service for managing WebRTC signaling tokens.
 * 
 * This service provides secure token generation and validation
 * for client authentication with the signaling server.
 * 
 * @author Tasawwur RTC Team
 * @since 1.0.0
 */
@Service
public class TokenService {
    // Implementation
}
```

### Frontend (TypeScript/React)

- **Style**: Follow [Airbnb TypeScript Style Guide](https://github.com/airbnb/javascript/tree/master/packages/eslint-config-airbnb-typescript)
- **Components**: Use functional components with hooks
- **State**: Use Zustand for global state, React Query for server state
- **Testing**: Jest + React Testing Library

```typescript
/**
 * Dashboard page component showing project overview and statistics.
 */
const DashboardPage: React.FC = () => {
  const { user } = useAuthStore()
  
  // Component implementation
}
```

### Documentation

- **Format**: Markdown with front matter
- **Style**: Clear, concise, example-driven
- **Structure**: Follow existing patterns
- **Links**: Use relative links for internal content

## 🧪 Testing Guidelines

### Test Categories

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test component interactions
3. **E2E Tests**: Test complete user workflows
4. **Performance Tests**: Validate latency and resource usage

### Testing Commands

```bash
# Run all tests
make test

# Run specific test suites
make test-android
make test-backend
make test-frontend

# Run integration tests
make test-integration

# Run performance tests
make perf-test
```

### Test Coverage Requirements

- **Minimum coverage**: 80% for all components
- **Critical paths**: 95% coverage required
- **New features**: Must include comprehensive tests

## 🚀 Performance Requirements

### Latency Goals
- **Glass-to-glass latency**: <200ms
- **API response time**: <100ms (95th percentile)
- **WebSocket connection**: <50ms

### Resource Usage
- **SDK footprint**: <5MB increase to APK
- **CPU usage**: <15% on Pixel 6a during active call
- **Memory usage**: <100MB peak for SDK

### Scalability Targets
- **Concurrent users**: 10,000+ per cluster
- **Horizontal scaling**: Linear performance scaling
- **Database**: <10ms query response time

## 📊 Code Quality

### Quality Gates

All contributions must pass:
- **Linting**: ESLint, Spotbugs, Ktlint
- **Security**: OWASP dependency check
- **Performance**: Load testing validation
- **Documentation**: API docs generation

### Quality Commands

```bash
# Run quality checks
make quality

# Security scanning
make security

# Performance validation
make perf-test
```

## 🔄 Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking API changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Release Checklist

1. **Pre-release**
   - [ ] Update version numbers
   - [ ] Update CHANGELOG.md
   - [ ] Run full test suite
   - [ ] Security and quality checks

2. **Release**
   - [ ] Create Git tag
   - [ ] Build and push Docker images
   - [ ] Deploy to staging
   - [ ] Run smoke tests

3. **Post-release**
   - [ ] Deploy to production
   - [ ] Update documentation
   - [ ] Announce release

## 🤝 Community Guidelines

### Code of Conduct

We are committed to providing a welcoming and inclusive environment. Please read our [Code of Conduct](CODE_OF_CONDUCT.md).

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Discord**: Real-time community chat
- **Email**: security@tasawwur-rtc.com for security issues

### Recognition

Contributors will be recognized in:
- **CONTRIBUTORS.md**: All contributors listed
- **Release notes**: Major contributions highlighted
- **Documentation**: Author attribution

## 📝 Documentation

### Documentation Types

1. **API Reference**: Auto-generated from code
2. **Tutorials**: Step-by-step guides
3. **How-to Guides**: Problem-solving focused
4. **Explanations**: Conceptual information

### Writing Guidelines

- **Audience**: Assume intermediate developer knowledge
- **Style**: Clear, concise, action-oriented
- **Examples**: Include working code samples
- **Testing**: Verify all code examples work

## 🆘 Getting Help

### Before Asking for Help

1. Check existing documentation
2. Search GitHub issues
3. Review troubleshooting guides
4. Try the minimal reproduction

### How to Ask for Help

1. **Provide context**: What are you trying to achieve?
2. **Include details**: OS, versions, error messages
3. **Share code**: Minimal reproduction example
4. **Show effort**: What have you tried?

### Support Channels

- **Community Support**: GitHub Discussions, Discord
- **Bug Reports**: GitHub Issues
- **Security Issues**: security@tasawwur-rtc.com
- **Commercial Support**: enterprise@tasawwur-rtc.com

## 🏆 Contribution Recognition

### Contributor Levels

- **Contributor**: Made accepted contributions
- **Regular Contributor**: 5+ merged PRs
- **Core Contributor**: 20+ merged PRs, domain expertise
- **Maintainer**: Commit access, review responsibilities

### Benefits

- **All Contributors**: Listed in CONTRIBUTORS.md
- **Regular Contributors**: Early access to new features
- **Core Contributors**: Influence on roadmap
- **Maintainers**: Direct collaboration with core team

## 📈 Metrics and Success

We track contribution success through:
- **Code quality**: Test coverage, linting scores
- **Performance**: Latency benchmarks, resource usage
- **Adoption**: SDK downloads, API usage
- **Community**: Contributors, issues resolved

---

Thank you for contributing to Tasawwur RTC! Together, we're building the future of real-time communication. 🚀
