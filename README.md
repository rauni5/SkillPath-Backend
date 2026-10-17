# SkillPath Backend

REST API for **SkillPath**, a career learning and collaboration platform. Learners pick a career goal, get a personalised learning roadmap built from a skill-dependency graph, verify skills with AI-generated quizzes, and team up on projects with other learners.

The mobile client is a Flutter app: [skillpath_app](https://github.com/rauni5/skillpath_app).

---

## Features

**Learning**
- Career roles and branches, each with a weighted list of required skills
- Roadmap generation: finds the user's missing skills (including transitive prerequisites) and orders them with a topological sort
- Gap analysis and branch recommendations against the user's current skills
- AI-generated skill checks (5 multiple-choice questions, graded server-side) that raise the user's proficiency level and complete matching roadmap steps
- Per-skill AI tutor chat and a general assistant chat with session history
- Gamification: achievements with configurable criteria and daily activity streaks

**Collaboration**
- Projects with required skills and roles, team size limits, and an `OPEN → FULL → COMPLETED / CANCELLED` lifecycle
- Join requests and owner invitations, with accept / reject flows
- Teammate recommendations ranked by a weighted match score (skills, career goal, availability)
- Per-project discussion boards: a public channel and a members-only team channel, with posts, comments and likes
- In-app notification centre plus Firebase Cloud Messaging push notifications

**Profiles and administration**
- Portfolio items, education and certifications; avatar upload to Supabase Storage
- Opt-in public profile shared through a revocable, regenerable link token
- Admin API for managing the skill catalogue, dependencies, roles, branches, achievements and users, with dashboard statistics

---

## Tech stack

| Area | Technology |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1 (Web, Security, Data JPA, Validation, Actuator) |
| Database | PostgreSQL, schema managed by Flyway migrations |
| Authentication | Firebase Authentication (ID tokens verified with the Firebase Admin SDK) |
| Push notifications | Firebase Cloud Messaging |
| AI | Google Gemini API (`generateContent` REST) |
| File storage | Supabase Storage (avatars) |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper included) |
| Containers | Multi-stage Dockerfile, Docker Compose for local PostgreSQL |

---

## Architecture

The backend follows a layered structure:

```
controller  →  service  →  repository  →  PostgreSQL
   (HTTP)     (business      (Spring Data
               logic)           JPA)
```

```
backend/src/main/java/com/skillpath/
├── algorithm/    Graph, topological sort, trie, heap, team-matching score
├── ai/           GeminiClient: the only class that talks to the AI provider
├── config/       Security, CORS, Firebase, async executor, RestTemplate
├── controller/   14 REST controllers
├── dto/          Request and response objects (kept separate from entities)
├── exception/    Custom exceptions and a global @RestControllerAdvice
├── model/        JPA entities and enums
├── repository/   Spring Data JPA repositories
├── security/     Firebase token filter and admin authorization filter
├── service/      Business logic
└── storage/      Supabase Storage client
```

### Request security flow

1. `FirebaseTokenFilter` reads the `Authorization: Bearer <Firebase ID token>` header and verifies it with the Firebase Admin SDK.
2. Tokens belonging to deactivated accounts are rejected with `403`.
3. `AdminAuthorizationFilter` guards everything under `/api/v1/admin/**` by checking the `is_admin` flag in the database.
4. Services enforce resource-level rules (for example, a user may only modify their own data, and only a project owner may manage members).

The API is stateless: there are no sessions and no passwords are stored.

### Algorithms

The `algorithm` package holds the data-structure logic behind the learning features:

| Class | Purpose |
|---|---|
| `SkillGraph` | Adjacency-list prerequisite graph; BFS collects all transitive prerequisites of a skill |
| `TopologicalSort` | Kahn's algorithm to order missing skills into a valid learning sequence; throws on a dependency cycle |
| `TeamMatching` | Match score from 0 to 100: 50% skill overlap (Jaccard similarity), 30% career-goal fit, 20% availability |
| `RecommendationHeap` | Priority queue returning the top-N scored candidates |
| `SkillTrie` | Prefix tree for skill-name autocomplete, loaded from the database at startup |

### AI integration

`GeminiClient` wraps the Gemini REST API behind plain Java types so the provider can be swapped in one place. It retries transient failures (HTTP 429 and 5xx) with exponential backoff and jitter, honours a `Retry-After` header when present, and maps failures to user-friendly `AiServiceException` messages (returned as `502`). Quizzes use a strict JSON response schema.

Skill checks draw from a shared per-skill pool of question sets, so Gemini is only called when a skill has no sets yet or a user has already seen every set in the pool.

### Database

Four Flyway migrations live in `src/main/resources/db/migration`:

| Migration | Contents |
|---|---|
| `V1__create_core_tables.sql` | Core schema: users, skills, dependencies, career roles and branches, projects, members, roadmap, discussions, achievements, AI chat, skill checks |
| `V2__seed_data.sql` | Seed data: skills, prerequisites, career roles, branches, branch requirements, achievements |
| `V3__appnotification.sql` | In-app notifications |
| `V4__public_profile.sql` | Public profile flag and token |

Hibernate runs with `ddl-auto: validate`, so the schema is owned entirely by Flyway. The schema uses composite primary keys for join tables, `CHECK` constraints for enum-like columns, `ON DELETE CASCADE` where appropriate, and indexes on common lookups (including a partial index for unread notifications).

---

## API overview

All endpoints are under `/api/v1` and require a Firebase ID token, except `/api/v1/public/**`, `/actuator/health` and the API docs.

| Area | Base path |
|---|---|
| Auth | `POST /auth/sync` (creates or fetches the user for the token's Firebase UID) |
| Users, portfolio, education, certifications, notifications, device tokens | `/users/{id}/...` |
| Skills and user skills | `/skills`, `/users/{userId}/skills` |
| Career goals, branch recommendations, gap analysis | `/career-roles`, `/users/{userId}/career-goal/...` |
| Roadmap | `/users/{userId}/roadmap` |
| Skill checks | `/users/{userId}/skills/{skillId}/skill-check/{generate,submit}` |
| AI tutor and assistant | `/users/{userId}/skills/{skillId}/chat`, `/users/{userId}/assistant-chat/...` |
| Dashboard, gamification | `/users/{userId}/dashboard`, `/users/{userId}/gamification/...` |
| Projects, membership, invites, recommendations | `/projects/...` |
| Project discussions | `/projects/{projectId}/discussion/...` |
| Public profile | `/users/{id}/public-profile/...`, `GET /public/profiles/{token}` |
| Admin | `/admin/...` |

With the app running locally, interactive documentation is at `http://localhost:8080/swagger-ui.html`.

---

## Getting started

### Prerequisites
- JDK 21
- Docker (for the local PostgreSQL container), or an existing PostgreSQL instance
- A Firebase project with a service account
- Optional: a Gemini API key and a Supabase project for AI features and avatar uploads

### 1. Start PostgreSQL

From the repository root:

```bash
export DB_PASSWORD=choose-a-password
docker compose up -d
```

This starts PostgreSQL 15 with database and user `skillpath`.

### 2. Configure environment variables

| Variable | Required | Description |
|---|---|---|
| `DB_PASSWORD` | Yes | Password for the `skillpath` database user |
| `FIREBASE_SERVICE_ACCOUNT` | Yes | Firebase service-account JSON, **base64-encoded** |
| `GEMINI_API_KEY` | For AI features | Google Gemini API key |
| `GEMINI_MODEL` | No | Defaults to `gemini-2.5-flash-lite` |
| `SUPABASE_URL` | For avatar upload | Supabase project URL |
| `SUPABASE_SERVICE_ROLE_KEY` | For avatar upload | Supabase service-role key (keep server-side only) |
| `SUPABASE_AVATAR_BUCKET` | No | Public bucket name, defaults to `avatars` |
| `PORT` | No | Defaults to `8080` |

To produce the Firebase value:

```bash
base64 -w0 serviceAccountKey.json     # Linux
base64 -i serviceAccountKey.json      # macOS
```

The database URL defaults to `jdbc:postgresql://localhost:5432/skillpath`; override it with `SPRING_DATASOURCE_URL` (and `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`) for a remote database.

### 3. Run

```bash
cd backend
./mvnw spring-boot:run
```

Flyway creates the schema and seeds the catalogue on first start.

### 4. Create the first admin

Sign in through the client once so your user exists, then call `POST /api/v1/admin/setup` with your Firebase token. This promotes the calling user to admin only if no admin exists yet. Afterwards, admins are managed through `PATCH /api/v1/admin/users/{userId}/admin`.

---

## Docker

```bash
cd backend
docker build -t skillpath-backend .
docker run -p 8080:8080 \
  -e DB_PASSWORD=... \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/skillpath \
  -e FIREBASE_SERVICE_ACCOUNT=... \
  skillpath-backend
```

The image uses a multi-stage build (JDK 21 to compile, JRE 21 Alpine to run).

## Deployment

The production instance runs as a container on **Azure Container Apps**, with **Aiven** hosting PostgreSQL and **Supabase** providing avatar storage. All secrets are supplied as environment variables, never committed.

When using a managed PostgreSQL provider such as Aiven, point `SPRING_DATASOURCE_URL` at the provider's host and port, and add `?sslmode=require` to the JDBC URL if the provider enforces TLS.

`render.yaml` at the repository root is an alternative [Render](https://render.com) blueprint (Docker runtime, health check on `/actuator/health`). The production configuration disables Swagger UI and API docs and exposes only the `health` actuator endpoint.

---

## Testing

```bash
cd backend
./mvnw test
```

The test suite currently contains a Spring context-load smoke test with the Firebase bean mocked. Unit tests for the algorithm package (graph, topological sort, matching, trie) and integration tests for the service layer are the next planned additions.
