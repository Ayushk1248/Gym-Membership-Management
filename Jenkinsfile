pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
    }

    triggers {
        pollSCM('*/2 * * * *')
    }

    environment {
        COMPOSE_PROJECT_NAME = 'gymms'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Test Image') {
            steps {
                bat 'docker build --target test --tag gymms-test:%BUILD_NUMBER% .'
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    def containerName = "gymms-test-${env.BUILD_NUMBER}"
                    bat 'if not exist target mkdir target'
                    bat 'if exist target\\ci-surefire-reports rmdir /s /q target\\ci-surefire-reports'
                    bat "docker create --name ${containerName} gymms-test:${env.BUILD_NUMBER}"
                    def testResult = bat(returnStatus: true,
                        script: "docker start --attach ${containerName}")
                    def reportResult = bat(returnStatus: true,
                        script: "docker cp ${containerName}:/workspace/target/surefire-reports target\\ci-surefire-reports")
                    bat(returnStatus: true, script: "docker rm -f ${containerName}")
                    if (reportResult != 0) {
                        error('Unable to retrieve Surefire test reports from the test container.')
                    }
                    junit testResults: 'target/ci-surefire-reports/TEST-*.xml', allowEmptyResults: false
                    if (testResult != 0) {
                        error('Unit tests failed.')
                    }
                }
            }
        }

        stage('Build Image') {
            steps {
                bat 'docker build --target production --tag gymms-app:local .'
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([file(credentialsId: 'gymms-compose-env', variable: 'COMPOSE_ENV_FILE')]) {
                    bat 'docker compose --project-name gymms --env-file "%COMPOSE_ENV_FILE%" up -d'
                }
            }
        }

        stage('Smoke Test') {
            steps {
                bat '''
                for /L %%i in (1,1,30) do (
                    curl.exe --fail --silent http://127.0.0.1:8080/health >nul && exit /b 0
                    timeout /t 2 /nobreak >nul
                )
                exit /b 1
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/ci-surefire-reports/TEST-*.xml', allowEmptyArchive: true
        }
    }
}
