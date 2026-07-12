## JVM / Java / SOLID / Single Responsibility Principle

### 1. Single Responsibility Principle (SRP)

The Single Responsibility Principle states that a class should have only one reason to change. This does not mean a class can have only one method or perform only one task. Instead, all of its responsibilities should belong to the same business concern or actor.

~~~java
class Employee {
    double calculateSalary() { ... }
    double calculateBonus() { ... }
}
~~~

### 2. Bad Implementation (violates SRP)

A class violates SRP when it mixes responsibilities that belong to different concerns. In this example, Employee contains business logic, reporting, and database persistence, so changes requested by different teams all affect the same class. 

~~~java
/**
 * SRP - Non complient example
 * ===========================
 * Employee with multiple legitimate beharviors
 */
package solid.srp.non_compliant;

public class BadPayrollApplication {
    public static void main(String[] args) {

        Employee employee = new Employee("John", 1000);
        System.out.println(employee.generatePayrollReport());
        employee.saveToDatabase();

        /*
            Employee: John, Salary: 1200.0
            Saving employee to DB
        */
    }
}

class Employee {
    private final String name;
    private final double baseSalary;

    Employee(String name, double baseSalary) {
        this.name = name;
        this.baseSalary = baseSalary;
    }

    double calculateSalary() {
        return baseSalary * 1.2;
    }

    String generatePayrollReport() {
        return "Employee: " + name + ", Salary: " + calculateSalary();
    }

    void saveToDatabase() {
        System.out.println("Saving employee to DB");
       
    }
}
~~~

### 3. SRP-compliant Implementation

A better design separates each responsibility into its own class. The Employee class contains only employee-related business logic, while reporting and persistence are handled by dedicated services. This keeps each class focused on a single reason to change.

~~~java
/**
 * SRP - Compliant design
 * ======================
 * All changes to Employee come from one reason:
 * changes in employee-related business rules
 */
package solid.srp;

public class PayrollApplication {
    public static void main(String[] args) {

        Employee employee = new Employee("John", 1000);
        PayrollReportService service = new PayrollReportService();
        EmployeeRepository repository = new EmployeeRepository();
        service.generate(employee);
        repository.save(employee);

        /*
            Employee: John / Salary: 1200.0
            Saving employee to DB: John
        */
    }
}

class Employee {
    private final String name;
    private final double baseSalary;

    Employee(String name, double baseSalary) {
        this.name = name;
        this.baseSalary = baseSalary;
    }

    double calculateSalary() {
        return baseSalary * 1.2;
    }

    String getName() {
        return name;
    }
}

class PayrollReportService {
    void generate(Employee employee) {
        System.out.println(
            "Reports employee: " + employee.getName() + " / " + 
            "Salary: " + employee.calculateSalary()
        );
    }
}

class EmployeeRepository {
    void save(Employee employee) {
        System.out.println("Saving employee to DB: " + employee.getName());
    }
}
~~~

### 4. Key Insights (SRP)

SRP is about responsibilities, not class size. A class may contain many methods as long as they all support the same business responsibility. When evaluating a design, think about who requests changes and whether those changes belong to the same business concern. 

~~~java
// Good: same responsibility
calculateSalary();
calculateBonus();

// Different responsibilities
generateReport();
saveToDatabase();
~~~
