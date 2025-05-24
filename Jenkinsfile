pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'darwinl06'
        DOCKER_CREDENTIALS_ID = 'huevos'
        SERVICES = 'api-gateway cloud-config favourite-service order-service payment-service product-service proxy-client service-discovery shipping-service user-service'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/darwinl-06/ecommerce-microservice-backend-app.git'
            }
        }

        stage('Verify Tools') {
            steps {
                sh 'java -version'
                sh 'docker --version'
                sh 'mvn -version'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    SERVICES.split().each { service ->
                        sh "docker build -t ${DOCKERHUB_USER}/${service}:latest ./${service}"
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                withCredentials([string(credentialsId: "${DOCKER_CREDENTIALS_ID}", variable: 'DOCKERHUB_PASSWORD')]) {
                    sh 'echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USER" --password-stdin'
                    script {
                        SERVICES.split().each { service ->
                            sh "docker push ${DOCKERHUB_USER}/${service}:latest"
                        }
                    }
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                echo 'Deploying services to Minikube...'
                // Reemplaza esto con tus archivos reales de despliegue
                // Por ejemplo:
                // sh 'kubectl apply -f k8s/configmap.yaml'
                // sh 'kubectl apply -f k8s/deployments/'
                // sh 'kubectl apply -f k8s/services/'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
