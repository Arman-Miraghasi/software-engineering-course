public class Sorter {
    public void sort(Sortable[] elements) {
        for (int i = elements.length - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                // We ask an element to compare elements[j] and elements[j + 1]
                if (elements[j].isBigger(elements[j], elements[j + 1])) {
                    Sortable temp = elements[j];
                    elements[j] = elements[j + 1];
                    elements[j + 1] = temp;
                }
            }
        }
    }
}