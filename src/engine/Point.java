package engine;

// This actually is useless class, since Java API contains
// a class called Point that provides the functionality we need.
// But I kept it here anyway.
public class Point {
    public int x, y;        // no place for data hiding, or encapsulation here -> no useless getters/setters

    public Point( int x, int y ) {
        this.x = x;
        this.y = y;
    }

    public Point( Point other ) {       // copy constructor
        this.x = other.x;
        this.y = other.y;
    }
}
