package org.example;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void testBookEquality() {
        Book b1 = new Book("1984", "George Orwell", 1949);
        Book b2 = new Book("1984", "George Orwell", 1949);

        assertThat(b1, samePropertyValuesAs(b2));
    }

    @Test
    void gettersReturnConstructorValues() {
        Book book = new Book("1984", "George Orwell", 1949);
        assertEquals("1984", book.getTitle());
        assertEquals("George Orwell", book.getAuthor());
        assertEquals(1949, book.getYear());
    }

    @Test
    void equalBooksHaveEqualHashCodes() {
        Book first = new Book("1984", "George Orwell", 1949);
        Book second = new Book("1984", "George Orwell", 1949);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void differentPropertiesMakeBooksUnequal() {
        Book book = new Book("1984", "George Orwell", 1949);
        assertNotEquals(book, new Book("Another Title", "George Orwell", 1949));
        assertNotEquals(book, new Book("1984", "Another Author", 1949));
        assertNotEquals(book, new Book("1984", "George Orwell", 1950));
        assertNotEquals(book, null);
        assertNotEquals(book, "1984");
    }

    @Test
    void toStringIncludesBookDetails() {
        assertEquals("1984 by George Orwell (1949)",
                new Book("1984", "George Orwell", 1949).toString());
    }
}
