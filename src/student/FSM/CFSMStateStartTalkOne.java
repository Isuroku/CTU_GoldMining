package student.FSM;

import mas.agents.Message;
import mas.agents.StringMessage;
import student.Agent;
import student.Messages.CMessageAgentCount;
import student.Messages.CMessageBase;
import student.Messages.EMessageType;

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
        Sense(false);
    }

    @Override
    public void OnExit()
    {

    }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        super.OnMessage(inMessage);

        if(inMessage.MessageType() == EMessageType.Hello)
            Memory().SetAgentCount(Memory().AgentCount() + 1);
    }

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {
        if(inUpdateNumber == 3)
        {
            _owner.SendBroadcastMessage(new CMessageAgentCount(_owner.getAgentId(), Memory().AgentCount()));
            _owner.SwitchState(this, EStateType.Patrol);
        }
    }

}
