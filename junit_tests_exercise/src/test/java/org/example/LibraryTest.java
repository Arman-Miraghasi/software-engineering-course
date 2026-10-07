package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {
    private Library library;
    private Book novel;

    @BeforeEach
    void setUp() {
        library = new Library();
        novel = new Book("1984", "George Orwell", 1949);
    }

    @Test
    void addBook() {
        library.addBook(novel);
        assertEquals(List.of(novel), library.getBooksByAuthor("George Orwell"));
        assertEquals(List.of(novel), library.getBooksByYear(1949));
    }

    @Test
    void rejectNullBook() {
        assertThrows(NullPointerException.class, () -> library.addBook(null));
    }

    @Test
    void removeBook() {
        library.addBook(novel);
        assertTrue(library.removeBook(new Book("1984", "George Orwell", 1949)));
        assertTrue(library.getBooksByAuthor("George Orwell").isEmpty());
        assertTrue(library.getBooksByYear(1949).isEmpty());
    }

    @Test
    void removingNonExistingBook() {
        library.addBook(novel);
        assertFalse(library.removeBook(new Book("Animal Farm", "George Orwell", 1945)));
        assertEquals(List.of(novel), library.getBooksByYear(1949));
    }

    @Test
    void searchByAuthorReturnsAllAndOnlyMatches() {
        Book otherNovel = new Book("Animal Farm", "George Orwell", 1945);
        library.addBook(novel);
        library.addBook(new Book("Dune", "Frank Herbert", 1965));
        library.addBook(otherNovel);
        assertEquals(List.of(novel, otherNovel), library.getBooksByAuthor("George Orwell"));
    }

    @Test
    void searchByYearReturnsAllAndOnlyMatches() {
        Book sameYear = new Book("Another Book", "Another Author", 1949);
        library.addBook(novel);
        library.addBook(new Book("Dune", "Frank Herbert", 1965));
        library.addBook(sameYear);
        assertEquals(List.of(novel, sameYear), library.getBooksByYear(1949));
    }

    @Test
    void searchByAuthorReturnEmptyListsWhenNothingMatches() {
        library.addBook(novel);
        assertTrue(library.getBooksByAuthor("Unknown").isEmpty());
    }

    @Test
    void searchByYearReturnEmptyListsWhenNothingMatches() {
        library.addBook(novel);
        assertTrue(library.getBooksByYear(2000).isEmpty());
    }

    @Test
    void emptyLibraryReturnsEmptyLists() {
        assertTrue(library.getBooksByAuthor("George Orwell").isEmpty());
        assertTrue(library.getBooksByYear(1949).isEmpty());
    }

    @Test
    void changingSearchResultsDoesNotChangeLibrary() {
        library.addBook(novel);
        library.getBooksByAuthor("George Orwell").clear();
        library.getBooksByYear(1949).clear();
        assertEquals(List.of(novel), library.getBooksByYear(1949));
    }

    @Test
    void removingDuplicateRemovesOnlyOneCopy() {
        library.addBook(novel);
        library.addBook(new Book("1984", "George Orwell", 1949));
        assertTrue(library.removeBook(novel));
        assertEquals(List.of(novel), library.getBooksByYear(1949));
    }
}
