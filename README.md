# AI-ready Idea Note

Vue 3와 Spring Boot로 만든 미니 프로젝트용 풀스택 스캐폴딩입니다. 아이디어 메모를 등록하고, 향후 실제 AI API로 교체할 수 있는 Mock 요약 API를 호출합니다.

## 구성

- Frontend: Vue 3, Vite
- Backend: Java 21, Spring Boot 3, Spring Data JPA
- Database: H2(in-memory, 개발/데모용)
- API 문서: Springdoc OpenAPI (`/swagger-ui.html`)
- CI: GitHub Actions에서 frontend build 및 backend test

## 실행

### Backend

JDK 21이 필요합니다. Maven은 wrapper가 자동으로 준비합니다.

```bash
cd backend
./mvnw spring-boot:run
```

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:minip`
  - User: `sa`
  - Password: 없음

### Frontend

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 http://localhost:5173 을 엽니다. Vite 개발 서버가 `/api` 요청을 Spring Boot로 프록시합니다.

## 핵심 API

| Method | Path | 설명 |
| --- | --- | --- |
| GET | `/api/notes` | 메모 목록 조회 |
| POST | `/api/notes` | 메모 등록 |
| POST | `/api/notes/{id}/ai-summary` | 비동기 AI 요약 작업 생성(Mock) |
| GET | `/api/ai-jobs/{jobId}` | AI 작업 상태/결과 조회 |

AI 연동부는 `AiSummaryProvider` 인터페이스 뒤에 격리했습니다. 실제 모델을 붙일 때 구현체만 교체하고 기존 JSON 응답 규격은 유지할 수 있습니다.

## 다음 단계

1. 팀 서비스 주제에 맞게 `Note` 도메인과 화면 문구 변경
2. H2를 PostgreSQL(Supabase 또는 Neon)로 교체
3. 실제 AI Provider 구현 및 API Key를 환경변수/GitHub Secret으로 주입
4. 배포 플랫폼 결정 후 CD workflow 추가
