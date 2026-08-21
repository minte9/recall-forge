## JS / Vue.js / Routing / Advanced Routing

### 1. Nested Routes

A route can declare `children`, rendered inside a nested `<router-view>`
in the parent route's component — models UIs like a settings page with
sub-tabs.

~~~javascript
{ path: "/user", component: User, children: [
  { path: "profile", component: Profile },
]}
~~~


### 2. Navigation Guards

`beforeEach` (global) and `beforeEnter` (per-route) run before a
navigation completes, letting code redirect or block it — the standard
place for auth checks.

~~~javascript
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isLoggedIn()) return "/login";
});
~~~


### 3. Lazy-Loaded Routes

A route's `component` can be a dynamic `import()` instead of a static
reference, splitting that route into its own bundle chunk loaded only
when visited.

~~~javascript
{ path: "/admin", component: () => import("./Admin.vue") }
~~~
