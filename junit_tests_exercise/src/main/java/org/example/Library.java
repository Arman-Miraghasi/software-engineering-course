package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Library {
    private List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        books.add(Objects.requireNonNull(book, "Book must not be null."));
    }

    public boolean removeBook(Book book) {
        return books.remove(book);
    }

    public List<Book> getBooksByAuthor(String author) {
        List<Book> matches = new ArrayList<>();
        for (Book book : books) {
            if (Objects.equals(book.getAuthor(), author)) matches.add(book);
        }
        return matches;
    }

    public List<Book> getBooksByYear(int year) {
        List<Book> matches = new ArrayList<>();
        for (Book book : books) {
            if (book.getYear() == year) matches.add(book);
        }
        return matches;
    }
}
