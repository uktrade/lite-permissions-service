@Library('lite-jenkins-pipeline') _
def slackChannels = [started: ['#lite-jenkins'], successful: ['#lite-jenkins'], failed: ['#lite-builds', '#lite-jenkins']]

node('jdk8') {
  currentBuild.displayName = "#${env.BUILD_NUMBER} - ${params.BUILD_VERSION}"
  slackBuildNotifier.notifyBuild("STARTED", slackChannels)
  try {
    def serviceName = 'permissions-service'
    def gitURL = "github.com/BISDigital/lite-${serviceName}"

    stage('Clean workspace'){
      deleteDir()
    }
    stage('Checkout files'){
      checkout scm
    }
    stage('Gradle publish'){
      sh 'chmod 777 gradlew'
      sh "./gradlew -PprojVersion=${params.BUILD_VERSION} :publishServicePublicationToLite-buildsRepository"
    }
    stage('Tag build'){
      withCredentials([usernamePassword(credentialsId: 'LITE-bot-github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
        sh("git -c 'user.name=Jenkins' -c 'user.email=jenkins@digital' tag  -a ${params.BUILD_VERSION} -m 'Jenkins'")
        sh("git push https://${env.GIT_USERNAME}:${env.GIT_PASSWORD}@${gitURL} --tags")
      }
    }
    stage('Docker build'){
      build job: 'new-docker-build', parameters: [[$class: 'StringParameterValue', name: 'SERVICE_NAME', value: serviceName], [$class: 'StringParameterValue', name: 'BUILD_VERSION', value: params.BUILD_VERSION], [$class: 'StringParameterValue', name: 'DOCKERFILE_PATH', value: '.']]
    }
    stage('Dev deploy'){
      build job: 'new-release-job', parameters: [
          [$class: 'StringParameterValue', name: 'IMAGE_NAME', value: 'svc/permissions-service'],
          [$class: 'StringParameterValue', name: 'BUILD_VERSION', value: params.BUILD_VERSION],
          [$class: 'StringParameterValue', name: 'TARGET_ENVIRONMENT', value: 'dev'],
          [$class: 'BooleanParameterValue', name: 'COMMENT_REFERENCED_JIRAS', value: true]
      ]
    }
  }
  catch (e) {
    currentBuild.result = "FAILED"
    throw e
  }
  finally {
    slackBuildNotifier.notifyBuild(currentBuild.result, slackChannels)
  }
}
