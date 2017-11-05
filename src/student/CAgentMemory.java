package student;

import mas.agents.Message;
import mas.agents.StringMessage;
import mas.agents.task.mining.StatusMessage;

import java.io.IOException;
import java.util.*;

public class CAgentMemory
{
    CAgentMemory(Agent owner)
    {
        _owner = owner;
    }

    protected Agent _owner;

    private int _agent_count = 1;
    public void SetAgenCount(int count)
    {
        _agent_count = count;
        _agent_pos = new Vector2D[_agent_count];
    }

    public int AgentCount() {return _agent_count;}

    private Vector2D _pos = null;
    public Vector2D Position() { return _pos; }

    Rect2D _map_rect;

    public Rect2D GetMapRect() { return _map_rect; }
    public int MapWidth() { return _map_rect.Width() + 1; }
    public int MapHeight() { return _map_rect.Height() + 1; }

    private Cell[] _cells;
    private Vector2D[] _agent_pos;

    ArrayList<Cell> _golds;
    ArrayList<Cell> _depots;

    public void InitCoord(StatusMessage sm) throws Exception
    {
        _golds = new ArrayList<Cell>();
        _depots = new ArrayList<Cell>();

        _pos = new Vector2D(sm.agentX, sm.agentY);

        _map_rect = new Rect2D(0, sm.width - 1, 0, sm.height - 1);

        _cells = new Cell[MapWidth() * MapHeight()];
        for(int i = 0; i < _cells.length; ++i)
            _cells[i] = new Cell( IndexToCoord(i) );

        //_owner.log(PatrolZone());

        /*if(_pos.x == 0 && _pos.y == 0)
        {
            Vector2D[] path = GetPath(new Vector2D(3, 3));
        }*/
    }

    public Rect2D PatrolZone()
    {
        int w = MapWidth() / _agent_count - 1;
        int w2 = w + 1;

        int l = w2 * (_owner.getAgentId() - 1);

        int r = l + w;

        int miss = MapWidth() - _agent_count * w2;

        if(_owner.getAgentId() == _agent_count)
            r += miss;

        /*if(r >= _map_width)
            r = _map_width - 1;*/
        return new Rect2D(l, r, 0, MapHeight() - 1);
    }

    private void RefreshEnviroment(StatusMessage sm, ArrayList<Vector2D> outNewObstacles, ArrayList<Vector2D> outNewGold, ArrayList<Vector2D> outNewDepots) throws Exception
    {
        if(_pos == null)
            InitCoord(sm);

        _pos.x = sm.agentX;
        _pos.y = sm.agentY;

        SetOtherAgentPos(_owner.getAgentId(), _pos);

        sm.sensorInput.forEach(data ->
        {
            Cell c = _cells[CoordToIndex(data.x, data.y)];
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

    private int CoordToIndex(int x, int y) { return y  * MapWidth() + x; }
    private int CoordToIndex(Vector2D pos) { return pos.y  * MapWidth() + pos.x; }

    private Vector2D IndexToCoord(int index)
    {
        int y = index / MapWidth();
        int x = index - y * MapWidth();
        return new Vector2D(x, y);
    }

    public void SetNewObstacles(ArrayList<Vector2D> coord_list)
    {
        coord_list.forEach(v -> GetCell(v).SetObstacle());
    }

    public void SetNewGold(ArrayList<Vector2D> coord_list)
    {
        coord_list.forEach(v ->
        {
            Cell c = GetCell(v);
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
            Cell c = GetCell(v);
            if(!c.Depot)
            {
                c.Depot = true;
                _depots.add(c);
            }
        });
    }

    public boolean IsPassableCell(Vector2D inCoord, boolean inAgentObstacle)
    {
        Cell c = GetCell(inCoord);
        return c.IsPassable(inAgentObstacle);
    }

    public int GetPassably(Vector2D inCoord)
    {
        Cell c = GetCell(inCoord);
        return c.GetPassably();
    }

    public void SetOtherAgentPos(int inAgentId, Vector2D inPos) throws Exception
    {
        _owner.log(String.format("SetOtherAgentPos: Agent %d in pos %s", inAgentId, inPos), true);
        if(_agent_pos[inAgentId - 1] != null)
        {
            Cell c = GetCell(_agent_pos[inAgentId - 1]);
            c.AgentId = 0;
        }
        _agent_pos[inAgentId - 1] = inPos;
        Cell c = GetCell(inPos);
        c.AgentId = inAgentId;
    }

    class Cell implements Comparable<Cell>
    {
        public Vector2D Pos;

        public boolean IsPassable(boolean agent_obstacle)
        {
            if(agent_obstacle)
                return _passable && AgentId == 0;
            return _passable;
        }

        int GetPassably()
        {
            if(AgentId != 0)
                return AgentId;
            if(!_passable)
                return 0;
            return -1;
        }

        public boolean IsObstacleFree() { return _passable ; }
        public void SetObstacle() { _passable = false; }

        private boolean _passable = true;

        public int AgentId = 0;

        public boolean Gold = false;
        public boolean Depot = false;

        public int WaveLength = 0;
        public long WaveNumber = Long.MIN_VALUE;
        public Cell Prev;

        public Cell(Vector2D inPos) { Pos = inPos; }

        @Override
        public String toString() {
            return String.format("%s: %s%s%s agent %d [wl:%d, wn: %d, p: %s]",
                    Pos,
                    _passable ? " passable" : "",
                    Gold ? " gold" : "",
                    Depot ? " depot" : "",
                    AgentId,
                    WaveLength, WaveNumber, Prev == null ? "none" : Prev.Pos);
        }

        @Override
        public int compareTo(Cell o)
        {
            return WaveLength - o.WaveLength;
        }
    }

    private Cell GetCell(Vector2D inCoord)
    {
        int index = CoordToIndex(inCoord);

        if(index < 0 || index >= _cells.length)
            throw new RuntimeException( String.format("Incorrect coord %s!", inCoord));

        return _cells[index];
    }

    int[] ClosestAgent(Vector2D target)
    {
        Cell s_cell = GetCell(target);

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
            Cell cell = (Cell)set.FindMin();
            set.DeleteMin();

            if(cell.AgentId > 0)
            {
                agents[agent_count] = cell.AgentId;
                agent_count++;
            }

            if(agent_count < need_count)
            {
                Vector2D[] neighbours = cell.Pos.GetNeighbours(_map_rect);
                for(int i = 0; i < neighbours.length; ++i)
                {
                    Vector2D p = neighbours[i];
                    Cell n = GetCell(p);
                    SetInWave(cell, n, set);
                }
            }
        }

        return agents;
    }

    private long _wave_number = Long.MIN_VALUE;
    public Vector2D[] GetPath(Vector2D target)
    {
        Cell f_cell = GetCell(target);
        Cell s_cell = GetCell(_pos);

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
                Vector2D[] neighbours = cell.Pos.GetNeighbours(_map_rect);
                for(int i = 0; i < neighbours.length; ++i)
                {
                    Vector2D p = neighbours[i];
                    Cell n = GetCell(p);
                    SetInWave(cell, n, set);
                }
            }
        }

        if(!found)
            return null;

        ArrayList<Vector2D> lst = new ArrayList<Vector2D>();

        Cell c = f_cell;
        while(c != null && c.Prev != null)
        {
            lst.add(new Vector2D(c.Pos));
            c = c.Prev;
        }

        Collections.reverse(lst);
        return lst.toArray(new Vector2D[lst.size()]);
    }

    private void SetInWave(Cell prev, Cell next, CBinaryHeap set)
    {
        if(!next.IsPassable(false) || prev.WaveNumber == next.WaveNumber)
            return;

        next.WaveNumber = prev.WaveNumber;
        next.WaveLength = prev.WaveLength + 1;
        next.Prev = prev;
        set.Insert(next);
    }

    public void OnMessage(Message inMessage) throws Exception
    {
        String msg_text = inMessage.stringify();

        String log_s =  String.format("have received %s from Agent %d" , msg_text, inMessage.getSender());
        _owner.log(log_s, false);

        if(msg_text.startsWith("REnv:"))
        {
            String s = msg_text.substring("REnv:".length());

            String[] arr = s.split(";");

            for(int i = 0; i < arr.length; i++)
            {
                String part = arr[i];
                String data_type = part.substring(0, 2);
                String data = part.substring(3, part.length() - 1);

                if(data_type.compareToIgnoreCase("ps") == 0)
                {
                    Vector2D pos = Vector2D.Parse(data);
                    SetOtherAgentPos(inMessage.getSender(), pos);
                }
                else
                {
                    ArrayList<Vector2D> coord_list = Vector2D.StringToList(data);

                    if(data_type.compareToIgnoreCase("no") == 0)
                        SetNewObstacles(coord_list);
                    else if(data_type.compareToIgnoreCase("ng") == 0)
                        SetNewGold(coord_list);
                    else if(data_type.compareToIgnoreCase("nd") == 0)
                        SetNewDepot(coord_list);
                }
            }
        }
    }

    public void RefreshEnviroment(StatusMessage sm) throws Exception
    {
        Vector2D old_pos = _pos == null ? new Vector2D(-1, -1) : new Vector2D(_pos);
        ArrayList<Vector2D> new_obstacles = new ArrayList<>();
        ArrayList<Vector2D> new_golds = new ArrayList<>();
        ArrayList<Vector2D> new_depots = new ArrayList<>();
        RefreshEnviroment(sm, new_obstacles, new_golds, new_depots);

        if(old_pos.equals(_pos) && new_obstacles.isEmpty() && new_golds.isEmpty() && new_depots.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();

        //все ключи в 2 символа
        {
            String s = _pos.toString();
            sb.append("ps[").append(s).append("];");
        }

        if(!new_obstacles.isEmpty())
        {
            String s = new_obstacles.toString();
            sb.append("no").append(s).append(";");
        }
        if(!new_golds.isEmpty())
        {
            String s = new_golds.toString();
            sb.append("ng").append(s).append(";");
        }
        if(!new_depots.isEmpty())
        {
            String s = new_depots.toString();
            sb.append("nd").append(s).append(";");
        }

        SendBroadcastMessage(new StringMessage(String.format("REnv:%s", sb)));
    }

    void SendBroadcastMessage(Message m) throws Exception
    {
        String log_s =  String.format("SendBroadcastMessage %s", m.stringify());
        _owner.log(log_s, true);

        for(int i = 1; i <= AgentCount(); i++)
            if(i != _owner.getAgentId())
                _owner.sendMessage(i, m);
    }
}
