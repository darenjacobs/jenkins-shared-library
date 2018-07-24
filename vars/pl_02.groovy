def call(body) {

        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()

        node (label: 'jenkins-node') {
            // Clean workspace before doing anything
            deleteDir()

            try {
                stage ('Clone') {
                    checkout scm
                }
                stage ('Build') {
                    sh "echo 'building ${config.projectName} ...'"
                    sh "mvn clean compile package"
                }
                stage ('Tests') {
                    sh "echo 'shell scripts to run static tests...'"
                    sh "mvn test"
                    sh "mvn verify"
                }
                stage ('Deploy') {
                    sh "echo 'deploying to server ${config.serverDomain}...'"
                    sh "echo mvn deploy"
                }
            } catch (err) {
                currentBuild.result = 'FAILED'
                throw err
            }
        }
    }
