# JVM / Spring / Framework / AOP

## 1. Aspect-Oriented Programming

AOP separates cross-cutting concerns from business logic.

Cross-cutting concerns = logic used in many places:  
- logging
- transactions
- security
- performance monitoring

Without AOP for example, logging is `mixed` with business logic.

~~~java
class Service {

    public void process() {
        System.out.println("Start log");   // logging
        
        // business logic
        
        System.out.println("End log");     // logging
    }
}
~~~

With AOP, business logic stays clear.  
Logging is moved to an aspect.  

~~~java
class Service {
    public void process() {
        // only business logic
    }
}
~~~

## 2. Real-life analogy

Imagine that you go to a bank.

1. You enter
2. Security check happens
3. You do your transaction
4. Logging happens

You ony care about `transaction`.  
But security + logging happen `automatically`.  

~~~java
class BankService {

    public void transferMoney() {
        System.out.println("Transferring money...");
    }
}
~~~

No logging here, no security here.    
They will go into an `aspect`.  

~~~xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>${spring.version}</version>
    </dependency>

    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-aop</artifactId>
        <version>${spring.version}</version>
    </dependency>

    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjweaver</artifactId>
        <version>1.9.24</version>
    </dependency>

    <dependency>
        <groupId>jakarta.annotation</groupId>
        <artifactId>jakarta.annotation-api</artifactId>
        <version>2.1.1</version>
    </dependency>
</dependencies>
~~~
~~~java
package com.example.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.*;

public class Main {
    public static void main(String[] args) {
        
        ApplicationContext context = 
            new AnnotationConfigApplicationContext(AppConfig.class);

        BankService service = context.getBean(BankService.class);
        service.transferMoney();

        // >> Security check
        // Transferring money...
        // >> Logging after method
    }
}

@Configuration
@ComponentScan("com.example")
@EnableAspectJAutoProxy  // enable AOP - Look Here
class AppConfig {

}

@Component
class BankService {

    public void transferMoney() {
        System.out.println("Transferring money...");
    }
}

// ======================
// Aspect (this is a AOP)
// ======================

@Aspect
@Component
class LoggingAspect {

    @Before("execution(* BankService.transferMoney(..))")
    public void before() {
        System.out.println(">> Security check");
    }

    @After("execution(* BankService.transferMoney(..))")
    public void after() {
        System.out.println(">> Logging after method");
    }
}
~~~

## 3. The magic (simplified)

Spring does NOT modify your class.  

Spring creates a `proxy object`.   
Proxy intercepts method calls.    
Executes `aspect` logic.    

~~~java
// ========================================
// What Spring REALLY does
// ========================================

// You think you have:
BankService service;

// Actually you have:
ProxyBankService proxy;

// When you call:
proxy.transferMoney();

// Spring does:
before();
realService.transferMoney();
after();
~~~