package student.FSM;

import mas.agents.Message;
import mas.agents.StringMessage;
import mas.agents.task.mining.StatusMessage;
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
        StatusMessage sm = sense();
        Memory().InitCoord(sm);
    }

    @Override
    public void OnExit()
    {

    }

    public void OnMessage(Message inMessage) throws Exception
    {
        super.OnMessage(inMessage);

        if(inMessage.stringify().compareToIgnoreCase("Hello") == 0)
            Memory().SetAgenCount(Memory().AgentCount() + 1);
    }

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {
        if(inUpdateNumber < 3)
            return;

        for(int i = 2; i <= Memory().AgentCount(); i++)
            _owner.sendMessage(i, new StringMessage(String.format("AgentCount:%d", Memory().AgentCount())));
        _owner.SwitchState(this, EStateType.Idle);

    }

}
