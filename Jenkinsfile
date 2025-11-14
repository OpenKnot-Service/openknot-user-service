pipeline {
    agent any

    environment {
        DB_URL = credentials('DB_URL')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')

        IMAGE_NAME = "openknot-user-service"
        CONTAINER_NAME = "openknot-user-service"
        APP_PORT = "8082"
        SPRING_PROFILES_ACTIVE = "prod"

        MESSAGES_MAIN_YML = credentials('USER_SERVICE_MESSAGES_MAIN_YML')
        MESSAGES_TEST_YML = credentials('USER_SERVICE_MESSAGES_TEST_YML')
        APPLICATION_TEST_YML = credentials('USER_SERVICE_APPLICATION_TEST_YML')
        SCHEMA_SQL = credentials('USER_SERVICE_SCHEMA_SQL_YML')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'GitHub_Token',
                    url: 'https://github.com/OpenKnot-Service/openknot-user-service.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh "chmod +x gradlew || true"
                sh "./gradlew clean test bootJar"
            }
        }

        stage('Build Docker Image') {
                    steps {
                        sh """
                            docker build \
                              -t ${IMAGE_NAME}:${BUILD_NUMBER} \
                              -t ${IMAGE_NAME}:latest \
                              .
                        """
                    }
                }

        stage('Deploy') {
            steps {
                sh """
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    docker run -d \
                      --name ${CONTAINER_NAME} \
                      -p ${APP_PORT}:${APP_PORT} \
                      -e SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
                      -e DB_URL=${DB_URL} \
                      -e DB_USERNAME=${DB_USERNAME} \
                      -e DB_PASSWORD=${DB_PASSWORD} \
                      --restart unless-stopped \
                      ${IMAGE_NAME}:latest
                """
            }
        }
    }

    post {
        failure {
            echo "빌드/배포 실패! 로그 확인 필요"
        }
        success {
            echo "배포 완료!"
        }
    }
}