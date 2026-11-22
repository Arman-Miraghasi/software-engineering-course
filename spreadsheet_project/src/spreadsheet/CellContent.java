package spreadsheet;

/** Immutable stored contents, separate from the calculated value. */
public sealed interface CellContent {
    String source();
    record TextContent(String source) implements CellContent {
        public TextContent { java.util.Objects.requireNonNull(source); }
    }
    record NumericContent(double value) implements CellContent {
        public NumericContent { FormulaTerm.finite(value); }
        public String source() { return Double.toString(value); }
    }
    record FormulaContent(String source, FormulaTerm expression) implements CellContent {
        public FormulaContent { java.util.Objects.requireNonNull(source); java.util.Objects.requireNonNull(expression); }
    }
}
