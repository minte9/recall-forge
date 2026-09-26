# API / REST / Production REST APIs / API Evolution

## 1. Changing Contracts Safely

Evolve by adding, never by changing meaning. New optional fields, new
endpoints and new optional parameters are safe if clients ignore unknown
fields. For a rename, add the new field, keep the old one populated, and
remove it only in a new version.

```json
{"name": "Dune",      // old, kept for existing clients
 "title": "Dune"}     // new, preferred
```

## 2. Deprecation

Announce removal before it happens: mark it in the docs/spec, and signal
it at runtime with the `Deprecation` and `Sunset` headers plus a link to
the replacement. Monitor usage of the deprecated endpoint before turning
it off.

```
HTTP/1.1 200 OK
Deprecation: @1767225600
Sunset: Wed, 30 Jun 2027 23:59:59 GMT
Link: </v2/books>; rel="successor-version"
```

## 3. Backward Compatibility

Old clients must keep working on every deploy. Guard it with contract
tests or spec diffing in CI, and be tolerant in what you accept
(ignore unknown input fields) while being strict in what you promise
(response shape).

```
breaking:  remove field, rename field, new required input,
           change type/format, change status code meaning
safe:      add optional field, add endpoint, relax validation
```
