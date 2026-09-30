public class Person implements Sortable {
    private String name;
    private String surname;

    public Person(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public void print_complete_name() {
        System.out.println("Full Name: " + this.name + " " + this.surname);
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public boolean isBigger(Object o1, Object o2) {
        Person p1 = (Person) o1;
        Person p2 = (Person) o2;

        int surnameComp = p1.getSurname().compareTo(p2.getSurname());
        if (surnameComp != 0) {
            return surnameComp > 0;
        }
        return p1.getName().compareTo(p2.getName()) > 0;
    }
}