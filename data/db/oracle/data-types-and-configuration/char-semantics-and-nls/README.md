# DB / Oracle / Data Types & Configuration / CHAR Semantics & NLS

## 1. CHAR Blank-Padded Comparison

A `CHAR(n)` value is right-padded with spaces to its full declared
length, and comparisons between two `CHAR` values compare the padded
forms -- so `CHAR(10)` values `'abc'` and `'abc       '` are equal. Mixing
`CHAR` and `VARCHAR2` in a comparison can silently behave differently
than expected, since `VARCHAR2` doesn't pad.

```sql
SELECT CAST('abc' AS CHAR(10)) = CAST('abc' AS CHAR(10)) FROM DUAL; -- TRUE
```

## 2. NLS Settings

NLS (National Language Support) parameters like `NLS_DATE_FORMAT` and
`NLS_NUMERIC_CHARACTERS` control how dates and numbers are implicitly
parsed and displayed *per session* -- the same query can behave
differently across two environments with different NLS settings, which is
exactly why explicit `TO_DATE`/`TO_CHAR` with a format mask is safer than
relying on implicit conversion.

```sql
SELECT * FROM nls_session_parameters WHERE parameter = 'NLS_DATE_FORMAT';
```
