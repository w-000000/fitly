# Architecture

```text
Browser
  │ HTTP/JSON
  ▼
Vue 3 + Vite (개발: localhost:5173)
  │ /api proxy 또는 운영 시 동일 도메인
  ▼
Spring Boot REST API (개발: localhost:8080 / 운영: Render)
  ├─ NoteController → NoteRepository → H2/PostgreSQL
  └─ AiJobController → AiSummaryService → AiSummaryProvider
                                          └─ Mock (현재)
                                             OpenAI/Claude (향후)
```

운영 배포에서는 Vue 빌드 결과를 Spring Boot의 정적 리소스에 포함한 Docker 이미지 하나를 Render에서 실행한다. Render는 CI 검사가 통과한 `main` 커밋을 자동 배포하고, Spring Boot는 환경변수로 Supabase PostgreSQL에 연결한다.

## AI 확장 지점

Frontend는 AI 공급자를 알지 못하며 작업 생성과 상태 조회 API만 사용합니다. Backend의 `AiSummaryProvider` 구현체를 실제 AI API 구현으로 교체해도 응답 JSON은 유지됩니다.

```json
{
  "jobId": "UUID",
  "status": "PENDING | COMPLETED | FAILED",
  "result": {
    "summary": "요약 결과",
    "model": "모델명",
    "mock": true
  },
  "error": null
}
```

운영 환경에서는 작업 상태를 메모리 Map이 아닌 별도 DB 테이블이나 Queue/Redis에 저장합니다.
