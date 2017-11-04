package student;

public class Rect2D
{
    public int Left;
    public int Right;
    public int Top;
    public int Bottom;

    public Rect2D() { Left = 0; Right = 0; Top = 0; Bottom = 0; }
    public Rect2D(int inLeft, int inRight, int inTop, int inBottom) { Left = inLeft; Right = inRight; Top = inTop; Bottom = inBottom; }

    @Override
    public String toString() { return String.format("Left: %d, Right: %d, Top: %d, Bottom: %d", Left, Right, Top, Bottom); }
}
