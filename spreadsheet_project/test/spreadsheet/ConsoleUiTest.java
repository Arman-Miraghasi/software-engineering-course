package spreadsheet;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUiTest {
    @Test
    void editReadDisplayAndConfirmExit() {
        String input = "1\n5\nA1\nnumber\n2\n5\nB1\nformula\n=A1*3\n9\nB1\n10\n4\nn\n4\ny\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ConsoleUi ui = new ConsoleUi(new SpreadsheetFacade(),
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), new PrintStream(buffer));
        ui.run();
        String output = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("6.0"));
        assertTrue(output.contains("Discard unsaved changes?"));
        assertFalse(output.contains("Error:"));
    }
}
