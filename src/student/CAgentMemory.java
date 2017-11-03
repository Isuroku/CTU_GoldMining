package student;

import mas.agents.task.mining.StatusMessage;

import java.util.*;

public class CAgentMemory
{
    CAgentMemory(Agent owner)
    {
        _owner = owner;
    }

    protected Agent _owner;

    private int _agent_count = 1;

    private Vector2D _pos;

    private int _map_width;
    private int _map_height;

    private Cell[] _cells;

    public void SetAgenCount(int count) { _agent_count = count; }
    public int AgentCount() {return _agent_count;}

    public void InitCoord(StatusMessage sm)
    {
        _pos = new Vector2D(sm.agentX, sm.agentY);

        _map_width = sm.width;
        _map_height = sm.height;

        _cells = new Cell[_map_width * _map_height];
        for(int i = 0; i < _cells.length; ++i)
            _cells[i] = new Cell( IndexToCoord(i) );

        /*if(_pos.x == 0 && _pos.y == 0)
        {
            Vector2D[] path = GetPath(new Vector2D(3, 3));
        }*/
    }

    private int CoordToIndex(Vector2D pos) { return pos.y  * _map_width + pos.x; }

    private Vector2D IndexToCoord(int index)
    {
        int y = index / _map_width;
        int x = index - y * _map_width;
        return new Vector2D(x, y);
    }

    class Cell implements Comparable<Cell>
    {
        public Vector2D Pos;

        public boolean Passable = true;

        public int WaveLength = 0;
        public long WaveNumber = Long.MIN_VALUE;
        public Cell Prev;

        public Cell(Vector2D inPos) { Pos = inPos; }

        @Override
        public String toString() {
            return String.format("%s:%s [wl:%d, wn: %d, p: %s]", Pos, Passable ? " passable" : "", WaveLength, WaveNumber, Prev == null ? "none" : Prev.Pos);
        }

        @Override
        public int compareTo(Cell o)
        {
            return WaveLength - o.WaveLength;
        }
    }

    long _wave_number = Long.MIN_VALUE;
    public Vector2D[] GetPath(Vector2D target)
    {
        Cell f_cell = _cells[CoordToIndex(target)];
        Cell s_cell = _cells[CoordToIndex(_pos)];

        if(f_cell == s_cell)
            return new Vector2D[] { _pos };

        _wave_number++;

        s_cell.Prev = null;
        s_cell.WaveLength = 0;
        s_cell.WaveNumber = _wave_number;

        CBinaryHeap set = new CBinaryHeap();
        set.Insert(s_cell);

        boolean found = false;
        while(!set.IsEmpty() && !found)
        {
            Cell cell = (Cell)set.FindMin();
            set.DeleteMin();

            found = cell == f_cell;

            if(!found)
            {
                Vector2D p = new Vector2D(cell.Pos);

                p.y--;
                if(p.y >= 0)
                {
                    Cell n = _cells[CoordToIndex(p)];
                    SetInWave(cell, n, set);
                }

                p.y += 2;
                if(p.y < _map_height)
                {
                    Cell n = _cells[CoordToIndex(p)];
                    SetInWave(cell, n, set);
                }

                p.y--;
                p.x--;
                if(p.x >= 0)
                {
                    Cell n = _cells[CoordToIndex(p)];
                    SetInWave(cell, n, set);
                }

                p.x += 2;
                if(p.x < _map_width)
                {
                    Cell n = _cells[CoordToIndex(p)];
                    SetInWave(cell, n, set);
                }
            }
        }

        if(!found)
            return new Vector2D[0];

        ArrayList<Vector2D> lst = new ArrayList<Vector2D>();

        Cell c = f_cell;
        while(c != null)
        {
            lst.add(new Vector2D(c.Pos));
            c = c.Prev;
        }

        Collections.reverse(lst);
        return (Vector2D[]) lst.toArray();
    }

    private void SetInWave(Cell prev, Cell next, CBinaryHeap set)
    {
        if(!next.Passable || prev.WaveNumber == next.WaveNumber)
            return;

        next.WaveNumber = prev.WaveNumber;
        next.WaveLength = prev.WaveLength + 1;
        next.Prev = prev;
        set.Insert(next);
    }

}
