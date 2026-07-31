## PVM / FastAPI / Security & Auth / API Keys & Scopes

### 1. API Key Auth

An API key check (via `APIKeyHeader` or `APIKeyQuery`) is a simpler
alternative to full OAuth2 -- a single static key checked against a
stored value, no token issuance/refresh flow needed. Suited to
service-to-service calls where a login flow doesn't make sense, not for
representing an individual end user.

~~~py
api_key_header = APIKeyHeader(name="X-API-Key")

def verify_api_key(key: str = Depends(api_key_header)):
    if key != settings.api_key:
        raise HTTPException(status_code=403, detail="Invalid API key")
~~~

### 2. OAuth2 Scopes

Scopes let a single OAuth2 setup express fine-grained permissions (
`items:read`, `items:write`) instead of one all-or-nothing authenticated
state -- a route declares the scopes it requires, and the security
dependency checks the token's granted scopes against them.

~~~py
@app.get("/items/")
def read_items(user=Security(get_current_user, scopes=["items:read"])):
    return items
~~~
