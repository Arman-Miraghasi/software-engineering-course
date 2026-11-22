# Spreadsheet

A Java console application for creating, editing, evaluating, and saving spreadsheets. Cells can contain numbers, text, or formulas, and are addressed by a column name and row number, such as `A1`, `B3`, or `AA12`.

## Features

- Store numbers, text, and formulas in a sparse spreadsheet.
- Read cell values as text or numbers.
- Evaluate arithmetic, cell references, nested functions, and rectangular ranges.
- Reject malformed formulas and circular dependencies before accepting an edit.
- Recalculate affected formulas when their inputs change while retaining cached results for unrelated cells.
- Load and save spreadsheets in S2V format.
- Inspect individual cells or display the spreadsheet as a grid.
- Confirm overwriting files or discarding unsaved changes.

## Requirements

- JDK 21, with `java` and `javac` available on the command line.
- Bash for the provided build and run scripts. On Windows, use WSL or Git Bash.
- Maven 3.9 or later to run the JUnit tests, or IntelliJ IDEA with its bundled Maven.

The parser library is included in `lib/expression-factory.jar`. Running the application through the scripts needs only JDK 21 and Bash. The JUnit tests use Maven, which downloads test and build dependencies on the first run.

## Build and run

From the project directory:

```bash
./run.sh
```

This compiles the sources and opens the console menu. To compile without starting the application:

```bash
./build.sh
```

In IntelliJ IDEA, open the project, select JDK 21, and run `Main` in `src/Main.java`. To use Maven and JUnit, right-click `pom.xml`, choose **Add as Maven Project** if it is not already imported, and reload the Maven project. Maven is configured to use the existing `src` and `test` directories.

## Using the application

The application works with one active spreadsheet. Start by creating a spreadsheet with option `1` or opening an existing file with option `2`.

| Option | Action |
| --- | --- |
| 1 | Create a blank spreadsheet |
| 2 | Open an S2V file by path |
| 3 | Save the active spreadsheet to a path |
| 4 | Exit the application |
| 5 | Set a cell's coordinate, content type, and content |
| 6 | Clear a cell |
| 7 | Inspect the stored content and its type |
| 8 | Read a cell's value as text |
| 9 | Read a cell's value as a number |
| 10 | Display current values in a grid |

For confirmation prompts, enter `y` to confirm. Any other answer cancels the action.

### Example session

1. Select `1` to create a spreadsheet.
2. Select `5`, enter coordinate `A1`, choose type `number`, and enter `2`.
3. Select `5`, enter coordinate `B1`, choose type `number`, and enter `3`.
4. Select `5`, enter coordinate `C1`, choose type `formula`, and enter `=SUMA(A1:B1;MAX(4;5))`.
5. Select `9` and enter `C1`. The result is `10.0`.
6. Change `A1` to the number `7`. Reading `C1` now returns `15.0`.
7. Select `3` and enter a destination such as `examples/my-sheet.s2v`.

The content types accepted by option `5` are `text`, `number`, and `formula`, or their short forms `t`, `n`, and `f`. Enter formulas directly, without surrounding quotation marks.

### Open the included example

Select option `2` and enter:

```text
examples/example.s2v
```

Then select `10` to display the spreadsheet. Its stored contents are:

```text
=C1+C2;4;1
;;2
;TOTAL;=A1+B1
```

Cell `A1` evaluates to `3`, and `C3` evaluates to `7`. Relative paths are resolved from the application's working directory; the run script starts it from the project directory.

## Cell values and formulas

A blank cell returns an empty string when read as text and zero when read as a number. Text that represents a number, such as `12.5`, can be read as a number. Reading other text as a number produces an error.

Formulas begin with `=` and support `+`, `-`, `*`, `/`, parentheses, unary signs, and decimal or scientific numeric literals. Multiplication and division take precedence over addition and subtraction.

```text
=1+2
=3*5+4
=(A1+B1)/2
=-A1+1.5
```

### Functions and ranges

Function arguments are separated by semicolons in the application.

| Function | Result | Example |
| --- | --- | --- |
| `SUMA` | Sum of all argument values | `=SUMA(1;2;3)` |
| `MIN` | Smallest argument value | `=MIN(A1;B1;4)` |
| `MAX` | Largest argument value | `=MAX(A1;B1;4)` |
| `PROMEDIO` | Arithmetic mean of all argument values | `=PROMEDIO(A1:B3)` |

Functions require at least one argument. Arguments may include numbers, individual cells, ranges, arithmetic expressions, or nested functions.

A range identifies an inclusive rectangle from its upper-left cell to its lower-right cell. For example, `A1:B3` contains `A1`, `A2`, `A3`, `B1`, `B2`, and `B3`. Ranges are used as function arguments.

```text
=SUMA(A1:B3;MAX(C1;C2);10)
=1+A1*((SUMA(A2:B5;PROMEDIO(B6:D8);C1;27)/4)+(D6-D8))
```

Syntax errors and edits that introduce cycles are rejected, preserving the previous cell content. Evaluation errors, such as division by zero or a reference to nonnumeric text, are reported on the affected cells. Correcting the input recalculates dependent formulas.

## S2V storage

An S2V file contains one spreadsheet row per line. Semicolons separate cells; empty fields preserve blank cells before later populated columns. Trailing empty fields are omitted. Files store formula source rather than calculated results.

Inside stored formulas, commas replace the semicolons between function arguments. For example:

```text
Application: =SUMA(A1;B1;2)
File:        =SUMA(A1,B1,2)
```

Loading restores the formula argument separators and evaluates formulas. A file with invalid syntax or circular dependencies does not replace the active spreadsheet.

S2V has no quoting mechanism in this implementation. Saving rejects text containing semicolons or line breaks, text starting with `=`, and numeric text that would reload as a number. This prevents ambiguous contents from silently changing type. Commas in ordinary text are preserved.

## Project structure

| Location | Responsibility |
| --- | --- |
| `src/Main.java` | Application entry point and parser selection |
| `src/spreadsheet/ConsoleUi.java` | Menu, prompts, confirmations, and grid display |
| `src/spreadsheet/SpreadsheetFacade.java` | Active session, application operations, and unsaved-change tracking |
| `src/spreadsheet/Spreadsheet.java` | Cell storage, dependency graph, cycle validation, and cached evaluation |
| `src/spreadsheet/Coordinate.java` | Coordinate validation and column-letter conversion |
| `src/spreadsheet/Cell.java` | Read-only cell snapshot |
| `src/spreadsheet/CellContent.java` | Text, numeric, and formula content types |
| `src/spreadsheet/FormulaTerm.java` | Expression nodes for literals, references, ranges, operations, and functions |
| `src/spreadsheet/FormulaParser.java` | Common interface for formula parsers |
| `src/spreadsheet/ShuntingYardParser.java` | Default parser using operator and operand stacks |
| `src/spreadsheet/ProvidedParser.java` | Adapter and node factory for the supplied parser |
| `src/spreadsheet/S2vRepository.java` | S2V loading, validation, and saving |
| `src/spreadsheet/SpreadsheetException.java` | User-visible domain and formula errors |
| `test/spreadsheet/` | JUnit 5 test classes for cells, expression nodes, parsers, coordinates, persistence, and console interaction |
| `pom.xml` | Maven configuration and JUnit dependencies |
| `examples/example.s2v` | Example spreadsheet from the assignment |
| `lib/` | Supplied parser binary and its attribution |

The console delegates application operations to the facade. The spreadsheet owns domain state and evaluation, while the repository handles persistence. A different interface can reuse the application and domain classes.


## Parser implementations

The default Shunting Yard parser supports the application's complete formula functionality. It builds the expression tree using an operator stack, an operand stack, and function argument frames.

The original [course parser](https://github.com/jlopezr/expression-builder-example) remains available for comparison:

```bash
./run.sh --provided-parser
```

The supplied parser supports the tested basic arithmetic, references, and simple function calls. Its bundled version has limitations with ranges and nested calls followed by further arguments. The adapter rejects these known cases; its conservative guard can also reject some grouped arguments. Use the default parser for full function and range support.

## Tests

Run the full suite from the project directory:

```bash
mvn test
```

The existing script also runs the JUnit suite through Maven:

```bash
./build.sh test
```

If Maven is not on your PATH, run tests from IntelliJ's Maven tool window under **Lifecycle → test**, or set `MAVEN_CMD` to your Maven executable when using the script.

To run one test class or method:

```bash
mvn -Dtest=SpreadsheetTest test
mvn -Dtest=SpreadsheetTest#rejectsDirectCycleWithoutChangingCell test
```

In IntelliJ, import `pom.xml`, then right-click the `test` directory or an individual test class and choose **Run Tests**. You can also use the run icon next to any `@Test` method.

| Test class | Coverage |
| --- | --- |
| `SpreadsheetTest` | Cell values, references, cycles, dependency updates, cached results, and error recovery |
| `FormulaTermTest` | Hand-built `1+2` and `3*5+4` expression trees and division by zero |
| `ShuntingYardParserTest` | Precedence, parentheses, unary signs, functions, ranges, complex formulas, and syntax errors |
| `ProvidedParserTest` | Supplied-parser integration and explicit rejection of its known unsupported cases |
| `CoordinateTest` | Coordinate conversion, normalization, and invalid addresses |
| `S2vRepositoryTest` | S2V loading and round trips, overwrite protection, ambiguous text, and session preservation |
| `ConsoleUiTest` | Editing, reading, displaying, and exit confirmation through the console |

JUnit's `@TempDir` supplies and cleans up temporary files for persistence tests. Maven Surefire prints a test summary and writes reports to `target/surefire-reports/`. Build output under `target/` is ignored by Git.

## Limits

Numbers use finite Java `double` values, so floating point rounding applies. Coordinates use positive Java integers. Ranges expand into individual cell dependencies, and evaluation follows dependency chains recursively. Very large ranges, very deep chains, and grids extending to distant coordinates may require substantial memory or processing time.
