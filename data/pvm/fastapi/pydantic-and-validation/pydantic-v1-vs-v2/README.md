## PVM / FastAPI / Pydantic & Validation / Pydantic v1 vs v2

### 1. Pydantic v2 Changes

Pydantic v2 rewrote its validation core in Rust (`pydantic-core`), giving
a large speed improvement, and renamed several methods -- `.dict()` became
`.model_dump()`, `.json()` became `.model_dump_json()`, `@validator`
became `@field_validator`. FastAPI supports both major versions, but
tutorials and code predating the rewrite often use the old v1 method
names.

~~~py
# v1
item.dict()
# v2
item.model_dump()
~~~

### 2. Settings Management

`BaseSettings` (from `pydantic-settings` in v2) reads configuration from
environment variables into a typed, validated model at startup -- the
same declarative validation `BaseModel` gives request bodies, applied to
application configuration instead.

~~~py
class Settings(BaseSettings):
    database_url: str
    debug: bool = False

settings = Settings()  # reads DATABASE_URL, DEBUG from the environment
~~~
