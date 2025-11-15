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

        // Internal Docker network endpoints
        SONAR_URL   = "http://sonarqube:9000"
        NEXUS_URL   = "http://nexus:8081"
        IMAGE       = "charantej/ecommerce-app"

        // DinD Docker host
        DOCKER_HOST = "tcp://dind:2375"
    }

    stages {

        /* ------------------------ CHECKOUT ------------------------ */
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/Charantej-afk/e-commers-webapp.git', branch: 'main'
            }
        }

        /* ------------------------ BUILD WAR ------------------------ */
        stage('Build WAR') {
            steps {
                sh """
                    mvn clean package -DskipTests
                    ls -l target
                """
            }
        }

        /* ------------------------ SONAR ------------------------ */
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

        /* ------------------------ UPLOAD TO NEXUS ------------------------ */
        stage('Upload WAR to Nexus') {
            steps {
                sh """
                    echo "Uploading WAR to Nexus..."
                    curl -v -u ${NEXUS_CRED_USR}:${NEXUS_CRED_PSW} \
                        --upload-file target/${APP_NAME}.war \
                        ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                """
            }
        }

        /* ------------------------ DOWNLOAD FROM NEXUS ------------------------ */
        stage('Download WAR from Nexus') {
            steps {
                sh "rm -f ${APP_NAME}.war || true"
                sh """
                    echo "Downloading WAR from Nexus..."
                    curl -u ${NEXUS_CRED_USR}:${NEXUS_CRED_PSW} \
                        -o ${APP_NAME}.war \
                        ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                """
            }
        }

        /* ------------------------ BUILD DOCKER IMAGE ------------------------ */
        stage('Build Docker Image') {
            steps {
                sh """
                    docker -H ${DOCKER_HOST} build -t ${IMAGE}:${VERSION} .
                    docker -H ${DOCKER_HOST} tag ${IMAGE}:${VERSION} ${IMAGE}:latest
                """
            }
        }

        /* ------------------------ PUSH DOCKER IMAGE ------------------------ */
        stage('Push Docker Image') {
            steps {
                sh """
                    echo ${DOCKER_HUB} | docker -H ${DOCKER_HOST} login -u charantej --password-stdin
                    docker -H ${DOCKER_HOST} push ${IMAGE}:${VERSION}
                    docker -H ${DOCKER_HOST} push ${IMAGE}:latest
                """
            }
        }

        /* ------------------------ DEPLOY ------------------------ */
        stage('Deploy App to Docker') {
            steps {
                sh """
                    docker -H ${DOCKER_HOST} rm -f ${APP_NAME} || true
                    docker -H ${DOCKER_HOST} run -d --name ${APP_NAME} -p 8080:8080 ${IMAGE}:latest
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
