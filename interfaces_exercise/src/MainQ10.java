import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainQ10 {
    public static void main(String[] args) {
        List<PersonStandard> persons = new ArrayList<>(Arrays.asList(
                new PersonStandard("John", "Smith"),
                new PersonStandard("Alice", "Brown"),
                new PersonStandard("David", "Smith"),
                new PersonStandard("Bob", "Adams"),
                new PersonStandard("Charlie", "Brown")
        ));

        Collections.sort(persons);

        System.out.println("--- Sorted Persons ---");
        for (PersonStandard p : persons) {
            p.print_complete_name();
        }

        List<RectangleStandard> rectangles = new ArrayList<>(Arrays.asList(
                new RectangleStandard(10, 5),   // area = 50.0
                new RectangleStandard(2, 3),    // area = 6.0
                new RectangleStandard(7, 8),    // area = 56.0
                new RectangleStandard(4, 4),    // area = 16.0
                new RectangleStandard(1, 20)    // area = 20.0
        ));

        Collections.sort(rectangles);

        System.out.println("\n--- Sorted Rectangles ---");
        for (RectangleStandard r : rectangles) {
            r.printInfo();
        }
    }
}