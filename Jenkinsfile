pipeline {
    agent any

    environment {
        JAVA_HOME = tool name: 'OpenJDK-21', type: 'jdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }

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
            echo '🧹 Cleaning up workspace...'
            cleanWs()
        }

        success {
            echo '✅ Build finished successfully.'
        }

        failure {
            echo '❌ Build failed.'
        }

        unstable {
            echo '⚠️ Build unstable.'
        }
    }
}
