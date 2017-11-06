package student;

import mas.agents.task.mining.StatusMessage;
import student.FSM.EStateType;
import student.Map.CMap;
import student.Messages.*;

import java.util.*;

public class CAgentMemory
{
    private final Agent _owner;

    private final CMap _map;

    private int _agent_count = 1;

    private Vector2D _pos = null;

    private CAgentInfo[] _agents_info;

    private final ArrayList<Vector2D> _agents_around_me = new ArrayList<>();

    private final HashSet<Vector2D> _dark_poses_small = new HashSet<>();
    private final HashSet<Vector2D> _dark_poses_full = new HashSet<>();

    public void CheckCoord(Vector2D pos)
    {
        if(pos.x < 0 || pos.x>= _map.MapWidth() || pos.y < 0 || pos.y >= _map.MapHeight())
            throw new RuntimeException( String.format("Incorrect coord %s!", pos));
    }

    class CAgentInfo
    {
        EStateType StateType = EStateType.Idle;
    }

    CAgentMemory(Agent owner)
    {
        _owner = owner;
        _map = new CMap(this);
    }

    public void SetAgentCount(int count)
    {
        _agent_count = count;
        _map.SetAgentCount(count);

        _agents_info = new CAgentInfo[count];
        for(int i = 0; i < count; ++i)
            _agents_info[i] = new CAgentInfo();

        Rect2D pz = PatrolZone();

        if(_owner.getAgentId() <= _agent_count / 2)
        {
            for(int x = pz.Left; x <= pz.Right; ++x)
                for(int y = pz.Top; y <= pz.Bottom; ++y)
                    _dark_poses_small.add(new Vector2D(x, y));
        }
        else
        {
            for(int x = pz.Right; x >= pz.Left; --x)
                for(int y = pz.Top; y <= pz.Bottom; ++y)
                    _dark_poses_small.add(new Vector2D(x, y));
        }
    }

    public int AgentCount() {return _agent_count;}

    public Vector2D Position() { return _pos; }

    private void InitCoord(StatusMessage sm) throws Exception
    {
        _pos = new Vector2D(sm.agentX, sm.agentY);

        _map.InitCoord(sm.width, sm.height);

        for(int x = 0; x < _map.MapWidth(); ++x)
            for(int y = 0; y < _map.MapHeight(); ++y)
                _dark_poses_full.add(new Vector2D(x, y));
    }

    private void DeleteDarkPos(Vector2D pos)
    {
        ArrayList<Vector2D> poses = pos.GetNeighbourhoodPoses(_map.GetMapRect(), 1);
        for(Vector2D p : poses)
        {
            _dark_poses_small.remove(p);
            _dark_poses_full.remove(p);
        }
    }

    private void RefreshEnvironment(StatusMessage sm, ArrayList<Vector2D> outNewObstacles, ArrayList<Vector2D> outNewGold, ArrayList<Vector2D> outNewDepots) throws Exception
    {
        if(_pos == null)
            InitCoord(sm);

        _pos.x = sm.agentX;
        _pos.y = sm.agentY;

        _map.SetOtherAgentPos(_owner.getAgentId(), _pos);

        DeleteDarkPos(_pos);

        _agents_around_me.clear();
        _map.RefreshEnvironment(sm, outNewObstacles, outNewGold, outNewDepots, _agents_around_me);
    }

    void OnMessage(CMessageBase inMessage) throws Exception
    {
        if(inMessage.MessageType() == EMessageType.REnv)
        {
            CMessageRefreshEnvironment msg = (CMessageRefreshEnvironment) inMessage;
            _map.SetOtherAgentPos(msg.Sender(), msg.Position());
            _map.SetNewObstacles(msg.Obstacles());
            _map.SetNewGold(msg.Golds());
            _map.SetNewDepot(msg.Depots());

            DeleteDarkPos(msg.Position());
        }
        else if(inMessage.MessageType() == EMessageType.ChangeState)
        {
            CMessageChangeState msg = (CMessageChangeState) inMessage;
            SetAgentState(msg.Sender(), msg.State());
        }
        else if(inMessage.MessageType() == EMessageType.GoldPicked)
        {
            CMessageGoldPicked msg = (CMessageGoldPicked) inMessage;
            DeleteGold(msg.GoldPos());
        }
    }

    public void DeleteGold(Vector2D pos)
    {
        _map.DeleteGold(pos);
    }

    public void RefreshEnvironment(StatusMessage sm) throws Exception
    {
        RefreshEnvironment(sm, true);
    }

    private Vector2D _sent_pos = new Vector2D(-1, -1);

    public void RefreshEnvironment(StatusMessage sm, boolean send) throws Exception
    {
        ArrayList<Vector2D> new_obstacles = new ArrayList<>();
        ArrayList<Vector2D> new_golds = new ArrayList<>();
        ArrayList<Vector2D> new_depots = new ArrayList<>();

        RefreshEnvironment(sm, new_obstacles, new_golds, new_depots);

        if(_sent_pos.equals(_pos) && new_obstacles.isEmpty() && new_golds.isEmpty() && new_depots.isEmpty() || !send)
            return;

        _sent_pos = new Vector2D(_pos);
        CMessageRefreshEnvironment msg = new CMessageRefreshEnvironment(_owner.getAgentId(), _pos, new_obstacles, new_golds, new_depots);
        _owner.SendBroadcastMessage(msg);
    }

    public Rect2D PatrolZone()
    {
        int w = _map.MapWidth() / _agent_count - 1;
        int w2 = w + 1;

        int l = w2 * (_owner.getAgentId() - 1);

        int r = l + w;

        int miss = _map.MapWidth() - _agent_count * w2;

        if(_owner.getAgentId() == _agent_count)
            r += miss;

        return new Rect2D(l, r, 0, _map.MapHeight() - 1);
    }

    Vector2D[] GetPath(Vector2D target)
    {
        return _map.GetPath(target);
    }

    public boolean IsPassableCell(Vector2D inCoord, boolean inAgentObstacle)
    {
        return _map.IsPassableCell(inCoord, inAgentObstacle);
    }

    int GetPassably(Vector2D inCoord)
    {
        return _map.GetPassably(inCoord);
    }

    Rect2D GetMapRect()
    {
        return _map.GetMapRect();
    }

    public void SetAgentState(int inAgentId, EStateType inStateType)
    {
        if(_agents_info != null)
            _agents_info[inAgentId - 1].StateType = inStateType;
    }

    public EStateType GetAgentState(int inAgentId)
    {
        return _agents_info[inAgentId - 1].StateType;
    }

    public Integer[] ClosestFreeAgent(Vector2D target)
    {
        return _map.ClosestFreeAgent(target);
    }

    public boolean IsGoldPresent() { return _map.IsGoldPresent(); }
    public Vector2D[] GetGolds() { return _map.GetGolds(); }

    public boolean IsDepotsPresent() { return _map.IsDepotsPresent(); }
    public Vector2D[] GetDepots() { return _map.GetDepots(); }

    public boolean IsAgentAroundMePresent()
    {
        for(Vector2D p : _agents_around_me)
        {
            if(p.IsNeighbour(_pos))
                return true;
        }

        return false;
    }

    public void Log(String format, boolean b) throws Exception
    {
        _owner.log(format, b);
    }

    public int IsOtherAgentOnCell(Vector2D pos)
    {
        return _map.IsOtherAgentOnCell(pos);
    }

    public Vector2D GetFreeNeighbourCell(Vector2D pos)
    {
        return _map.GetFreeNeighbourCell(pos);
    }

    public Vector2D GetNearestDepot()
    {
        return _map.GetNearestDepot(_pos);
    }

    public Vector2D GetFirstDarkPoint()
    {
        if(!_dark_poses_small.isEmpty())
            return _dark_poses_small.iterator().next();

        if(!_dark_poses_full.isEmpty())
            return _dark_poses_full.iterator().next();

        return null;
    }

    public Vector2D GetAgentPos(int inAgentId)
    {
        return _map.GetAgentPos(inAgentId);
    }

    public ArrayList<Vector2D> GetNeighbourhoodPoses(int inDist, boolean inAgentObstacle)
    {
        return _map.GetNeighbourhoodPoses(_pos, inDist, inAgentObstacle);
    }
}
