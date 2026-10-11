## 1. What is GraphQL

GraphQL is a query language and runtime for APIs created by Meta Platforms. It lets clients request exactly the data they nee - no more, no less.

Instead of hitting multiple endpoints like in REST, you send one request describing the structure of the data you want, and the server responds with that exact structure.

```
REST: 
GET /users/1
GET /users/1/posts
GET /posts/5/comments
```

```
GraphQL: 
“Give me user 1 with posts and comments” → one request
```

## 2. GraphQL vs REST

REST usually organizes an API around resources and HTTP endpoints. For example, an application dealing with books might expose different endpoints:

```
GET /api/books 
GET /api/books/10 
GET /api/books/10/author 
GET /api/authors/5/books
```

Each endpoint normally has a response structure determined by the server.

```
GET /api/books/10 
```

 The client receives all the fields.
 
```json
{
	"id": 10, 
	"title": "Clean Code", 
	"isbn": "123456", 
	"year": 2008, 
	"description": "...", 
	"author": { 
		"id": 5, 
		"name": "Robert C. Martin" 
	} 
}
```


With GraphQL an application exposes an HTTP endpoint:

```
POST /graphql
```

The client then describes what it needs:

```GraphQL
query { 
	book(id: 10) { 
		title 
	}
}
```

and receives only:

```json
{ 
	"data": { 
		"book": { 
			"title": "Clean Code" 
		} 
	} 
}
```

If another client needs more information, it can use the same GraphQL API:

```GraphQL
query {
    book(id: 10) {
        title
        year
        author {
            name
        }
    }
}
```

The response then contains those requested fields:

```json
{
    "data": {
        "book": {
            "title": "Clean Code",
            "year": 2008,
            "author": {
                "name": "Robert C. Martin"
            }
        }
    }
}
```

## 3. Benefits and Trade-offs

Two commonly GraphQL benefits are avoiding over-fetching and under-fetching. 

Over-fetching means receiving more data than the client actually needs. 
Under-fetching happens when one response doesn't contain enough information, forcing the client to make additional requests. 

```
GET /users/1 
       ↓ 
find author information 

GET /users/1/posts 
       ↓ 
find posts 

GET /posts/5/comments 
       ↓ 
find comments
```

GraphQL can expose these relationships through its schema, allowing the client to request the related information together:

```
query {
    user(id: 1) {
        name
        posts {
            title
            comments {
                text
            }
        }
    }
}
```

Other important GraphQL benefits include a strongly typed schema, predictable responses, schema introspection, and a good fit for applications where different clients need different subsets of the same data.

However, GraphQL also introduces complexity. Nested queries can produce expensive database access, caching is less straightforward than simple REST URLs, authorization needs careful design, and poorly implemented relationships can cause problems such as the +1 query problem.