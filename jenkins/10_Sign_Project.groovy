@Library('snowdrop-lib')_

pipeline {
    agent any
    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: '365', artifactNumToKeepStr: '60'))
        disableConcurrentBuilds()
    }
    parameters {
        credentials(
                credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                defaultValue: 'GITHUB_CREDENTIALS',
                description: 'GitHub credentials to checkout the project and push it\'s tags to GitHub.',
                name: 'GITHUB_CREDENTIALS',
                required: true
        )
        string(
                defaultValue: '4BD5F787F27F97744BC09E019C1CA69653E98E56',
                description: 'GPG key to sign the release files with.',
                name: 'GPG_KEY'
        )
        credentials(
                credentialType: 'com.cloudbees.plugins.credentials.impl.FileCredentialsImpl',
                defaultValue: 'secring.gpg',
                description: 'File with the secret GPG key to sign the release files with.',
                name: 'GPG_KEY_SEC_FILE',
                required: true
        )
        credentials(
                credentialType: 'com.cloudbees.plugins.credentials.impl.FileCredentialsImpl',
                defaultValue: 'pubring.gpg',
                description: 'File with the public GPG key to sign the release files with.',
                name: 'GPG_KEY_PUB_FILE',
                required: true
        )
        credentials(
                credentialType: 'com.cloudbees.plugins.credentials.impl.StringCredentialsImpl',
                defaultValue: 'GPG_KEY_PASSPHRASE',
                description: 'Passphrase for the secret GPG key to sign the release files with.',
                name: 'GPG_KEY_PPHRASE',
                required: true
        )
    }
    stages {
        stage('SCM Checkout project') {
            steps {
                script {
                    sh 'git config --global http.sslVerify false'
                }

                script {
                    sh 'rm -Rf gpg-issue'
                }

                dir('gpg-issue') {
                    deleteDir()

                    checkout scm: [
                            $class    : 'GitSCM',
                            branches  : [[name: "main"]],
                            extensions: [
                                    [$class: 'CleanCheckout']
                                    , [$class: 'WipeWorkspace']
                                    , [$class: 'CleanBeforeCheckout']
                                    , [$class: 'LocalBranch', localBranch: "main"]
                                    , [$class: 'RelativeTargetDirectory'
                                    ]
                                    , [$class: 'CloneOption', noTags: false, reference: '', shallow: false]
                                    , [$class: 'CheckoutOption']
                            ]
                            , submoduleCfg: []
                            , userRemoteConfigs: [[credentialsId: "${GITHUB_CREDENTIALS}",url: "git@github.com:ch007m/gpg-issue.git"]]
                    ]
                }
            }
        }
        stage ('Extract information') {
            environment {
                GNUPGHOME = "${WORKSPACE}/.gnupg"
            }
            steps {
                withCredentials([file(credentialsId: '${GPG_KEY_SEC_FILE}',variable: 'GPG_KEY_SEC_FILE_CONTENTS')
                                 , file(credentialsId: '${GPG_KEY_PUB_FILE}',variable: 'GPG_KEY_PUB_FILE_CONTENTS')
                                 , string(credentialsId: '${GPG_KEY_PPHRASE}',variable: 'GPG_KEY_PPHRASE_CONTENTS')
                ]) {
                    script {
                        println("GPG_KEY_SEC_FILE: ${GPG_KEY_SEC_FILE}")
                        println("GPG_KEY: ${GPG_KEY}")
                        println("GPG_KEY_PPHRASE: ${GPG_KEY_PPHRASE}")
                        println("GPG_KEY_SEC_FILE_CONTENTS: ${GPG_KEY_SEC_FILE_CONTENTS}")

                        sh '''
                        mkdir -p ${GNUPGHOME}
                        cp -f ${GPG_KEY_SEC_FILE_CONTENTS} ${GNUPGHOME}/secring.gpg
                        cp -f ${GPG_KEY_PUB_FILE_CONTENTS} ${GNUPGHOME}/pubring.gpg
                        chmod 700 ${GNUPGHOME}
                        '''
                    }
                }
            }
        }
        stage ('Check dependency tree') {
            steps {
                echo 'mvn dependency:tree'
                dir('gpg-issue') {
                    sh "mvn dependency:tree"
                }
            }
        }
        stage ('Execute release') {
            environment {
                // We need to put the .gnupg homedir somewhere, the workspace is too long a path name
                // for the sockets, so we instead use a subdirectory of the user home (typically /home/jenkins).
                // By using the executor number as part of that name, we ensure nobody else will concurrently
                // use this directory
                GNUPGHOME = "${WORKSPACE}/.gnupg/"
            }
            steps {
                echo 'Sign project...'
                withCredentials([
                        string(credentialsId: '${GPG_KEY_PPHRASE}',variable: 'GPG_KEY_PPHRASE_CONTENTS')
                ]) {
                    dir('gpg-issue') {
                        sh '''
                        export GPG_PASSPHRASE=$GPG_KEY_PPHRASE_CONTENTS
                        mvn package gpg:sign -Dgpg.args="--pinentry-mode loopback" -Dgpg.keyname=$GPG_KEY -X
                        '''
                    }
                }
            }
        }
    }
}