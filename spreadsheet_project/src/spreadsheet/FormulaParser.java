package spreadsheet;

/** Strategy boundary shared by the supplied parser and the Shunting Yard replacement. */
public interface FormulaParser {
    FormulaTerm parse(String formula);
    static String expression(String formula) {
        if (formula == null || !formula.startsWith("=")) throw new SpreadsheetException("A formula must start with =");
        String expression = formula.substring(1).trim();
        if (expression.isEmpty()) throw new SpreadsheetException("Empty formula");
        return expression;
    }
}
