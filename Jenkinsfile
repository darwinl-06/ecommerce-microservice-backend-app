pipeline {
    agent any

    tools {
        maven 'mvn'
        jdk 'JDK_11'
    }

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
                bat 'java -version'
                bat 'docker --version'
                bat 'mvn -version'
            }
        }

        stage('Check Kube Context') {
            steps {
                bat 'kubectl config current-context'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    SERVICES.split().each { service ->
                        bat "cd ${service} && mvn clean package -DskipTests"
                        bat "docker build -t %DOCKERHUB_USER%/${service}:latest .\\${service}"
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                withCredentials([string(credentialsId: "${DOCKER_CREDENTIALS_ID}", variable: 'DOCKERHUB_PASSWORD')]) {
                    bat 'echo %DOCKERHUB_PASSWORD% | docker login -u %DOCKERHUB_USER% --password-stdin'
                    script {
                        SERVICES.split().each { service ->
                            bat "docker push %DOCKERHUB_USER%/${service}:latest"
                        }
                    }
                }
            }
        }

        stage('Deploy common configuration') {
            steps {
                bat """
                echo Applying common configuration...
                kubectl apply -f k8s\\common-config.yaml
                """
            }
        }

        stage('Deploy core services to k8s in minikube') {
            steps {
                bat 'kubectl apply -f k8s\\zipkin --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=zipkin -n ecommerce-app --timeout=200s'

                bat 'kubectl apply -f k8s\\service-discovery --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=service-discovery -n ecommerce-app --timeout=200s'

                bat 'kubectl apply -f k8s\\cloud-config --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=cloud-config -n ecommerce-app --timeout=300s'
            }
        }

        stage('Deploy to Minikube') {
            steps {
                bat 'kubectl apply -f k8s\\api-gateway --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=api-gateway  -n ecommerce-app --timeout=200s'

                bat 'kubectl apply -f k8s\\user-service --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=user-service -n ecommerce-app --timeout=200s'

                bat 'kubectl apply -f k8s\\proxy-client --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=proxy-client -n ecommerce-app --timeout=300s'

                bat 'kubectl apply -f k8s\\product-service --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=product-service -n ecommerce-app --timeout=200s'

                bat 'kubectl apply -f k8s\\shipping-service --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=shipping-service -n ecommerce-app --timeout=200s'

                bat 'kubectl apply -f k8s\\order-service --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=order-service -n ecommerce-app --timeout=300s'

                bat 'kubectl apply -f k8s\\favourite-service --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=favourite-service -n ecommerce-app --timeout=200s'

                bat 'kubectl apply -f k8s\\payment-service --namespace=ecommerce-app'
                bat 'kubectl wait --for=condition=ready pod -l app=payment-service -n ecommerce-app --timeout=200s'

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
