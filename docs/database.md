# FITLY 데이터베이스 운영 가이드

## 현재 구조

| 스키마 | 용도 | 상태 |
| --- | --- | --- |
| `public` | ERD3 기준 신규 물리 모델 24개 테이블 | 구축 완료, 모든 테이블 RLS 활성화 |
| `legacy_pre_erd3` | ERD3 이전 Spring JPA 프로토타입 10개 테이블 | 읽기 전용 보존(현재 API 미사용) |
| `private` | `updated_at` 트리거 함수 등 내부 객체 | Data API 비공개 |

ERD의 논리 엔티티 `USER`는 PostgreSQL 예약어와의 충돌을 피하기 위해
물리 테이블 `app_user`로 구현했습니다.

Spring 엔티티와 Repository는 ERD3의 `public` 스키마에 맞춰져 있습니다. 로컬과
Supabase 프로필 모두 Hibernate가 스키마를 변경하지 않고 `validate`만 수행합니다.
`legacy_pre_erd3`는 이전 데이터 확인이 필요한 경우에만 직접 조회합니다.

## 로컬 PostgreSQL

Docker Desktop이 실행 중이어야 합니다. 저장소 루트에서 다음 명령을 사용합니다.

```bash
npx --yes supabase@2.116.0 start
npx --yes supabase@2.116.0 db reset --local
```

로컬 PostgreSQL 접속 주소는 다음과 같습니다.

```text
postgresql://postgres:postgres@127.0.0.1:54322/postgres
```

스키마와 제약조건을 검증합니다.

```bash
npx --yes supabase@2.116.0 db lint --local --schema public --level warning --fail-on error
psql postgresql://postgres:postgres@127.0.0.1:54322/postgres \
  -f supabase/tests/erd3_schema_test.sql
```

테스트 SQL은 임시 fixture를 트랜잭션 안에서 생성하고 마지막에 모두 롤백합니다.

로컬 서비스를 종료할 때는 다음 명령을 사용합니다.

```bash
npx --yes supabase@2.116.0 stop
```

## 마이그레이션

마이그레이션 적용 순서는 다음과 같습니다.

1. `20260903080000_archive_legacy_prototype.sql`: 기존 프로토타입 테이블이 있을 때만
   `legacy_pre_erd3`로 이동합니다. 새 DB에서는 아무 작업도 하지 않습니다.
2. `20260903080119_create_fitly_erd3_schema.sql`: ERD3 테이블, FK, 제약조건, 인덱스,
   트리거, RLS 및 기준 데이터를 생성합니다.

새 DB 변경은 Dashboard에서 직접 수정하지 말고 새 마이그레이션 파일로 추가합니다.
Hibernate 설정은 항상 `validate`로 유지하고 `update` 또는 `create`를 사용하지 않습니다.

## Spring Boot 연결

`.env.example`을 `.env`로 복사한 뒤 Session pooler의 JDBC 접속 정보를 입력합니다.
비밀번호를 JDBC URL에 넣지 않고 별도 환경변수로 관리합니다.

```bash
set -a
source .env
set +a
cd backend
./mvnw spring-boot:run
```

기본 `local` 프로필은 로컬 Supabase PostgreSQL을 사용합니다. 위처럼 `.env`를 불러오면
`SPRING_PROFILES_ACTIVE=supabase`가 적용되어 원격 Supabase PostgreSQL을 사용합니다.

Session pooler는 장시간 실행되는 Spring Boot 서버 연결에 사용하고, JDBC URL에는
`sslmode=require`를 유지합니다. 소규모 환경의 Hikari 풀 기본값은 최대 5개입니다.

## 보안 원칙

- `.env`와 `.local-backups/`는 Git에 포함하지 않습니다.
- Publishable key만 브라우저에서 사용할 수 있습니다.
- Secret key, service role key, DB 비밀번호는 프론트엔드 코드나 Git에 넣지 않습니다.
- 현재 `public` 테이블은 RLS를 활성화하고 `anon`, `authenticated`, `service_role`의
  테이블 권한을 회수한 deny-by-default 상태입니다.
- Supabase Auth를 도입할 때 사용자별 RLS 정책을 별도 마이그레이션으로 추가합니다.

## ERD3 도메인 묶음

- 사용자: `app_user`, `role`, `user_role`, `user_profile`, `style`, `user_preferred_style`
- 제휴사·상품: `business`, `business_member`, `business_contract`, `product`,
  `product_description_generation`, `product_variant`
- 옷장·추천: `wardrobe_item`, `recommendation_request`,
  `recommendation_request_wardrobe`, `recommendation`, `outfit_feedback`,
  `recommendation_item`
- 대여·반납: `rental_order`, `rental_item`, `group_rental_detail`,
  `group_rental_request_item`, `return_process`
- 정산: `business_settlement`
