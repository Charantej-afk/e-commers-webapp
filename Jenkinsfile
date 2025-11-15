pipeline {
    agent { label 'docker' }

    tools {
        jdk 'JDK17'
        maven 'Maven'
    }

    environment {
        APP_NAME = "ecommerce-app"
        DOCKER_IMAGE = "ecommerce-app-image"
    }

    stages {

        stage('Checkout Code') {
            steps {
                echo "Pulling code from GitHub..."
                git branch: 'main', url: 'https://github.com/Charantej-afk/E-commerce-project-springBoot.git'
            }
        }

        stage('Build WAR') {
            steps {
                echo "Building WAR file using Maven..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image..."
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Stop Existing Container') {
            steps {
                script {
                    echo "Stopping existing container (if running)..."
                    sh """
                        CONTAINER_ID=\$(docker ps -aq --filter name=${APP_NAME})
                        if [ ! -z "\$CONTAINER_ID" ]; then
                            docker stop \$CONTAINER_ID || true
                            docker rm \$CONTAINER_ID || true
                        fi
                    """
                }
            }
        }

        stage('Run New Container') {
            steps {
                echo "Starting new container..."
                sh """
                    docker run -d --name ${APP_NAME} -p 8080:8080 ${DOCKER_IMAGE}
                """
            }
        }

    }

    post {
        success {
            echo "Deployment completed successfully!"
        }
        failure {
            echo "Pipeline failed. Check logs!"
        }
    }
}
