## JS / Vue.js / Routing / Vue Router Basics

### 1. Routes & router-view

Routes map a URL path to a component, defined in a router config;
`<router-view>` in the app template is where the matched component
actually renders.

~~~javascript
const routes = [{ path: "/about", component: About }];
~~~

~~~vue
<router-view></router-view>
~~~


### 2. Navigating

`<router-link>` renders an `<a>` that updates the URL without a full
page reload; `router.push()` navigates programmatically from script,
e.g. after a form submits.

~~~vue
<router-link to="/about">About</router-link>
~~~

~~~javascript
router.push("/dashboard");
~~~


### 3. Route Params

Dynamic segments (`/users/:id`) are read via `route.params` inside a
component, letting one route definition render different data based on
the URL.

~~~javascript
const routes = [{ path: "/users/:id", component: UserView }];
// inside UserView:
const route = useRoute();
route.params.id;
~~~
