pipeline {
    agent any

    environment {
        // App info
        APP_NAME    = "ecommerce-app"
        VERSION     = "1.0.${BUILD_NUMBER}"

        // Internal Docker network endpoints
        SONAR_URL   = "http://sonarqube:9000"
        NEXUS_URL   = "http://nexus:8081"
        IMAGE       = "charantejafk/ecommerce-app"
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
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('My SonarQube Server') {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.host.url=${SONAR_URL} \
                                -Dsonar.login=$SONAR_TOKEN
                        """
                    }
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
                withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PSW')]) {
                    sh """
                        echo "Uploading WAR to Nexus..."
                        curl -v -u $NEXUS_USER:$NEXUS_PSW \
                        --upload-file target/${APP_NAME}.war \
                        ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                    """
                }
            }
        }

        /* ------------------------ DOWNLOAD FROM NEXUS ------------------------ */
        stage('Download WAR from Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PSW')]) {
                    sh """
                        rm -f ${APP_NAME}.war || true
                        echo "Downloading WAR from Nexus..."
                        curl -u $NEXUS_USER:$NEXUS_PSW \
                        -o ${APP_NAME}.war \
                        ${NEXUS_URL}/repository/maven-releases/com/ecommerce/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.war
                    """
                }
            }
        }

        /* ------------------------ BUILD DOCKER IMAGE ------------------------ */
        stage('Build Docker Image') {
            steps {
                sh """
                    docker -H tcp://dind:2375 build -t ${IMAGE}:${VERSION} .
                    docker -H tcp://dind:2375 tag ${IMAGE}:${VERSION} ${IMAGE}:latest
                """
            }
        }

        /* ------------------------ PUSH DOCKER IMAGE ------------------------ */
        stage('Push Docker Image') {
            steps {
                withCredentials([string(credentialsId: 'DOCKER_HUB', variable: 'DOCKER_PWD')]) {
                    sh """
                        echo $DOCKER_PWD | docker -H tcp://dind:2375 login -u charantejafk --password-stdin
                        docker -H tcp://dind:2375 push ${IMAGE}:${VERSION}
                        docker -H tcp://dind:2375 push ${IMAGE}:latest
                    """
                }
            }
        }

        /* ------------------------ DEPLOY ------------------------ */
        stage('Deploy App to Docker') {
            steps {
                sh """
                    docker -H tcp://dind:2375 rm -f ${APP_NAME} || true
                    docker -H tcp://dind:2375 run -d --name ${APP_NAME} -p 8080:8080 ${IMAGE}:latest
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
