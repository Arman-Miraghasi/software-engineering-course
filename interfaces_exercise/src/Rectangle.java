public class Rectangle implements Sortable {
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getArea() {
        return this.width * this.height;
    }

    @Override
    public boolean isBigger(Object o1, Object o2) {
        Rectangle r1 = (Rectangle) o1;
        Rectangle r2 = (Rectangle) o2;
        return r1.getArea() > r2.getArea();
    }

    public void printInfo() {
        System.out.println("Rectangle [width=" + width + ", height=" + height + ", area=" + getArea() + "]");
    }
}