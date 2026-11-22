package spreadsheet;

import java.util.*;
import static spreadsheet.CellContent.*;

/** Information Expert for contents and their acyclic dependency graph. */
public final class Spreadsheet {
    private final FormulaParser parser;
    private final Map<Coordinate, CellContent> cells = new TreeMap<>();
    private final Map<Coordinate, Set<Coordinate>> dependencies = new HashMap<>(), dependents = new HashMap<>();
    private final Map<Coordinate, Result> results = new HashMap<>();
    private long evaluationCount;
    private record Result(double value, String error) { }
    public Spreadsheet() { this(new ShuntingYardParser()); }
    public Spreadsheet(FormulaParser parser) { this.parser = Objects.requireNonNull(parser); }
    public void setText(String address, String text) { set(Coordinate.parse(address), new TextContent(text)); }
    public void setNumber(String address, double value) { set(Coordinate.parse(address), new NumericContent(value)); }
    public void setFormula(String address, String formula) {
        Coordinate coordinate = Coordinate.parse(address);
        set(coordinate, new FormulaContent(formula, parser.parse(formula)));
    }
    public void clear(String address) { setText(address, ""); }
    public Cell inspect(String address) {
        Coordinate c = Coordinate.parse(address); return new Cell(c, cells.getOrDefault(c, new TextContent("")));
    }
    public Map<Coordinate, CellContent> contents() { return Collections.unmodifiableMap(new TreeMap<>(cells)); }
    public long evaluationCount() { return evaluationCount; }
    private void set(Coordinate target, CellContent content) {
        Set<Coordinate> refs = content instanceof FormulaContent f ? f.expression().references() : Set.of();
        // Validate before committing any part of the edit.
        for (Coordinate ref : refs) {
            Set<Coordinate> visited = new HashSet<>();
            Deque<Coordinate> pending = new ArrayDeque<>(); pending.push(ref);
            while (!pending.isEmpty()) {
                Coordinate current = pending.pop();
                if (current.equals(target)) throw new SpreadsheetException("Circular dependency at " + target);
                if (visited.add(current)) pending.addAll(dependencies.getOrDefault(current, Set.of()));
            }
        }
        for (Coordinate old : dependencies.getOrDefault(target, Set.of())) {
            Set<Coordinate> reverse = dependents.get(old);
            reverse.remove(target); if (reverse.isEmpty()) dependents.remove(old);
        }
        if (refs.isEmpty()) dependencies.remove(target); else dependencies.put(target, refs);
        refs.forEach(ref -> dependents.computeIfAbsent(ref, k -> new HashSet<>()).add(target));
        if (content instanceof TextContent t && t.source().isEmpty()) cells.remove(target); else cells.put(target, content);
        Set<Coordinate> affected = new HashSet<>(); Deque<Coordinate> pending = new ArrayDeque<>(); pending.add(target);
        while (!pending.isEmpty()) {
            Coordinate c = pending.pop();
            if (affected.add(c)) pending.addAll(dependents.getOrDefault(c, Set.of()));
        }
        affected.forEach(results::remove);
        affected.forEach(c -> { if (cells.get(c) instanceof FormulaContent) result(c, new HashSet<>()); });
    }
    private Result result(Coordinate c, Set<Coordinate> active) {
        if (results.containsKey(c)) return results.get(c);
        if (!active.add(c)) return new Result(0, "Circular dependency at " + c);
        Result result;
        try {
            CellContent content = cells.get(c);
            double value;
            if (content == null) value = 0;
            else if (content instanceof NumericContent n) value = n.value();
            else if (content instanceof TextContent t) value = t.source().isEmpty() ? 0 : FormulaTerm.finite(Double.parseDouble(t.source()));
            else {
                evaluationCount++;
                value = ((FormulaContent) content).expression().evaluate(ref -> {
                    Result r = result(ref, active);
                    if (r.error != null) throw new SpreadsheetException(ref + ": " + r.error);
                    return r.value;
                });
            }
            result = new Result(value, null);
        } catch (NumberFormatException e) { result = new Result(0, "Text is not a number"); }
        catch (SpreadsheetException e) { result = new Result(0, e.getMessage()); }
        finally { active.remove(c); }
        results.put(c, result); return result;
    }
    public double number(String address) {
        Result result = result(Coordinate.parse(address), new HashSet<>());
        if (result.error != null) throw new SpreadsheetException(address + ": " + result.error);
        return result.value;
    }
    public String text(String address) {
        CellContent content = inspect(address).content();
        return content instanceof TextContent t ? t.source() : Double.toString(number(address));
    }
    public Map<Coordinate, String> errors() {
        Map<Coordinate, String> errors = new TreeMap<>();
        cells.forEach((c, content) -> {
            if (content instanceof FormulaContent) {
                Result r = result(c, new HashSet<>()); if (r.error != null) errors.put(c, r.error);
            }
        });
        return Collections.unmodifiableMap(errors);
    }
}
