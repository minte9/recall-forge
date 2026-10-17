# Recall Forge

**Turn your Markdown notes into AI-graded spaced-repetition reviews.**

[Live app](https://recallforge.dev) · [Architecture](docs/ARCHITECTURE.md)

Write notes the way you already do, in Markdown or straight from your
Obsidian vault. Recall Forge splits them into topics, asks you a fresh
AI-generated question on each one, grades your free-text answer with
feedback, and schedules the next review based on how well you remembered.

No flashcards to write. No "show answer / I knew it" self-grading.

## How it works

1. **Import** – upload a `.md` file; every `##` / `###` heading becomes a topic.
2. **Review** – get a question for a due topic and answer in your own words.
3. **Get graded** – OpenAI scores the answer and explains what you missed.
4. **Repeat at the right time** – the score updates your memory strength
   and sets the next review date.

## Features

- Markdown & Obsidian import, with re-sync when a file changes
- Difficulty-aware questions, generated fresh each review
- AI grading with written feedback
- Spaced-repetition scheduling with mastery streaks
- Dashboard: due topics, weak and strong areas, per-category progress
- XP, levels and streaks to keep the daily habit going
- Starter packs to get going without your own notes
- Google / GitHub login, per-user daily caps and token tracking

## Tech stack

Spring Boot 4 (Java) · Spring Data JPA · PostgreSQL · Spring Security OAuth2 ·
Vue 3 (no build step) · OpenAI API · Docker Compose

## Run locally

Requires Docker and an OpenAI API key.

~~~sh
git clone https://github.com/minte9/recall-forge.git
cd recall-forge

# compose.yml attaches to an external Traefik network
docker network create traefik-net

# APP_SECURITY_ENABLED=false skips OAuth login for local use
OPENAI_API_KEY=sk-... 
APP_SECURITY_ENABLED=false 

docker compose up --build
~~~

Then open http://localhost:9090.


## Architecture

![Screenshot](docs/images/recall-forge_architecture.png)