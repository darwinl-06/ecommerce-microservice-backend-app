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
        K8S_NAMESPACE = 'ecommerce'
    }

    stages {
        stage('Init') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'master') {
                        env.SPRING_PROFILE = 'prod'
                        env.IMAGE_TAG = 'prod'
                        env.DEPLOYMENT_SUFFIX = '-prod'

                    } else if (env.BRANCH_NAME == 'release') {
                        env.SPRING_PROFILE = 'stage'
                        env.IMAGE_TAG = 'stage'
                        env.DEPLOYMENT_SUFFIX = '-stage'

                    } else {
                        env.SPRING_PROFILE = 'dev'
                        env.IMAGE_TAG = 'dev'
                        env.DEPLOYMENT_SUFFIX = '-dev'
                    }

                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Namespace: ${env.K8S_NAMESPACE}"
                    echo "Spring profile: ${env.SPRING_PROFILE}"
                    echo "Image tag: ${env.IMAGE_TAG}"
                    echo "Deployment suffix: ${env.DEPLOYMENT_SUFFIX}"
                }
            }
        }

        stage('Ensure Namespace') {
            steps {
                script {
                    def ns = env.K8S_NAMESPACE
                    bat "kubectl get namespace ${ns} || kubectl create namespace ${ns}"
                }
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/darwinl-06/ecommerce-microservice-backend-app.git'
            }
        }

        stage('Verify Tools') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker --version'
                bat 'kubectl config current-context'

            }          
        }

        stage('Unit Tests') {
            parallel {
                stage('Unit Tests') {
                    when {
                        anyOf {
                            branch 'dev'
                            branch 'dev'
                            branch 'release'
                            expression { env.BRANCH_NAME.startsWith('feature/') }
                        }
                    }
                    steps {
                        script {
                            echo "🔍 Running Unit Tests for ${env.BRANCH_NAME}"
                            bat "mvn clean test -DfailIfNoTests=false"
                        }
                    }
                }
            }
        }

        stage('Integration Tests') {
            parallel {
                stage('Integration Tests') {
                    when {
                        anyOf {
                            branch 'dev'
                            expression { env.BRANCH_NAME.startsWith('feature/') }
                            allOf {
                                not { branch 'master' }
                                not { branch 'release' }
                            }
                        }
                    }
                    steps {
                        script {
                            echo "🧪 Running Integration Tests for ${env.BRANCH_NAME}"
                            bat "mvn clean verify -Dit.test=*ControllerTest.java -DfailIfNoTests=false -Dfailsafe.failIfNoSpecifiedTests=false"
                        }
                    }
                }
            }
        }

        stage('E2E Tests') {
                    parallel {
                        stage('E2E Tests') {
                            when {
                                anyOf {
                                    branch 'master'
                                    expression { env.BRANCH_NAME.startsWith('feature/') }
                                    allOf {
                                        not { branch 'master' }
                                        not { branch 'release' }
                                    }
                                }
                            }
                            steps {
                                echo '👻👻👻👻👻👻'
                            }
                        }
                    }
                }

        stage('Build Services') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'release'
                }
            }
            steps {
                bat "mvn clean package -DskipTests"
            }
        }

        stage('Build Docker Images') {
            when { branch 'dev' }
            steps {
                script {
                    SERVICES.split().each { service ->
                        bat "docker build -t ${DOCKERHUB_USER}/${service}:${IMAGE_TAG} .\\${service}"
                    }
                }
            }
        }

        stage('Push Docker Images') {
            when { branch 'dev' }
            steps {
                withCredentials([string(credentialsId: "${DOCKER_CREDENTIALS_ID}", variable: 'DOCKERHUB_PASSWORD')]) {
                    bat "echo ${DOCKERHUB_PASSWORD} | docker login -u ${DOCKERHUB_USER} --password-stdin"
                    script {
                        SERVICES.split().each { service ->
                            bat "docker push ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy Common Config') {
            when { branch 'master' }
            steps {
                bat "kubectl apply -f k8s\\common-config.yaml -n ${K8S_NAMESPACE}"
            }
        }

        stage('Deploy Core Services') {
            when { branch 'master' }
            steps {
                bat "kubectl apply -f k8s\\zipkin -n ${K8S_NAMESPACE}"
                bat "kubectl wait --for=condition=ready pod -l app=zipkin -n ${K8S_NAMESPACE} --timeout=200s"

                bat """
                    set IMAGE_TAG=${IMAGE_TAG}
                    set K8S_NAMESPACE=${K8S_NAMESPACE}
                    bash -c "envsubst < k8s/service-discovery/deployment.yaml | kubectl apply -n %K8S_NAMESPACE% -f -"
                """
                bat "kubectl wait --for=condition=ready pod -l app=service-discovery -n ${K8S_NAMESPACE} --timeout=200s"

                bat """
                    set IMAGE_TAG=${IMAGE_TAG}
                    set K8S_NAMESPACE=${K8S_NAMESPACE}
                    bash -c "envsubst < k8s/cloud-config/deployment.yaml | kubectl apply -n %K8S_NAMESPACE% -f -"
                """
                bat "kubectl wait --for=condition=ready pod -l app=cloud-config -n ${K8S_NAMESPACE} --timeout=300s"
            }
        }


        stage('Deploy Microservices') {
            when { branch 'master' }
            steps {
                script {
                    echo '👻👻👻👻👻👻'
                }
            }
        }
    }

    post {
        always {
            script {
                echo "📋 Collecting Test Reports and Artifacts"

            }
        }
        success {
            script {
                echo "✅ Pipeline completed successfully for ${env.BRANCH_NAME} branch."
                echo "📊 Environment: ${env.SPRING_PROFILE}"

                if (env.BRANCH_NAME == 'master') {
                    echo "🚀 Production deployment completed successfully!"
                } else if (env.BRANCH_NAME == 'release') {
                    echo "🎯 Staging deployment completed successfully!"
                } else {
                    echo "🔧 Development tests completed successfully!"
                }
            }
        }
        failure {
            script {
                echo "❌ Pipeline failed for ${env.BRANCH_NAME} branch."
                echo "🔍 Check the logs for details."
                echo "📧 Notify the development team about the failure."

            }
        }
        unstable {
            script {
                echo "⚠️  Pipeline completed with warnings for ${env.BRANCH_NAME} branch."
                echo "🔍 Some tests may have failed. Review test reports."
            }
        }
    }
}
