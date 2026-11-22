package spreadsheet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static spreadsheet.CellContent.*;

/** Persistence service. S2V has no quoting: ambiguous text is rejected before writing. */
public final class S2vRepository {
    public Spreadsheet load(Path path, FormulaParser parser) throws IOException {
        Spreadsheet sheet = new Spreadsheet(parser);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (int row = 0; row < lines.size(); row++) {
            String[] fields = lines.get(row).split(";", -1);
            for (int col = 0; col < fields.length; col++) {
                String field = fields[col];
                if (field.isEmpty()) continue;
                String address = new Coordinate(col + 1, row + 1).toString();
                try {
                    if (field.startsWith("=")) sheet.setFormula(address, field.replace(',', ';'));
                    else if (isNumber(field)) sheet.setNumber(address, Double.parseDouble(field));
                    else sheet.setText(address, field);
                } catch (SpreadsheetException e) { throw new SpreadsheetException("Loading " + address + ": " + e.getMessage()); }
            }
        }
        return sheet;
    }
    private static boolean isNumber(String text) {
        return text.matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?");
    }
    public void save(Spreadsheet sheet, Path path, boolean overwrite) throws IOException {
        Map<Coordinate, CellContent> cells = sheet.contents();
        for (var entry : cells.entrySet()) {
            if (entry.getValue() instanceof TextContent t &&
                    (t.source().contains(";") || t.source().contains("\n") || t.source().contains("\r") ||
                     t.source().startsWith("=") || isNumber(t.source())))
                throw new SpreadsheetException("Text at " + entry.getKey() + " cannot be represented unambiguously in S2V");
        }
        StringBuilder output = new StringBuilder();
        int lastRow = cells.keySet().stream().mapToInt(Coordinate::row).max().orElse(0);
        Map<Integer, Integer> lastColumns = new HashMap<>();
        cells.keySet().forEach(c -> lastColumns.merge(c.row(), c.column(), Math::max));
        for (int row = 1; row <= lastRow; row++) {
            int lastColumn = lastColumns.getOrDefault(row, 0);
            for (int col = 1; col <= lastColumn; col++) {
                if (col > 1) output.append(';');
                CellContent content = cells.get(new Coordinate(col, row));
                if (content != null) output.append(content instanceof FormulaContent ? content.source().replace(';', ',') : content.source());
            }
            output.append('\n');
        }
        Path absolute = path.toAbsolutePath();
        Path temp = Files.createTempFile(absolute.getParent(), ".spreadsheet-", ".s2v");
        try {
            Files.writeString(temp, output, StandardCharsets.UTF_8);
            // Move without REPLACE_EXISTING enforces the caller's overwrite decision.
            if (overwrite) Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            else Files.move(temp, absolute);
        } finally { Files.deleteIfExists(temp); }
    }
}
