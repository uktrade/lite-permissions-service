node('jdk8') {

  sh "oc login ${OC_CREDS} --insecure-skip-tls-verify=true"

  sh "oc project lite"

  stage 'Clean workspace'

  deleteDir()

  stage 'Checkout files'

  checkout scm

  stage 'Gradle build'

  sh 'chmod 777 gradlew'
  sh './gradlew build'

  step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/**/*.xml'])

  stage 'OpenShift build'

  sh "oc start-build permissions-service --from-dir=."
}
