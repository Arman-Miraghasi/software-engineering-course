public class MainQ8 {
    public static void main(String[] args) {
        Sorter sorter = new Sorter();

        Person[] persons = {
                new Person("John", "Smith"),
                new Person("Alice", "Brown"),
                new Person("David", "Smith"),
                new Person("Bob", "Adams"),
                new Person("Charlie", "Brown")
        };

        sorter.sort(persons);

        System.out.println("--- Sorted Persons ---");
        for (Person p : persons) {
            p.print_complete_name();
        }

        Rectangle[] rectangles = {
                new Rectangle(10, 5),
                new Rectangle(2, 3),
                new Rectangle(7, 8),
                new Rectangle(4, 4),
                new Rectangle(1, 20)
        };

        sorter.sort(rectangles);

        System.out.println("\n--- Sorted Rectangles ---");
        for (Rectangle r : rectangles) {
            r.printInfo();
        }
    }
}