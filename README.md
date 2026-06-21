# Recall Forge

The application import topics from a local README.md, ask review questions,  
evaluates answers with OpenAI, and stores memory in database.  

## 1. Backend-only
v1.0.1

- 1.1 Project structure
- 1.2 Gradle Settings 
- 1.3 Gradle Build
- 1.4 Docker Compose
- 1.5 OpenAPI 
- 1.6 Application Properties
- 1.7 Main App
- 1.8 Domain model (JPA)
- 1.9 Repositories
- 1.10 DTOs
- 1.11 OpenAI Config
- 1.12 Services
- 1.13 Controllers
- 1.14 Run the project
- 1.15 Api Requests

## 2. Repetition System
v1.0.2

- 2.1 Review History Endpoint
- 2.2 Due Topics Endpoint
- 2.3 Test it
- 2.4 Dashboard
- 2.5 Upload markdown file
- 2.6 Database clear
- 2.7 Due topics
- 2.8 Categories
- 2.9 Database backup
- 2.10 Queue Summary

## 3. Dashboard
v1.0.3

- 3.1 Improved Summary
- 3.2 Dashboard Graphics
- 3.3 Subdomain with Traefix
- 3.4 Markdowns Summary

## 4. Reward Layer
v1.0.4

- 4.1 XP points
- 4.2 Perfect streak bonus
- 4.3 Reward bar
- 4.4 Level title

## 5. OAuth2
v1.0.5

### 5.1 Add dependency

Spring Boot/Security auto-configures OAuth clients from spring.security.oauth2.client.registration.*;  
Google and GitHub are predefined providers when registration IDs are google and github.  

https://console.cloud.google.com/apis/credentials
https://github.com/settings/developers

#### build.gradle

~~~sh
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
}
~~~

#### application.yml

~~~sh
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:}
            client-secret: ${GOOGLE_CLIENT_SECRET:}
            scope:
              - openid
              - profile
              - email
          github:
            client-id: ${GITHUB_CLIENT_ID:}
            client-secret: ${GITHUB_CLIENT_SECRET:}
            scope:
              - read:user
              - user:email
~~~

Redirect URLs to register:

~~~sh
http://localhost:9090/login/oauth2/code/google
http://localhost:9090/login/oauth2/code/github
https://recall.minte9.com/login/oauth2/code/google
https://recall.minte9.com/login/oauth2/code/github
~~~


#### .env.example

~~~sh
APP_SECURITY_ENABLED=true
APP_SECURITY_USERNAME=myuser
APP_SECURITY_PASSWORD=mypass
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=
~~~

#### compose.yml

~~~yml
    app:
        build: .
            container_name: recallforge-app
            restart: unless-stopped
        depends_on:
            - postgres
        ports:
            - "127.0.0.1:9090:9090"
        environment:
            SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/recallforge
            SPRING_DATASOURCE_USERNAME: recallforge
            SPRING_DATASOURCE_PASSWORD: recallforge
            OPENAI_API_KEY: ${OPENAI_API_KEY}
            APP_SECURITY_ENABLED: ${APP_SECURITY_ENABLED}
            APP_SECURITY_USERNAME: ${APP_SECURITY_USERNAME}
            APP_SECURITY_PASSWORD: ${APP_SECURITY_PASSWORD}
            GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
            GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
~~~

#### SecurityConfig.java

~~~java
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers(
            "/",
            "/oauth2/**",
            "/login/oauth2/**",
            "/images",
            "/css/**",
            "/js/**"
        ).permitAll()
        .anyRequest().authenticated()
    )
    .formLogin(form -> form
        .defaultSuccessUrl("/", true)
    )
    .oauth2Login(oauth2 -> oauth2
        .defaultSuccessUrl("/", true)
    )
    .logout(logout -> logout
        .logoutSuccessUrl("/login?logout")
    );
~~~

Later, when we create our own landing page with login links, we add:

~~~html
<a href="/login">Login with username/password</a>
<a href="/oauth2/authorization/google">Login with Google</a>
<a href="/oauth2/authorization/github">Login with GitHub</a>
~~~

Only when you build a fully custom login page should we add:

~~~java
.formLogin(form -> form.loginPage("/login"))
.oauth2Login(oauth2 -> oauth2.loginPage("/login"))
~~~

The OAuth endpoints exist automatically:

~~~sh
/oauth2/authorization/google
/oauth2/authorization/github
~~~


### 5.2 Povider setup

Google Cloud Console:  
Create OAuth Project and create new client and test it.

Github:  
Developer Settings > New OAuth App

#### Test  it:

~~~sh
docker compose up -d --build

http://localhost:9090/
~~~


### 5.3 Database integration


UserAccountEntity:  
src/main/java/dev/recallforge/domain/UserAccount.java

Repository:
src/main/java/dev/recallforge/repository/UserAccountRepository.java

Custom OAuth user service:
src/main/java/dev/recallforge/security/DatabaseOAuth2UserService.java

Wire it into SecurityConfig:

~~~java
private final DatabaseOAuth2UserService databaseOAuth2UserService;

public SecurityConfig(DatabaseOAuth2UserService databaseOAuth2UserService) {
    this.databaseOAuth2UserService = databaseOAuth2UserService;
}
~~~

Then update OAuth config:

~~~java
.oauth2Login(oauth2 -> oauth2
    .defaultSuccessUrl("/", true)
    .userInfoEndpoint(userInfo -> userInfo
        .userService(databaseOAuth2UserService)
    )
)
~~~

Create temporary endpoint:

~~~java
package dev.recallforge.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Object me(Authentication authentication) {
        return authentication;
    }
}
~~~


#### Test it

Login with Google/GitHub and visit:
http://localhost:9090/api/me

Also check DB:
select * from user_accounts;



### 5.6 App user integration

Add current user service.

src/main/java/dev/recallforge/security/CurrentUserService.java

~~~java
package dev.recallforge.security;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.repository.UserAccountRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount currentUser() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof OAuth2AuthenticationToken token)) {
            throw new IllegalStateException("Current user is not OAuth2 authenticated");
        }

        OAuth2User oauthUser = token.getPrincipal();

        String provider = token.getAuthorizedClientRegistrationId();
        String providerUserId = extractProviderUserId(oauthUser);

        return userAccountRepository
            .findByProviderAndProviderUserId(provider, providerUserId)
            .orElseThrow(() -> new IllegalStateException("User not found in database"));
    }

    public Long currentUserId() {
        return currentUser().getId();
    }

    private String extractProviderUserId(OAuth2User oauthUser) {
        Object id = oauthUser.getAttribute("sub"); // Google

        if (id == null) {
            id = oauthUser.getAttribute("id"); // GitHub
        }

        if (id == null) {
            throw new IllegalStateException("Missing OAuth provider user id");
        }

        return id.toString();
    }
}
~~~

Add a temporary debug endpoint:

~~~java
package dev.recallforge.controller;

import dev.recallforge.domain.UserAccount;
import dev.recallforge.security.CurrentUserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final CurrentUserService currentUserService;

    public MeController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/api/me")
    public UserAccount me() {
        return currentUserService.currentUser();
    }
}
~~~


### 5.7 Rate limits




## Architecture

![Screenshot](docs/images/recall-forge_architecture.png)

## Screenshots

![Screenshot](docs/images/recall-forge_4.png)