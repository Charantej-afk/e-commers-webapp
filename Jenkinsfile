pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        NEXUS_CRED  = credentials('NEXUS_CRED')
        DOCKER_HUB  = credentials('DOCKER_HUB')

        APP_NAME = "ecommerce-app"
        IMAGE    = "charantej/ecommerce-app"
        VERSION  = "1.0.${BUILD_NUMBER}"

        SONAR_URL = "http://sonarqube:9000"
        NEXUS_URL = "http://nexus:8081"

        MAVEN_HOME = "/usr/share/maven"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/Charantej-afk/E-commeres-repo.git', branch: 'main'
            }
        }

        stage('Build WAR using Maven') {
            steps {
                sh """
                    ${MAVEN_HOME}/bin/mvn clean package -DskipTests
                """
            }
        }

        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('My SonarQube Server') {
                    sh """
                        ${MAVEN_HOME}/bin/mvn sonar:sonar \
                          -Dsonar.projectKey=ecommerce-app \
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

        stage('Upload WAR to Nexus') {
            steps {
                sh """
                    curl -v -u ${NEXUS_CRED_USR}:${NEXUS_CRED_PSW} \
                    --upload-file target/${APP_NAME}.war \
                    ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                """
            }
        }

        stage('Download WAR from Nexus') {
            steps {
                sh "rm -f ${APP_NAME}.war || true"

                sh """
                    curl -u ${NEXUS_CRED_USR}:${NEXUS_CRED_PSW} \
                    -o ${APP_NAME}.war \
                    ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${IMAGE}:${VERSION} .
                    docker tag ${IMAGE}:${VERSION} ${IMAGE}:latest
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                sh """
                    echo ${DOCKER_HUB} | docker login -u charantej --password-stdin
                    docker push ${IMAGE}:${VERSION}
                    docker push ${IMAGE}:latest
                """
            }
        }

        stage('Deploy App to Docker') {
            steps {
                sh """
                    docker rm -f ${APP_NAME} || true

                    docker run -d --name ${APP_NAME} \
                        -p 8080:8080 \
                        ${IMAGE}:latest
                """
            }
        }
    }

    post {
        success {
            echo "🎉 Pipeline Successfully Completed!"
        }
        failure {
            echo "❌ Pipeline Failed"
        }
    }
}
