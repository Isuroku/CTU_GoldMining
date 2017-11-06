package student;

public class Rect2D
{
    public final int Left;
    public final int Right;
    public final int Top;
    public final int Bottom;

    public Rect2D() { Left = 0; Right = 0; Top = 0; Bottom = 0; }
    public Rect2D(int inLeft, int inRight, int inTop, int inBottom) { Left = inLeft; Right = inRight; Top = inTop; Bottom = inBottom; }

    @Override
    public String toString() { return String.format("Left: %d, Right: %d, Top: %d, Bottom: %d", Left, Right, Top, Bottom); }

    public int Width()
    {
        return Right - Left;
    }

    public int Height()
    {
        return Bottom - Top;
    }

    public boolean InsideSoft(Vector2D our_pos)
    {
        return our_pos.x >= Left && our_pos.x <= Right && our_pos.y >= Top && our_pos.y <= Bottom;
    }
}
