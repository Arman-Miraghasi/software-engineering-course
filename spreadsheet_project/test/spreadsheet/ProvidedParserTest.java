package spreadsheet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProvidedParserTest {
    private Spreadsheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Spreadsheet(new ProvidedParser());
    }

    @Test
    void evaluateBasicArithmetic() {
        sheet.setFormula("A1", "=1+2");
        sheet.setFormula("A2", "=3*5+4");
        sheet.setFormula("A3", "=(3+5)*4-8/2");
        assertEquals(3, sheet.number("A1"), 1e-9);
        assertEquals(19, sheet.number("A2"), 1e-9);
        assertEquals(28, sheet.number("A3"), 1e-9);
    }

    @Test
    void resolveReferencesAndRecalculateAfterEdit() {
        sheet.setFormula("A1", "=1+2");
        sheet.setNumber("B1", 4);
        sheet.setFormula("C1", "=A1+B1");
        assertEquals(7, sheet.number("C1"), 1e-9);
        sheet.setNumber("B1", 8);
        assertEquals(11, sheet.number("C1"), 1e-9);
    }

    @Test
    void rejectCyclesAndPreservePreviousContent() {
        sheet.setNumber("A1", 3);
        sheet.setFormula("B1", "=A1+1");
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("A1", "=B1"));
        assertEquals(3, sheet.number("A1"), 1e-9);
        assertEquals(4, sheet.number("B1"), 1e-9);
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("Z1", "=Z1"));
        assertEquals("", sheet.inspect("Z1").content().source());
    }

    @Test
    void evaluateSupportedNestedFunction() {
        sheet.setFormula("A1", "=SUMA(1;2;MAX(3;5))");
        assertEquals(8, sheet.number("A1"), 1e-9);
    }

    @Test
    void rejectKnownUnsupportedFunctionArguments() {
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("A1", "=PROMEDIO(2;MIN(3;5);7)"));
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("A1", "=SUMA(A2:B3)"));
    }
}
