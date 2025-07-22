pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                echo '📦 Preparing workspace...'
                sh 'chmod +x mvnw'  // 👈 Đảm bảo mvnw có quyền thực thi
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building the project...'
                sh './mvnw clean compile'
            }
        }

        stage('Test') {
            steps {
                echo '🧪 Running tests...'
                sh './mvnw test'
            }
        }

        stage('Package') {
            steps {
                echo '📦 Packaging the project...'
                sh './mvnw package -DskipTests'
            }
        }
    }

    post {
        always {
            echo '🧹 Deploying web'
            cleanWs()
        }

        success {
            echo '✅ Web Deployed.'
        }

        failure {
            echo '❌ Build failed.'
        }

        unstable {
            echo '⚠️ Build unstable.'
        }
    }
}
