package student.Map;

import mas.agents.task.mining.StatusMessage;
import student.CAgentMemory;
import student.CBinaryHeap;
import student.Rect2D;
import student.Vector2D;

import java.util.ArrayList;
import java.util.Collections;

public class CMap
{
    private CAgentMemory _owner;

    private Rect2D _map_rect;

    private CMapCell[] _cells;

    //private Vector2D[] _agent_pos;

    private ArrayList<CMapCell> _golds = new ArrayList<>();
    private ArrayList<CMapCell> _depots = new ArrayList<>();

    public CMap(CAgentMemory owner)
    {
        _owner = owner;
    }

    public Rect2D GetMapRect() { return _map_rect; }
    public int MapWidth() { return _map_rect.Width() + 1; }
    public int MapHeight() { return _map_rect.Height() + 1; }

    private int CoordToIndex(int x, int y) { return y  * MapWidth() + x; }
    private int CoordToIndex(Vector2D pos) { return pos.y  * MapWidth() + pos.x; }

    private Vector2D IndexToCoord(int index)
    {
        int y = index / MapWidth();
        int x = index - y * MapWidth();
        return new Vector2D(x, y);
    }

    public void InitCoord(int width, int height) throws Exception
    {
        _map_rect = new Rect2D(0, width - 1, 0, height - 1);

        _cells = new CMapCell[MapWidth() * MapHeight()];
        for(int i = 0; i < _cells.length; ++i)
            _cells[i] = new CMapCell( IndexToCoord(i) );

        /*if(_pos.x == 0 && _pos.y == 0)
        {
            Vector2D[] path = GetPath(new Vector2D(3, 3));
        }*/
    }

    public void SetNewObstacles(ArrayList<Vector2D> coord_list)
    {
        coord_list.forEach(v -> GetCell(v).SetObstacle());
    }

    public void SetNewGold(ArrayList<Vector2D> coord_list)
    {
        coord_list.forEach(v ->
        {
            CMapCell c = GetCell(v);
            if(!c.Gold)
            {
                c.Gold = true;
                _golds.add(c);
            }
        });
    }

    public void SetNewDepot(ArrayList<Vector2D> coord_list)
    {
        coord_list.forEach(v ->
        {
            CMapCell c = GetCell(v);
            if(!c.Depot)
            {
                c.Depot = true;
                _depots.add(c);
            }
        });
    }

    public boolean IsPassableCell(Vector2D inCoord, boolean inAgentObstacle)
    {
        CMapCell c = GetCell(inCoord);
        return c.IsPassable(inAgentObstacle);
    }

    public int GetPassably(Vector2D inCoord)
    {
        CMapCell c = GetCell(inCoord);
        return c.GetPassably();
    }

    public void SetOtherAgentPos(int inAgentId, Vector2D inPos) throws Exception
    {
        _owner.log(String.format("SetOtherAgentPos: Agent %d in pos %s", inAgentId, inPos), true);
        /*if(_agent_pos[inAgentId - 1] != null)
        {
            CMapCell c = GetCell(_agent_pos[inAgentId - 1]);
            c.AgentId = 0;
        }
        _agent_pos[inAgentId - 1] = inPos;*/
        CMapCell c = GetCell(inPos);
        c.AgentId = inAgentId;
    }

    public void RefreshEnviroment(StatusMessage sm, ArrayList<Vector2D> outNewObstacles, ArrayList<Vector2D> outNewGold, ArrayList<Vector2D> outNewDepots) throws Exception
    {
        sm.sensorInput.forEach(data ->
        {
            CMapCell c = _cells[CoordToIndex(data.x, data.y)];
            if(data.type == StatusMessage.OBSTACLE && c.IsObstacleFree())
            {
                c.SetObstacle();
                outNewObstacles.add(c.Pos);
            }
            if(data.type == StatusMessage.GOLD && !c.Gold)
            {
                c.Gold = true;
                _golds.add(c);
                outNewGold.add(c.Pos);
            }
            if(data.type == StatusMessage.DEPOT && !c.Depot)
            {
                c.Depot = true;
                _depots.add(c);
                outNewDepots.add(c.Pos);
            }
        });
    }

    private CMapCell GetCell(Vector2D inCoord)
    {
        int index = CoordToIndex(inCoord);

        if(index < 0 || index >= _cells.length)
            throw new RuntimeException( String.format("Incorrect coord %s!", inCoord));

        return _cells[index];
    }

    int[] ClosestAgent(Vector2D target)
    {
        CMapCell s_cell = GetCell(target);

        _wave_number++;

        s_cell.Prev = null;
        s_cell.WaveLength = 0;
        s_cell.WaveNumber = _wave_number;

        CBinaryHeap set = new CBinaryHeap();
        set.Insert(s_cell);

        int need_count = 2;
        int[] agents = new int[need_count];

        int agent_count = 0;
        while(!set.IsEmpty() && agent_count < need_count)
        {
            CMapCell cell = (CMapCell)set.FindMin();
            set.DeleteMin();

            if(cell.AgentId > 0)
            {
                agents[agent_count] = cell.AgentId;
                agent_count++;
            }

            if(agent_count < need_count)
            {
                Vector2D[] neighbours = cell.Pos.GetNeighbours(_map_rect);
                for(Vector2D p : neighbours)
                {
                    CMapCell n = GetCell(p);
                    SetInWave(cell, n, set);
                }
            }
        }

        return agents;
    }

    private long _wave_number = Long.MIN_VALUE;
    public Vector2D[] GetPath(Vector2D target)
    {
        CMapCell f_cell = GetCell(target);
        CMapCell s_cell = GetCell(_owner.Position());

        if(f_cell == s_cell)
            return null;

        _wave_number++;

        s_cell.Prev = null;
        s_cell.WaveLength = 0;
        s_cell.WaveNumber = _wave_number;

        CBinaryHeap set = new CBinaryHeap();
        set.Insert(s_cell);

        boolean found = false;
        while(!set.IsEmpty() && !found)
        {
            CMapCell cell = (CMapCell)set.DeleteMin();

            found = cell == f_cell;

            if(!found)
            {
                Vector2D[] neighbours = cell.Pos.GetNeighbours(_map_rect);
                for(Vector2D p : neighbours)
                {
                    CMapCell n = GetCell(p);
                    SetInWave(cell, n, set);
                }
            }
        }

        if(!found)
            return null;

        ArrayList<Vector2D> lst = new ArrayList<>();

        CMapCell c = f_cell;
        while(c != null && c.Prev != null)
        {
            lst.add(new Vector2D(c.Pos));
            c = c.Prev;
        }

        Collections.reverse(lst);
        return lst.toArray(new Vector2D[lst.size()]);
    }

    private void SetInWave(CMapCell prev, CMapCell next, CBinaryHeap set)
    {
        if(!next.IsPassable(false) || prev.WaveNumber == next.WaveNumber)
            return;

        next.WaveNumber = prev.WaveNumber;
        next.WaveLength = prev.WaveLength + 1;
        next.Prev = prev;
        set.Insert(next);
    }

}
