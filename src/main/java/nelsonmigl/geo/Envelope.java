package nelsonmigl.geo;

import javafx.scene.chart.PieChart;

public class Envelope {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public Envelope(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public Envelope(Coordinate min, Coordinate max) {
        this(min.x, max.x, min.y, max.y);
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public boolean contains(double x, double y){
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
    }

    public boolean contains(Coordinate coordinate) {
        return contains(coordinate.x, coordinate.y);
    }
}
