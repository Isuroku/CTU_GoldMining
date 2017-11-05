package student.FSM;

import mas.agents.Message;
import mas.agents.StringMessage;
import student.Agent;
import student.Messages.CMessageAgentCount;
import student.Messages.CMessageBase;
import student.Messages.CMessageHello;
import student.Messages.EMessageType;

public class CFSMStateStartTalkOthers extends CFSMBaseState
{
    public CFSMStateStartTalkOthers(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.TalkOthers; }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        super.OnMessage(inMessage);

        if(inMessage.MessageType() == EMessageType.AgentCount)
        {
            Memory().SetAgentCount(((CMessageAgentCount)inMessage).Count());
            Sense();
            _owner.SwitchState(this, EStateType.Patrol);
        }
    }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws Exception
    {
        Sense(false);
        _owner.SendMessage(1, new CMessageHello(_owner.getAgentId()));
    }

    @Override
    public void OnExit()
    {

    }

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {

    }
}

