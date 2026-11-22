package spreadsheet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class S2vRepositoryTest {
    @TempDir
    Path directory;
    private S2vRepository repository;
    private Path file;

    @BeforeEach
    void setUp() throws IOException {
        repository = new S2vRepository();
        file = directory.resolve("sheet.s2v");
        Files.writeString(file, "=C1+C2;4;1\n;;2\n;TOTAL;=A1+B1\n;=SUMA(A1,C1,2)\n");
    }

    @Test
    void loadAssignmentExampleAndRestoreArgumentSeparators() throws IOException {
        Spreadsheet sheet = repository.load(file, new ShuntingYardParser());
        assertEquals(3, sheet.number("A1"), 1e-9);
        assertEquals(7, sheet.number("C3"), 1e-9);
        assertEquals(6, sheet.number("B4"), 1e-9);
        assertEquals("=SUMA(A1;C1;2)", sheet.inspect("B4").content().source());
    }

    @Test
    void roundTripPreservesFormulaSourceAndValues() throws IOException {
        Spreadsheet sheet = repository.load(file, new ShuntingYardParser());
        Path copy = directory.resolve("copy.s2v");
        repository.save(sheet, copy, false);
        Spreadsheet loaded = repository.load(copy, new ProvidedParser());
        assertEquals(7, loaded.number("C3"), 1e-9);
        assertEquals(6, loaded.number("B4"), 1e-9);
        assertTrue(Files.readString(copy).contains("=SUMA(A1,C1,2)"));
    }

    @Test
    void rejectAmbiguousTextBeforeChangingExistingFile() throws IOException {
        Spreadsheet sheet = repository.load(file, new ShuntingYardParser());
        String original = Files.readString(file);
        sheet.setText("A9", "1");
        assertThrows(SpreadsheetException.class, () -> repository.save(sheet, file, true));
        assertEquals(original, Files.readString(file));
    }

    @Test
    void saveWithoutOverwritePreservesExistingFile() throws IOException {
        String original = Files.readString(file);
        assertThrows(FileAlreadyExistsException.class, () -> repository.save(new Spreadsheet(), file, false));
        assertEquals(original, Files.readString(file));
    }

    @Test
    void failedOpenPreservesActiveSessionAndUnsavedChanges() throws IOException {
        SpreadsheetFacade app = new SpreadsheetFacade();
        app.create();
        app.set("A1", "number", "42");
        Path invalid = directory.resolve("bad.s2v");
        Files.writeString(invalid, "=B1;=A1\n");
        assertThrows(SpreadsheetException.class, () -> app.open(invalid));
        assertEquals(42, app.sheet().number("A1"), 1e-9);
        assertTrue(app.hasUnsavedChanges());
    }

    @Test
    void savingSessionClearsUnsavedChanges() throws IOException {
        SpreadsheetFacade app = new SpreadsheetFacade();
        app.create();
        app.set("A1", "number", "42");
        assertTrue(app.hasUnsavedChanges());
        app.save(directory.resolve("session.s2v"), false);
        assertFalse(app.hasUnsavedChanges());
    }
}
