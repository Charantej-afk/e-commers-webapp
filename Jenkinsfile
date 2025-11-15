pipeline {
    agent any

    environment {
        SONAR_TOKEN     = credentials('SONAR_TOKEN')
        NEXUS_CRED      = credentials('NEXUS_CRED')
        DOCKER_HUB      = credentials('DOCKER_HUB')

        IMAGE_NAME      = "charantej/ecommerce-app"
        APP_NAME        = "ecommerce-app"
        VERSION         = "1.0.${BUILD_NUMBER}"

        NEXUS_URL       = "http://nexus:8081"
        SONAR_URL       = "http://sonarqube:9000"
    }

    stages {

        /* ---------------------------------------------------------
         * 1. Checkout Code
         * --------------------------------------------------------- */
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/Charantej-afk/E-commeres-repo.git', branch: 'main'
            }
        }

        /* ---------------------------------------------------------
         * 2. Maven Build -> WAR (inside container)
         * --------------------------------------------------------- */
        stage('Build Maven WAR') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        /* ---------------------------------------------------------
         * 3. SonarQube Analysis
         * --------------------------------------------------------- */
        stage('SonarQube Analysis') {
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

        /* ---------------------------------------------------------
         * 4. Sonar Quality Gate
         * --------------------------------------------------------- */
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        /* ---------------------------------------------------------
         * 5. Upload WAR to Nexus
         * --------------------------------------------------------- */
        stage('Upload WAR to Nexus') {
            steps {
                echo "Uploading WAR to Nexus..."

                // IMPORTANT: WAR name is ALWAYS target/ecommerce-app.war
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

        /* ---------------------------------------------------------
         * 6. Download WAR From Nexus
         * --------------------------------------------------------- */
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

        /* ---------------------------------------------------------
         * 7. Build Docker Image
         * --------------------------------------------------------- */
        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${IMAGE_NAME}:${VERSION} .
                    docker tag ${IMAGE_NAME}:${VERSION} ${IMAGE_NAME}:latest
                """
            }
        }

        /* ---------------------------------------------------------
         * 8. Push Docker Image to Docker Hub
         * --------------------------------------------------------- */
        stage('Push Docker Image') {
            steps {
                sh """
                    echo ${DOCKER_HUB} | docker login -u charantej --password-stdin
                    docker push ${IMAGE_NAME}:${VERSION}
                    docker push ${IMAGE_NAME}:latest
                """
            }
        }

        /* ---------------------------------------------------------
         * 9. Deploy Container
         * --------------------------------------------------------- */
        stage('Deploy Application') {
            steps {
                sh """
                    docker rm -f ${APP_NAME} || true

                    docker run -d --name ${APP_NAME} \
                    -p 8080:8080 \
                    ${IMAGE_NAME}:latest
                """
            }
        }
    }

    post {
        success {
            echo "🎉 Pipeline Success: Build → Nexus → Docker → Deploy"
        }
        failure {
            echo "❌ Pipeline Failed"
        }
    }
}
