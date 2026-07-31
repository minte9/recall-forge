## PVM / FastAPI / Security & Auth / OAuth2 & JWT

### 1. OAuth2PasswordBearer

`OAuth2PasswordBearer` is a built-in dependency that extracts a bearer
token from the `Authorization` header -- used as `Depends()` on any
protected route, and it also tells FastAPI's auto-generated docs to show
an "Authorize" button, so the interactive OpenAPI UI can send
authenticated requests too.

~~~py
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

@app.get("/me")
def read_me(token: str = Depends(oauth2_scheme)):
    return decode_token(token)
~~~

### 2. JWT Validation

Decoding and verifying a JWT (checking the signature and expiry) happens
inside a dependency, typically raising `HTTPException(401)` on failure --
so every protected route just declares `Depends(get_current_user)` and
gets consistent auth enforcement without repeating the verification logic.

~~~py
def get_current_user(token: str = Depends(oauth2_scheme)):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")
    return payload
~~~
