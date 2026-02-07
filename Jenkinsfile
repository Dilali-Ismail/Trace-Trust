pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        SONAR_TOKEN = credentials('sonarqube-token')
    }

    stages {
        stage('🔍 Checkout') {
            steps {
                echo '📥 Récupération du code source...'
                checkout scm
            }
        }

        stage('🧹 Clean') {
            steps {
                echo '🧹 Nettoyage du projet...'
                sh 'mvn clean'
            }
        }

        stage('🔨 Build') {
            steps {
                echo '🔨 Compilation du projet...'
                sh 'mvn compile'
            }
        }

        stage('🧪 Tests') {
            steps {
                echo '🧪 Exécution des tests unitaires...'
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('📊 JaCoCo Report') {
            steps {
                echo '📊 Génération du rapport de couverture...'
                sh 'mvn jacoco:report'
            }
        }

        stage('🔍 SonarQube Analysis') {
            steps {
                echo '🔍 Analyse SonarQube...'
                withSonarQubeEnv('sonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=trace-and-trust \
                        -Dsonar.projectName="Trace and Trust API" \
                        -Dsonar.host.url=http://host.docker.internal:9000 \
                        -Dsonar.token=${SONAR_TOKEN}
                    '''
                }
            }
        }

        stage('✅ Quality Gate') {
            steps {
                echo '✅ Vérification du Quality Gate...'
                waitForQualityGate abortPipeline: true
            }
        }

        stage('📦 Package') {
            steps {
                echo '📦 Création du JAR...'
                sh 'mvn package -DskipTests'
            }
        }

        stage('🎉 Success') {
            steps {
                echo '🎉 Build réussi !'
                echo '✅ Tous les tests sont passés'
                echo '✅ Quality Gate OK'
                echo '✅ Artefact créé'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline exécuté avec succès !'
        }
        failure {
            echo '❌ Pipeline échoué !'
        }
        always {
            echo '🧹 Nettoyage...'
        }
    }
}