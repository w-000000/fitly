# Architecture

```text
Browser
  │ HTTP/JSON
  ▼
Vue 3 + Vite (localhost:5173)
  │ /api proxy
  ▼
Spring Boot REST API (localhost:8080)
  ├─ NoteController → NoteRepository → H2/PostgreSQL
  └─ AiJobController → AiSummaryService → AiSummaryProvider
                                          └─ Mock (현재)
                                             OpenAI/Claude (향후)
```

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
