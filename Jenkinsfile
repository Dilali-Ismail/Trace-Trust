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
                bat 'mvn clean'
            }
        }

        stage('🔨 Build') {
            steps {
                echo '🔨 Compilation du projet...'
                bat 'mvn compile'
            }
        }

        stage('🧪 Tests') {
            steps {
                echo '🧪 Exécution des tests unitaires...'
                bat 'mvn test'
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
                bat 'mvn jacoco:report'
            }
        }

        stage('🔍 SonarQube Analysis') {
            steps {
                echo '🔍 Analyse SonarQube...'
                withSonarQubeEnv('SonarQube') {
                    bat """
                        mvn sonar:sonar ^
                        -Dsonar.projectKey=trace-and-trust ^
                        -Dsonar.projectName="Trace and Trust API" ^
                        -Dsonar.host.url=http://host.docker.internal:9000 ^
                        -Dsonar.token=%SONAR_TOKEN%
                    """
                }
            }
        }

        stage('✅ Quality Gate') {
            steps {
                echo '✅ Vérification du Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('📦 Package') {
            steps {
                echo '📦 Création du JAR...'
                bat 'mvn package -DskipTests'
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