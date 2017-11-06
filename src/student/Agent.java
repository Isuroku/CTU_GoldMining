package student;

import mas.agents.AbstractAgent;
import mas.agents.Message;
import mas.agents.SimulationApi;
import mas.agents.StringMessage;
import mas.agents.task.mining.*;
import student.FSM.*;
import student.Messages.CMessageBase;
import student.Messages.CMessageChangeState;
import student.Messages.EMessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Agent extends AbstractAgent
{
    private CFreeFSM _fsm;

    private final ArrayList<CFSMBaseState> _states = new ArrayList<CFSMBaseState>();

    private long _update_counter = 0;

    public CAgentMemory Memory;
    private CCoordinator Coordinator;
    public CAgentMover Mover;

    public Agent(int id, InputStream is, OutputStream os, SimulationApi api) throws Exception
    {
        super(id, is, os, api);

        Memory = new CAgentMemory(this);
        Mover = new CAgentMover(this);

        if(id == 1)
            Coordinator = new CCoordinator(this);

        _states.add(new CFSMStateIdle(this));
        _states.add(new CFSMStatePatrol(this));
        _states.add(new CFSMStateTakeGold(this));
        _states.add(new CFSMStateGoldToDepot(this));

        _fsm = new CFreeFSM(_states.get(0));
    }


    @Override
    public void act() throws Exception
    {
       while(true)
       {
            if(_update_counter == 0)
                _fsm.state().OnEnter(null);

            while (messageAvailable()) {
                Message m = readMessage();

                CMessageBase msg = CMessageBase.CreateMessage(m.getSender(), m.stringify());
                LogMessage(0, msg);

                Memory.OnMessage(msg);

                if(Coordinator != null)
                    Coordinator.OnMessage(msg);

                _fsm.state().OnMessage(msg);
            }

            ++_update_counter;

            if(Coordinator != null)
                Coordinator.Update(_update_counter);

            _fsm.state().Update(_update_counter);

            try {
                Thread.sleep(50);
            } catch(InterruptedException ie) {}
        }
    }

    public CFSMBaseState SwitchState(EStateType inStateType) throws Exception
    {
        for(int i = 0; i < _states.size(); ++i)
        {
            if(_states.get(i).GetStateType() == inStateType)
            {
                EStateType old_state = _fsm.state().GetStateType();
                _fsm.Switch(_states.get(i));
                log(String.format("change state from %s to %s", old_state, inStateType), true);

                Memory.SetAgentState(getAgentId(), _fsm.state().GetStateType());
                SendBroadcastMessage(new CMessageChangeState(getAgentId(), _fsm.state().GetStateType()));

                return _fsm.state();
            }
        }

        throw new RuntimeException(String.format("Can't find state %s", inStateType));
    }

    public void log(Object obj, boolean print) throws Exception
    {
        //if(print)
          //  log(obj);
    }

    private void LogMessage(int Recipient, CMessageBase msg) throws Exception
    {
        /*if(msg.MessageType() == EMessageType.TakeGold ||
                msg.MessageType() == EMessageType.GoldPicked ||
                msg.MessageType() == EMessageType.PickUp)*/

        //if(msg.MessageType() == EMessageType.AgentCount)
          //  log(String.format("%s%s", Recipient == 0 ? "Receive: " : "Send to " + Recipient + ": ", msg.toString()));
    }

    public void SendMessage(int Recipient, CMessageBase msg) throws Exception
    {
        LogMessage(Recipient, msg);
        sendMessage(Recipient, msg.CodeToMessage());
    }

    public void SendBroadcastMessage(CMessageBase msg) throws Exception
    {
        for(int i = 1; i <= Memory.AgentCount(); i++)
        {
            if(i != getAgentId())
                SendMessage(i, msg);
            else if(Coordinator != null)
                Coordinator.OnMessage(msg);
        }
    }
}
