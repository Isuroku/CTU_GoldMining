package student;

import mas.agents.Message;
import mas.agents.StringMessage;
import mas.agents.task.mining.StatusMessage;
import student.Map.CMap;
import student.Messages.CMessageBase;
import student.Messages.CMessageRefreshEnvironment;
import student.Messages.EMessageType;

import java.util.*;

public class CAgentMemory
{
    private Agent _owner;

    private CMap _map;

    private int _agent_count = 1;

    CAgentMemory(Agent owner)
    {
        _owner = owner;
        _map = new CMap(this);
    }

    public void SetAgentCount(int count)
    {
        _agent_count = count;
        _map.SetAgentCount(count);
    }

    public int AgentCount() {return _agent_count;}

    private Vector2D _pos = null;
    public Vector2D Position() { return _pos; }


    private void InitCoord(StatusMessage sm) throws Exception
    {
        _pos = new Vector2D(sm.agentX, sm.agentY);

        _map.InitCoord(sm.width, sm.height);

        //_owner.log(PatrolZone());
    }

    private void RefreshEnvironment(StatusMessage sm, ArrayList<Vector2D> outNewObstacles, ArrayList<Vector2D> outNewGold, ArrayList<Vector2D> outNewDepots) throws Exception
    {
        if(_pos == null)
            InitCoord(sm);

        _pos.x = sm.agentX;
        _pos.y = sm.agentY;

        _map.SetAgentPos(_owner.getAgentId(), _pos);

        _map.RefreshEnvironment(sm, outNewObstacles, outNewGold, outNewDepots);
    }

    void OnMessage(CMessageBase inMessage) throws Exception
    {
        if(inMessage.MessageType() != EMessageType.REnv)
            return;

        CMessageRefreshEnvironment msg = (CMessageRefreshEnvironment)inMessage;
        _map.SetOtherAgentPos(msg.Sender(), msg.Position());
        _map.SetNewObstacles(msg.Obstacles());
        _map.SetNewGold(msg.Golds());
        _map.SetNewDepot(msg.Depots());
    }

    public void RefreshEnvironment(StatusMessage sm) throws Exception
    {
        Vector2D old_pos = _pos == null ? new Vector2D(-1, -1) : new Vector2D(_pos);

        ArrayList<Vector2D> new_obstacles = new ArrayList<>();
        ArrayList<Vector2D> new_golds = new ArrayList<>();
        ArrayList<Vector2D> new_depots = new ArrayList<>();

        RefreshEnvironment(sm, new_obstacles, new_golds, new_depots);

        if(old_pos.equals(_pos) && new_obstacles.isEmpty() && new_golds.isEmpty() && new_depots.isEmpty())
            return;

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

    public void log(Object obj, boolean print) throws Exception
    {
        _owner.log(obj, print);
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
}
