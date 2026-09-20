# RecallForge Architecture

A map of the codebase for new contributors (and for Claude sessions that need
to find the right file fast). Keep this doc short: it describes shape and
flow, not per-method behavior — that lives in the header comment of each
class. When a class's responsibility changes, update its header comment.
Only touch this file when a whole package's role changes or a new feature
area appears.

## What it is

RecallForge is a spaced-repetition learning app. Users import topics from
markdown files, get an AI-generated question per topic, answer it, get an
AI-graded score + feedback, and the app schedules the next review date based
on that score. An XP/rewards layer gamifies progress; a dashboard summarizes
due/weak/strong areas.

## Stack

- Backend: Spring Boot (Java), REST API, Spring Data JPA, PostgreSQL, Spring
  Security OAuth2 (Google, GitHub login).
- Frontend: static HTML + Vue 3 (loaded via CDN `<script>` tag, no build
  step/SFCs) in `src/main/resources/static`.
- External service: OpenAI (question generation + answer grading).

## Request flow

```
Browser (static/js/app.js, api.js, markdown.js)
   -> REST controllers (controller/*)
   -> services (service/*)
   -> repositories (repository/*, Spring Data JPA)
   -> PostgreSQL
```

Question generation and answer grading go through `OpenAiService`, which
calls OpenAI via the `RestClient` bean configured in `config/OpenAiConfig`.

**Auth**: `config/SecurityConfig` wires OAuth2 login (Google via OIDC,
GitHub via plain OAuth2). On login, `security/DatabaseOAuth2UserService`
(GitHub) or `security/DatabaseOidcUserService` (Google) upserts a
`domain/UserAccount` row keyed by `(provider, providerUserId)`.
`security/CurrentUserService` reads the authenticated principal on every
request to resolve "who is making this call" — nearly every service depends
on it for scoping data to the current user.

## Package layout

| Package | Role |
|---|---|
| `controller` | REST endpoints. Thin — parse request, delegate to a service, map the result to a DTO/response. |
| `service` | Business logic: spaced repetition, markdown import/sync, rewards, dashboard aggregation, admin, starter packs. |
| `domain` | JPA entities (`@Entity`) plus small enums (`Role`). |
| `dto` | Request/response records exposed over the REST API. |
| `repository` | Spring Data JPA repositories + native/projection queries. |
| `security` | OAuth2 user-loading services and `CurrentUserService` (current-user resolution). |
| `config` | Spring `@Configuration` classes and `@ConfigurationProperties` records (`app.*` settings). |
| `exception` | Domain exceptions + `ApiExceptionHandler` (`@RestControllerAdvice` mapping them to HTTP status codes). |

## Feature → classes index

- **Markdown import & sync** — `MarkdownController` → `MarkdownFileService`
  (upload/update/delete, dedup by content hash, re-sync topics on re-import)
  + `MarkdownTopicImporter` (stateless `##`/`###` heading parser) →
  `domain/MarkdownFile`, `domain/Topic`.
- **Review lifecycle** — `ReviewController` → `ReviewService` (start review,
  get question via `OpenAiService`, submit answer, grade via `OpenAiService`,
  update memory score) → `RepetitionService` (spaced-repetition scoring/
  interval rules) → `domain/Review`, `domain/Topic`.
- **Topic selection/ordering** — `TopicService` (which due topic to serve
  next, subcategory/file/topic-number ordering, due-count queries).
- **AI integration** — `OpenAiService` (prompt building for question
  generation and answer grading, response parsing) + `config/OpenAiConfig`
  (RestClient bean, API key, model).
- **Rewards / XP** — `RewardService`: streaks, goal days, mastery %, and XP
  (25 × perfect code challenges, with a level derived from it) — all scoped
  to a category and computed live from review history, no stored counters.
  `domain/UserReward` only holds the streak's day bookkeeping.
- **Dashboard / analytics** — `DashboardController` → `DashboardService`
  (due counts, weak/strong knowledge areas, markdown summaries, category
  options, review-limit projections).
- **Daily limits** — `config/AppReviewLimitProperties`
  (`app.reviews.daily-limit`, `app.reviews.daily-question-limit`), enforced
  in `ReviewService`, surfaced via `DailyReviewLimitExceededException` /
  `DailyQuestionLimitExceededException` → `ApiExceptionHandler` (HTTP 429).
- **OAuth2 login** — `config/SecurityConfig`,
  `security/DatabaseOAuth2UserService`, `security/DatabaseOidcUserService`,
  `config/AppLoginProperties` (`app.login.enabled` kill switch),
  `config/AppSecurityProperties` (`app.security.enabled`, dev bypass).
- **Current user resolution** — `security/CurrentUserService`: resolves the
  authenticated `UserAccount`, admin checks (`requireAdmin`), and the
  "danger zone" resets (`resetCurrentUserLearningData`,
  `resetCurrentUserReviews`, `resetCurrentUserReviewsForMarkdownFile`).
- **Admin** — `AdminController` → `AdminService` (per-user stats, admin-only).
- **Starter packs** — `StarterPackController` → `StarterPackService`: clones
  markdown files owned by a fixed template user (id `1`) into the current
  user's account via `MarkdownFileService`.

## Frontend

Vue 3 via CDN (`Vue.createApp`), no bundler/build step:
- `static/js/api.js` — fetch wrapper for the REST API.
- `static/js/app.js` — main Vue app (dashboard, review flow, markdown list).
- `static/js/markdown.js` — markdown-specific view helpers.
- `static/js/icons.js` — inline SVG icon set.
- `static/index.html`, `login.html`, `privacy.html`, `terms.html`.

## Configuration

`src/main/resources/application.yml` holds Spring config plus custom
`app.*` / `openai.*` / `recallforge.*` keys, most overridable via env vars
(`APP_SECURITY_ENABLED`, `APP_LOGIN_ENABLED`, `APP_DAILY_REVIEW_LIMIT`,
`APP_DAILY_QUESTION_LIMIT`, `OPENAI_API_KEY`, OAuth2 client id/secret, …).
Typed access to the `app.*` keys goes through the `@ConfigurationProperties`
records in `config/` rather than `@Value` scattered through the code.

## Where to look next

- Task-specific workflow guides: `.claude/*.md` (unit testing, integration
  testing, debugging, frontend, content authoring).
- Role-based personas: `.agents/*.md` (backend-engineer, ui-engineer,
  test-engineer, content-author, qa-engineer).
- `.claude/` vs `.agents/` convention: `docs/ai/claude-code.md`.
- Build/version history and setup walkthrough: `README.md`.
