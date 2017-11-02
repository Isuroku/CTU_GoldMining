package student;

import mas.agents.AbstractAgent;
import mas.agents.Message;
import mas.agents.SimulationApi;
import mas.agents.StringMessage;
import mas.agents.task.mining.*;
import student.FSM.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Agent extends AbstractAgent
{
    JCDriver _driver;

    CFreeFSM _fsm;

    ArrayList<CFSMBaseState> _states = new ArrayList<CFSMBaseState>();

    long _update_counter = 0;

    public int _agent_count = 1;

    public Agent(int id, InputStream is, OutputStream os, SimulationApi api) throws Exception
    {
        super(id, is, os, api);
        //_driver = new JCDriver(this);

        if(id == 1)
            _states.add(new CFSMStateStartTalkOne(this));
        else
            _states.add(new CFSMStateStartTalkOthers(this));

        _states.add(new CFSMStateIdle(this));

        _fsm = new CFreeFSM(_states.get(0));
    }


    @Override
    public void act() throws Exception
    {
        sendMessage(0, new StringMessage("Hello"));

        while(true) {

            if(_update_counter == 0)
                _fsm.state().OnEnter(null);

            while (messageAvailable()) {
                Message m = readMessage();
                _fsm.state().OnMessage(m);
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
    }
}
