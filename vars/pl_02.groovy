def call(body) {

        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()

        node (label: 'swarm') {
            // Clean workspace before doing anything
            deleteDir()

            try {
                stage ('Clone') {
                    container('jenkins-node') {
                        checkout scm
                    }
                }
                stage ('Build') {
                    container('jenkins-node') {
                        sh "echo 'building ${config.projectName} ...'"
                        sh "mvn clean compile package"
                    }
                }
                stage ('Tests') {
                    container('jenkins-node') {
                        sh "echo 'shell scripts to run static tests...'"
                        sh "mvn test"
                        sh "mvn verify"
                    }
                }
                stage ('Deploy') {
                    container('jenkins-node') {
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
