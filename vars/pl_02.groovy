def call(body) {

        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()

        node (label: 'jenkins-pod') {
            // Clean workspace before doing anything
            deleteDir()

            try {
                stage ('Clone') {
                    container('jenkins-pod') {
                        checkout scm
                    }
                }
                stage ('Build') {
                    container('jenkins-pod') {
                        sh "echo 'building ${config.projectName} ...'"
                        sh "mvn clean compile package"
                    }
                }
                stage ('Tests') {
                    container('jenkins-pod') {
                        sh "echo 'shell scripts to run static tests...'"
                        sh "mvn test"
                        sh "mvn verify"
                    }
                }
                stage ('Deploy') {
                    container('jenkins-pod') {
                        sh "echo 'deploying to server ${config.serverDomain}...'"
                        sh "echo mvn deploy"
                    }
                }
            } catch (err) {
                currentBuild.result = 'FAILED'
                throw err
            }
        }
    }
