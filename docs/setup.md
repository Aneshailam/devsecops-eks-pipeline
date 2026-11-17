# Infrastructure setup on AWS EC2

The pipeline tools run on EC2 instances in one VPC. Adjust sizes to your budget.

| Server | Purpose | Suggested type | Open ports (Security Group) |
|--------|---------|----------------|-----------------------------|
| Jenkins | CI/CD server | t2.medium | 22 (your IP), 8080 |
| SonarQube | Code analysis | t2.medium | 22 (your IP), 9000 |
| Nexus | Artifact repository | t2.medium | 22 (your IP), 8081 |

Restrict SSH (22) to your own IP, not 0.0.0.0/0.

## 1. Jenkins server (Ubuntu)

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven docker.io
sudo usermod -aG docker ubuntu
# install Jenkins from the official Jenkins apt repository, then:
sudo systemctl enable --now jenkins
```

Also install on this server: Trivy, kubectl, AWS CLI.
Open `http://JENKINS_IP:8080`, unlock with
`sudo cat /var/lib/jenkins/secrets/initialAdminPassword`,
and install plugins: Git, Pipeline, GitHub, SonarQube Scanner,
Config File Provider, Pipeline Maven Integration.

## 2. SonarQube server

```bash
sudo apt update && sudo apt install -y docker.io
sudo docker run -d --name sonarqube -p 9000:9000 sonarqube:lts-community
```

Open `http://SONAR_IP:9000` (default login admin/admin, change it).
Create a token and add it in Jenkins: Manage Jenkins > System > SonarQube servers
(name it `sonar-server`).

## 3. Nexus server

```bash
sudo apt update && sudo apt install -y docker.io
sudo docker run -d --name nexus -p 8081:8081 sonatype/nexus3
```

Open `http://NEXUS_IP:8081`. Use the `maven-releases` and `maven-snapshots`
repositories. Add the Nexus username and password in Jenkins as a
Maven settings file (Config File Provider, ID `maven-settings`) with server IDs
`nexus-releases` and `nexus-snapshots`.

## 4. GitHub webhook

In the GitHub repo: Settings > Webhooks > Add webhook
- Payload URL: `http://JENKINS_IP:8080/github-webhook/`
- Content type: `application/json`
- Event: Just the push event

## 5. Amazon EKS cluster

```bash
eksctl create cluster --name devsecops-cluster --region ap-south-1 \
  --nodegroup-name workers --node-type t3.medium --nodes 2
aws eks update-kubeconfig --name devsecops-cluster --region ap-south-1
```

Give the Jenkins server access to the cluster (kubeconfig and IAM permissions).

## 6. Clean up

```bash
eksctl delete cluster --name devsecops-cluster --region ap-south-1
```
Stop or terminate the EC2 instances to avoid charges.
