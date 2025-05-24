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

        // 👇 NUEVAS ETAPAS AÑADIDAS AQUÍ
        stage('Deploy common configuration') {
            steps {
                sh """
                echo "Applying common configuration..."
                kubectl apply -f k8s/common-config.yaml
                """
            }
        }

        stage('Deploy core services to k8s in minikube') {
            steps {
                sh """
                kubectl apply -f k8s/zipkin/
                kubectl wait --for=condition=ready pod -l app=zipkin --timeout=120s

                kubectl apply -f k8s/service-discovery/
                kubectl wait --for=condition=ready pod -l app=service-discovery --timeout=120s

                kubectl apply -f k8s/cloud-config/
                kubectl wait --for=condition=ready pod -l app=cloud-config --timeout=120s
                """
            }
        }

        stage('Deploy to Minikube') {
            steps {
                echo 'Deploying services to Minikube...'
                // Agrega aquí los `kubectl apply` para tus servicios restantes
                // Ejemplo:
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
