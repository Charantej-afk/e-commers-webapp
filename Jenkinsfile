pipeline {

    agent {
        docker {
            image 'maven:3.8.6-openjdk-17'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        NEXUS_CRED  = credentials('NEXUS_CRED')
        DOCKER_HUB  = credentials('DOCKER_HUB')

        APP_NAME    = "ecommerce-app"
        IMAGE_NAME  = "charantej/ecommerce-app"
        VERSION     = "1.0.${BUILD_NUMBER}"

        SONAR_URL   = "http://sonarqube:9000"
        NEXUS_URL   = "http://nexus:8081"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/Charantej-afk/E-commeres-repo.git', branch: 'main'
            }
        }

        stage('Build Maven WAR') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('My SonarQube Server') {
                    sh """
                    mvn sonar:sonar \
                        -Dsonar.projectKey=${APP_NAME} \
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
                sh "ls -l target/"

                nexusPublisher nexusInstanceId: 'Nexus',
                nexusRepositoryId: 'maven-releases',
                items: [[
                    $class: 'MavenDeploymentItem',
                    artifactId: "${APP_NAME}",
                    classifier: '',
                    file: "target/${APP_NAME}.war",
                    groupId: "com.ecommerce",
                    version: "${VERSION}"
                ]]
            }
        }

        stage('Download WAR from Nexus') {
            steps {
                sh "rm -f ecommerce-app.war || true"

                nexusArtifactDownloader(
                    artifacts: [[
                        artifactId: "${APP_NAME}",
                        classifier: '',
                        extension: 'war',
                        groupId: 'com.ecommerce',
                        version: "${VERSION}"
                    ]],
                    credentialsId: 'NEXUS_CRED',
                    nexusUrl: "${NEXUS_URL}",
                    repository: 'maven-releases',
                    targetDirectory: '.'
                )

                sh "mv *.war ecommerce-app.war"
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -t ${IMAGE_NAME}:${VERSION} .
                docker tag ${IMAGE_NAME}:${VERSION} ${IMAGE_NAME}:latest
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                sh """
                echo ${DOCKER_HUB} | docker login -u charantej --password-stdin
                docker push ${IMAGE_NAME}:${VERSION}
                docker push ${IMAGE_NAME}:latest
                """
            }
        }

        stage('Deploy Application') {
            steps {
                sh """
                docker rm -f ${APP_NAME} || true
                docker run -d --name ${APP_NAME} -p 8080:8080 ${IMAGE_NAME}:latest
                """
            }
        }
    }

    post {
        success { echo "🎉 Pipeline Successful!" }
        failure { echo "❌ Pipeline Failed" }
    }
}
