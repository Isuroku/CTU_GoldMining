package student.FSM;

import mas.agents.Message;
import mas.agents.StringMessage;
import mas.agents.task.mining.StatusMessage;
import student.Agent;
import student.CAgentMemory;
import student.Vector2D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public abstract class CFSMBaseState
{
    public abstract EStateType GetStateType();
    public abstract void OnEnter(CFSMBaseState inPrevState) throws Exception;
    public abstract void OnExit();
    public abstract void Update(long inUpdateNumber) throws Exception;

    CAgentMemory Memory() { return _owner.Memory; }

    public void OnMessage(Message inMessage) throws Exception
    {
        String msg_text = inMessage.stringify();

        String log_s =  String.format("have received %s from Agent %d" , msg_text, inMessage.getSender());
        _owner.log(log_s);

        if(msg_text.startsWith("REnv:"))
        {
            String s = msg_text.substring("REnv:".length());

            String[] arr = s.split(";");

            for(int i = 0; i < arr.length; i++)
            {
                String part = arr[i];
                String data_type = part.substring(0, 2);
                String data = part.substring(3, part.length() - 1);

                ArrayList<Vector2D> coord_list = Vector2D.StringToList(data);

                if(data_type.compareToIgnoreCase("no") == 0)
                    Memory().SetNewObstacles(coord_list);
                else if(data_type.compareToIgnoreCase("ng") == 0)
                    Memory().SetNewGold(coord_list);
                else if(data_type .compareToIgnoreCase("nd") == 0)
                    Memory().SetNewDepot(coord_list);
            }
        }
    }

    void SendMessage(int recipient, Message m) throws IOException
    {
        _owner.sendMessage(recipient, m);
    }

    void SendBroadcastMessage(Message m) throws IOException
    {
        for(int i = 0; i < Memory().AgentCount(); i++)
            if(i != _owner.getAgentId())
                _owner.sendMessage(i, m);
    }

    protected void RefreshEnviroment(StatusMessage sm) throws IOException
    {
        ArrayList<Vector2D> new_obstacles = new ArrayList<>();
        ArrayList<Vector2D> new_golds = new ArrayList<>();
        ArrayList<Vector2D> new_depots = new ArrayList<>();
        Memory().RefreshEnviroment(sm, new_obstacles, new_golds, new_depots);

        if(new_obstacles.isEmpty() && new_golds.isEmpty() && new_depots.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();

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



    public StatusMessage left() throws IOException
    {
        StatusMessage sm = _owner.left();
        RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage right() throws IOException
    {
        StatusMessage sm = _owner.right();
        RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage up() throws IOException
    {
        StatusMessage sm = _owner.up();
        RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage down() throws IOException
    {
        StatusMessage sm = _owner.down();
        RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage pick() throws IOException
    {
        StatusMessage sm = _owner.pick();
        RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage drop() throws IOException
    {
        StatusMessage sm = _owner.drop();
        RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage sense() throws IOException
    {
        return sense(true);
    }

    public StatusMessage sense(boolean send) throws IOException
    {
        StatusMessage sm = _owner.sense();
        if(send)
            RefreshEnviroment(sm);
        return sm;
    }

    CFSMBaseState(Agent owner)
    {
        _owner = owner;
    }

    protected Agent _owner;
}
