package student;

public class Vector2D
{
    public int x;
    public int y;

    public Vector2D() { x = 0; y = 0; }
    public Vector2D(int inX, int inY) { x = inX; y = inY; }
    public Vector2D(Vector2D other) { x = other.x; y = other.y; }

    @Override
    public String toString() {
        return String.format("%d-%d", x, y);
    }
}
