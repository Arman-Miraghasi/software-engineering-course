package spreadsheet;

import java.util.*;
import java.util.regex.*;

/** Iterative Shunting Yard: operator stack + operand stack build the expression composite. */
public final class ShuntingYardParser implements FormulaParser {
    private static final Pattern TOKEN = Pattern.compile(
            "\\G\\s*(?:(\\d+(?:\\.\\d*)?(?:[eE][+-]?\\d+)?|\\.\\d+(?:[eE][+-]?\\d+)?)|([A-Za-z]+[0-9]*)|([+*/():;\\-]))");
    private static final class Frame {
        final String function;
        final int base;
        int separators;
        Frame(String function, int base) { this.function = function; this.base = base; }
    }
    public FormulaTerm parse(String formula) {
        String input = FormulaParser.expression(formula);
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(input);
        int position = 0;
        while (position < input.length()) {
            if (!matcher.find(position)) throw new SpreadsheetException("Invalid token at position " + (position + 1));
            tokens.add(matcher.group().trim().toUpperCase(Locale.ROOT)); position = matcher.end();
        }
        Deque<Object> operators = new ArrayDeque<>();
        Deque<FormulaTerm> values = new ArrayDeque<>();
        boolean expectingOperand = true;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.matches("[A-Z]+") && i + 1 < tokens.size() && tokens.get(i + 1).equals("(")) {
                if (!expectingOperand) throw new SpreadsheetException("Missing operator before function");
                operators.push(new Frame(token, values.size())); i++;
            } else if (token.equals("(")) {
                if (!expectingOperand) throw new SpreadsheetException("Missing operator before (");
                operators.push(new Frame(null, values.size()));
            } else if (token.equals(")") || token.equals(";")) {
                if (expectingOperand) throw new SpreadsheetException("Missing operand or empty argument");
                while (!operators.isEmpty() && operators.peek() instanceof String) apply((String)operators.pop(), values);
                if (operators.isEmpty()) throw new SpreadsheetException("Unmatched parenthesis or separator");
                Frame frame = (Frame)operators.peek();
                if (token.equals(";")) {
                    if (frame.function == null) throw new SpreadsheetException("Argument separator outside function");
                    frame.separators++; expectingOperand = true;
                } else {
                    operators.pop();
                    int count = frame.separators + 1;
                    if (values.size() != frame.base + count) throw new SpreadsheetException("Invalid argument list");
                    if (frame.function != null) {
                        List<FormulaTerm> args = new ArrayList<>();
                        for (int n = 0; n < count; n++) args.add(values.pop());
                        Collections.reverse(args); values.push(new FormulaTerm.FunctionCall(frame.function, args));
                    } else if (values.peek() instanceof FormulaTerm.CellRange) {
                        throw new SpreadsheetException("Ranges must be direct function arguments");
                    }
                    expectingOperand = false;
                }
            } else if (Set.of("+", "-", "*", "/", ":").contains(token)) {
                String op = token;
                if (expectingOperand) {
                    if (!token.equals("+") && !token.equals("-")) throw new SpreadsheetException("Missing left operand");
                    op = "u" + token;
                } else {
                    while (!operators.isEmpty() && operators.peek() instanceof String top && precedence(top) >= precedence(op))
                        apply((String) operators.pop(), values);
                }
                operators.push(op); expectingOperand = true;
            } else {
                if (!expectingOperand) throw new SpreadsheetException("Missing operator");
                if (token.matches("[A-Z]+[0-9]+")) values.push(new FormulaTerm.CellReference(Coordinate.parse(token)));
                else {
                    try { values.push(new FormulaTerm.NumberLiteral(Double.parseDouble(token))); }
                    catch (NumberFormatException e) { throw new SpreadsheetException("Invalid operand: " + token); }
                }
                expectingOperand = false;
            }
        }
        if (expectingOperand) throw new SpreadsheetException("Missing final operand");
        while (!operators.isEmpty()) {
            Object op = operators.pop();
            if (!(op instanceof String)) throw new SpreadsheetException("Unclosed parenthesis");
            apply((String)op, values);
        }
        if (values.size() != 1 || values.peek() instanceof FormulaTerm.CellRange) throw new SpreadsheetException("Invalid formula or standalone range");
        return values.pop();
    }
    private static int precedence(String op) {
        return switch (op) { case ":" -> 4; case "u+", "u-" -> 3; case "*", "/" -> 2; default -> 1; };
    }
    private static void apply(String op, Deque<FormulaTerm> values) {
        if (values.isEmpty()) throw new SpreadsheetException("Missing operand");
        FormulaTerm right = values.pop();
        if (op.startsWith("u")) {
            values.push(new FormulaTerm.ArithmeticOperation(op.charAt(1), new FormulaTerm.NumberLiteral(0), right)); return;
        }
        if (values.isEmpty()) throw new SpreadsheetException("Missing left operand");
        FormulaTerm left = values.pop();
        if (op.equals(":")) {
            if (!(left instanceof FormulaTerm.CellReference a) || !(right instanceof FormulaTerm.CellReference b))
                throw new SpreadsheetException("Range endpoints must be cell references");
            values.push(new FormulaTerm.CellRange(a.target(), b.target()));
        } else values.push(new FormulaTerm.ArithmeticOperation(op.charAt(0), left, right));
    }
}
