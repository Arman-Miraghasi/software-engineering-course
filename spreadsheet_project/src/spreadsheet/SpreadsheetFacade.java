package spreadsheet;

import java.io.IOException;
import java.nio.file.Path;

/** GRASP Controller / Facade for a single active editing session. */
public final class SpreadsheetFacade {
    private final FormulaParser parser;
    private final S2vRepository repository = new S2vRepository();
    private Spreadsheet active;
    private boolean dirty;
    public SpreadsheetFacade() { this(new ShuntingYardParser()); }
    public SpreadsheetFacade(FormulaParser parser) { this.parser = parser; }
    public boolean hasUnsavedChanges() { return dirty; }
    public void create() { active = new Spreadsheet(parser); dirty = false; }
    public Spreadsheet sheet() {
        if (active == null) throw new SpreadsheetException("Create or open a spreadsheet first");
        return active;
    }
    public void open(Path path) throws IOException {
        Spreadsheet loaded = repository.load(path, parser); active = loaded; dirty = false;
    }
    public void save(Path path, boolean overwrite) throws IOException { repository.save(sheet(), path, overwrite); dirty = false; }
    public void set(String address, String type, String source) {
        switch (type.toLowerCase(java.util.Locale.ROOT)) {
            case "text", "t" -> sheet().setText(address, source);
            case "number", "n" -> {
                try { sheet().setNumber(address, Double.parseDouble(source)); }
                catch (NumberFormatException e) { throw new SpreadsheetException("Invalid number: " + source); }
            }
            case "formula", "f" -> sheet().setFormula(address, source);
            default -> throw new SpreadsheetException("Choose text, number, or formula");
        }
        dirty = true;
    }
    public void clear(String address) { sheet().clear(address); dirty = true; }
}
