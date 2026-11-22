package spreadsheet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpreadsheetTest {
    private Spreadsheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new Spreadsheet();
    }

    @Test
    void blankCellReturnsEmptyTextAndZero() {
        assertEquals("", sheet.text("ZZ99"));
        assertEquals(0, sheet.number("ZZ99"), 1e-9);
    }

    @Test
    void numericTextCanBeReadAsNumber() {
        sheet.setText("AA12", "12.5");
        assertEquals("12.5", sheet.text("AA12"));
        assertEquals(12.5, sheet.number("AA12"), 1e-9);
    }

    @Test
    void nonnumericTextCannotBeReadAsNumber() {
        sheet.setText("AA12", "Hello");
        assertEquals("Hello", sheet.text("AA12"));
        assertThrows(SpreadsheetException.class, () -> sheet.number("AA12"));
    }

    @Test
    void referencesFollowChangedInputs() {
        sheet.setNumber("A1", 3);
        sheet.setNumber("B1", 4);
        sheet.setFormula("C1", "=A1+B1");
        assertEquals(7, sheet.number("C1"), 1e-9);
        sheet.setNumber("B1", 8);
        assertEquals(11, sheet.number("C1"), 1e-9);
    }

    @Test
    void rejectsDirectCycleWithoutChangingCell() {
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("A1", "=A1+1"));
        assertEquals("", sheet.inspect("A1").content().source());
    }

    @Test
    void rejectsIndirectCycleAndPreservesPreviousValues() {
        sheet.setFormula("A1", "=A2");
        sheet.setFormula("A2", "=A3");
        sheet.setNumber("A3", 10);
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("A3", "=A1"));
        assertEquals(10, sheet.number("A3"), 1e-9);
        assertEquals(10, sheet.number("A1"), 1e-9);
    }

    @Test
    void replacingFormulaRemovesItsOldDependencies() {
        sheet.setFormula("A1", "=A2");
        sheet.setFormula("A2", "=A3");
        sheet.setNumber("A2", 4);
        sheet.setFormula("A3", "=A1");
        assertEquals(4, sheet.number("A3"), 1e-9);
    }

    @Test
    void rejectsCycleIntroducedThroughRange() {
        sheet.setNumber("E1", 1);
        sheet.setFormula("G1", "=SUMA(E1:F2)");
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("E1", "=G1"));
        assertThrows(SpreadsheetException.class, () -> sheet.setFormula("E1", "=SUMA(E1:F2)"));
        assertEquals(1, sheet.number("E1"), 1e-9);
    }

    @Test
    void fixingReferencedTextRecoversDependentFormula() {
        sheet.setText("A1", "Hello");
        sheet.setFormula("B1", "=A1+1");
        assertThrows(SpreadsheetException.class, () -> sheet.number("B1"));
        sheet.setNumber("A1", 2);
        assertEquals(3, sheet.number("B1"), 1e-9);
        assertTrue(sheet.errors().isEmpty());
    }

    @Test
    void unrelatedEditKeepsCachedFormulaResults() {
        sheet.setFormula("A1", "=1+2");
        long before = sheet.evaluationCount();
        sheet.setNumber("Z1", 100);
        assertEquals(3, sheet.number("A1"), 1e-9);
        assertEquals(before, sheet.evaluationCount());
    }

    @Test
    void affectedFormulasAreEvaluatedOnceIncludingSharedDependencies() {
        sheet.setNumber("A1", 1);
        sheet.setFormula("B1", "=A1+1");
        sheet.setFormula("C1", "=A1+2");
        sheet.setFormula("D1", "=B1+C1");
        sheet.setFormula("E1", "=A1+A1");
        long before = sheet.evaluationCount();
        sheet.setNumber("A1", 2);
        assertEquals(4, sheet.evaluationCount() - before);
        assertEquals(7, sheet.number("D1"), 1e-9);
        assertEquals(4, sheet.number("E1"), 1e-9);
    }

    @Test
    void clearingCellUpdatesItsDependents() {
        sheet.setNumber("A1", 2);
        sheet.setFormula("B1", "=A1+A1");
        sheet.clear("A1");
        assertEquals(0, sheet.number("B1"), 1e-9);
        assertEquals("", sheet.text("A1"));
    }
}
