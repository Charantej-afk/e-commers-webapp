pipeline {
    agent any

    environment {
        // Docker and Nexus variables
        DOCKER_IMAGE = "charantejafk/ecommerce-app"
        DOCKER_TAG = "latest"
        NEXUS_URL = "http://nexus:8081/repository/maven-releases"
        NEXUS_GROUP = "com/ecommerce"
        NEXUS_ARTIFACT = "ecommerce-app"
        VERSION = "1.0.14"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Charantej-afk/e-commers-webapp.git', branch: 'main'
            }
        }

        stage('Build WAR') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('My SonarQube Server') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Upload WAR to Nexus') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PSW')
                ]) {
                    sh """
                        curl -v -u $NEXUS_USER:$NEXUS_PSW --upload-file target/${NEXUS_ARTIFACT}.war \
                        $NEXUS_URL/$NEXUS_GROUP/$NEXUS_ARTIFACT/$VERSION/${NEXUS_ARTIFACT}-${VERSION}.war
                    """
                }
            }
        }

        stage('Download WAR from Nexus') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'NEXUS_CRED', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PSW')
                ]) {
                    sh """
                        rm -f ${NEXUS_ARTIFACT}.war
                        curl -u $NEXUS_USER:$NEXUS_PSW -o ${NEXUS_ARTIFACT}.war \
                        $NEXUS_URL/$NEXUS_GROUP/$NEXUS_ARTIFACT/$VERSION/${NEXUS_ARTIFACT}-${VERSION}.war
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker -H tcp://dind:2375 build -t ${DOCKER_IMAGE}:${VERSION} ."
                sh "docker -H tcp://dind:2375 tag ${DOCKER_IMAGE}:${VERSION} ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([
                    string(credentialsId: 'DOCKER_HUB', variable: 'DOCKER_PWD')
                ]) {
                    sh """
                        echo $DOCKER_PWD | docker -H tcp://dind:2375 login -u charantejafk --password-stdin
                        docker -H tcp://dind:2375 push ${DOCKER_IMAGE}:${VERSION}
                        docker -H tcp://dind:2375 push ${DOCKER_IMAGE}:${DOCKER_TAG}
                    """
                }
            }
        }

        stage('Deploy Docker Container') {
            steps {
                sh """
                    docker -H tcp://dind:2375 rm -f ecommerce-app || true
                    docker -H tcp://dind:2375 run -d --name ecommerce-app -p 8080:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed!"
        }
    }
}
