pipeline {
    agent any

    tools {
        maven 'mvn'
        jdk 'JDK_11'
    }

    environment {
        GOOGLE_APPLICATION_CREDENTIALS = credentials('gcp-service-account-key')
        DOCKERHUB_USER = 'darwinl06'
        DOCKER_CREDENTIALS_ID = 'huevos'
        SERVICES = 'service-discovery cloud-config api-gateway product-service user-service order-service payment-service shipping-service favourite-service proxy-client locust'
        K8S_NAMESPACE = 'ecommerce'
        PATH = "C:\\Program Files (x86)\\Google\\Cloud SDK\\google-cloud-sdk\\bin;${env.PATH}"
        TF_VAR_project_id = 'proyecto-final-ingesoftv'
    }

    stages {

        stage('Init') {
            steps {
                script {
                    def profileConfig = [
                        master : ['prod', '-prod'],
                        stage  : ['stage', '-stage']
                    ]
                    def config = profileConfig[env.BRANCH_NAME] ?: ['dev', '-dev']

                    env.SPRING_PROFILES_ACTIVE = config[0]
                    env.IMAGE_TAG = config[0]
                    env.DEPLOYMENT_SUFFIX = config[1]

                    echo "📦 Branch: ${env.BRANCH_NAME}"
                    echo "🌱 Spring profile: ${env.SPRING_PROFILES_ACTIVE}"
                    echo "🏷️ Image tag: ${env.IMAGE_TAG}"
                    echo "📂 Deployment suffix: ${env.DEPLOYMENT_SUFFIX}"
                }
            }
        }

        stage('Authenticate to GCP') {
            steps {
                withCredentials([file(credentialsId: 'gcp-service-account-key', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                    bat """
                    echo "🔐 Activando cuenta de servicio..."
                    gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS
                    gcloud config set project $TF_VAR_project_id
                    """
                }
            }
        }

        stage('Get GKE Credentials') {
            steps {
                script {
                    def clusterName = "ecommerce-cluster-${env.SPRING_PROFILES_ACTIVE}"
                    bat """
                    echo 🔐 Obteniendo credenciales del cluster GKE...
                    gcloud container clusters get-credentials ${clusterName} --zone us-central1 --project ${env.TF_VAR_project_id}
                    """
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

        stage('Build & Package') {
            when { anyOf { branch 'master'; branch 'stage'; branch 'dev'; } }
            steps {
                bat "mvn clean package -DskipTests"
            }
        }

        stage('Ensure Namespace') {
            steps {
                bat "kubectl get namespace ${K8S_NAMESPACE} || kubectl create namespace ${K8S_NAMESPACE}"
                bat "kubectl get namespace monitoring || kubectl create namespace monitoring"
            }
        }

//         stage('Run SonarQube Analysis') {
//             when { branch 'master' }
//             tools {
//                 jdk 'JDK_20'
//             }
//             environment {
//                 JAVA_HOME = tool 'JDK_20'
//                 PATH = "${JAVA_HOME}/bin:${env.PATH}"
//                 scannerHome = tool 'SonarQubeOtraCosa'
//             }
//             steps {
//                 script {
//                     def javaServices = [
//                         'api-gateway',
//                         'cloud-config',
//                         'favourite-service',
//                         'order-service',
//                         'payment-service',
//                         'product-service',
//                         'proxy-client',
//                         'service-discovery',
//                         'shipping-service',
//                         'user-service',
//                         'e2e-tests'
//                     ]
//
//                     withSonarQubeEnv(credentialsId: 'access_sonarqube', installationName: 'sonarqubesecae') {
//                         javaServices.each { service ->
//                             dir(service) {
//                                 bat "${scannerHome}/bin/sonar-scanner " +
//                                 "-Dsonar.projectKey=${service} " +
//                                 "-Dsonar.projectName=${service} " +
//                                 '-Dsonar.sources=src ' +
//                                 '-Dsonar.java.binaries=target/classes'
//                             }
//                         }
//
//                         dir('locust') {
//                             bat "${scannerHome}/bin/sonar-scanner " +
//                             '-Dsonar.projectKey=locust ' +
//                             '-Dsonar.projectName=locust ' +
//                             '-Dsonar.sources=test'
//                         }
//                     }
//                 }
//             }
//         }

         stage('Trivy Vulnerability Scan & Report') {
             when { branch 'stage' }
             environment {
                 TRIVY_PATH = 'C:/ProgramData/chocolatey/bin'
             }
             steps {
                 script {
                     env.PATH = "${TRIVY_PATH};${env.PATH}"

                     def services = [
                         'api-gateway',
                         'cloud-config',
                         'favourite-service',
                         'order-service',
                         'payment-service',
                         'product-service',
                         'proxy-client',
                         'service-discovery',
                         'shipping-service',
                         'user-service'
                     ]

                     bat """
                     if not exist trivy-reports (
                         mkdir trivy-reports
                     )
                     """

                     services.each { service ->
                         def reportPath = "trivy-reports\\${service}.html"

                         echo "🔍 Escaneando imagen ${IMAGE_TAG} con Trivy para ${service}..."
                         bat """
                         trivy image --format template ^
                             --template "@C:/ProgramData/chocolatey/lib/trivy/tools/contrib/html.tpl" ^
                             --severity HIGH,CRITICAL ^
                             -o ${reportPath} ^
                             ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}
                         """
                     }

                     publishHTML(target: [
                         allowMissing: true,
                         alwaysLinkToLastBuild: true,
                         keepAll: true,
                         reportDir: 'trivy-reports',
                         reportFiles: '*.html',
                         reportName: 'Trivy Scan Report'
                     ])
                 }
             }
         }

//         stage('Build & Push Docker Images') {
//             when { anyOf { branch 'stage'; branch 'master' } }
//             steps {
//                 withCredentials([string(credentialsId: "${DOCKER_CREDENTIALS_ID}", variable: 'DOCKERHUB_PASSWORD')]) {
//                     bat "echo ${DOCKERHUB_PASSWORD} | docker login -u ${DOCKERHUB_USER} --password-stdin"
//
//                     script {
//                         SERVICES.split().each { service ->
//                             bat "docker build -t ${DOCKERHUB_USER}/${service}:${IMAGE_TAG} .\\${service}"
//                             bat "docker push ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}"
//                         }
//                     }
//                 }
//             }
//         }

        stage('Unit Tests') {
            when {
                anyOf {
                    branch 'dev'; branch 'stage';
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                }
            }
            steps {
                script {
                    ['user-service', 'product-service', 'payment-service'].each {
                        bat "mvn test -pl ${it}"
                    }
                }
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Integration Tests') {
            when {
                anyOf {
                    branch 'dev'; branch 'stage';
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                }
            }
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        bat "mvn verify -pl ${it}"
                    }
                }
                junit '**/target/failsafe-reports/TEST-*.xml'
            }
        }

        stage('E2E Tests') {
            when {
                anyOf {
                    branch 'stage';
                }
            }
            steps {
                bat "mvn verify -pl e2e-tests"
                junit 'e2e-tests/target/failsafe-reports/*.xml'
            }
        }

        stage('Levantar contenedores para pruebas') {
            when {
                anyOf {
                    branch 'stage'
                }
            }
            steps {
                script {
                    bat '''

                    docker network create ecommerce-test || true

                    echo 🚀 Levantando ZIPKIN...
                    docker run -d --name zipkin-container --network ecommerce-test -p 9411:9411 openzipkin/zipkin

                    echo 🚀 Levantando EUREKA...
                    docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 ^
                        -e SPRING_PROFILES_ACTIVE=dev ^
                        -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                        darwinl06/service-discovery:%IMAGE_TAG%

                    call :waitForService http://localhost:8761/actuator/health

                    echo 🚀 Levantando CLOUD-CONFIG...
                    docker run -d --name cloud-config-container --network ecommerce-test -p 9296:9296 ^
                        -e SPRING_PROFILES_ACTIVE=dev ^
                        -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                        -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ ^
                        -e EUREKA_INSTANCE=cloud-config-container ^
                        darwinl06/cloud-config:%IMAGE_TAG%

                    call :waitForService http://localhost:9296/actuator/health

                    call :runService order-service 8300
                    call :runService payment-service 8400
                    call :runService product-service 8500
                    call :runService shipping-service 8600
                    call :runService user-service 8700
                    call :runService favourite-service 8800

                    echo ✅ Todos los contenedores están arriba y saludables.
                    exit /b 0

                    :runService
                    set "NAME=%~1"
                    set "PORT=%~2"
                    echo 🚀 Levantando %NAME%...
                    docker run -d --name %NAME%-container --network ecommerce-test -p %PORT%:%PORT% ^
                        -e SPRING_PROFILES_ACTIVE=dev ^
                        -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                        -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                        -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                        -e EUREKA_INSTANCE=%NAME%-container ^
                        darwinl06/%NAME%:%IMAGE_TAG%
                    call :waitForService http://localhost:%PORT%/%NAME%/actuator/health
                    exit /b 0

                    :waitForService
                    set "URL=%~1"
                    echo ⏳ Esperando a que %URL% esté disponible...
                    :wait_loop
                    for /f "delims=" %%i in ('curl -s %URL% ^| jq -r ".status"') do (
                        if "%%i"=="UP" goto :eof
                    )
                    timeout /t 5 /nobreak
                    goto wait_loop
                    '''
                }
            }
        }

        stage('Run Load Tests with Locust') {
            when {
                anyOf {
                    branch 'stage'
                }
            }
            steps {
                script {
                    bat '''
                    echo 🚀 Levantando Locust para order-service...

                    if not exist locust-reports (
                        mkdir locust-reports
                    )

                    docker run --rm --network ecommerce-test ^
                    -v "%CD%\\locust-reports:/mnt/locust" ^
                    -v "%CD%\\locust:/mnt" ^
                    -v "%CD%\\locust-results:/app" ^
                    darwinl06/locust:%IMAGE_TAG% ^
                    -f /mnt/test/order-service/locustfile.py ^
                    --host http://order-service-container:8300/ ^
                    --headless -u 10 -r 2 -t 1m ^
                    --only-summary ^
                    --html /mnt/locust/order-service-report-load.html

                    echo 🚀 Levantando Locust para payment-service...

                    docker run --rm --network ecommerce-test ^
                    -v "%CD%\\locust-reports:/mnt/locust" ^
                    -v "%CD%\\locust:/mnt" ^
                    -v "%CD%\\locust-results:/app" ^
                    darwinl06/locust:%IMAGE_TAG% ^
                    -f /mnt/test/payment-service/locustfile.py ^
                    --host http://payment-service-container:8400 ^
                    --headless -u 10 -r 2 -t 1m ^
                    --only-summary ^
                    --html /mnt/locust/payment-service-report-load.html

                    echo 🚀 Levantando Locust para favourite-service...

                    docker run --rm --network ecommerce-test ^
                    -v "%CD%\\locust-reports:/mnt/locust" ^
                    -v "%CD%\\locust:/mnt" ^
                    -v "%CD%\\locust-results:/app" ^
                    darwinl06/locust:%IMAGE_TAG% ^
                    -f /mnt/test/favourite-service/locustfile.py ^
                    --host http://favourite-service-container:8800 ^
                    --headless -u 10 -r 2 -t 1m ^
                    --only-summary ^
                    --html /mnt/locust/favourite-service-report-load.html

                    echo ✅ Pruebas completadas
                    '''
                }
            }
        }

        stage('Run Stress Tests with Locust') {
            when {
                anyOf {
                    branch 'stage'
                }
            }
            steps {
                script {
                    bat '''
                    echo 🔥 Levantando Locust para prueba de estrés...

                    docker run --rm --network ecommerce-test ^
                    -v "%CD%\\locust-reports:/mnt/locust" ^
                    -v "%CD%\\locust:/mnt" ^
                    -v "%CD%\\locust-results:/app" ^
                    darwinl06/locust:%IMAGE_TAG% ^
                    -f /mnt/test/order-service/locustfile.py ^
                    --host http://order-service-container:8300 ^
                    --headless -u 50 -r 5 -t 1m ^
                    --only-summary ^
                    --html /mnt/locust/order-service-report-stress.html

                    docker run --rm --network ecommerce-test ^
                    -v "%CD%\\locust-reports:/mnt/locust" ^
                    -v "%CD%\\locust:/mnt" ^
                    -v "%CD%\\locust-results:/app" ^
                    darwinl06/locust:%IMAGE_TAG% ^
                    -f /mnt/test/payment-service/locustfile.py ^
                    --host http://payment-service-container:8400 ^
                    --headless -u 50 -r 5 -t 1m ^
                    --only-summary ^
                    --html /mnt/locust/payment-service-report-stress.html

                    docker run --rm --network ecommerce-test ^
                    -v "%CD%\\locust-reports:/mnt/locust" ^
                    -v "%CD%\\locust:/mnt" ^
                    -v "%CD%\\locust-results:/app" ^
                    darwinl06/locust:%IMAGE_TAG% ^
                    -f /mnt/test/favourite-service/locustfile.py ^
                    --host http://favourite-service-container:8800 ^
                    --headless -u 50 -r 5 -t 1m ^
                    --only-summary ^
                    --html /mnt/locust/favourite-service-report-stress.html

                    echo ✅ Pruebas de estrés completadas
                    '''
                }
            }
        }

        stage('Detener y eliminar contenedores') {
            when {
                anyOf {
                    branch 'stage'
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                }
            }
            steps {
                script {
                    bat """
                    echo 🛑 Deteniendo y eliminando contenedores...

                    docker rm -f locust || exit 0
                    docker rm -f favourite-service-container || exit 0
                    docker rm -f user-service-container || exit 0
                    docker rm -f shipping-service-container || exit 0
                    docker rm -f product-service-container || exit 0
                    docker rm -f payment-service-container || exit 0
                    docker rm -f order-service-container || exit 0
                    docker rm -f cloud-config-container || exit 0
                    docker rm -f service-discovery-container || exit 0
                    docker rm -f zipkin-container || exit 0

                    echo 🧹 Todos los contenedores eliminados
                    """
                }
            }
        }

//         stage('Waiting approval for deployment') {
//             when { branch 'master' }
//             steps {
//                 script {
//                     emailext(
//                         to: '$DEFAULT_RECIPIENTS',
//                         subject: "Action Required: Approval Needed for Deploy of Build #${env.BUILD_NUMBER}",
//                         body: """\
//                         The build #${env.BUILD_NUMBER} for branch *${env.BRANCH_NAME}* has completed and is pending approval for deployment.
//                         Please review the changes and approve or abort
//                         You can access the build details here:
//                         ${env.BUILD_URL}
//                         """
//                     )
//                     input message: 'Approve deployment to production (kubernetes) ?', ok: 'Deploy'
//                 }
//             }
//         }       

        stage('Deploy Observability Stack') {
            when { branch 'master' }
            steps {
                bat '''
                    echo "📊 Deploying Prometheus and Grafana with pre-configured dashboards..."
                    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                    helm repo add grafana https://grafana.github.io/helm-charts
                    helm repo update

                    echo "🚀 Installing Prometheus..."
                    helm upgrade --install prometheus prometheus-community/prometheus ^
                      --namespace monitoring ^
                      -f monitoring/prometheus-values.yaml ^
                      --wait --timeout=300s

                    echo "🚀 Installing Grafana with dashboards..."
                    helm upgrade --install grafana grafana/grafana ^
                      --namespace monitoring ^
                      -f monitoring/grafana-values.yaml ^
                      --wait --timeout=300s

                      echo "✅ Observability stack deployed successfully!"
                '''
            }
        }
        
     
        stage('Deploy Common Config') {
            when { anyOf { branch 'master' } }
            steps {
                bat "kubectl apply -f k8s\\common-config.yaml -n ${K8S_NAMESPACE}"
            }
        }
        
        stage('Deploy Core Services') {
            when { anyOf { branch 'master' } }
            steps {
                bat "kubectl apply -f k8s\\zipkin -n ${K8S_NAMESPACE}"
                bat "kubectl rollout status deployment/zipkin -n ${K8S_NAMESPACE} --timeout=300s"

                bat "kubectl apply -f k8s\\service-discovery -n ${K8S_NAMESPACE}"
                bat "kubectl set image deployment/service-discovery service-discovery=${DOCKERHUB_USER}/service-discovery:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
                bat "kubectl set env deployment/service-discovery SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} -n ${K8S_NAMESPACE}"
                bat "kubectl rollout status deployment/service-discovery -n ${K8S_NAMESPACE} --timeout=300s"

                bat "kubectl apply -f k8s\\cloud-config -n ${K8S_NAMESPACE}"
                bat "kubectl set image deployment/cloud-config cloud-config=${DOCKERHUB_USER}/cloud-config:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
                bat "kubectl set env deployment/cloud-config SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} -n ${K8S_NAMESPACE}"
                bat "kubectl rollout status deployment/cloud-config -n ${K8S_NAMESPACE} --timeout=300s"
            }
        }

//         stage('Deploy Microservices') {
//             when { anyOf { branch 'master' } }
//             steps {
//                 script {
//                     SERVICES.split().each { svc ->
//                         if (!['locust', 'shipping-service', 'favourite-service', 'proxy-client'].contains(svc)) {
//                             bat "kubectl apply -f k8s\\${svc} -n ${K8S_NAMESPACE}"
//                             bat "kubectl set image deployment/${svc} ${svc}=${DOCKERHUB_USER}/${svc}:${IMAGE_TAG} -n ${K8S_NAMESPACE}"
//                             bat "kubectl set env deployment/${svc} SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} -n ${K8S_NAMESPACE}"
//                             bat "kubectl rollout status deployment/${svc} -n ${K8S_NAMESPACE} --timeout=300s"
//                         }
//                     }
//                 }
//             }
//         }
        
        stage('Generate and Archive Release Notes') {
            when {
                branch 'master'
            }
            steps {
                bat '''
                echo "📝 Generando Release Notes con convco..."
                convco changelog > RELEASE_NOTES.md
                '''
                archiveArtifacts artifacts: 'RELEASE_NOTES.md', fingerprint: true
            }
        }
    }    post {
        success {
            echo "✅ Pipeline OK (${env.BRANCH_NAME}) - ${SPRING_PROFILES_ACTIVE}"

            script {
                if (env.BRANCH_NAME == 'stage') {
                    publishHTML([
                        reportDir: 'locust-reports',
                        reportFiles: 'order-service-report.html, payment-service-report.html, favourite-service-report.html',
                        reportName: 'Locust Stress Test Reports',
                        keepAll: true
                    ])
                }
                
                if (env.BRANCH_NAME == 'master') {
                    echo """
                    🎉 ===== DEPLOYMENT SUCCESSFUL ===== 🎉
                    
                    📊 GRAFANA DASHBOARDS READY!
                    
                    🔗 Access Instructions:
                    1. Port-forward: kubectl port-forward svc/grafana 3000:80 -n monitoring
                    2. Open: http://localhost:3000
                    3. Login: admin / admin123
                    
                    📋 Available Dashboards:
                    ├── Kubernetes/
                    │   ├── Kubernetes Nodes (ID: 1860)
                    │   ├── Kubernetes Pods (ID: 6417)
                    │   ├── Kubernetes Cluster (ID: 7249)
                    │   ├── Kubernetes Deployments (ID: 8588)
                    │   ├── Kubernetes Persistent Volumes (ID: 13646)
                    │   ├── Kubelet Metrics (ID: 2029)
                    │   └── Etcd Metrics (ID: 3070)
                    └── Monitoring/
                        ├── Prometheus Stats (ID: 3662)
                        └── Grafana Overview (ID: 179)
                    
                    📖 Full documentation: monitoring/GRAFANA_DASHBOARDS.md
                    
                    🚀 Happy Monitoring! 🚀
                    """
                }
            }
        }
        failure {
            echo "❌ Falló pipeline en ${env.BRANCH_NAME}. Ver logs."
            emailext(
                attachLog: true,
                body: '$DEFAULT_CONTENT',
                subject: '$DEFAULT_SUBJECT',
                to: '$DEFAULT_RECIPIENTS',
            )
        }
        unstable {
            echo "⚠️ Finalizó con advertencias en ${env.BRANCH_NAME}"
        }
    }
}
