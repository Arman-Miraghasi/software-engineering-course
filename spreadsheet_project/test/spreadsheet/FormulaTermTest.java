package spreadsheet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormulaTermTest {
    @Test
    void addHandBuiltLiterals() {
        var expression = new FormulaTerm.ArithmeticOperation('+',
                new FormulaTerm.NumberLiteral(1), new FormulaTerm.NumberLiteral(2));
        assertEquals(3, expression.evaluate(c -> 0), 1e-9);
    }

    @Test
    void evaluateHandBuiltMultiplicationAndAddition() {
        var expression = new FormulaTerm.ArithmeticOperation('+',
                new FormulaTerm.ArithmeticOperation('*', new FormulaTerm.NumberLiteral(3),
                        new FormulaTerm.NumberLiteral(5)), new FormulaTerm.NumberLiteral(4));
        assertEquals(19, expression.evaluate(c -> 0), 1e-9);
    }

    @Test
    void divideByZeroThrowsError() {
        var expression = new FormulaTerm.ArithmeticOperation('/',
                new FormulaTerm.NumberLiteral(1), new FormulaTerm.NumberLiteral(0));
        assertThrows(SpreadsheetException.class, () -> expression.evaluate(c -> 0));
    }
}
