# 팀 프로젝트 컨벤션 문서

## 개요
본 문서는 팀 프로젝트 협업 시 코드 스타일, 브랜치 전략, 레포지토리 구조, 이슈 관리 및 배포 정책 등 규칙을 통일하기 위해 작성되었습니다.


모든 팀원은 본 문서를 숙지하고 준수하며, 필요한 경우 PR로 문서를 업데이트합니다.

---

## 프로젝트 구조 및 관리

**Monorepo + Yarn Workspaces 전략**

- 하나의 레포지토리에서 프론트엔드, 백엔드, 공통 패키지, 인프라 코드를 함께 관리합니다.

- apps/: 실행 앱 (frontend, backend 등)

- packages/: 공통 라이브러리, 유틸, 디자인 시스템 등

- Yarn Workspaces를 사용하여 의존성을 통합 관리합니다.

**예시 폴더 구조**
```
root/
├── apps/
│   ├── frontend/
│   └── backend/
├── packages/
│   ├── utils/
│   └── …
├── infra/
│   └── terraform/
├── db/
│   └── migrations/
├── package.json
├── yarn.lock
└── README.md
```
---

## Git 브랜치 전략 : git-flow 기반 전략
- `main`: 실제 배포(운영) 브랜치
- `develop`: 다음 릴리즈 준비 브랜치
- `feature/`: 기능 개발 브랜치
- `release/`: 배포 준비 브랜치 (필요 시)
- `hotfix/`: 운영 긴급 수정 브랜치

---

## Merge

|브랜치|Merge방식|비고|
|------|------|------|
|feature → develop|Squash & Merge|기능 단위 커밋만 유지|
|develop → main|Rebase & Merge|배포 시점 히스토리 선형화, 깔끔하게 유지|
|release → main|Merge Commit|배포 단위 구분|
|hotfix → main|Merge Commit|긴급 패치 구분|
|hotfix → develop|Merge Commit|develop에 패치 내용 반영|

---

## 커밋 컨벤션

### 형식
```
type(scope)

내용(한글)
```
type(scope)는 제목, 내용(한글)은 본문

### 주요 type 키워드
- feat: 새로운 기능 추가
- fix: 버그 수정
- docs: 문서 수정
- style: 코드 스타일 변경 (포매팅, 세미콜론 등)
- refactor: 리팩토링 (기능 변경 없음)
- perf: 성능 개선
- test: 테스트 코드 추가/수정
- chore: 빌드/설정 관련 작업
- ci: CI/CD 설정 변경
- build: 빌드 시스템 변경


### 주요 scope 키워드
- frontend: React 관련 작업
- backend: Spring Boot 관련 작업
- api: 공통 API 또는 REST 인터페이스
- auth: 인증/인가 기능
- db: 데이터베이스, 마이그레이션 작업
- utils: 공통 유틸리티
- infra: AWS 및 IaC 관련 작업
- deps: 의존성 관리
- docs: 문서 관련 작업
- config : 프로젝트 설정, 세팅 관련 작업


### 예시
```
feat(frontend): 회원가입 페이지 UI 추가
fix(backend): 로그인 예외 처리 개선
refactor(api): 응답 포맷 통일
chore(db): users 테이블 index 추가
ci(infra): S3 배포 워크플로우 설정
docs(convention): 커밋 컨벤션 문서 작성
```

---

## 코드 스타일 가이드

### 공통
- 변수명: camelCase
- 클래스명: PascalCase
- 상수: UPPER_SNAKE_CASE
- 파일(문서)명: kebab-case
- 브랜치 명 : kebab-case
- 중괄호
    ```
    void func() {
    }
    ```

- 들여쓰기 : tab = 스페이스 4칸

### Frontend
- 언어: TypeScript
- 상태 관리: Redux Toolkit
- 컴포넌트명: PascalCase
- 훅 함수: use 접두어 사용 (예: useUserStore)
- Props 타입 정의: interface 사용 권장
- Linter: ESLint (Airbnb 기반 또는 팀 커스텀 규칙)
- Formatter: Prettier (pre-commit hook 적용)
- CSS 관리: CSS Module 또는 styled-components (팀 협의)

### Backend (Spring Boot)
- 언어: Java
- 패키지 구조: 도메인 중심 설계
- 네이밍: Controller, Service, Repository Layer 분리
- DTO 네이밍: ~Request, ~Response 접미사 사용
- RESTful API 설계 준수
- 예외 처리: GlobalExceptionHandler 사용
- 공통 응답 포맷 예시:
    ```
    json
    {
    "status": "success",
    "data": {},
    "message": "요청이 성공했습니다."
    }
    ```

### DB (MariaDB)
- 테이블명: 복수형 소문자 (예: users)
- 컬럼명: snake_case
- 인덱스 네이밍: idx_테이블명_컬럼명
- ERD 설계 문서 공유 및 리뷰 필수
- 마이그레이션 파일은 db/migrations 폴더에 관리

### Infra (AWS)
- IaC: Terraform 사용 권장
- 리소스 네이밍: 소문자 + 하이픈(-)
- 환경 변수 및 시크릿 관리: AWS Systems Manager Parameter Store 또는 Secrets Manager 사용
- 배포: GitLab CI/CD 기반, main 브랜치 기준 자동 배포

---

## 이슈 및 작업 관리
### 이슈 (GitLab Issues)
- 제목: 간결하고 명확하게 작성 (예: [FE] 로그인 페이지 UI 버그)
- 본문:
    - 개요: 어떤 문제가 발생했는지, 또는 어떤 기능을 추가할 것인지
    - Task list: 체크 박스 `- [ ]` 활용하여 진행(예정) 사항 목록 명시
- 라벨: bug, feature, enhancement, question, documentation 등
- Assignee: 담당자 지정
- 예시 이슈 제목: `[BE] 회원가입 시 중복 이메일 예외 처리 필요`

### 마일스톤 (Milestone)
- 목표 단위로 관리 (예: MVP 출시, v1.0, v1.1 버전 릴리즈)
- 마감 기한 설정
- 포함 이슈: 해당 릴리즈/목표에 포함되는 기능과 버그 이슈를 연결


### Merge Request(MR)
- 제목: `type(scope): 내용`
```
feat(frontend): 회원가입 페이지 UI 추가
```
- 본문:
    - 작업 개요
    - 관련 이슈 번호: `Closes #이슈번호` 형태로 작성하여 이슈 자동 종료 활용
    - 작업 상세 내용 및 구현 방식
- MR merge 조건: 
    - 최소 1명 이상의 리뷰 승인 필수
    - 커밋 컨벤션, 코딩 스타일 준수 여부 확인
    - CI/CD 도입 시 빌드 및 테스트 통과 여부 확인
    - PR 승인 후 MR 브랜치에 해당되는 방식으로 Merge
---

## Jira 양식 작성
### Jira 이슈(Story/Bug/Task) 기본 구조
공통 필드
|항목|내용|
|---|---|
|Summary|간결하고 명확한 제목|
|Description|상세 내용, 배경, 목표|
|Type|Story / Bug / Task 등|
|Priority|High, Medium, Low|
|Assignee|담당자 지정|
|Reporter|작성자|
|Epic Link|상위 Epic 연결 (존재할 시)|
|Sprint|포함될 Sprint 선택|
|Labels|frontend, backend 등 태그|


### Description 상세 구조 (권장)
```
## 개요(목적과 배경을 명사형으로 작성)
---
MarkDown 활용하여 작업(예정) 내용 작성

참고 자료 필요한 경우 파일 혹은 url 등록
```

### Jira Sub-task
- 상위 이슈(Story) 하위에 세부 작업 단위로 작성
- Sub-task 제목은 `[모듈명] 작업 내용` 형태 권장 (예: `[FE] 로그인 form UI`, `[BE] 회원가입 validation`)


### Epic
- 큰 목표 단위 (예: "회원가입/로그인", "MVP 출시 준비")
- Story들을 Epic에 연결

### Story
- 제목: 
```
[FE] 회원가입 페이지 UI 및 유효성 검사 추가
```
---

## 배포 정책
- CI/CD 구축 시 추가
---

## 문서 관리
- 본 문서는 레포 루트에 CONVENTION.md로 관리
- 시각 자료 및 상세 가이드는 GitLab Wiki에 작성
- 변경 시 PR 필요, 전 팀원 검토 후 merge
---

## 참고 및 동의
모든 팀원은 본 문서를 숙지하고, 작업 시 준수할 것을 동의합니다. 필요 시 팀 논의를 거쳐 업데이트할 수 있습니다.

---

## 문서 버전 관리
- v1.0 (2025-07-14): 최초 작성
---
