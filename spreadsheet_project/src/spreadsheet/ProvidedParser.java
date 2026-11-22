package spreadsheet;

import parser.*;
import java.util.*;

/** Adapter and Abstract Factory for the unmodified course expression builder. */
public final class ProvidedParser implements FormulaParser {
    public FormulaTerm parse(String formula) {
        try {
            String expression = FormulaParser.expression(formula);
            if (expression.contains(":"))
                throw new SpreadsheetException("Supplied parser does not support ranges; use the Shunting Yard parser");
            if (expression.matches("(?s).*\\)\\s*;.*"))
                throw new SpreadsheetException("Supplied parser misparses nested calls followed by arguments; use the Shunting Yard parser");
            ExpressionBuilder builder = new ExpressionBuilder(new NodeFactory());
            builder.buildExpression(expression);
            if (!(builder.getExpression() instanceof FormulaTerm term)) throw new SpreadsheetException("Parser returned no expression");
            if (term instanceof FormulaTerm.CellRange) throw new SpreadsheetException("A range requires a function");
            return term;
        } catch (SpreadsheetException e) { throw e; }
        catch (RuntimeException e) { throw new SpreadsheetException("Invalid formula: " + e.getMessage()); }
    }
    public static final class NodeFactory implements AbstractFactory {
        public AbstractNode buildOperator(char op, AbstractNode left, AbstractNode right) {
            if (op == ':') {
                if (left instanceof FormulaTerm.CellReference a && right instanceof FormulaTerm.CellReference b)
                    return new FormulaTerm.CellRange(a.target(), b.target());
                throw new SpreadsheetException("Range endpoints must be cells");
            }
            return new FormulaTerm.ArithmeticOperation(op, (FormulaTerm) left, (FormulaTerm) right);
        }
        public AbstractNode buildFunction(String name, AbstractNode[] parameters) {
            if (parameters == null) throw new SpreadsheetException("Supplied parser cannot parse these function arguments; use the Shunting Yard parser");
            return new FormulaTerm.FunctionCall(name, Arrays.stream(parameters).map(n -> (FormulaTerm)n).toList());
        }
        public AbstractNode buildNumberConstant(double number) { return new FormulaTerm.NumberLiteral(number); }
        public AbstractNode buildTextConstant(String text) { throw new SpreadsheetException("Text is not a numeric formula operand"); }
        public AbstractNode buildCellReference(String reference) {
            String[] ends = reference.split(":", -1);
            if (ends.length == 2) return new FormulaTerm.CellRange(Coordinate.parse(ends[0]), Coordinate.parse(ends[1]));
            return new FormulaTerm.CellReference(Coordinate.parse(reference));
        }
        public AbstractNode buildError(String detail) { throw new SpreadsheetException("Invalid formula: " + detail); }
    }
}
