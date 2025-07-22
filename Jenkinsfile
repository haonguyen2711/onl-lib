pipeline {
    agent any

    environment {
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
        MAVEN_CLI_OPTS = '--batch-mode --errors --fail-at-end --show-version'
        TEST_DB_URL = 'jdbc:h2:mem:testdb'
    }

    tools {
        maven 'Maven-3.9.5'
        jdk 'OpenJDK-21'
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
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.GIT_BRANCH = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                }

                echo "📝 Building commit: ${env.GIT_COMMIT_SHORT} on branch: ${env.GIT_BRANCH}"
            }
        }

        stage('Environment Setup') {
            steps {
                echo '🛠️ Setting up build environment...'
                sh '''
                    mkdir -p storage/books
                    mkdir -p storage/temp
                    mkdir -p keys
                    mkdir -p logs
                    mkdir -p target/test-results
                '''
                sh '''
                    echo "Java Version:"
                    java -version
                    echo "Maven Version:"
                    mvn -version
                '''
            }
        }

        stage('Security Scan (Dependencies)') {
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
                        echo "⚠️ Dependency check failed: ${e.getMessage()}"
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

        stage('Build & Compile') {
            steps {
                echo '🔨 Building application...'
                sh 'mvn ${MAVEN_CLI_OPTS} clean compile -DskipTests=true'
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
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
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

        stage('Static Security Scan') {
            steps {
                echo '🔒 Running static application security testing...'
                script {
                    try {
                        sh '''
                            mvn ${MAVEN_CLI_OPTS} \
                            com.github.spotbugs:spotbugs-maven-plugin:check
                        '''
                    } catch (Exception e) {
                        echo "⚠️ SpotBugs issues found: ${e.getMessage()}"
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
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            echo '🧹 Cleaning up...'
            sh 'rm -rf storage logs target || true'

            archiveArtifacts artifacts: 'logs/**/*.log', allowEmptyArchive: true

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
            echo '✅ Build completed successfully!'
        }

        failure {
            echo '❌ Build failed!'
        }

        unstable {
            echo '⚠️ Build completed with warnings!'
        }
    }
}
