# Company Salary additional questions:

## 1- Is your design ready to easily accept changes in how to calculate the total salary? for example for calculating the total salary taking into account taxes.

Yes, because of encapsulation.

Because the formula to compute salary is encapsulated inside `Worker.getTotalSalary()` rather than scattered throughout `Main.java`, you only ever need to change the logic in one place.

For example, if you introduce a tax deduction (say, a 15% flat income tax or a tax rate parameter), external code that calls `worker.getTotalSalary()` does not need to change at all:

```java
// Inside Worker.java
public double getTotalSalary() {
    double gross = this.base_salary + this.complement;
    double taxRate = 0.15; // 15% tax
    return gross * (1.0 - taxRate);
}
```

## 2- How to implement the case where I don't use all of the 100 "spaces" for workers? (e.g., my company only has 20)

When allocating a fixed-size array of 100 elements (`new Worker[100]`), any unused positions remain `null`. Calling methods on a `null` element results in a `NullPointerException`.

To handle having fewer workers (such as 20) safely, we track the actual number of workers stored using a counter variable (`workerCount`) instead of iterating over the entire capacity of the array (`workers.length`).