package spreadsheet;

import java.util.Locale;
import java.util.regex.*;

/** Immutable, one-based cell address. */
public record Coordinate(int column, int row) implements Comparable<Coordinate> {
    public Coordinate {
        if (column < 1 || row < 1) throw new SpreadsheetException("Coordinates must be positive");
    }
    public static Coordinate parse(String input) {
        Matcher m = Pattern.compile("([A-Z]+)([1-9][0-9]*)").matcher(input.toUpperCase(Locale.ROOT));
        if (!m.matches()) throw new SpreadsheetException("Invalid coordinate: " + input);
        try {
            int column = 0;
            for (char c : m.group(1).toCharArray()) column = Math.addExact(Math.multiplyExact(column, 26), c - 'A' + 1);
            return new Coordinate(column, Integer.parseInt(m.group(2)));
        } catch (ArithmeticException | NumberFormatException e) {
            throw new SpreadsheetException("Coordinate too large: " + input);
        }
    }
    public String columnName() {
        StringBuilder result = new StringBuilder();
        for (int n = column; n > 0; n = (n - 1) / 26) result.append((char) ('A' + (n - 1) % 26));
        return result.reverse().toString();
    }
    @Override public String toString() { return columnName() + row; }
    @Override public int compareTo(Coordinate other) {
        int r = Integer.compare(row, other.row);
        return r == 0 ? Integer.compare(column, other.column) : r;
    }
}
