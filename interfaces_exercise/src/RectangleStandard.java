public class RectangleStandard implements Comparable<RectangleStandard> {
    private double width;
    private double height;

    public RectangleStandard(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getArea() {
        return this.width * this.height;
    }

    @Override
    public int compareTo(RectangleStandard other) {
        return Double.compare(this.getArea(), other.getArea());
    }

    public void printInfo() {
        System.out.println("Rectangle [width=" + width + ", height=" + height + ", area=" + getArea() + "]");
    }
}