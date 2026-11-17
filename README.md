# DevSecOps CI/CD Pipeline on Amazon EKS

![CI](https://github.com/YOUR_GITHUB_USERNAME/devsecops-eks-pipeline/actions/workflows/ci.yml/badge.svg)

A CI/CD pipeline where every push passes automated quality and security gates before it is deployed to a multi-node Amazon EKS cluster.

## Pipeline flow

```
GitHub push -> Jenkins -> Maven compile & test -> Trivy file scan -> SonarQube analysis
            -> Maven package -> Nexus -> Docker build -> Trivy image scan
            -> Docker Hub -> Amazon EKS (behind a load balancer) -> Prometheus & Grafana
```

The same workflow is also built in **GitHub Actions** (`.github/workflows/ci.yml`).

## Tech stack

| Area | Tools |
|------|-------|
| Source control | Git, GitHub |
| CI/CD | Jenkins, GitHub Actions |
| Build | Maven, Java 17, Spring Boot |
| Security | Trivy (files and images), SonarQube |
| Artifacts | Nexus, Docker Hub |
| Containers | Docker (multi-stage build) |
| Orchestration | Kubernetes, Amazon EKS |
| Monitoring | Prometheus, Grafana |

## Repository structure

```
app/                  Spring Boot sample app, tests and Dockerfile
k8s/                  Kubernetes Deployment and Service
monitoring/           Prometheus and Grafana install guide and values
Jenkinsfile           Jenkins pipeline
sonar-project.properties
.github/workflows/    GitHub Actions workflow
docs/                 EC2 setup guide, screenshots and notes
```

## Pipeline stages

1. **Checkout** the code from GitHub.
2. **Compile and test** with Maven.
3. **Trivy file scan** for vulnerable dependencies and misconfigurations.
4. **SonarQube analysis** for code quality and bugs.
5. **Package** the jar and publish it to **Nexus**.
6. **Docker build** using a multi-stage Dockerfile and a non-root user.
7. **Trivy image scan** on the built image.
8. **Push** the image to Docker Hub.
9. **Deploy** to Amazon EKS with `kubectl apply`.
10. **Monitor** with Prometheus and Grafana.

## Run the app locally

```bash
cd app
mvn spring-boot:run
# open http://localhost:8080 and http://localhost:8080/health
```

## Build and run with Docker

```bash
cd app
docker build -t devsecops-demo .
docker run -p 8080:8080 devsecops-demo
```

## Setup needed for the pipeline

Full steps: [`docs/setup.md`](docs/setup.md). Monitoring: [`monitoring/install-monitoring.md`](monitoring/install-monitoring.md).

- Jenkins with tools named `maven3`, `jdk17` and `sonar-scanner`
- A SonarQube server configured in Jenkins as `sonar-server`
- Jenkins credential `dockerhub-creds` (Docker Hub username and token)
- Trivy and Docker installed on the Jenkins agent
- An EKS cluster, with `kubectl` on the Jenkins agent pointing to it
- Replace `YOUR_DOCKERHUB_USERNAME` and `YOUR_GITHUB_USERNAME` in the `Jenkinsfile` and this README

## Screenshots

See [`docs/screenshots.md`](docs/screenshots.md).

## Cleanup

Delete the EKS cluster and its node group when you are done to avoid AWS charges.

## Lessons learned

- Put security scans early so problems fail fast.
- Multi-stage builds and a non-root user keep the image small and safer.
- Readiness and liveness probes make rollouts safer on Kubernetes.
