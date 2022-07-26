def call(Map pipelineParams){
    pipeline {
    agent {
        docker {image 'maven:3.8.1-adoptopenjdk-11'}
        }
    options {
        copyArtifactPermission('devops-exercise-downstream-job');
    }
    stages {
        stage('Checkout'){
            steps{
                checkout(
                    [
                        $class: 'GitSCM',
                        branches: [[name: pipelineParams.branch]], 
                        extensions: [], 
                        userRemoteConfigs: 
                        [
                            [
                                url: pipelineParams.url
                            ]
                        ]
                    ]
                )
            }
        }
        stage('Build') {
            steps{
                sh 'mvn clean compile'
                }
        }
        
        stage('Test'){
            steps{
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh 'mvn test'
                }
            }
        }
        
        
        stage('Check Vulnerabilities'){
            steps{
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh 'mvn dependency-check:check'
                    dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
                }
            }
        }
        
        stage('Package'){
            steps{
                sh 'mvn -Dmaven.test.failure.ignore=true clean package'
            }
        }
        
        
    }
    post {
        always {
            junit '**/target/surefire-reports/TEST-*.xml'
            archiveArtifacts artifacts: '**/*.jar, **/target/surefire-reports/TEST-*.xml', fingerprint: true
            build job: 'devops-exercise-downstream-job',
             parameters: [
                 [$class: 'StringParameterValue', name: 'build_number', value: "${BUILD_NUMBER}"],
                 [$class: 'StringParameterValue', name: 'job_name', value: "${JOB_NAME}"]]
        }
    }
}
}