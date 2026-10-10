# Vehicle Tax Management System

Java implementation of question 4 of **Exercise OOD 1**, following the supplied use case description and both sequence diagrams. No class diagram is generated. Requires JDK 21; no external libraries or build tool are needed.

Compile and run from the project directory:

```sh
mkdir -p out
javac -d out src/*.java
java -cp out Main
```

In IntelliJ IDEA, run `Main.main()`. The console menu exposes all 12 use cases. Search with an empty query to list all registered people or vehicles. Enter measurements using a decimal point.

For a populated example covering all vehicle types, assignment, transfer, and the annual report:

```sh
java -cp out Main --demo
```

Run the dependency-free regression tests:

```sh
javac -d out src/*.java tests/*.java
java -cp out VehicleTaxSystemTest
```

## Implementation and diagram correspondence

- `Main`: the console interface used by the City Administrator.
- `VehicleTaxSystem`: the **System** lifeline; registers, edits, deletes, and searches records, manages ownership, and generates reports.
- `Person`: stores license number, name, surname, address, and an externally read-only collection of owned vehicles.
- `Vehicle`, `Car`, and `Motorcycle`: store vehicle details and calculate tax polymorphically through `calculateTax()`.
- `OwnershipResult`: represents transfer/assignment success, missing records, already-owned assignment, and the diagram's “transfer not required” result.
- `AnnualTaxReport`: an immutable snapshot of owner details, vehicle details, vehicle taxes, and owner totals.

`transferVehicle(plateNumber, newLicenseNumber)` looks up both records, returns the diagram's failure results when a record is missing, obtains the current owner, and detects a transfer to the same owner. For a valid transfer it calls `removeVehicle()` if there is an old owner, `setOwner()`, `addVehicle()`, and finally `calculateTax()`. The optional old-owner branch also permits transfer of an unowned vehicle, as drawn in the sequence diagram. `assignVehicle()` specifically requires an unowned vehicle.

`generateAnnualTaxReport()` calls `getAllPersons()`, iterates each person's `getVehicles()`, calls each vehicle's `calculateTax()`, adds vehicle entries, and sums the owner's tax. People without vehicles appear with EUR 0.00; unowned vehicles do not appear under any person. Reports use the current registration and ownership state, without historical ownership or prorating.

## Rules and chosen policies

| Vehicle | Annual tax in EUR |
| --- | --- |
| Motorcycle | Engine displacement in cc × 0.10 |
| Petrol car | CO2 emissions in grams × 1.40 |
| Diesel car | CO2 emissions in grams × 1.80 |
| Hybrid car | CO2 emissions in grams × 1.20 |

Money uses `BigDecimal`. Each vehicle tax is rounded to two decimals using `HALF_UP`; owner totals sum these displayed amounts. Motorcycle displacement must be positive; car emissions may be zero but cannot be negative.

License numbers and plates are unique, trimmed, and case-insensitive (stored in uppercase). They are stable identifiers: editing changes descriptive details and tax characteristics, including a car's fuel type. Car and motorcycle categories cannot be exchanged through an edit. Searches match substrings of identifiers and descriptive fields, ignoring case.

Deleting a person retains their registered vehicles and makes them unowned. Deleting a vehicle removes it from its owner's collection. Missing-record operations and invalid edits leave existing records unchanged. Assignment and transfer update both sides of the ownership association.

Data is stored in memory for a single application run, as the exercise does not specify persistence. Start `Main` for an empty registry or `Main --demo` for a separate demonstration that exits after printing the report.
