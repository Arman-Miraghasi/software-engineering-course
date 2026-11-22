package spreadsheet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShuntingYardParserTest {
    private Spreadsheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Spreadsheet(new ShuntingYardParser());
    }

    @Test
    void evaluateSimpleAddition() {
        sheet.setFormula("A1", "=1+2");
        assertEquals(3, sheet.number("A1"), 1e-9);
    }

    @Test
    void multiplicationTakesPrecedenceOverAddition() {
        sheet.setFormula("A1", "=3*5+4");
        assertEquals(19, sheet.number("A1"), 1e-9);
    }

    @Test
    void parenthesesOverridePrecedence() {
        sheet.setFormula("A1", "=(3+5)*4-8/2");
        assertEquals(28, sheet.number("A1"), 1e-9);
    }

    @Test
    void evaluateUnarySignsAndScientificLiterals() {
        sheet.setFormula("A1", "=--2+-3*-2");
        assertEquals(8, sheet.number("A1"), 1e-9);
        sheet.setFormula("A1", "=1e2+.5");
        assertEquals(100.5, sheet.number("A1"), 1e-9);
    }

    @Test
    void evaluateAllFunctionsWithNestedArguments() {
        sheet.setFormula("A1", "=SUMA(1;2;MAX(3;5))");
        sheet.setFormula("A2", "=PROMEDIO(2;MIN(3;5);7)");
        assertEquals(8, sheet.number("A1"), 1e-9);
        assertEquals(4, sheet.number("A2"), 1e-9);
    }

    @Test
    void evaluateInclusiveRangeAndMixedArguments() {
        sheet.setNumber("E1", 1);
        sheet.setNumber("E2", 2);
        sheet.setNumber("F1", 3);
        sheet.setNumber("F2", 4);
        sheet.setNumber("C1", 11);
        sheet.setFormula("G1", "=SUMA(E1:F2;5)");
        sheet.setFormula("G2", "=SUMA(E1:F2;PROMEDIO(2;4);C1;27)");
        assertEquals(15, sheet.number("G1"), 1e-9);
        assertEquals(51, sheet.number("G2"), 1e-9);
        sheet.setNumber("F2", 8);
        assertEquals(19, sheet.number("G1"), 1e-9);
        assertEquals(55, sheet.number("G2"), 1e-9);
    }

    @Test
    void evaluateComplexAssignmentExpression() {
        sheet.setNumber("A1", 3);
        sheet.setNumber("C1", 11);
        sheet.setNumber("E1", 1);
        sheet.setNumber("E2", 2);
        sheet.setNumber("F1", 3);
        sheet.setNumber("F2", 4);
        sheet.setFormula("H1", "=1+A1*((SUMA(E1:F2;PROMEDIO(E1:F2);C1;27)/4)+(E2-F2))");
        assertEquals(32.875, sheet.number("H1"), 1e-9);
    }

    @Test
    void rejectMalformedFormulasWithoutReplacingPreviousContent() {
        sheet.setNumber("C9", 42);
        for (String formula : List.of("=1+", "=BOGUS(1)", "=SUMA()", "=SUMA(F2:E1)", "=1/0+",
                "=1 2", "=2(3)", "=()", "=(1", "=1)", "=SUMA(1;;2)", "=SUMA(1;)",
                "=A1:B2", "=SUMA(A1:B2+1)", "=1@2", "=1,2", "=A0", "=1e309")) {
            assertThrows(SpreadsheetException.class, () -> sheet.setFormula("C9", formula), formula);
            assertEquals(42, sheet.number("C9"), 1e-9, formula);
        }
    }
}
