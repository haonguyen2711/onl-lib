pipeline {
    agent any
    
    environment {
        // Maven settings for Java 21
        MAVEN_OPTS = '-Xmx1024m -Xms512m'
        MAVEN_CLI_OPTS = '--batch-mode --errors --fail-at-end --show-version'
        
        // Docker settings (cần có Docker Hub username)
        DOCKER_IMAGE = 'haonguyen2711/onllib-app'  // Thay đổi username cho phù hợp
        DOCKER_TAG = "${BUILD_NUMBER}"
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        skipStagesAfterUnstable()
        parallelsAlwaysFailFast()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Checking out source code...'
                checkout scm
                
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH = sh(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()
                }
                
                echo "📝 Building commit: ${env.GIT_COMMIT_SHORT} on branch: ${env.GIT_BRANCH}"
            }
        }
        
        stage('Build & Package') {
            steps {
                echo '🔨 Building and packaging application...'
                sh '''
                    chmod +x mvnw
                    ./mvnw ${MAVEN_CLI_OPTS} clean package \
                    -DskipTests=true
                '''
                
                // Archive artifacts
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Docker Build & Push') {
            steps {
                echo '🐳 Building and pushing Docker image...'
                script {
                    def image = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    
                    // Tag with latest if main branch
                    if (env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'master') {
                        image.tag('latest')
                    }
                    
                    // Tag with branch name
                    image.tag("${env.GIT_BRANCH}-${env.GIT_COMMIT_SHORT}")
                    
                    // Push to Docker Hub (default registry)
                    docker.withRegistry('', 'docker-hub-credentials') {
                        echo "🚀 Pushing ${DOCKER_IMAGE}:${DOCKER_TAG} to Docker Hub..."
                        image.push("${DOCKER_TAG}")
                        
                        // Push latest tag if main/master branch
                        if (env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'master') {
                            echo "🚀 Pushing ${DOCKER_IMAGE}:latest to Docker Hub..."
                            image.push('latest')
                        }
                        
                        // Push branch tag
                        echo "🚀 Pushing ${DOCKER_IMAGE}:${env.GIT_BRANCH}-${env.GIT_COMMIT_SHORT} to Docker Hub..."
                        image.push("${env.GIT_BRANCH}-${env.GIT_COMMIT_SHORT}")
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Cleaning up workspace...'
            
            // Clean up Docker images
            sh '''
                docker image prune -f
            '''
            
            // Archive logs
            archiveArtifacts artifacts: 'logs/**/*.log', allowEmptyArchive: true
            
            // Simple build info
            echo """
            📋 Build Information:
            - Build Number: ${env.BUILD_NUMBER}
            - Git Commit: ${env.GIT_COMMIT_SHORT}
            - Branch: ${env.GIT_BRANCH}
            - Build Result: ${currentBuild.result ?: 'SUCCESS'}
            - Duration: ${currentBuild.durationString}
            """
        }
        
        success {
            echo '✅ Pipeline completed successfully!'
        }
        
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
