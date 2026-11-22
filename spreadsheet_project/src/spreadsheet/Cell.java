package spreadsheet;

/** Read-only snapshot exposed to callers; mutations go through Spreadsheet. */
public record Cell(Coordinate coordinate, CellContent content) { }
