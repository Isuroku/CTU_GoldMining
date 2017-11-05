package student.FSM;

import mas.agents.Message;
import mas.agents.StringMessage;
import mas.agents.task.mining.StatusMessage;
import student.*;

import java.io.IOException;
import java.util.ArrayList;

public abstract class CFSMBaseState
{
    public abstract EStateType GetStateType();
    public abstract void OnEnter(CFSMBaseState inPrevState) throws Exception;
    public abstract void OnExit();
    public abstract void Update(long inUpdateNumber) throws Exception;

    CAgentMemory Memory() { return _owner.Memory; }
    CAgentMover Mover() { return _owner.Mover; }

    public void OnMessage(Message inMessage) throws Exception
    {
        String msg_text = inMessage.stringify();

        if(msg_text.compareToIgnoreCase("letpass") == 0)
        {
            String log_s =  String.format("have received %s from Agent %d" , msg_text, inMessage.getSender());
            _owner.log(log_s, true);

            Mover().LetPass();
        }
    }

    void SendMessage(int recipient, Message m) throws IOException
    {
        _owner.sendMessage(recipient, m);
    }

    void SendBroadcastMessage(Message m) throws IOException
    {
        for(int i = 1; i <= Memory().AgentCount(); i++)
            if(i != _owner.getAgentId())
                _owner.sendMessage(i, m);
    }

    public StatusMessage Pick() throws Exception
    {
        StatusMessage sm = _owner.pick();
        Memory().RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage Drop() throws Exception
    {
        StatusMessage sm = _owner.drop();
        Memory().RefreshEnviroment(sm);
        return sm;
    }

    public StatusMessage Sense() throws Exception
    {
        return Sense(true);
    }

    public StatusMessage Sense(boolean send) throws Exception
    {
        StatusMessage sm = _owner.sense();
        if(send)
            Memory().RefreshEnviroment(sm);
        return sm;
    }

    CFSMBaseState(Agent owner)
    {
        _owner = owner;
    }

    protected Agent _owner;
}
