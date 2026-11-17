pipeline {
    agent any

    tools {
        maven 'maven3'      // configure in Manage Jenkins > Tools
        jdk   'jdk17'
    }

    triggers {
        githubPush()    // runs on every push via the GitHub webhook
    }

    environment {
        IMAGE_NAME  = 'YOUR_DOCKERHUB_USERNAME/devsecops-demo'
        IMAGE_TAG   = "${BUILD_NUMBER}"
        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/YOUR_GITHUB_USERNAME/devsecops-eks-pipeline.git'
            }
        }

        stage('Compile') {
            steps { sh 'cd app && mvn -B clean compile' }
        }

        stage('Unit Tests') {
            steps { sh 'cd app && mvn -B test' }
        }

        stage('Trivy File Scan') {
            steps { sh 'trivy fs --exit-code 0 --severity HIGH,CRITICAL --format table -o trivy-fs-report.txt .' }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh '''$SCANNER_HOME/bin/sonar-scanner'''
                }
            }
        }

        stage('Package') {
            steps { sh 'cd app && mvn -B package -DskipTests' }
        }

        stage('Publish to Nexus') {
            steps {
                withMaven(globalMavenSettingsConfig: 'maven-settings', maven: 'maven3') {
                    sh 'cd app && mvn -B deploy -DskipTests'
                }
            }
        }

        stage('Docker Build') {
            steps { sh 'cd app && docker build -t $IMAGE_NAME:$IMAGE_TAG .' }
        }

        stage('Trivy Image Scan') {
            steps { sh 'trivy image --exit-code 0 --severity HIGH,CRITICAL --format table -o trivy-image-report.txt $IMAGE_NAME:$IMAGE_TAG' }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    sh '''
                        echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                        docker push $IMAGE_NAME:$IMAGE_TAG
                    '''
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                sh '''
                    sed -i "s|IMAGE_PLACEHOLDER|$IMAGE_NAME:$IMAGE_TAG|g" k8s/deployment.yaml
                    kubectl apply -f k8s/
                    kubectl rollout status deployment/devsecops-demo
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'trivy-*.txt', allowEmptyArchive: true
        }
    }
}
