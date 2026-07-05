# LUC — Implementation Guide

> Lebanese University Connect: a Reddit-style Q&A platform connecting LU students with LU alumni.
> This document is the **single source of truth** for the build. Follow it top-to-bottom.

---

## 0. How to use this document

- Build in the order given in Section 12 (Build Plan). Each step has a **Definition of Done** — do not move on until the DoD is met.
- When the doc says "follow the template in Section 11", copy that template verbatim and adapt names. Do not invent new patterns.
- If something is ambiguous, **default to the simplest option** and add a `// TODO(decide)` comment. Do not add features not listed here.
- All paths in this doc are relative to the repo root unless stated.
- Java package root is `com.luc.qa`. Do not change it.

---

## 1. What we're building (scope)

An English-language, Reddit-style web platform where:

- **Anyone (unauthenticated)** can browse the global feed, communities, questions, answers, and public profiles **without signing in**. Account is required only to post, vote, reply, report, or submit alumni verification.
- **Anyone (Member)** can sign up with email + password, ask questions, post answers, vote, and report.
- **Students** are members who additionally provided their LU student ID at signup. Gets a "Student" badge.
- **Alumni** are users who submitted a verification request (LinkedIn URL + grad info), which an admin manually approved. Gets an "Alumni" badge with grad year + current position shown next to their posts.
- **Admins** review verification requests, handle reports, ban users, manage communities.

The Q&A model is **Reddit-style threaded, organized into hierarchical communities**:
- Content lives inside **communities** (analogous to subreddits) arranged as a tree by path, e.g.:
  ```
  cs                                  ← Computer Science (root)
  cs/study_abroad
  cs/study_abroad/france
  cs/study_abroad/germany
  cs/jobs
  cs/jobs/backend_skills
  cs/jobs/frontend_skills
  cs/masters/funding
  ```
- Each Question belongs to **exactly one** community.
- Browsing a community shows its own questions plus, by default, questions in all descendant communities (toggle to "exact community only" available).
- Answers can have nested replies (self-referential parent).
- Up/down votes on Questions and Answers. Score = sum.
- Sorting: Hot (recent + score), New (created_at), Top (score).

**MVP scope is Computer Science only.** All seed communities live under the `cs` root. Other majors (engineering, medicine, business, etc.) come later — schema already supports them, just seed more roots.

**Anonymous posting allowed.** Author is still recorded for moderation, but UI shows "Anonymous".

### Out of scope for MVP
Real-time updates, push notifications, search beyond `ILIKE`, file uploads, peer vouching, diploma upload path, mobile app, email digests, **Arabic / RTL / any i18n** (English-only).

---

## 2. Tech stack (locked — do not substitute)

| Layer | Choice | Version |
|---|---|---|
| Backend | Spring Boot | 4.x (latest stable) |
| Language | Java | 21 |
| Build | Gradle (Kotlin DSL) | 8.10+ |
| ORM | Spring Data JPA + Hibernate | 7.x |
| Migrations | Liquibase | 4.27+ |
| Mapping | MapStruct | 1.6+ |
| Boilerplate | Lombok | 1.18+ |
| Auth | Spring Security OAuth2 Resource Server | (matches Spring Boot 4) |
| API Docs | springdoc-openapi | 2.7+ |
| Frontend | Next.js (App Router) | 15.x |
| Frontend language | TypeScript | 5.x |
| Styling | Tailwind CSS | 4.x |
| UI components | shadcn/ui | latest |
| Auth client | next-auth (Auth.js) | v5 (beta is fine) |
| Database | PostgreSQL | 16 |
| Auth server | Keycloak | 26.x |
| Container | Docker Compose | v2 |
| Tests | JUnit 5 + Testcontainers (backend), Vitest (frontend) | latest |

**Do not** use Maven, Flyway, Webpack, plain React-with-Vite, JWT generation in the backend, or any auth solution that isn't Keycloak.

---

## 3. Repository layout

```
luc/
├── README.md
├── IMPLEMENTATION.md                ← this file
├── .env.example
├── .gitignore
├── docker-compose.yml
├── docker-compose.override.yml      ← dev volume mounts + hot reload
│
├── keycloak/
│   ├── realm-export.json            ← realm + clients + roles + IdPs
│   └── themes/                      ← custom theme later, empty for now
│
├── backend/
│   ├── Dockerfile
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle/wrapper/...
│   └── src/
│       ├── main/
│       │   ├── java/com/luc/qa/...  ← (see Section 4)
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       └── db/changelog/
│       │           ├── master.xml
│       │           └── changes/
│       │               ├── 001-initial-schema.xml
│       │               ├── 002-add-votes.xml
│       │               └── ...
│       └── test/
│           └── java/com/luc/qa/...
│
└── frontend/
    ├── Dockerfile
    ├── package.json
    ├── tsconfig.json
    ├── next.config.mjs
    ├── tailwind.config.ts
    ├── postcss.config.mjs
    └── src/
        ├── app/
        │   ├── layout.tsx                  ← root layout (no locale)
        │   ├── page.tsx                    ← global feed (public)
        │   ├── c/
        │   │   └── [...path]/
        │   │       └── page.tsx            ← community page, catch-all for /c/cs/study_abroad/france (public)
        │   ├── questions/
        │   │   ├── new/page.tsx            ← requires auth
        │   │   └── [id]/page.tsx           ← public read
        │   ├── profile/
        │   │   ├── page.tsx                ← own profile (requires auth)
        │   │   └── [id]/page.tsx           ← public profile
        │   ├── alumni/
        │   │   └── verify/page.tsx
        │   ├── admin/
        │   │   ├── verifications/page.tsx
        │   │   ├── reports/page.tsx
        │   │   ├── users/page.tsx
        │   │   ├── communities/page.tsx    ← admin manage community tree
        │   │   └── dashboard/page.tsx
        │   └── auth/
        │       ├── signin/page.tsx
        │       └── signup/page.tsx
        ├── components/
        │   ├── ui/                         ← shadcn primitives
        │   ├── question/
        │   ├── answer/
        │   ├── vote/
        │   ├── community/                  ← CommunityBreadcrumb, CommunityTree, CommunityPicker
        │   ├── user/
        │   └── layout/                     ← Header, Sidebar, Footer
        └── lib/
            ├── api.ts                      ← typed fetch wrapper
            ├── auth.ts                     ← next-auth config
            └── types.ts                    ← shared DTO types
```

---

## 4. Backend package structure

> Mirror this structure exactly. Each module is a vertical slice. Cross-module references go through services, never repositories.

```
com.luc.qa
│
├── LucApplication.java                   ← @SpringBootApplication
│
├── common/
│   ├── config/
│   │   ├── JpaConfig.java                ← @EnableJpaAuditing
│   │   ├── OpenApiConfig.java
│   │   ├── SecurityConfig.java           ← JWT resource server + CORS
│   │   ├── CorsConfig.java
│   │   └── KeycloakConfig.java           ← @ConfigurationProperties
│   │
│   ├── entity/
│   │   ├── BaseEntity.java               ← @MappedSuperclass: id + version
│   │   └── AuditableEntity.java          ← extends BaseEntity: createdAt + updatedAt
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java   ← @RestControllerAdvice
│   │   ├── ErrorResponse.java
│   │   ├── NotFoundException.java        ← abstract base
│   │   ├── BadRequestException.java
│   │   ├── ForbiddenException.java
│   │   ├── ConflictException.java
│   │   ├── UserNotFoundException.java
│   │   ├── QuestionNotFoundException.java
│   │   ├── AnswerNotFoundException.java
│   │   ├── CommunityNotFoundException.java
│   │   ├── CommunityHierarchyException.java   ← cycle / depth violation
│   │   ├── VoteNotAllowedException.java
│   │   ├── VerificationNotFoundException.java
│   │   ├── VerificationAlreadyPendingException.java
│   │   └── ReportNotFoundException.java
│   │
│   ├── keycloak/
│   │   └── KeycloakAdminClient.java      ← wraps Keycloak Admin REST
│   │
│   ├── pagination/
│   │   ├── PageRequestDTO.java
│   │   └── PageResponseDTO.java
│   │
│   ├── security/
│   │   ├── CurrentUser.java              ← @AuthenticationPrincipal helper
│   │   └── JwtRoleConverter.java         ← maps Keycloak realm roles → Spring authorities
│   │
│   └── seed/
│       └── DataSeeder.java               ← dev profile only: seed CS community tree, admin user
│
└── module/
    │
    ├── user/
    │   ├── entity/
    │   │   ├── User.java
    │   │   └── UserRole.java             ← MEMBER, STUDENT, ALUMNI, ADMIN
    │   ├── filter/
    │   │   └── UserProvisioningFilter.java
    │   ├── repository/
    │   │   └── UserRepository.java
    │   ├── service/
    │   │   ├── UserProvisioningService.java
    │   │   ├── UserService.java
    │   │   ├── UserServiceImpl.java
    │   │   ├── AdminUserService.java
    │   │   └── AdminUserServiceImpl.java
    │   ├── controller/
    │   │   ├── UserController.java       ← /api/me, /api/users/{id}
    │   │   └── AdminUserController.java  ← /api/admin/users
    │   ├── dto/
    │   │   ├── UserResponseDTO.java
    │   │   ├── PublicAuthorDTO.java      ← what shows on a post
    │   │   ├── UpdateProfileRequestDTO.java
    │   │   ├── OnboardingRequestDTO.java ← student_id
    │   │   ├── AdminUserFilterDTO.java
    │   │   └── AdminUserResponseDTO.java
    │   ├── mapper/
    │   │   └── UserMapper.java
    │   └── specification/
    │       └── UserSpecifications.java
    │
    ├── alumni/
    │   ├── entity/
    │   │   ├── AlumniProfile.java
    │   │   ├── AlumniVerification.java
    │   │   ├── VerificationStatus.java   ← PENDING, APPROVED, REJECTED
    │   │   ├── Faculty.java              ← enum of LU faculties (see Appendix A)
    │   │   └── Degree.java               ← BS, MS, PhD, DIPLOMA
    │   ├── repository/
    │   │   ├── AlumniProfileRepository.java
    │   │   └── AlumniVerificationRepository.java
    │   ├── service/
    │   │   ├── AlumniVerificationService.java (+Impl)
    │   │   ├── AlumniProfileService.java (+Impl)
    │   │   └── AdminAlumniService.java (+Impl)
    │   ├── controller/
    │   │   ├── AlumniController.java     ← submit + view own
    │   │   └── AdminAlumniController.java
    │   ├── dto/
    │   │   ├── SubmitVerificationRequestDTO.java
    │   │   ├── VerificationResponseDTO.java
    │   │   ├── ReviewVerificationRequestDTO.java
    │   │   ├── AlumniProfileResponseDTO.java
    │   │   ├── UpdateAlumniProfileRequestDTO.java
    │   │   ├── AdminVerificationFilterDTO.java
    │   │   └── AdminVerificationResponseDTO.java
    │   ├── mapper/
    │   │   ├── AlumniProfileMapper.java
    │   │   └── AlumniVerificationMapper.java
    │   └── specification/
    │       └── VerificationSpecifications.java
    │
    ├── question/
    │   ├── entity/
    │   │   ├── Question.java
    │   │   └── QuestionStatus.java       ← OPEN, CLOSED, DELETED
    │   ├── repository/
    │   │   └── QuestionRepository.java
    │   ├── service/
    │   │   ├── QuestionService.java (+Impl)
    │   │   └── AdminQuestionService.java (+Impl)
    │   ├── controller/
    │   │   ├── QuestionController.java
    │   │   └── AdminQuestionController.java
    │   ├── dto/
    │   │   ├── CreateQuestionRequestDTO.java
    │   │   ├── UpdateQuestionRequestDTO.java
    │   │   ├── QuestionResponseDTO.java
    │   │   ├── QuestionSummaryDTO.java   ← for feed
    │   │   └── QuestionFilterDTO.java
    │   ├── mapper/
    │   │   └── QuestionMapper.java
    │   └── specification/
    │       └── QuestionSpecifications.java
    │
    ├── answer/
    │   ├── entity/
    │   │   └── Answer.java               ← self-ref parentAnswerId
    │   ├── repository/
    │   │   └── AnswerRepository.java
    │   ├── service/
    │   │   ├── AnswerService.java
    │   │   └── AnswerServiceImpl.java
    │   ├── controller/
    │   │   └── AnswerController.java
    │   ├── dto/
    │   │   ├── CreateAnswerRequestDTO.java
    │   │   ├── UpdateAnswerRequestDTO.java
    │   │   ├── AnswerResponseDTO.java
    │   │   └── AnswerTreeNodeDTO.java
    │   └── mapper/
    │       └── AnswerMapper.java
    │
    ├── vote/
    │   ├── entity/
    │   │   ├── Vote.java
    │   │   └── VoteTargetType.java       ← QUESTION, ANSWER
    │   ├── repository/
    │   │   └── VoteRepository.java
    │   ├── service/
    │   │   ├── VoteService.java
    │   │   └── VoteServiceImpl.java
    │   ├── controller/
    │   │   └── VoteController.java
    │   └── dto/
    │       ├── CastVoteRequestDTO.java
    │       └── VoteResponseDTO.java
    │
    ├── community/                    ── Hierarchical communities (subreddits) ──
    │   ├── entity/
    │   │   └── Community.java            ← path + parent_id + depth
    │   ├── repository/
    │   │   └── CommunityRepository.java
    │   ├── service/
    │   │   ├── CommunityService.java (+Impl)
    │   │   ├── CommunityPathService.java (+Impl)   ← path validation / descendant queries
    │   │   └── AdminCommunityService.java (+Impl)
    │   ├── controller/
    │   │   ├── CommunityController.java
    │   │   └── AdminCommunityController.java
    │   ├── dto/
    │   │   ├── CommunityResponseDTO.java
    │   │   ├── CommunityTreeNodeDTO.java          ← nested children for tree rendering
    │   │   ├── CreateCommunityRequestDTO.java
    │   │   └── UpdateCommunityRequestDTO.java
    │   ├── mapper/
    │   │   └── CommunityMapper.java
    │   └── specification/
    │       └── CommunitySpecifications.java
    │
    ├── feed/
    │   ├── service/
    │   │   ├── FeedService.java
    │   │   └── FeedServiceImpl.java
    │   ├── controller/
    │   │   └── FeedController.java
    │   └── dto/
    │       ├── FeedFilterDTO.java
    │       └── FeedSort.java             ← HOT, NEW, TOP
    │
    ├── moderation/
    │   ├── entity/
    │   │   ├── Report.java
    │   │   ├── ReportReason.java         ← enum: SPAM, HARASSMENT, OFF_TOPIC, OTHER
    │   │   ├── ReportTargetType.java     ← QUESTION, ANSWER, USER
    │   │   └── ReportStatus.java
    │   ├── repository/
    │   │   └── ReportRepository.java
    │   ├── service/
    │   │   ├── ReportService.java (+Impl)
    │   │   └── AdminReportService.java (+Impl)
    │   ├── controller/
    │   │   ├── ReportController.java
    │   │   └── AdminReportController.java
    │   ├── dto/
    │   │   ├── CreateReportRequestDTO.java
    │   │   ├── ReportResponseDTO.java
    │   │   ├── ResolveReportRequestDTO.java
    │   │   └── AdminReportFilterDTO.java
    │   └── specification/
    │       └── ReportSpecifications.java
    │
    └── dashboard/
        ├── service/
        │   ├── AdminDashboardService.java
        │   └── AdminDashboardServiceImpl.java
        ├── controller/
        │   └── AdminDashboardController.java
        └── dto/
            └── DashboardMetricsResponseDTO.java
```

---

## 5. Database schema

> Generated via Liquibase XML changelogs in `backend/src/main/resources/db/changelog/changes/`. Always use changesets — never `ddl-auto=update`.

### users
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| keycloak_id | UUID | UNIQUE NOT NULL |
| email | VARCHAR(255) | UNIQUE NOT NULL |
| display_name | VARCHAR(100) | NOT NULL |
| avatar_url | VARCHAR(500) | |
| bio | TEXT | |
| role | VARCHAR(20) | NOT NULL DEFAULT 'MEMBER' |
| student_id | VARCHAR(20) | UNIQUE (nullable) |
| verified_student_at | TIMESTAMP | |
| verified_alumni_at | TIMESTAMP | |
| is_banned | BOOLEAN | NOT NULL DEFAULT FALSE |
| ban_reason | TEXT | |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |
| version | BIGINT | NOT NULL DEFAULT 0 |

Indexes: `keycloak_id`, `email`, `role`.

### alumni_profiles
| Column | Type | Constraints |
|---|---|---|
| user_id | BIGINT | PK FK→users(id) |
| grad_year | INT | NOT NULL |
| faculty | VARCHAR(50) | NOT NULL |
| degree | VARCHAR(20) | NOT NULL |
| major | VARCHAR(100) | NOT NULL |
| current_position | VARCHAR(200) | |
| current_company | VARCHAR(200) | |
| linkedin_url | VARCHAR(500) | |
| is_public | BOOLEAN | NOT NULL DEFAULT TRUE |
| created_at, updated_at, version | | |

### alumni_verifications
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK→users(id) NOT NULL |
| linkedin_url | VARCHAR(500) | NOT NULL |
| claimed_grad_year | INT | NOT NULL |
| claimed_faculty | VARCHAR(50) | NOT NULL |
| claimed_degree | VARCHAR(20) | NOT NULL |
| claimed_major | VARCHAR(100) | NOT NULL |
| claimed_position | VARCHAR(200) | |
| claimed_company | VARCHAR(200) | |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' |
| submitted_at | TIMESTAMP | NOT NULL |
| reviewer_id | BIGINT | FK→users(id) |
| reviewed_at | TIMESTAMP | |
| rejection_reason | TEXT | |
| created_at, updated_at, version | | |

Unique partial index: `(user_id) WHERE status = 'PENDING'` (one open request per user).

### questions
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| author_id | BIGINT | FK→users(id) NOT NULL |
| community_id | BIGINT | FK→communities(id) NOT NULL |
| title | VARCHAR(300) | NOT NULL |
| body | TEXT | NOT NULL |
| is_anonymous | BOOLEAN | NOT NULL DEFAULT FALSE |
| status | VARCHAR(20) | NOT NULL DEFAULT 'OPEN' |
| view_count | INT | NOT NULL DEFAULT 0 |
| answer_count | INT | NOT NULL DEFAULT 0 |
| score | INT | NOT NULL DEFAULT 0 |
| created_at, updated_at, version | | |

Indexes: `created_at DESC`, `score DESC`, `author_id`, `status`, `community_id`.

### answers
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| question_id | BIGINT | FK→questions(id) NOT NULL |
| parent_answer_id | BIGINT | FK→answers(id) (nullable, for nesting) |
| author_id | BIGINT | FK→users(id) NOT NULL |
| body | TEXT | NOT NULL |
| is_anonymous | BOOLEAN | NOT NULL DEFAULT FALSE |
| is_deleted | BOOLEAN | NOT NULL DEFAULT FALSE |
| score | INT | NOT NULL DEFAULT 0 |
| created_at, updated_at, version | | |

Indexes: `question_id`, `parent_answer_id`, `author_id`.

### votes
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| voter_id | BIGINT | FK→users(id) NOT NULL |
| target_type | VARCHAR(20) | NOT NULL |
| target_id | BIGINT | NOT NULL |
| value | SMALLINT | NOT NULL CHECK (value IN (-1, 1)) |
| created_at, updated_at, version | | |

Unique: `(voter_id, target_type, target_id)`. Index: `(target_type, target_id)`.

### communities
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| path | VARCHAR(200) | UNIQUE NOT NULL — full materialized path e.g. `cs/study_abroad/france` |
| slug | VARCHAR(50) | NOT NULL — last segment of the path, e.g. `france` |
| name | VARCHAR(100) | NOT NULL — display name e.g. "France" |
| description | TEXT | |
| parent_id | BIGINT | FK→communities(id) (nullable for root) |
| depth | INT | NOT NULL — 0 for root |
| question_count | INT | NOT NULL DEFAULT 0 — denormalized |
| created_at, updated_at, version | | |

Indexes: `path` (unique), `parent_id`, `depth`.

**Rules enforced in service layer (not DB):**
- `slug` matches `^[a-z0-9_]{1,50}$`.
- `path` is recomputed on insert as `parent.path + '/' + slug`, or just `slug` if root.
- `depth` is `parent.depth + 1`, or 0 if root.
- Max depth = 5 (no deeper nesting allowed).
- Cannot delete a community that has children or questions — must be empty.
- Cannot change `parent_id` after creation (no moving subtrees in MVP — keeps `path` stable).

**Descendant query pattern (Postgres):**
```sql
SELECT * FROM communities WHERE path = :path OR path LIKE :path || '/%'
```
Use the `LIKE` form (not `path LIKE :path || '%'`) so `cs/jobs` doesn't match `cs/jobs_extra`.

### reports
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| reporter_id | BIGINT | FK→users(id) NOT NULL |
| target_type | VARCHAR(20) | NOT NULL |
| target_id | BIGINT | NOT NULL |
| reason | VARCHAR(50) | NOT NULL |
| details | TEXT | |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' |
| resolver_id | BIGINT | FK→users(id) |
| resolved_at | TIMESTAMP | |
| resolution_note | TEXT | |
| created_at, updated_at, version | | |

Index: `status`, `(target_type, target_id)`.

---

## 6. Keycloak setup

### Realm
- Name: `luc`
- Display name: "Lebanese University Connect"
- Login theme: keycloak (default for MVP)
- Default locale: `en`. (No other locales enabled — English only for MVP.)

### Clients

**1. `luc-backend`** (bearer-only, for backend JWT validation)
- Access Type: `bearer-only`
- Service accounts: disabled
- Not used for login, only token validation.

**2. `luc-frontend`** (public, PKCE)
- Access Type: `public`
- Standard flow: enabled
- Direct access grants: disabled
- Valid redirect URIs: `http://localhost:3000/api/auth/callback/keycloak`
- Web origins: `http://localhost:3000`
- Authorization Code + PKCE.

**3. `luc-admin-client`** (confidential, for backend → Keycloak Admin API)
- Access Type: `confidential`
- Service Accounts: enabled
- Service account roles: `realm-management/manage-users`, `realm-management/view-users`
- Used by `KeycloakAdminClient` to assign roles after alumni approval.

### Realm roles
- `MEMBER` (default — assigned to every new user)
- `STUDENT`
- `ALUMNI`
- `ADMIN`

Configure `MEMBER` as a **default role** so every signup gets it.

### Identity Providers
- **LinkedIn** (OIDC). Used only as a login convenience, **NOT as proof of alumni status**.
  - Scope: `openid email profile`
  - Required: `clientId` + `clientSecret` from LinkedIn Developer console.

> ⚠️ LinkedIn does **not** expose education/job data via standard scopes. Verification is manual (admin reviews the LinkedIn URL the user submits separately).

### Token claims
Map realm roles into the JWT under `realm_access.roles` (default). The backend `JwtRoleConverter` extracts these into Spring authorities prefixed `ROLE_`.

### Export
Export the realm to `keycloak/realm-export.json` so it imports automatically on container start.

---

## 7. API contract

> All endpoints under `/api`. JSON only. Auth via `Authorization: Bearer <JWT>` (Keycloak access token).
> Standard error response: `{ "timestamp", "status", "error", "message", "path", "details": [...] }`.

### Public (no auth) — full browse access, Reddit-style
| Method | Path | Description |
|---|---|---|
| GET | `/api/feed` | Paginated global feed. Query: `sort=HOT\|NEW\|TOP`, `community=<path>`, `includeDescendants=true\|false` (default true), `page`, `size` |
| GET | `/api/questions/{id}` | Question detail |
| GET | `/api/questions/{id}/answers` | Threaded answers for a question |
| GET | `/api/communities` | Full community tree (nested) |
| GET | `/api/communities/{path}` | Single community by path (path is URL-encoded, slashes kept, e.g. `cs%2Fstudy_abroad%2Ffrance` or use `/api/communities/by-path?path=cs/study_abroad/france`) |
| GET | `/api/communities/{path}/children` | Direct children of a community |
| GET | `/api/users/{id}/public-profile` | Public profile (display name, badge, alumni info if public) |
| GET | `/api/health` | Spring Boot Actuator |

> All public endpoints work without an `Authorization` header. If a token *is* present, responses may include extra fields (e.g., the current user's own vote on each item) but the resource is the same.

### Authenticated (any role)
| Method | Path | Description |
|---|---|---|
| GET | `/api/me` | Current user profile (creates local user on first call via provisioning filter) |
| PUT | `/api/me` | Update own profile (display_name, bio, avatar_url) |
| POST | `/api/me/onboarding` | First-time onboarding: set student_id |
| POST | `/api/questions` | Create question. Body must include `communityPath` (e.g. `cs/jobs/backend_skills`) |
| PUT | `/api/questions/{id}` | Update own question |
| DELETE | `/api/questions/{id}` | Soft-delete own question |
| POST | `/api/questions/{id}/answers` | Create top-level answer |
| POST | `/api/answers/{id}/replies` | Reply to an answer |
| PUT | `/api/answers/{id}` | Update own answer |
| DELETE | `/api/answers/{id}` | Soft-delete own answer |
| POST | `/api/votes` | Cast vote: `{targetType, targetId, value}`. Re-posting the same vote removes it. Posting opposite vote flips. |
| POST | `/api/reports` | Report content |
| POST | `/api/alumni/verifications` | Submit verification request |
| GET | `/api/alumni/verifications/me` | Get own verification status |
| GET | `/api/alumni/profiles/me` | Own alumni profile (if approved) |
| PUT | `/api/alumni/profiles/me` | Update own alumni profile (position, company, visibility) |

### Admin (role ADMIN)
| Method | Path | Description |
|---|---|---|
| GET | `/api/admin/users` | List users with filters: `role`, `isBanned`, `search` |
| PUT | `/api/admin/users/{id}/ban` | Ban with `{reason}` |
| PUT | `/api/admin/users/{id}/unban` | |
| GET | `/api/admin/verifications` | Queue, filter by status |
| POST | `/api/admin/verifications/{id}/approve` | Approves: creates AlumniProfile, assigns ALUMNI role in Keycloak, sets `verified_alumni_at` |
| POST | `/api/admin/verifications/{id}/reject` | Body: `{rejectionReason}` |
| GET | `/api/admin/reports` | Filter by status, target_type |
| POST | `/api/admin/reports/{id}/resolve` | Body: `{resolutionNote, action: NONE\|DELETE_CONTENT\|BAN_USER}` |
| GET | `/api/admin/questions` | Admin list (includes deleted, all statuses) |
| DELETE | `/api/admin/questions/{id}` | Hard-delete |
| GET | `/api/admin/dashboard` | Counts: users, questions, answers, pending verifications, open reports |
| POST | `/api/admin/communities` | Create community. Body: `{slug, name, description, parentPath?}` (omit parentPath for a root) |
| PUT | `/api/admin/communities/{id}` | Update `name` / `description` only (path/parent are immutable in MVP) |
| DELETE | `/api/admin/communities/{id}` | Delete — fails if community has children or questions |

---

## 8. Backend implementation patterns

### 8.1 build.gradle.kts (root for backend)
Get this from Spring Initializr (https://start.spring.io) with: Gradle/Kotlin, Spring Boot 4.x, Java 21, dependencies:
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server
- Validation
- Liquibase
- PostgreSQL Driver
- Lombok
- springdoc-openapi-starter-webmvc-ui
- mapstruct + mapstruct-processor (add manually)
- spring-boot-starter-test
- testcontainers (postgres + junit-jupiter)

### 8.2 application.yml
```yaml
spring:
  application:
    name: luc-backend
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate.format_sql: true
      hibernate.jdbc.time_zone: UTC
    open-in-view: false
  liquibase:
    change-log: classpath:db/changelog/master.xml
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
          jwk-set-uri: ${KEYCLOAK_JWK_URI}

server:
  port: 8080
  servlet:
    context-path: /

keycloak:
  admin:
    server-url: ${KEYCLOAK_SERVER_URL}
    realm: luc
    client-id: luc-admin-client
    client-secret: ${KEYCLOAK_ADMIN_CLIENT_SECRET}

springdoc:
  api-docs.path: /v3/api-docs
  swagger-ui.path: /swagger-ui.html
```

### 8.3 SecurityConfig (template)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRoleConverter jwtRoleConverter;
    private final UserProvisioningFilter userProvisioningFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET,
                    "/api/feed/**",
                    "/api/questions/**",
                    "/api/communities/**",
                    "/api/users/*/public-profile",
                    "/api/health/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                jwt.jwtAuthenticationConverter(jwtRoleConverter)))
            .addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
```

### 8.4 JwtRoleConverter
```java
@Component
public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractRealmRoles(jwt).stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    @SuppressWarnings("unchecked")
    private Collection<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return List.of();
        return (Collection<String>) realmAccess.getOrDefault("roles", List.of());
    }
}
```

### 8.5 UserProvisioningFilter
Runs once per authenticated request. If the JWT's `sub` (Keycloak UUID) has no matching row in `users`, create one.

```java
@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {

    private final UserProvisioningService provisioningService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            provisioningService.provisionIfAbsent(jwtAuth.getToken());
        }
        chain.doFilter(req, res);
    }
}
```

`UserProvisioningService` reads `sub`, `email`, `preferred_username`, `given_name`, `family_name` from the JWT and `findByKeycloakId(...)`-or-creates.

### 8.6 BaseEntity / AuditableEntity
```java
@MappedSuperclass
@Getter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;
}

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
```

### 8.7 Entity template (Question)
```java
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id")
    private Community community;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "answer_count", nullable = false)
    private int answerCount;

    @Column(nullable = false)
    private int score;
}
```

### 8.7b Community entity
```java
@Entity
@Table(name = "communities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 200)
    private String path;             // 'cs/study_abroad/france'

    @Column(nullable = false, length = 50)
    private String slug;             // 'france'

    @Column(nullable = false, length = 100)
    private String name;             // 'France'

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Community parent;        // null for root

    @Column(nullable = false)
    private int depth;

    @Column(name = "question_count", nullable = false)
    private int questionCount;
}
```

### 8.8 Service interface + impl
```java
public interface QuestionService {
    Question findById(Long id);
    Page<QuestionSummaryDTO> findFeed(QuestionFilterDTO filter, Pageable pageable);
    Question create(CreateQuestionRequestDTO request, String keycloakId);
    Question update(Long id, UpdateQuestionRequestDTO request, String keycloakId);
    void softDelete(Long id, String keycloakId);
    void incrementView(Long id);
}

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional(readOnly = true)
    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new QuestionNotFoundException(id));
    }

    @Override
    public Question create(CreateQuestionRequestDTO request, String keycloakId) {
        User author = userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));

        Community community = communityRepository.findByPath(request.getCommunityPath())
            .orElseThrow(() -> new CommunityNotFoundException(request.getCommunityPath()));

        Question q = Question.builder()
            .author(author)
            .community(community)
            .title(request.getTitle().trim())
            .body(request.getBody())
            .anonymous(request.isAnonymous())
            .status(QuestionStatus.OPEN)
            .build();

        Question saved = questionRepository.save(q);
        communityRepository.incrementQuestionCount(community.getId());
        return saved;
    }
    // ...
}
```

### 8.9 Controller template
```java
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionMapper questionMapper;

    @GetMapping("/{id}")
    public QuestionResponseDTO get(@PathVariable Long id) {
        questionService.incrementView(id);
        return questionMapper.toResponse(questionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuestionResponseDTO> create(
        @Valid @RequestBody CreateQuestionRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Question created = questionService.create(request, jwt.getSubject());
        return ResponseEntity
            .created(URI.create("/api/questions/" + created.getId()))
            .body(questionMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public QuestionResponseDTO update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateQuestionRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return questionMapper.toResponse(questionService.update(id, request, jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        questionService.softDelete(id, jwt.getSubject());
    }
}
```

### 8.10 Mapper template
```java
@Mapper(componentModel = "spring", uses = {UserMapper.class, CommunityMapper.class})
public interface QuestionMapper {

    @Mapping(target = "author", source = "author", qualifiedByName = "toPublicAuthor")
    @Mapping(target = "community", source = "community")
    QuestionResponseDTO toResponse(Question question);

    QuestionSummaryDTO toSummary(Question question);

    List<QuestionResponseDTO> toResponseList(List<Question> questions);
}
```

Anonymous masking goes in `UserMapper.toPublicAuthor` — if `question.anonymous`, return a fixed `PublicAuthorDTO` with `displayName = "Anonymous"` and no id/badge.

### 8.11 Repository template
```java
public interface QuestionRepository
    extends JpaRepository<Question, Long>,
            JpaSpecificationExecutor<Question> {

    Optional<Question> findByIdAndStatusNot(Long id, QuestionStatus status);

    @Modifying
    @Query("UPDATE Question q SET q.viewCount = q.viewCount + 1 WHERE q.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Question q SET q.score = q.score + :delta WHERE q.id = :id")
    void adjustScore(@Param("id") Long id, @Param("delta") int delta);
}
```

### 8.12 Specifications template
```java
public final class QuestionSpecifications {
    private QuestionSpecifications() {}

    public static Specification<Question> hasStatus(QuestionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Question> inCommunityExact(String path) {
        return (root, query, cb) -> {
            Join<Question, Community> c = root.join("community");
            return cb.equal(c.get("path"), path);
        };
    }

    public static Specification<Question> inCommunityOrDescendants(String path) {
        return (root, query, cb) -> {
            Join<Question, Community> c = root.join("community");
            return cb.or(
                cb.equal(c.get("path"), path),
                cb.like(c.get("path"), path + "/%")
            );
        };
    }

    public static Specification<Question> titleOrBodyContains(String search) {
        return (root, query, cb) -> {
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("body")), like)
            );
        };
    }
}
```

Use in service:
```java
Specification<Question> spec = Specification.where(hasStatus(QuestionStatus.OPEN));
if (filter.getCommunityPath() != null) {
    spec = spec.and(filter.isIncludeDescendants()
        ? inCommunityOrDescendants(filter.getCommunityPath())
        : inCommunityExact(filter.getCommunityPath()));
}
if (filter.getSearch() != null) spec = spec.and(titleOrBodyContains(filter.getSearch()));
Page<Question> page = questionRepository.findAll(spec, pageable);
```

### 8.13 GlobalExceptionHandler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
        ErrorResponse body = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(400)
            .error("Validation failed")
            .message("Request body contains invalid fields")
            .path(req.getRequestURI())
            .details(details)
            .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(req.getRequestURI())
            .build());
    }
}
```

### 8.14 Alumni verification — flow detail

**Submit (user-facing):**
1. `POST /api/alumni/verifications` with `SubmitVerificationRequestDTO`.
2. Service checks: no existing PENDING for this user → else throw `VerificationAlreadyPendingException`.
3. Persist `AlumniVerification` with `status=PENDING`, `submitted_at=now`.

**Admin review:**
1. `GET /api/admin/verifications?status=PENDING&page=0&size=20` returns the queue.
2. `POST /api/admin/verifications/{id}/approve`:
   - Transactional:
     - Load verification; assert `status=PENDING`.
     - Set `status=APPROVED`, `reviewer_id`, `reviewed_at`.
     - Create or update `AlumniProfile` from the claimed fields.
     - Update `user.role = ALUMNI`, `user.verified_alumni_at = now`.
     - Call `KeycloakAdminClient.assignRealmRole(user.keycloakId, "ALUMNI")`.
3. `POST /api/admin/verifications/{id}/reject` with `{rejectionReason}`:
   - Set `status=REJECTED`, save reason. User can resubmit.

### 8.15 Vote logic
- `POST /api/votes` with `{targetType, targetId, value}`.
- Service finds existing vote on (voter, target):
  - **None** → insert. Adjust target.score by `value`.
  - **Same value** → delete (toggle off). Adjust score by `-value`.
  - **Opposite value** → update. Adjust score by `2*newValue` (e.g., -1 → +1 is +2).
- Wrap in `@Transactional` with optimistic lock retry (`@Retryable(OptimisticLockingFailureException.class)`).
- Authors **cannot vote on their own content**: throw `VoteNotAllowedException`.

### 8.16 Feed sorting
- `NEW` → `ORDER BY created_at DESC`
- `TOP` → `ORDER BY score DESC, created_at DESC`
- `HOT` → use a simple Reddit-like formula: `score / pow((hoursSinceCreated + 2), 1.5)`. Compute in SQL with `EXTRACT(EPOCH FROM (NOW() - created_at))/3600`. For MVP, OK to compute in Java after fetching top 200 by recency.

### 8.17 KeycloakAdminClient
Use the official `keycloak-admin-client` library. Initialize with service account credentials from `application.yml`. Expose:
```java
void assignRealmRole(UUID keycloakUserId, String roleName);
void removeRealmRole(UUID keycloakUserId, String roleName);
void disableUser(UUID keycloakUserId);  // for ban
void enableUser(UUID keycloakUserId);
```

---

## 9. Frontend implementation

### 9.1 next.config.mjs
```js
/** @type {import('next').NextConfig} */
export default {
  reactStrictMode: true,
  output: 'standalone',
};
```

### 9.2 src/app/layout.tsx (root layout, no locale routing)
```tsx
import { SessionProvider } from 'next-auth/react';

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <SessionProvider>{children}</SessionProvider>
      </body>
    </html>
  );
}
```

### 9.3 Community page (catch-all route)
**`src/app/c/[...path]/page.tsx`**
```tsx
import { api } from '@/lib/api';

type Props = { params: { path: string[] } };

export default async function CommunityPage({ params }: Props) {
  const path = params.path.join('/');                          // 'cs/study_abroad/france'
  const community = await api.get<CommunityResponseDTO>(`/api/communities/by-path?path=${path}`);
  const feed = await api.get<PageResponseDTO<QuestionSummaryDTO>>(
    `/api/feed?community=${encodeURIComponent(path)}&includeDescendants=true&sort=HOT`
  );
  return (
    <div>
      <CommunityBreadcrumb path={path} />
      <h1>{community.name}</h1>
      <p>{community.description}</p>
      <FeedList items={feed.content} />
    </div>
  );
}
```

Note: this is a server component, runs server-side, **no auth token required** to load.

### 9.4 next-auth config (`src/lib/auth.ts`)
```ts
import NextAuth from 'next-auth';
import Keycloak from 'next-auth/providers/keycloak';

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Keycloak({
      clientId: process.env.KEYCLOAK_CLIENT_ID!,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET ?? '', // public client, can be empty
      issuer: process.env.KEYCLOAK_ISSUER!,
    }),
  ],
  callbacks: {
    async jwt({ token, account }) {
      if (account?.access_token) {
        token.accessToken = account.access_token;
        token.idToken = account.id_token;
      }
      return token;
    },
    async session({ session, token }) {
      (session as any).accessToken = token.accessToken;
      return session;
    },
  },
});
```

### 9.5 Typed API client (`src/lib/api.ts`)
```ts
import { auth } from './auth';

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const session = await auth();
  const token = (session as any)?.accessToken;
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...init.headers,
    },
  });
  if (!res.ok) throw new Error(`API ${res.status}: ${await res.text()}`);
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  get:  <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: any) => request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put:  <T>(path: string, body: any) => request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  del:  <T>(path: string) => request<T>(path, { method: 'DELETE' }),
};
```

### 9.6 Pages to build (MVP set)
| Path | Auth required? | Description |
|---|---|---|
| `/` | No | Global feed (hot/new/top tabs) |
| `/c/[...path]` | No | Community page — feed scoped to a community + its descendants |
| `/questions/[id]` | No | Question + threaded answers (read-only without auth) |
| `/questions/new` | Yes | Ask question form — includes community picker |
| `/profile` | Yes | Own profile (edit) |
| `/profile/[id]` | No | Public profile |
| `/alumni/verify` | Yes | Submit alumni verification form |
| `/admin/verifications` | Admin | |
| `/admin/reports` | Admin | |
| `/admin/users` | Admin | |
| `/admin/communities` | Admin | Manage community tree |
| `/admin/dashboard` | Admin | Counts |
| `/auth/signin` | No | Redirect to Keycloak |

### 9.7 Public read pattern
Public pages (feed, community, question detail, public profile) must **fetch without a token**:
- Use server components that call `fetch(API_URL + path)` directly without going through the authed `api.ts` wrapper.
- Provide a parallel `apiPublic.ts` helper that omits the auth header, **or** make `api.ts` send the token only if a session exists (returning unauthenticated request otherwise).

Unauthenticated users that try to click "Ask Question", "Reply", or "Vote" → middleware/redirect to `/auth/signin?next=<current>`.

---

## 10. Docker Compose

### 10.1 docker-compose.yml (base)
```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: luc
      POSTGRES_USER: luc
      POSTGRES_PASSWORD: luc
    ports: ["5432:5432"]
    volumes: ["postgres_data:/var/lib/postgresql/data"]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U luc"]
      interval: 5s
      timeout: 5s
      retries: 10

  keycloak:
    image: quay.io/keycloak/keycloak:26.0
    command: ["start-dev", "--import-realm"]
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/luc
      KC_DB_USERNAME: luc
      KC_DB_PASSWORD: luc
      KC_HEALTH_ENABLED: "true"
    ports: ["8081:8080"]
    volumes:
      - ./keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json:ro
    depends_on:
      postgres:
        condition: service_healthy

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/luc
      SPRING_DATASOURCE_USERNAME: luc
      SPRING_DATASOURCE_PASSWORD: luc
      KEYCLOAK_ISSUER_URI: http://keycloak:8080/realms/luc
      KEYCLOAK_JWK_URI: http://keycloak:8080/realms/luc/protocol/openid-connect/certs
      KEYCLOAK_SERVER_URL: http://keycloak:8080
      KEYCLOAK_ADMIN_CLIENT_SECRET: ${KEYCLOAK_ADMIN_CLIENT_SECRET}
    ports: ["8080:8080"]
    depends_on:
      postgres:
        condition: service_healthy
      keycloak:
        condition: service_started

  frontend:
    build: ./frontend
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
      KEYCLOAK_ISSUER: http://localhost:8081/realms/luc
      KEYCLOAK_CLIENT_ID: luc-frontend
      NEXTAUTH_URL: http://localhost:3000
      NEXTAUTH_SECRET: ${NEXTAUTH_SECRET}
    ports: ["3000:3000"]
    depends_on:
      - backend

volumes:
  postgres_data:
```

> **Note on Keycloak issuer URLs:** `KEYCLOAK_ISSUER_URI` (backend) uses the in-network hostname `keycloak`. `KEYCLOAK_ISSUER` (frontend, browser-facing) uses `localhost`. The JWT issuer claim must match exactly. To solve the mismatch in dev, either (a) set `KC_HOSTNAME_URL=http://localhost:8081` on Keycloak and have the backend also reach it via `localhost:8081` from inside Docker via `host.docker.internal:8081`, or (b) add `keycloak` to `/etc/hosts` on the host machine pointing to `127.0.0.1` and use `http://keycloak:8081` everywhere. **Option (a) is preferred.**

### 10.2 backend/Dockerfile
```dockerfile
FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 10.3 frontend/Dockerfile
```dockerfile
FROM node:22-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci

FROM node:22-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
```
(Requires `output: 'standalone'` in `next.config.mjs`.)

---

## 11. Code template cheat sheet

When adding a new feature module `foo`, create files in this order:

1. `module/foo/entity/Foo.java` (extends `AuditableEntity`)
2. Liquibase changeset `db/changelog/changes/NNN-add-foo.xml`
3. `module/foo/repository/FooRepository.java`
4. `module/foo/specification/FooSpecifications.java` (if filtering needed)
5. `module/foo/dto/{Create,Update,Filter,Response}FooDTO.java`
6. `module/foo/mapper/FooMapper.java`
7. `module/foo/service/FooService.java` + `FooServiceImpl.java`
8. `module/foo/controller/FooController.java`
9. `common/exception/FooNotFoundException.java`
10. Integration test under `src/test/java/com/luc/qa/module/foo/`

**Naming rules:**
- Entities: `Foo`, `FooStatus` (enum). Singular.
- DTOs: `CreateFooRequestDTO`, `UpdateFooRequestDTO`, `FooResponseDTO`, `FooFilterDTO`.
- Services: interface `FooService` + impl `FooServiceImpl`. Admin variants: `AdminFooService`.
- Exceptions: `FooNotFoundException` extends `NotFoundException`.

---

## 12. Build plan (step-by-step)

> Build in this order. Each step has a **Definition of Done**. Do not skip.

### Step 0 — Repo bootstrap
- [ ] `git init` in repo root.
- [ ] Create directory tree from Section 3 (empty placeholders OK).
- [ ] Write `.gitignore` (Node, Java, Gradle, IDE, .env).
- [ ] Write `.env.example` listing every env var (see Appendix B).
- [ ] Write minimal `README.md` with one-liner + how to run.

**DoD:** `git status` is clean, repo structure matches Section 3.

### Step 1 — Docker compose skeleton
- [ ] `docker-compose.yml` with 4 services (`postgres`, `keycloak`, `backend`, `frontend`).
- [ ] Backend and frontend can be placeholder containers (Spring Boot init / Next.js init).
- [ ] `docker compose up` brings all 4 up healthy.

**DoD:** `curl http://localhost:8080/actuator/health` → 200, `http://localhost:8081` shows Keycloak login, `http://localhost:3000` shows Next.js default page, Postgres accepts connections.

### Step 2 — Keycloak realm config
- [ ] Use Keycloak UI to set up realm, clients, roles, LinkedIn IdP (LinkedIn IdP can be configured later).
- [ ] Export to `keycloak/realm-export.json` with `--realm luc --users realm_file`.
- [ ] Recreate Keycloak container → realm imports automatically.
- [ ] Create one test user with role `ADMIN` via the bootstrap seed (or manually in Keycloak UI).

**DoD:** Login via Keycloak Account Console (`/realms/luc/account`) works for the test user.

### Step 3 — Backend skeleton
- [ ] Generate from Spring Initializr (Section 8.1 deps).
- [ ] Add MapStruct + Lombok plugins to `build.gradle.kts` with annotation processor order.
- [ ] Write `LucApplication.java`, `application.yml`, empty `master.xml`.
- [ ] `SecurityConfig`, `JwtRoleConverter` (Section 8.3, 8.4).
- [ ] `BaseEntity`, `AuditableEntity` (Section 8.6).
- [ ] `GlobalExceptionHandler`, `ErrorResponse`, base exception classes.
- [ ] Backend container builds, starts, connects to Postgres, validates a Keycloak-issued JWT.

**DoD:** `GET /api/health` → 200 unauthenticated. `GET /api/me` → 401 unauthenticated. `GET /api/me` with valid Keycloak token → 500 (service not implemented yet) or 404 — confirms JWT is parsed.

### Step 4 — User module + provisioning
- [ ] `users` table changeset.
- [ ] `User`, `UserRole`, `UserRepository`, `UserMapper`, `UserService(+Impl)`, `UserProvisioningService`, `UserProvisioningFilter`.
- [ ] `UserController` with `GET /api/me`, `PUT /api/me`, `POST /api/me/onboarding`.
- [ ] Admin: `AdminUserController` with list + ban/unban. `KeycloakAdminClient` for disabling user.

**DoD:** Hit `/api/me` with a fresh Keycloak user's token → backend creates a row, returns the profile. Hit again → returns same row. Ban via admin endpoint → user gets 403 on next request.

### Step 5 — Frontend skeleton + auth
- [ ] `npx create-next-app@latest frontend --typescript --tailwind --app`.
- [ ] Add Auth.js v5 with Keycloak provider (Section 9.4).
- [ ] Create `/auth/signin` that triggers `signIn('keycloak')`.
- [ ] Create `/profile` that fetches `/api/me` and renders it.
- [ ] Public homepage `/` shows "Sign in" button when unauthenticated; user dropdown when authenticated.

**DoD:** Visiting `/` works **without signing in**. Click "Sign in" → Keycloak login → redirect back → `/profile` shows authenticated user's data.

### Step 6 — Community module
- [ ] `communities` table changeset.
- [ ] Full module per Section 11 cheat sheet (entity, repo, service, controller, DTOs, mapper, specifications).
- [ ] Service enforces: slug regex, path computation, depth limit, no parent change after create, no delete if children/questions exist.
- [ ] Seed initial CS communities in `DataSeeder` (dev profile):
  ```
  cs                          — Computer Science
  cs/general
  cs/study_abroad
  cs/study_abroad/france
  cs/study_abroad/germany
  cs/study_abroad/uk
  cs/study_abroad/usa
  cs/study_abroad/canada
  cs/masters
  cs/masters/france
  cs/masters/germany
  cs/masters/funding
  cs/jobs
  cs/jobs/backend_skills
  cs/jobs/frontend_skills
  cs/jobs/devops
  cs/jobs/data_science
  cs/jobs/interviews
  cs/jobs/salaries
  cs/internships
  cs/courses
  ```
- [ ] Frontend: `/c/[...path]` page renders community + descendant tree picker. Admin: `/admin/communities` lets you add/edit/delete.

**DoD:** `GET /api/communities` returns the seeded tree. `GET /api/communities/by-path?path=cs/jobs` returns one. Admin can create a new community under any existing parent. Trying to delete a community with children → 409 Conflict.

### Step 7 — Question module
- [ ] `questions` table changeset.
- [ ] Full module per Section 11 cheat sheet.
- [ ] Service rejects question creation if `communityPath` doesn't exist.
- [ ] **Read endpoints are public** (no token required). Write/edit/delete require auth.
- [ ] Frontend: `/questions/new` (auth-gated, includes community picker), `/questions/[id]` (public read), feed `/` with NEW sort only (no auth required).
- [ ] Community page `/c/[...path]` shows questions in that community + all descendants.

**DoD:** **Without signing in**, visiting `/` shows the feed and `/questions/123` shows a question. Authenticated user can post a question after picking a community.

### Step 8 — Answer module (threaded)
- [ ] `answers` table changeset.
- [ ] Service builds the tree: load flat list `findByQuestionIdOrderByCreatedAtAsc`, group by `parentAnswerId` in Java.
- [ ] Frontend: nested rendering with indent + collapse.

**DoD:** Reply chain 3 levels deep renders correctly. Author-only edit/delete works.

### Step 9 — Vote module
- [ ] `votes` table changeset.
- [ ] Toggle/flip/remove logic per Section 8.15.
- [ ] Optimistic lock retry.
- [ ] Frontend: ↑/↓ buttons on questions + answers; show score; optimistic UI.

**DoD:** Voting works, double-clicking same direction removes the vote, flipping subtracts old + adds new in one transaction. Cannot vote on own content.

### Step 10 — Feed sort + community filter
- [ ] Add HOT and TOP sorts (Section 8.16).
- [ ] Filter by `community` path + `includeDescendants` flag (Section 8.12).
- [ ] Frontend: tab switcher (Hot/New/Top) on `/` and `/c/[...path]`.

**DoD:** All three sorts produce sensible ordering on a seeded dataset. `/c/cs/jobs` shows posts from `cs/jobs/backend_skills`, `cs/jobs/frontend_skills`, etc. With `includeDescendants=false`, only posts whose community is exactly `cs/jobs` show.

### Step 11 — Alumni verification
- [ ] `alumni_profiles` + `alumni_verifications` tables.
- [ ] User flow: `/alumni/verify` submits form → status visible on profile.
- [ ] Admin flow: `/admin/verifications` queue with approve/reject.
- [ ] On approve: `KeycloakAdminClient.assignRealmRole(..., "ALUMNI")` + create `AlumniProfile` + set `verified_alumni_at`.
- [ ] Public author DTO includes badge + (if public) grad year and current position.

**DoD:** Submit → appears in admin queue → admin opens LinkedIn URL → click approve → user's next answer shows the Alumni badge with their position.

### Step 12 — Reporting / moderation
- [ ] `reports` table.
- [ ] User: "Report" button on questions/answers/users → reason picker.
- [ ] Admin: `/admin/reports` queue, resolve with action (none / delete content / ban user).

**DoD:** Report flow end-to-end. Admin action on report propagates correctly (e.g., DELETE_CONTENT soft-deletes the question).

### Step 13 — Admin dashboard
- [ ] Counts query: total users, total alumni, total questions, total answers, pending verifications, open reports.
- [ ] Simple counter cards on `/admin/dashboard`. No charts needed for MVP.

**DoD:** Numbers match `SELECT COUNT(*)` queries against the DB.

### Step 14 — Tests
- [ ] Integration test per controller using `@SpringBootTest` + Testcontainers Postgres.
- [ ] Cover happy path + 1 sad path per endpoint.
- [ ] Cover public-access cases: feed/question detail without `Authorization` header.
- [ ] Cover community hierarchy: `inCommunityOrDescendants` correctness with edge cases (similar prefix paths like `cs/jobs` vs `cs/jobs_extra`).
- [ ] Frontend: smoke test that unauthenticated browse → sign in → ask question → answer flow works (Playwright or manual checklist).

**DoD:** `./gradlew test` passes. Manual checklist passes.

---

## 13. Common gotchas

1. **Keycloak issuer URL mismatch.** The JWT `iss` claim must match what the backend expects. Set `KC_HOSTNAME_URL=http://localhost:8081` on the Keycloak container so it issues tokens with that URL, and have the backend resolve `http://localhost:8081` via `host.docker.internal` from inside Docker.
2. **CORS.** `luc-frontend` (browser) calls `luc-backend`. Configure `CorsConfigurationSource` to allow `http://localhost:3000`.
3. **MapStruct annotation processor order.** In `build.gradle.kts`, declare `annotationProcessor("org.projectlombok:lombok")` **before** `annotationProcessor("org.mapstruct:mapstruct-processor")` and add the Lombok-MapStruct binding (`lombok-mapstruct-binding`).
4. **`open-in-view: false`.** Already in `application.yml`. Means lazy fetch must be explicit — use `@EntityGraph` or fetch joins on repository methods returning entities to controllers.
5. **Liquibase + Hibernate `ddl-auto: validate`.** Never let Hibernate alter the schema. All changes go through changesets.
6. **Anonymous masking.** Do it in the **mapper**, not the service. Service returns the full entity; mapper checks `entity.anonymous` and replaces author fields. This keeps moderation queries simple.
7. **Soft delete.** Questions and answers use `status=DELETED` or `is_deleted=true`. Repository default queries filter them out via `@Where` or explicit filtering — pick one and stick with it (recommend explicit filtering for clarity).
8. **N+1 on feed.** When listing questions, use `@EntityGraph(attributePaths = {"author", "community"})` on the feed query.
9. **Vote race condition.** Always re-fetch the vote inside a transaction before updating score. Use optimistic locking on `Question`/`Answer` for score updates.
10. **Keycloak Admin Client startup.** It needs Keycloak to be reachable when the bean initializes. Use `@Lazy` injection or guard in `KeycloakAdminClient` so backend doesn't crash if Keycloak is briefly down.
11. **Community path prefix matching trap.** Use `path LIKE 'cs/jobs/%'` (with the trailing slash) — not `path LIKE 'cs/jobs%'`, which would incorrectly match `cs/jobs_extra`. Encoded in `inCommunityOrDescendants` already; don't deviate.
12. **Public endpoints with optional auth.** A public endpoint may still want to know the current user (e.g., to show their vote state). In Spring Security, `@AuthenticationPrincipal Jwt jwt` will be `null` for anonymous requests on permitted endpoints — handle the null case.

---

## Appendix A — Lebanese University faculties (Faculty enum)

```java
public enum Faculty {
    LITERATURE_AND_HUMAN_SCIENCES,
    INFORMATION_AND_DOCUMENTATION,
    LAW_POLITICAL_AND_ADMINISTRATIVE_SCIENCES,
    ECONOMIC_SCIENCES_AND_BUSINESS_ADMINISTRATION,
    SCIENCES,
    EDUCATION,
    ENGINEERING,
    AGRICULTURE_AND_VETERINARY_SCIENCES,
    MEDICAL_SCIENCES,
    DENTISTRY,
    PHARMACY,
    PUBLIC_HEALTH,
    FINE_ARTS_AND_ARCHITECTURE,
    TOURISM_AND_HOSPITALITY_MANAGEMENT,
    SOCIAL_SCIENCES,
    TECHNOLOGY,
    INSTITUTE_OF_PHYSICAL_EDUCATION
}
```

## Appendix B — `.env.example`
```dotenv
# Postgres
POSTGRES_USER=luc
POSTGRES_PASSWORD=luc
POSTGRES_DB=luc

# Keycloak
KEYCLOAK_ADMIN_CLIENT_SECRET=replace-me
KEYCLOAK_LINKEDIN_CLIENT_ID=
KEYCLOAK_LINKEDIN_CLIENT_SECRET=

# Frontend (next-auth)
NEXTAUTH_SECRET=replace-with-openssl-rand-base64-32
```

## Appendix C — Acceptance manual checklist (MVP demo)
1. **Without signing in**, visit `/` → see feed.
2. **Without signing in**, click on a question → see question detail + answers.
3. **Without signing in**, visit `/c/cs/jobs/backend_skills` → see community page + its questions.
4. **Without signing in**, click "Ask Question" → redirected to `/auth/signin`.
5. Sign up via email → land on feed.
6. Onboarding form asks for student ID → save → "Student" badge appears on profile.
7. Click "Ask Question" → community picker shows the CS tree → pick `cs/jobs/backend_skills` → submit. Anonymous off. See it on the community feed.
8. Browsing `/c/cs/jobs` shows the post (via descendant include). Toggle "exact only" → post disappears.
9. Sign in as second user. Upvote the question. Score goes 0 → 1. Double-click upvote → score back to 0.
10. Reply to the question. Reply to the reply. Tree renders 2 deep.
11. Open `/alumni/verify` → submit form with a real LinkedIn URL.
12. As admin, see it in `/admin/verifications`. Click LinkedIn link. Approve.
13. Back on first user's session → "Alumni" badge appears next to their next post, showing grad year + current position.
14. Report a question as spam. Admin resolves with DELETE_CONTENT. Question disappears from feed.
15. Admin dashboard shows correct counts.

If all 15 pass → MVP is done.
