pipeline {
    agent any
    
    environment {
        // Maven settings
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
        MAVEN_CLI_OPTS = '--batch-mode --errors --fail-at-end --show-version'
        
        // Docker settings
        DOCKER_IMAGE = 'onllib-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_REGISTRY = 'your-registry.com' // Thay đổi theo registry của bạn
        
        // Database settings for testing
        TEST_DB_URL = 'jdbc:h2:mem:testdb'
        
        // Notification settings
        SLACK_CHANNEL = '#onllib-ci'
        EMAIL_RECIPIENTS = 'dev@company.com'
    }
    
    tools {
        maven 'Maven-3.9.5' // Đảm bảo Maven đã được cấu hình trong Jenkins
        jdk 'OpenJDK-21'    // Đảm bảo Java 21 đã được cấu hình trong Jenkins
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
        
        stage('Environment Setup') {
            steps {
                echo '🛠️ Setting up build environment...'
                
                script {
                    // Create necessary directories
                    sh '''
                        mkdir -p storage/books
                        mkdir -p storage/temp
                        mkdir -p keys
                        mkdir -p logs
                        mkdir -p target/test-results
                    '''
                    
                    // Display environment info
                    sh '''
                        echo "Java Version:"
                        java -version
                        echo "Maven Version:"
                        mvn -version
                        echo "Git Commit: ${GIT_COMMIT_SHORT}"
                        echo "Branch: ${GIT_BRANCH}"
                    '''
                }
            }
        }
        
        stage('Code Quality & Security Scan') {
            parallel {
                stage('SonarQube Analysis') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                            changeRequest()
                        }
                    }
                    steps {
                        echo '🔍 Running SonarQube analysis...'
                        script {
                            try {
                                withSonarQubeEnv('SonarQube') {
                                    sh '''
                                        mvn ${MAVEN_CLI_OPTS} \
                                        sonar:sonar \
                                        -Dsonar.projectKey=online-library-encryption \
                                        -Dsonar.projectName="Online Library Encryption System" \
                                        -Dsonar.java.source=21
                                    '''
                                }
                            } catch (Exception e) {
                                echo "⚠️ SonarQube analysis failed: ${e.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
                
                stage('Dependency Check') {
                    steps {
                        echo '🔒 Checking for security vulnerabilities...'
                        script {
                            try {
                                sh '''
                                    mvn ${MAVEN_CLI_OPTS} \
                                    org.owasp:dependency-check-maven:check \
                                    -DfailBuildOnCVSS=7
                                '''
                            } catch (Exception e) {
                                echo "⚠️ Security vulnerabilities found: ${e.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'OWASP Dependency Check Report'
                            ])
                        }
                    }
                }
            }
        }
        
        stage('Build & Compile') {
            steps {
                echo '🔨 Building application...'
                sh '''
                    mvn ${MAVEN_CLI_OPTS} clean compile \
                    -DskipTests=true
                '''
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                sh '''
                    mvn ${MAVEN_CLI_OPTS} test \
                    -Dspring.profiles.active=test \
                    -Dspring.datasource.url=${TEST_DB_URL}
                '''
            }
            post {
                always {
                    // Publish test results
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    
                    // Publish coverage report
                    publishCoverage adapters: [
                        jacocoAdapter('target/site/jacoco/jacoco.xml')
                    ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                }
            }
        }
        
        stage('Integration Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo '🔧 Running integration tests...'
                script {
                    try {
                        sh '''
                            # Start test database (H2 in-memory)
                            mvn ${MAVEN_CLI_OPTS} \
                            test \
                            -Dtest=**/*IntegrationTest \
                            -Dspring.profiles.active=integration-test
                        '''
                    } catch (Exception e) {
                        echo "⚠️ Integration tests failed: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                echo '📦 Packaging application...'
                sh '''
                    mvn ${MAVEN_CLI_OPTS} package \
                    -DskipTests=true \
                    -Dspring.profiles.active=production
                '''
                
                // Archive artifacts
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    def image = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    
                    // Tag with latest if main branch
                    if (env.GIT_BRANCH == 'main') {
                        image.tag('latest')
                    }
                    
                    // Tag with branch name
                    image.tag("${env.GIT_BRANCH}-${env.GIT_COMMIT_SHORT}")
                }
            }
        }
        
        stage('Security Scanning') {
            parallel {
                stage('Docker Image Scan') {
                    steps {
                        echo '🔍 Scanning Docker image for vulnerabilities...'
                        script {
                            try {
                                sh '''
                                    # Using Trivy for container scanning
                                    trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_IMAGE}:${DOCKER_TAG}
                                '''
                            } catch (Exception e) {
                                echo "⚠️ Docker image vulnerabilities found: ${e.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
                
                stage('SAST Scan') {
                    steps {
                        echo '🔒 Running static application security testing...'
                        script {
                            try {
                                sh '''
                                    # Using SpotBugs for static analysis
                                    mvn ${MAVEN_CLI_OPTS} com.github.spotbugs:spotbugs-maven-plugin:check
                                '''
                            } catch (Exception e) {
                                echo "⚠️ Static security issues found: ${e.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            environment {
                STAGING_DB_URL = credentials('staging-db-url')
                STAGING_DB_USER = credentials('staging-db-user')
                STAGING_DB_PASS = credentials('staging-db-pass')
            }
            steps {
                echo '🚀 Deploying to staging environment...'
                script {
                    try {
                        sh '''
                            # Deploy using Docker Compose
                            docker-compose -f docker-compose.staging.yml down
                            docker-compose -f docker-compose.staging.yml up -d
                            
                            # Wait for application to start
                            sleep 30
                            
                            # Health check
                            curl -f http://staging.onllib.local:8080/api/actuator/health || exit 1
                        '''
                        
                        // Run smoke tests
                        sh '''
                            # Run basic API tests
                            mvn ${MAVEN_CLI_OPTS} test \
                            -Dtest=**/*SmokeTest \
                            -Dtest.base.url=http://staging.onllib.local:8080/api
                        '''
                        
                    } catch (Exception e) {
                        echo "❌ Staging deployment failed: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                }
            }
            environment {
                PROD_DB_URL = credentials('prod-db-url')
                PROD_DB_USER = credentials('prod-db-user')
                PROD_DB_PASS = credentials('prod-db-pass')
            }
            steps {
                echo '🎯 Deploying to production environment...'
                
                // Manual approval for production
                input message: 'Deploy to Production?', ok: 'Deploy',
                      submitterParameter: 'APPROVER'
                
                script {
                    try {
                        // Push to registry
                        docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                            def image = docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}")
                            image.push()
                            image.push('latest')
                        }
                        
                        // Blue-Green deployment
                        sh '''
                            # Deploy to production
                            kubectl set image deployment/onllib-app \
                            onllib-app=${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG} \
                            --namespace=production
                            
                            # Wait for rollout
                            kubectl rollout status deployment/onllib-app --namespace=production --timeout=300s
                            
                            # Health check
                            sleep 60
                            curl -f https://api.onllib.com/actuator/health || exit 1
                        '''
                        
                        echo "✅ Production deployment successful! Approved by: ${env.APPROVER}"
                        
                    } catch (Exception e) {
                        echo "❌ Production deployment failed: ${e.getMessage()}"
                        
                        // Rollback
                        sh '''
                            echo "🔄 Rolling back deployment..."
                            kubectl rollout undo deployment/onllib-app --namespace=production
                        '''
                        
                        currentBuild.result = 'FAILURE'
                        throw e
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
                docker system prune -f --volumes
            '''
            
            // Archive logs
            archiveArtifacts artifacts: 'logs/**/*.log', allowEmptyArchive: true
            
            // Publish build info
            script {
                def buildInfo = [
                    'Build Number': env.BUILD_NUMBER,
                    'Git Commit': env.GIT_COMMIT_SHORT,
                    'Branch': env.GIT_BRANCH,
                    'Build Result': currentBuild.result ?: 'SUCCESS',
                    'Duration': currentBuild.durationString
                ]
                
                writeJSON file: 'build-info.json', json: buildInfo
                archiveArtifacts artifacts: 'build-info.json'
            }
        }
        
        success {
            echo '✅ Pipeline completed successfully!'
            
            // Slack notification
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'good',
                message: """
                ✅ *Online Library Build Successful!*
                
                📋 *Build:* ${env.BUILD_NUMBER}
                🌿 *Branch:* ${env.GIT_BRANCH}
                📝 *Commit:* ${env.GIT_COMMIT_SHORT}
                ⏱️ *Duration:* ${currentBuild.durationString}
                
                🔗 <${env.BUILD_URL}|View Build>
                """.stripIndent()
            )
        }
        
        failure {
            echo '❌ Pipeline failed!'
            
            // Slack notification
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'danger',
                message: """
                ❌ *Online Library Build Failed!*
                
                📋 *Build:* ${env.BUILD_NUMBER}
                🌿 *Branch:* ${env.GIT_BRANCH}
                📝 *Commit:* ${env.GIT_COMMIT_SHORT}
                ⏱️ *Duration:* ${currentBuild.durationString}
                
                🔗 <${env.BUILD_URL}|View Build>
                📋 <${env.BUILD_URL}console|Console Output>
                """.stripIndent()
            )
            
            // Email notification
            emailext(
                subject: "❌ Online Library Build Failed - ${env.BUILD_NUMBER}",
                body: """
                The Online Library build has failed.
                
                Build Number: ${env.BUILD_NUMBER}
                Branch: ${env.GIT_BRANCH}
                Commit: ${env.GIT_COMMIT_SHORT}
                
                Please check the build logs: ${env.BUILD_URL}console
                """,
                to: env.EMAIL_RECIPIENTS
            )
        }
        
        unstable {
            echo '⚠️ Pipeline completed with warnings!'
            
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'warning',
                message: """
                ⚠️ *Online Library Build Unstable!*
                
                📋 *Build:* ${env.BUILD_NUMBER}
                🌿 *Branch:* ${env.GIT_BRANCH}
                📝 *Commit:* ${env.GIT_COMMIT_SHORT}
                
                Please review warnings and test failures.
                🔗 <${env.BUILD_URL}|View Build>
                """.stripIndent()
            )
        }
    }
}
