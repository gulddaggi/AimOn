pipeline {
  agent any

  environment {
    DEPLOY_PATH = "${WORKSPACE}"
  }

  stages {
    stage('Clean Workspace') {
      steps {
        deleteDir()
      }
    }

    stage('SCM Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Git Info') {
      steps {
        echo "현재 브랜치: ${env.GIT_BRANCH}"
        sh 'git branch -a'
      }
    }

    stage('Inject Secret .env.dev') {
      steps {
        withCredentials([file(credentialsId: 'env-dev', variable: 'ENV_FILE')]) {
          sh 'cp $ENV_FILE .env.dev'
        }
      }
    }

    stage('Start MySQL (compose up & wait)') {
      steps {
        script {
          sh '''
            set -e
            cd ${WORKSPACE}
            docker-compose --env-file .env.dev -f docker-compose.prod.yml up -d mysql

            # 컨테이너 이름 확인 (정규식 대신 grep으로 정확히 매칭)
            MYSQL_CONTAINER=$(docker ps --format '{{.Names}}' | grep '^mysql$' || true)
            if [ -z "$MYSQL_CONTAINER" ]; then
              echo "ERROR: mysql container not found after compose up"
              docker ps
              exit 1
            fi

            echo "MySQL 컨테이너 확인됨: $MYSQL_CONTAINER"
          '''
        }
      }
    }

    stage('Apply DB Schema') {
      steps {
        withCredentials([
          string(credentialsId: 'mysql-user', variable: 'MYSQL_USER'),
          string(credentialsId: 'mysql-password', variable: 'MYSQL_PASSWORD'),
          string(credentialsId: 'mysql-database', variable: 'MYSQL_DATABASE')
        ]) {
          sh '''
            echo "DB Schema 적용 중..."
            docker exec -i mysql mysql -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE < ./db/init/V1__schema.sql
            echo "DB Schema 적용 완료"
          '''
        }
      }
    }

    stage('Build Spring Boot') {
      steps {
        dir('apps/backend') {
          sh 'chmod +x ./gradlew'
          sh './gradlew build -x test'
        }
      }
    }

    stage('Docker Compose Build & Deploy') {
      steps {
        script {
          def BUILD_JAR = 'apps/backend/build/libs/backend-0.0.1-SNAPSHOT.jar'
          def DEST_JAR = "${DEPLOY_PATH}/apps/backend/build/libs/app.jar"

          def DOCKER_COMPOSE_FILE = ''
          def ENV_FILE = ''
          def USE_ENV_FILE = false

          if (env.GIT_BRANCH == 'origin/main') {
            DOCKER_COMPOSE_FILE = 'docker-compose.dev.yml'
            ENV_FILE = '.env.dev'
            USE_ENV_FILE = true
            echo '[main] 브랜치 배포 실행'
          } else if (env.GIT_BRANCH == 'origin/develop') {
            DOCKER_COMPOSE_FILE = 'docker-compose.prod.yml'
            ENV_FILE = '.env.dev'
            USE_ENV_FILE = true
            echo '[develop] 브랜치 배포 실행'
          } else if (env.GIT_BRANCH.startsWith('origin/merge-requests/')) {
            DOCKER_COMPOSE_FILE = 'docker-compose.prod.yml'
            ENV_FILE = '.env.dev'
            USE_ENV_FILE = true
            echo "[MR] Merge Request 브랜치 배포 실행"
          } else {
            error "해당 브랜치는 배포 대상이 아닙니다: ${env.GIT_BRANCH}"
          }

          // JAR 복사
          sh """
            mkdir -p ${DEPLOY_PATH}/apps/backend/build/libs/
            cp ${BUILD_JAR} ${DEST_JAR}
          """

          if (USE_ENV_FILE) {
            sh """
              cd ${DEPLOY_PATH}
              docker-compose --env-file ${ENV_FILE} -f ${DOCKER_COMPOSE_FILE} down --remove-orphans
              docker-compose --env-file ${ENV_FILE} -f ${DOCKER_COMPOSE_FILE} up -d --build
            """
          } else {
            sh """
              cd ${DEPLOY_PATH}
              docker-compose -f ${DOCKER_COMPOSE_FILE} down --remove-orphans
              docker-compose -f ${DOCKER_COMPOSE_FILE} up -d --build
            """
          }
        }
      }
    }

    stage('AI Post-Deploy (seed/index/reload)') {
      steps {
        sh '''
          set -e

          echo "[AI] mysql healthy 대기"
          until [ "$(docker inspect -f '{{json .State.Health.Status}}' mysql | tr -d '"')" = "healthy" ]; do
            echo "  waiting mysql..."
            sleep 3
          done

          echo "[AI] aimon-ai 컨테이너 실행 확인"
          while ! docker ps --format '{{.Names}}' | grep -q '^aimon-ai$'; do
            sleep 2
          done

          sleep 5

          echo "[AI] Django migrate (idempotent)"
          docker exec -e SKIP_RAG_INIT=1 aimon-ai sh -lc "cd /app/server && python manage.py migrate --noinput"

          echo "[AI] JSON → DB 시드 (FULL_REFRESH=1)"
          docker exec -e FULL_REFRESH=1 aimon-ai sh -lc "cd /app/server && python json_to_db.py"

          echo "[AI] 인덱스 권한 보정"
          docker exec -u 0 aimon-ai sh -lc "mkdir -p /app/server/normalized_faiss_index && chown -R appuser:appuser /app/server/normalized_faiss_index"

          echo "[AI] FAISS 인덱스 생성"
          docker exec aimon-ai sh -lc "cd /app/server && python normalize_embedding.py"
        '''
      }
    }
  }

  post {
    always {
      echo '배포 완료 또는 실패. GitLab에서 상태를 확인하세요.'
    }
  }
}
