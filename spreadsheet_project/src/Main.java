import spreadsheet.*;

public class Main {
    public static void main(String[] args) {
        FormulaParser parser = args.length > 0 && args[0].equals("--provided-parser")
                ? new ProvidedParser() : new ShuntingYardParser();
        new ConsoleUi(new SpreadsheetFacade(parser), System.in, System.out).run();
    }
}
