package student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Vector2D
{
    public int x;
    public int y;

    public Vector2D() { x = 0; y = 0; }
    public Vector2D(int inX, int inY) { x = inX; y = inY; }
    public Vector2D(Vector2D other) { x = other.x; y = other.y; }

    @Override
    public String toString() {
        return String.format("%d:%d", x, y);
    }

    public boolean equals(Vector2D o)
    {
        if(x != o.x)
            return false;
        return y == o.y;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Vector2D vector2D = (Vector2D) o;

        if(x != vector2D.x)
            return false;
        return y == vector2D.y;
    }

    @Override
    public int hashCode()
    {
        int result = 27;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }

    public static Vector2D Parse(String str)
    {
        if(str.isEmpty())
            throw new RuntimeException( "Parse Vector2D from empty string!" );

        String[] as = str.split(":");
        if(as.length != 2)
            throw new RuntimeException( "Parse Vector2D: error in split!" );

        int x = Integer.parseInt(as[0].trim());
        int y = Integer.parseInt(as[1].trim());

        return new Vector2D(x, y);
    }

    public static String ListToString(ArrayList<Vector2D> inList)
    {
        Iterator<Vector2D> it = inList.iterator();
        if (! it.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();

        for (;;) {
            Vector2D e = it.next();
            sb.append(e);
            if (! it.hasNext())
                return sb.toString();
            sb.append(',').append(' ');
        }
    }

    public static ArrayList<Vector2D> StringToList(String inStrData)
    {
        ArrayList<Vector2D> res = new ArrayList<>();
        if(inStrData.isEmpty())
            return res;

        String[] ap = inStrData.split(",");
        for(int i = 0; i < ap.length; i++)
            res.add(Vector2D.Parse(ap[i]));


        return res;
    }

    public Vector2D[] GetNeighbours(Rect2D inZone)
    {
        ArrayList<Vector2D> arr = new ArrayList<>();

        int nx = x - 1;
        if(inZone == null || (nx >= inZone.Left && nx <= inZone.Right))
            arr.add(new Vector2D(nx, y));

        nx = x + 1;
        if(inZone == null || (nx >= inZone.Left && nx <= inZone.Right))
            arr.add(new Vector2D(nx, y));

        int ny = y - 1;
        if(inZone == null || (ny >= inZone.Top && ny <= inZone.Bottom))
            arr.add(new Vector2D(x, ny));

        ny = y + 1;
        if(inZone == null || (ny >= inZone.Top && ny <= inZone.Bottom))
            arr.add(new Vector2D(x, ny));

        Collections.shuffle(arr);
        return arr.toArray(new Vector2D[arr.size()]);
    }

    public ArrayList<Vector2D> GetNeighbourhoodPoses(Rect2D inZone, int inDist)
    {
        ArrayList<Vector2D> arr = new ArrayList<>();

        for(int nx = x - inDist; nx <= x + inDist; nx++)
            for(int ny = y - inDist; ny <= y + inDist; ny++)
                if(inZone == null || (nx >= inZone.Left && nx <= inZone.Right && ny >= inZone.Top && ny <= inZone.Bottom))
                    arr.add(new Vector2D(nx, ny));

        return arr;
    }

    public boolean IsNeighbour(Vector2D pos)
    {
        int dx = pos.x - x;
        int dy = pos.y - y;

        return dx == 0 && Math.abs(dy) == 1 || dy == 0 && Math.abs(dx) == 1;
    }
}
