public class PersonStandard implements Comparable<PersonStandard> {
    private String name;
    private String surname;

    public PersonStandard(String name, String surname) {
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
    public int compareTo(PersonStandard other) {
        int surnameComparison = this.surname.compareTo(other.surname);
        if (surnameComparison != 0) {
            return surnameComparison;
        }
        return this.name.compareTo(other.name);
    }
}