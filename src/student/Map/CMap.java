package student.Map;

import mas.agents.task.mining.StatusMessage;
import student.CAgentMemory;
import student.CBinaryHeap;
import student.FSM.EStateType;
import student.Rect2D;
import student.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class CMap
{
    private CAgentMemory _owner;

    private Rect2D _map_rect;

    private CMapCell[] _cells;

    private CMapCell[] _agent_pos;

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
        {
            Vector2D p = IndexToCoord(i);
            _cells[i] = new CMapCell(p);
        }

        /*if(_pos.x == 0 && _pos.y == 0)
        {
            Vector2D[] path = GetPath(new Vector2D(3, 3));
        }*/
    }

    public void SetAgentCount(int count)
    {
        _agent_pos = new CMapCell[count];
    }

    public void SetNewObstacles(Iterable<Vector2D> coord_list)
    {
        coord_list.forEach(v -> GetCell(v).SetObstacle());
    }

    public void SetNewGold(Iterable<Vector2D> coord_list)
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

    public void SetNewDepot(Iterable<Vector2D> coord_list)
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
        if(_agent_pos == null)
            return;

        _owner.Log(String.format("SetOtherAgentPos: Agent %d in pos %s", inAgentId, inPos), false);

        CMapCell old_cell = _agent_pos[inAgentId - 1];
        if(old_cell != null)
            old_cell.AgentId = 0;

        CMapCell c = GetCell(inPos);
        c.AgentId = inAgentId;
        _agent_pos[inAgentId - 1] = c;
    }

    public void RefreshEnvironment(StatusMessage sm,
                                   ArrayList<Vector2D> outNewObstacles,
                                   ArrayList<Vector2D> outNewGold,
                                   ArrayList<Vector2D> outNewDepots,
                                   ArrayList<Vector2D> outAgents) throws Exception
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
            if(data.type == StatusMessage.AGENT)
                outAgents.add(c.Pos);
        });
    }

    private CMapCell GetCell(Vector2D inCoord)
    {
        int index = CoordToIndex(inCoord);

        if(index < 0 || index >= _cells.length)
            throw new RuntimeException( String.format("Incorrect coord %s!", inCoord));

        return _cells[index];
    }

    public Integer[] ClosestFreeAgent(Vector2D target)
    {
        CMapCell s_cell = GetCell(target);

        _wave_number++;

        s_cell.Prev = null;
        s_cell.WaveLength = 0;
        s_cell.WaveNumber = _wave_number;

        CBinaryHeap set = new CBinaryHeap();
        set.Insert(s_cell);

        int need_count = 2;
        ArrayList<Integer> agents = new ArrayList<>();

        while(!set.IsEmpty() && agents.size() < need_count)
        {
            CMapCell cell = (CMapCell)set.DeleteMin();

            if(cell.AgentId > 0 && _owner.GetAgentState(cell.AgentId) == EStateType.Patrol)
                agents.add(cell.AgentId);

            if(agents.size() < need_count)
            {
                Vector2D[] neighbours = cell.Pos.GetNeighbours(_map_rect);
                for(Vector2D p : neighbours)
                {
                    CMapCell n = GetCell(p);
                    SetInWave(cell, n, set);
                }
            }
        }

        return agents.toArray(new Integer[agents.size()]);
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

    public boolean IsGoldPresent() { return !_golds.isEmpty(); }
    public Vector2D[] GetGolds()
    {
        Vector2D[] arr = new Vector2D[_golds.size()];
        for(int i = 0; i < _golds.size(); ++i)
            arr[i] = _golds.get(i).Pos;
        return arr;
    }

    public boolean IsDepotsPresent() { return !_depots.isEmpty(); }
    public Vector2D[] GetDepots()
    {
        Vector2D[] arr = new Vector2D[_depots.size()];
        for(int i = 0; i < _depots.size(); ++i)
            arr[i] = _depots.get(i).Pos;
        return arr;
    }

    public boolean IsAgentAroundMePresent(Vector2D pos)
    {
        Vector2D[] neighbours = pos.GetNeighbours(_map_rect);
        for(Vector2D p : neighbours)
        {
            CMapCell n = GetCell(p);
            if(n.AgentId > 0)
                return true;
        }

        return false;
    }

    public void DeleteGold(Vector2D inGoldPos)
    {
        for(int i = 0; i < _golds.size(); ++i)
        {
            CMapCell cell = _golds.get(i);

            if(cell.Pos.equals(inGoldPos))
            {
                cell.Gold = false;
                _golds.remove(i);
                return;
            }
        }
    }

    public boolean IsOtherAgentOnCell(Vector2D pos)
    {
        CMapCell cell = GetCell(pos);
        return cell.AgentId > 0;
    }

    public Vector2D GetFreeNeighbourCell(Vector2D pos)
    {
        Vector2D[] neighbours = pos.GetNeighbours(_map_rect);
        for(Vector2D p : neighbours)
        {
            CMapCell n = GetCell(p);
            if(n.IsPassable(true))
                return n.Pos;
        }

        return null;
    }

    public Vector2D GetNearestDepot(Vector2D our_pos)
    {
        CMapCell s_cell = GetCell(our_pos);

        _wave_number++;

        s_cell.Prev = null;
        s_cell.WaveLength = 0;
        s_cell.WaveNumber = _wave_number;

        CBinaryHeap set = new CBinaryHeap();
        set.Insert(s_cell);

        while(!set.IsEmpty())
        {
            CMapCell cell = (CMapCell)set.DeleteMin();

            if(cell.Depot)
                return cell.Pos;

            Vector2D[] neighbours = cell.Pos.GetNeighbours(_map_rect);
            for(Vector2D p : neighbours)
            {
                CMapCell n = GetCell(p);
                SetInWave(cell, n, set);
            }
        }

        throw new RuntimeException("Can't find path to any depots!");
    }
}
