package student;

import mas.agents.AbstractAgent;
import mas.agents.Message;
import mas.agents.SimulationApi;
import mas.agents.StringMessage;
import mas.agents.task.mining.*;
import student.FSM.*;
import student.Messages.CMessageBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Agent extends AbstractAgent
{
    CFreeFSM _fsm;

    ArrayList<CFSMBaseState> _states = new ArrayList<CFSMBaseState>();

    long _update_counter = 0;

    public CAgentMemory Memory;
    public CAgentMover Mover;

    public Agent(int id, InputStream is, OutputStream os, SimulationApi api) throws Exception
    {
        super(id, is, os, api);

        Memory = new CAgentMemory(this);
        Mover = new CAgentMover(this);

        if(id == 1)
            _states.add(new CFSMStateStartTalkOne(this));
        else
            _states.add(new CFSMStateStartTalkOthers(this));

        _states.add(new CFSMStateIdle(this));
        _states.add(new CFSMStatePatrol(this));

        _fsm = new CFreeFSM(_states.get(0));
    }


    @Override
    public void act() throws Exception
    {
        while(true) {

            if(_update_counter == 0)
                _fsm.state().OnEnter(null);

            while (messageAvailable()) {
                Message m = readMessage();

                CMessageBase msg = CMessageBase.CreateMessage(m.getSender(), m.stringify());
                LogMessage(msg);

                Memory.OnMessage(msg);

                _fsm.state().OnMessage(msg);
            }

            _fsm.state().Update(++_update_counter);

            try {
                Thread.sleep(200);
            } catch(InterruptedException ie) {}
        }
    }

    public void SwitchState(CFSMBaseState inOldState, EStateType inStateType) throws Exception
    {
        for(int i = 0; i < _states.size(); ++i)
            if(_states.get(i).GetStateType() == inStateType)
            {
                _fsm.Switch(inOldState, _states.get(i));
                log(String.format("change state from %s to %s", inOldState.GetStateType(), inStateType));
                return;
            }

        log(String.format("SwitchState ERROR: can't find state %s", inStateType));
    }

    public void log(Object obj, boolean print) throws Exception
    {
        if(print)
            log(obj);
    }

    void LogMessage(CMessageBase msg) throws Exception
    {
        log(msg.toString());
    }

    public void SendMessage(int Recipient, CMessageBase msg) throws Exception
    {
        sendMessage(Recipient, msg.CodeToMessage());
    }

    public void SendBroadcastMessage(CMessageBase msg) throws Exception
    {
        for(int i = 1; i <= Memory.AgentCount(); i++)
            if(i != getAgentId())
                SendMessage(i, msg);
    }
}
