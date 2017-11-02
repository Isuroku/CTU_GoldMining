package student.FSM;

import mas.agents.Message;
import mas.agents.StringMessage;
import student.Agent;

import java.io.IOException;

public class CFSMStateStartTalkOne extends CFSMBaseState
{
    public CFSMStateStartTalkOne(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.TalkOne; }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws Exception
    {

    }

    @Override
    public void OnExit()
    {

    }

    public void OnMessage(Message inMessage) throws Exception
    {
        super.OnMessage(inMessage);

        if(inMessage.stringify().compareToIgnoreCase("Hello") == 0)
        {
            _owner._agent_count++;
        }
    }

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {
        if(inUpdateNumber < 3)
        return;

        for(int i = 2; i <= _owner._agent_count; i++)
            _owner.sendMessage(i, new StringMessage(String.format("AgentCount:%d", _owner._agent_count)));
        _owner.SwitchState(this, EStateType.Idle);

    }

}
