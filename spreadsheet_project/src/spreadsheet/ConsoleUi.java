package spreadsheet;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Console boundary: all questions and confirmations stay outside the domain. */
public final class ConsoleUi {
    private final SpreadsheetFacade app;
    private final Scanner input;
    private final PrintStream output;
    public ConsoleUi(SpreadsheetFacade app, InputStream input, PrintStream output) {
        this.app = app; this.input = new Scanner(input); this.output = output;
    }
    private String ask(String prompt) {
        output.print(prompt + ": ");
        if (!input.hasNextLine()) throw new NoSuchElementException();
        return input.nextLine();
    }
    private boolean confirm(String prompt) { return ask(prompt + " [y/N]").equalsIgnoreCase("y"); }
    private boolean canReplace() { return !app.hasUnsavedChanges() || confirm("Discard unsaved changes?"); }
    public void run() {
        output.println("Spreadsheet — formulas use = and function arguments use ;");
        while (true) {
            output.println("1 Create | 2 Open | 3 Save | 4 Exit | 5 Set | 6 Clear | 7 Inspect | 8 Text | 9 Number | 10 Display");
            try {
                switch (ask("Option").trim()) {
                    case "1" -> { if (canReplace()) app.create(); }
                    case "2" -> { if (canReplace()) { app.open(Path.of(ask("S2V path"))); showErrors(); } }
                    case "3" -> {
                        app.sheet(); Path path = Path.of(ask("Destination path"));
                        boolean exists = Files.exists(path);
                        if (!exists || confirm("Overwrite existing file?")) { app.save(path, exists); output.println("Saved"); }
                    }
                    case "4" -> { if (canReplace()) return; }
                    case "5" -> { app.set(ask("Coordinate"), ask("Type (text/number/formula)"), ask("Content")); showErrors(); }
                    case "6" -> { app.clear(ask("Coordinate")); showErrors(); }
                    case "7" -> {
                        Cell cell = app.sheet().inspect(ask("Coordinate"));
                        output.println(cell.coordinate() + " " + cell.content().getClass().getSimpleName() + ": " + cell.content().source());
                    }
                    case "8" -> output.println(app.sheet().text(ask("Coordinate")));
                    case "9" -> output.println(app.sheet().number(ask("Coordinate")));
                    case "10" -> display();
                    default -> output.println("Unknown option");
                }
            } catch (NoSuchElementException e) { output.println("End of input"); return; }
            catch (IOException | SpreadsheetException | IllegalArgumentException e) { output.println("Error: " + e.getMessage()); }
        }
    }
    private void showErrors() { app.sheet().errors().forEach((c, e) -> output.println("Error at " + c + ": " + e)); }
    private void display() {
        var contents = app.sheet().contents();
        if (contents.isEmpty()) { output.println("Empty spreadsheet"); return; }
        int rows = contents.keySet().stream().mapToInt(Coordinate::row).max().orElse(0);
        int cols = contents.keySet().stream().mapToInt(Coordinate::column).max().orElse(0);
        output.print("Row");
        for (int c = 1; c <= cols; c++) output.print("\t" + new Coordinate(c, 1).columnName());
        output.println();
        for (int r = 1; r <= rows; r++) {
            output.print(r);
            for (int c = 1; c <= cols; c++) {
                String address = new Coordinate(c, r).toString();
                try { output.print("\t" + app.sheet().text(address)); }
                catch (SpreadsheetException e) { output.print("\t#ERROR(" + e.getMessage() + ")"); }
            }
            output.println();
        }
    }
}
