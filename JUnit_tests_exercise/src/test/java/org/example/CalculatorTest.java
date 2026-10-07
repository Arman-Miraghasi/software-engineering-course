package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {
    Calculator calculator = new Calculator();

    @Test
    void add() {
        assertEquals(8, calculator.add(5, 3));
    }

    @Test
    void subtract() {
        assertEquals(2, calculator.subtract(5, 3));
    }

    @Test
    void multiply() {
        assertEquals(15, calculator.multiply(5, 3));
    }

    @Test
    void divide() {
        assertEquals(1, calculator.divide(5, 3));
    }

    @Test
    void divideByZero() {
        assertThrows(IllegalArgumentException.class, () -> calculator.divide(5, 0));
    }

    @Test
    void testCompare() {
        assertEquals("5 is greater than 3", calculator.compare(5, 3));
    }

    @Test
    void testCompareEqual() {
        assertEquals("5 is equal to 5", calculator.compare(5, 5));
    }

    @Test
    void testCompareLess() {
        assertEquals("3 is less than 5", calculator.compare(3, 5));
    }
}
