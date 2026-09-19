public class Main {
    public static void main(String[] args) {
        Worker[] workers = new Worker[100];
        int workerCount = 0;

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("Worker " + (i + 1), 2500.0, 500.0);
            workerCount++;
        }
        double globalTotalSalary = 0.0;

        System.out.printf("%-15s %-15s %-15s %-15s%n", "Name", "Base Salary", "Complement", "Total Salary");
        System.out.println("------------------------------------------------------------------");

        for (int i = 0; i < workerCount; i++) {
            Worker worker = workers[i];
            double totalSalary = worker.getTotalSalary();
            globalTotalSalary += totalSalary;

            System.out.printf("%-15s %-15.2f %-15.2f %-15.2f%n",
                    worker.getName(),
                    worker.getBaseSalary(),
                    worker.getComplement(),
                    totalSalary);
        }

        System.out.println("------------------------------------------------------------------");
        System.out.printf("Global Total Salary for all workers: %.2f%n", globalTotalSalary);
    }
}