package student;

import java.util.ArrayList;
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
}
