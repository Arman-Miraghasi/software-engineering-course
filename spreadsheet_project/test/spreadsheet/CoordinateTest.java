package spreadsheet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {
    @Test
    void normalizeLowercaseAndConvertMultipleColumnLetters() {
        Coordinate coordinate = Coordinate.parse("aa12");
        assertEquals(new Coordinate(27, 12), coordinate);
        assertEquals("AA12", coordinate.toString());
    }

    @Test
    void rejectInvalidCoordinates() {
        assertThrows(SpreadsheetException.class, () -> Coordinate.parse("A01"));
        assertThrows(SpreadsheetException.class, () -> Coordinate.parse("0A"));
        assertThrows(SpreadsheetException.class, () -> Coordinate.parse("ZZZZZZZZZZZZ1"));
    }
}
