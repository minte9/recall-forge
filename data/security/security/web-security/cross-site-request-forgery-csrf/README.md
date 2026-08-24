## Security / Security / Web Security / Cross-Site Request Forgery (CSRF)

### 1. CSRF Attacks

CSRF tricks a logged-in user's browser into submitting a request to a site they're authenticated on, without their knowledge -- a malicious page auto-submits a form to `bank.com/transfer`, and the browser attaches the user's valid session cookie automatically. The request looks completely legitimate to the server because the cookie is real.

~~~html
<!-- On an attacker's page, auto-submitted -->
<form action="https://bank.com/transfer" method="POST">
  <input type="hidden" name="amount" value="1000">
</form>
~~~

### 2. CSRF Tokens

A CSRF token is a random value the server embeds in each form and checks on submission -- since the attacker's page has no way to read that token from the victim's session, it can't include it in the forged request, so the server rejects it. This works because the attack relies on cookies being sent automatically, but the token isn't attached automatically the way a cookie is.

~~~java
@GetMapping("/transfer-form")
String form(Model model, HttpSession session) {
    model.addAttribute("csrfToken", session.getAttribute("csrf"));
    return "transfer";
}
~~~
