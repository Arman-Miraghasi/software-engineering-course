package spreadsheet;

/** A user-visible domain or formula error. */
public class SpreadsheetException extends RuntimeException {
    public SpreadsheetException(String message) { super(message); }
}
