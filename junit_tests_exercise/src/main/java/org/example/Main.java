package org.example;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Library library = new Library();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n1. Add book\n2. Remove book\n3. Search by author\n4. Search by year\n0. Exit");
                String choice = readLine(scanner, "Choose an option: ");
                if (choice == null || choice.equals("0")) {
                    System.out.println("Goodbye!");
                    return;
                }
                try {
                    switch (choice) {
                        case "1" -> {
                            Book book = readBook(scanner);
                            library.addBook(book);
                            System.out.println("Added: " + book);
                        }
                        case "2" -> {
                            Book book = readBook(scanner);
                            if (library.removeBook(book)) {
                                System.out.println("Removed: " + book);
                            } else {
                                System.out.println("Book not found.");
                            }
                        }
                        case "3" -> printBooks(library.getBooksByAuthor(readRequiredLine(scanner, "Author: ")));
                        case "4" -> printBooks(library.getBooksByYear(readYear(scanner)));
                        default -> System.out.println("Invalid option. Choose 0 to 4.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Year must be a whole number. Please try again.");
                } catch (IllegalStateException e) {
                    System.out.println("\nInput ended. Goodbye!");
                    return;
                }
            }
        }
    }

    private static Book readBook(Scanner scanner) {
        String title = readRequiredLine(scanner, "Title: ");
        String author = readRequiredLine(scanner, "Author: ");
        return new Book(title, author, readYear(scanner));
    }

    private static int readYear(Scanner scanner) {
        return Integer.parseInt(readRequiredLine(scanner, "Year: "));
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        } else {
            return null;
        }
    }

    private static String readRequiredLine(Scanner scanner, String prompt) {
        String value = readLine(scanner, prompt);
        if (value == null) throw new IllegalStateException("Input ended.");
        return value;
    }

    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            for (Book book : books) System.out.println(book);
        }
    }
}
