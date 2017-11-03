package student.FSM;

import mas.agents.Message;
import mas.agents.task.mining.StatusMessage;
import student.Agent;
import student.CAgentMemory;

import java.io.IOException;

public abstract class CFSMBaseState
{
    public abstract EStateType GetStateType();
    public abstract void OnEnter(CFSMBaseState inPrevState) throws IOException, Exception;
    public abstract void OnExit();
    public abstract void Update(long inUpdateNumber) throws Exception;

    public CAgentMemory Memory() { return _owner.Memory; }

    public void OnMessage(Message inMessage) throws Exception
    {
        String s =  String.format("Agent %d have received %s" , _owner.getAgentId(), inMessage.stringify());
        _owner.log(s);
    }

    public void SendMessage(int recipient, Message m) throws IOException
    {
        _owner.sendMessage(recipient, m);
    }

    public void SendBroadcastMessage(Message m) throws IOException
    {
        for(int i = 0; i < Memory().AgentCount(); i++)
            if(i != _owner.getAgentId())
                _owner.sendMessage(i, m);
    }

    public StatusMessage left() throws IOException { return _owner.left(); }
    public StatusMessage right() throws IOException { return _owner.right(); }
    public StatusMessage up() throws IOException { return _owner.up();}
    public StatusMessage down() throws IOException { return _owner.down(); }
    public StatusMessage pick() throws IOException { return _owner.pick(); }
    public StatusMessage drop() throws IOException { return _owner.drop(); }
    public StatusMessage sense() throws IOException { return _owner.sense(); }

    CFSMBaseState(Agent owner)
    {
        _owner = owner;
    }

    protected Agent _owner;
}
