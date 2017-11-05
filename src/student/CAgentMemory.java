package student;

import mas.agents.Message;
import mas.agents.StringMessage;
import mas.agents.task.mining.StatusMessage;
import student.Map.CMap;

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

    public void SetAgenCount(int count)
    {
        _agent_count = count;
        _agent_pos = new Vector2D[_agent_count];
    }

    public int AgentCount() {return _agent_count;}

    private Vector2D _pos = null;
    public Vector2D Position() { return _pos; }


    private Vector2D[] _agent_pos;

    public void InitCoord(StatusMessage sm) throws Exception
    {
        _pos = new Vector2D(sm.agentX, sm.agentY);

        _map.InitCoord(sm.width, sm.height);

        //_owner.log(PatrolZone());
    }

    private void RefreshEnviroment(StatusMessage sm, ArrayList<Vector2D> outNewObstacles, ArrayList<Vector2D> outNewGold, ArrayList<Vector2D> outNewDepots) throws Exception
    {
        if(_pos == null)
            InitCoord(sm);

        _pos.x = sm.agentX;
        _pos.y = sm.agentY;

        _map.SetOtherAgentPos(_owner.getAgentId(), _pos);

        _map.RefreshEnviroment(sm, outNewObstacles, outNewGold, outNewDepots);
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
                    _map.SetOtherAgentPos(inMessage.getSender(), pos);
                }
                else
                {
                    ArrayList<Vector2D> coord_list = Vector2D.StringToList(data);

                    if(data_type.compareToIgnoreCase("no") == 0)
                        _map.SetNewObstacles(coord_list);
                    else if(data_type.compareToIgnoreCase("ng") == 0)
                        _map.SetNewGold(coord_list);
                    else if(data_type.compareToIgnoreCase("nd") == 0)
                        _map.SetNewDepot(coord_list);
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

    public Vector2D[] GetPath(Vector2D target)
    {
        return _map.GetPath(target);
    }

    public boolean IsPassableCell(Vector2D inCoord, boolean inAgentObstacle)
    {
        return _map.IsPassableCell(inCoord, inAgentObstacle);
    }

    public int GetPassably(Vector2D inCoord)
    {
        return _map.GetPassably(inCoord);
    }

    public Rect2D GetMapRect()
    {
        return _map.GetMapRect();
    }
}
