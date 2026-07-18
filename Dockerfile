# ============================================================
# DOCKERFILE — Enterprise HRM System
# ============================================================
#
# MULTI-STAGE BUILD — Industry best practice for production images
# ─────────────────────────────────────────────────────────────
#
# WHY multi-stage?
#   Stage 1 (build): Uses JDK (large, ~500MB) with Maven to compile
#   Stage 2 (runtime): Uses JRE (small, ~200MB) to run the app
#
#   Result: Final image only contains the runtime JRE + compiled JAR
#   NOT the Maven, source code, or build tools.
#   This dramatically reduces the attack surface and image size.
#
# LAYERED JARs:
#   Spring Boot Maven Plugin creates a "layered JAR" where
#   dependencies, Spring Boot libs, and app classes are in separate
#   layers. Docker caches each layer independently.
#
#   WHY does this matter?
#   If only application code changes (not dependencies), Docker
#   reuses the cached dependency layer → MUCH faster builds.
#
# Build command: docker build -t enterprise-hrm:1.0 .
# Run command:   docker run -p 8080:8080 enterprise-hrm:1.0

# ─────────────────────────────────────────────────────────────
# STAGE 1: BUILD
# ─────────────────────────────────────────────────────────────

# Use Eclipse Temurin (OpenJDK distribution by Adoptium) — free, production-grade
# Use Maven with Eclipse Temurin (JDK 21) for the build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Set working directory inside the container
# All subsequent commands run relative to this path
WORKDIR /app

# CACHE OPTIMIZATION:
# Copy pom.xml FIRST and download dependencies BEFORE copying source code.
# If pom.xml doesn't change, Docker reuses this cached layer.
# This means dependency downloads only happen when pom.xml changes.
COPY pom.xml .

# Download dependencies only (skip actual compilation)
# -B = batch mode (no colors, for CI) | -q = quiet | -e = show errors
# dependency:go-offline caches all deps in the Docker layer
RUN mvn -B -q -e dependency:go-offline

# Now copy source code (this layer changes frequently)
COPY src ./src

# Build the application JAR, skipping tests
# (Tests should run in CI/CD pipeline separately)
# -DskipTests = skip test execution but still compile them
RUN mvn -B -q -e package -DskipTests

# Extract layered JAR for optimized Docker layer caching
# The spring-boot-maven-plugin generates a layered JAR structure
WORKDIR /app/target

# Extract layers: dependencies, spring-boot-loader, snapshot-dependencies, application
RUN java -Djarmode=layertools -jar enterprise-hrm-*.jar extract

# ─────────────────────────────────────────────────────────────
# STAGE 2: RUNTIME
# ─────────────────────────────────────────────────────────────

# JRE only (not JDK) — no compiler, no javac → smaller, more secure
# alpine = minimal Linux base (~5MB vs ~200MB Ubuntu)
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user for security
# WHY? Running as root inside a container is a security risk.
# If the app is compromised, the attacker has root inside the container.
RUN addgroup -S hrm && adduser -S hrm -G hrm

# Set working directory
WORKDIR /app

# Copy the extracted JAR layers from the builder stage
# Order matters: least-changed layers first (cached more often)
COPY --from=builder /app/target/dependencies/ ./
COPY --from=builder /app/target/spring-boot-loader/ ./
COPY --from=builder /app/target/snapshot-dependencies/ ./
COPY --from=builder /app/target/application/ ./

# Create log directory and set ownership
RUN mkdir -p logs && chown -R hrm:hrm /app

# Switch to non-root user
USER hrm

# Document that the app listens on port 8080
# (This is documentation only — doesn't actually expose the port)
EXPOSE 8080

# Environment variables with defaults
# These can be overridden at runtime: docker run -e SPRING_PROFILES_ACTIVE=prod
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# HEALTH CHECK:
# Docker periodically runs this command to determine container health.
# --interval=30s  — check every 30 seconds
# --timeout=3s    — if check takes >3s, mark as unhealthy
# --retries=3     — must fail 3 consecutive times to be UNHEALTHY
# --start-period=30s — grace period after container starts (app startup time)
HEALTHCHECK --interval=30s --timeout=3s --retries=3 --start-period=30s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Application entry point
# Using exec form (JSON array) — signals go directly to the JVM process
# NOT shell form — shell form wraps with /bin/sh -c which doesn't forward signals
ENTRYPOINT ["java", \
    "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", \
    "com.enterprise.hrm.EhrApplication"]
