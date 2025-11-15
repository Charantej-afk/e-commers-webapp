pipeline {
    agent any

    environment {
        // Jenkins credentials
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        NEXUS_CRED  = credentials('NEXUS_CRED')
        DOCKER_HUB  = credentials('DOCKER_HUB')

        // App info
        APP_NAME    = "ecommerce-app"
        VERSION     = "1.0.${BUILD_NUMBER}"

        // Internal service URLs inside Docker network
        SONAR_URL   = "http://sonarqube:9000"
        NEXUS_URL   = "http://nexus:8081"

        // Docker DinD daemon
        DOCKER_HOST = "tcp://dind:2375"

        IMAGE       = "charantej/ecommerce-app"
    }

    stages {

        /* ------------------------ CHECKOUT ------------------------ */
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/Charantej-afk/E-commeres-repo.git', branch: 'main'
            }
        }

        /* ------------------------ BUILD WAR USING MAVEN CONTAINER ------------------------ */
        stage('Maven Build') {
            agent {
                docker {
                    image 'maven:3.8.6-openjdk-17'
                    args '-v $WORKSPACE:/workspace -w /workspace'
                }
            }
            steps {
                sh "mvn clean package -DskipTests"
            }
        }

        /* ------------------------ SONAR SCAN ------------------------ */
        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('My SonarQube Server') {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.login=${SONAR_TOKEN}
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        /* ------------------------ UPLOAD WAR TO NEXUS ------------------------ */
        stage('Upload WAR to Nexus') {
            steps {
                sh """
                    curl -v -u ${NEXUS_CRED_USR}:${NEXUS_CRED_PSW} \
                    --upload-file target/${APP_NAME}-${VERSION}.war \
                    ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                """
            }
        }

        /* ------------------------ DOWNLOAD WAR FROM NEXUS ------------------------ */
        stage('Download WAR from Nexus') {
            steps {
                sh """
                    curl -u ${NEXUS_CRED_USR}:${NEXUS_CRED_PSW} \
                    -o ${APP_NAME}.war \
                    ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                """
            }
        }

        /* ------------------------ BUILD DOCKER IMAGE USING DinD ------------------------ */
        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${IMAGE}:${VERSION} .
                    docker tag ${IMAGE}:${VERSION} ${IMAGE}:latest
                """
            }
        }

        /* ------------------------ PUSH TO DOCKER HUB ------------------------ */
        stage('Push Docker Image') {
            steps {
                sh """
                    echo ${DOCKER_HUB} | docker login -u charantej --password-stdin
                    docker push ${IMAGE}:${VERSION}
                    docker push ${IMAGE}:latest
                """
            }
        }

        /* ------------------------ DEPLOY CONTAINER ------------------------ */
        stage('Deploy to Docker') {
            steps {
                sh """
                    docker rm -f ${APP_NAME} || true
                    docker run -d --name ${APP_NAME} -p 8080:8080 ${IMAGE}:latest
                """
            }
        }
    }

    post {
        success {
            echo "🎉 Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed!"
        }
    }
}
