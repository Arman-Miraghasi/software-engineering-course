package spreadsheet;

import java.util.*;
import java.util.function.ToDoubleFunction;
import parser.AbstractNode;

/** Composite expression; evaluation receives cell lookup rather than depending on a UI. */
public interface FormulaTerm extends AbstractNode {
    double evaluate(ToDoubleFunction<Coordinate> cells);
    default Set<Coordinate> references() { return Set.of(); }
    default List<Double> values(ToDoubleFunction<Coordinate> cells) { return List.of(evaluate(cells)); }

    static double finite(double number) {
        if (!Double.isFinite(number)) throw new SpreadsheetException("Non-finite numerical result");
        return number;
    }
    record NumberLiteral(double value) implements FormulaTerm {
        public NumberLiteral { finite(value); }
        public double evaluate(ToDoubleFunction<Coordinate> cells) { return value; }
    }
    record CellReference(Coordinate target) implements FormulaTerm {
        public double evaluate(ToDoubleFunction<Coordinate> cells) { return cells.applyAsDouble(target); }
        public Set<Coordinate> references() { return Set.of(target); }
    }
    record CellRange(Coordinate topLeft, Coordinate bottomRight) implements FormulaTerm {
        public CellRange {
            if (topLeft.column() > bottomRight.column() || topLeft.row() > bottomRight.row())
                throw new SpreadsheetException("Range must run from top left to bottom right");
        }
        public Set<Coordinate> references() {
            Set<Coordinate> result = new LinkedHashSet<>();
            for (long row = topLeft.row(); row <= bottomRight.row(); row++)
                for (long col = topLeft.column(); col <= bottomRight.column(); col++)
                    result.add(new Coordinate((int) col, (int) row));
            return Collections.unmodifiableSet(result);
        }
        public List<Double> values(ToDoubleFunction<Coordinate> cells) {
            return references().stream().map(cells::applyAsDouble).toList();
        }
        public double evaluate(ToDoubleFunction<Coordinate> cells) {
            throw new SpreadsheetException("Ranges are only valid as function arguments");
        }
    }
    record ArithmeticOperation(char operator, FormulaTerm left, FormulaTerm right) implements FormulaTerm {
        public ArithmeticOperation {
            if ("+-*/".indexOf(operator) < 0) throw new SpreadsheetException("Unknown operator: " + operator);
            if (left instanceof CellRange || right instanceof CellRange)
                throw new SpreadsheetException("Ranges are only valid as function arguments");
        }
        public double evaluate(ToDoubleFunction<Coordinate> cells) {
            double a = left.evaluate(cells), b = right.evaluate(cells);
            return finite(switch (operator) {
                case '+' -> a + b; case '-' -> a - b; case '*' -> a * b;
                case '/' -> { if (b == 0) throw new SpreadsheetException("Division by zero"); yield a / b; }
                default -> throw new SpreadsheetException("Unknown operator");
            });
        }
        public Set<Coordinate> references() {
            Set<Coordinate> result = new HashSet<>(left.references()); result.addAll(right.references()); return Set.copyOf(result);
        }
    }
    record FunctionCall(String name, List<FormulaTerm> arguments) implements FormulaTerm {
        public FunctionCall {
            name = name.toUpperCase(Locale.ROOT);
            arguments = List.copyOf(arguments);
            if (!Set.of("SUMA", "MIN", "MAX", "PROMEDIO").contains(name)) throw new SpreadsheetException("Unknown function: " + name);
            if (arguments.isEmpty()) throw new SpreadsheetException("Functions require at least one argument");
        }
        public double evaluate(ToDoubleFunction<Coordinate> cells) {
            DoubleSummaryStatistics stats = arguments.stream().flatMap(a -> a.values(cells).stream()).mapToDouble(Double::doubleValue).summaryStatistics();
            return finite(switch (name) {
                case "SUMA" -> stats.getSum(); case "MIN" -> stats.getMin();
                case "MAX" -> stats.getMax(); case "PROMEDIO" -> stats.getAverage();
                default -> throw new SpreadsheetException("Unknown function");
            });
        }
        public Set<Coordinate> references() {
            Set<Coordinate> result = new HashSet<>(); arguments.forEach(a -> result.addAll(a.references())); return Set.copyOf(result);
        }
    }
}
