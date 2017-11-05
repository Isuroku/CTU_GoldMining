package student.FSM;

import student.Agent;
import student.Messages.CMessageAgentCount;
import student.Messages.CMessageBase;
import student.Messages.CMessageHello;
import student.Messages.EMessageType;

public class CFSMStateIdle extends CFSMBaseState
{
    public CFSMStateIdle(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.Idle; }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        super.OnMessage(inMessage);

        if(inMessage.MessageType() == EMessageType.AgentCount)
        {
            Memory().SetAgentCount(((CMessageAgentCount)inMessage).Count());
            Sense();
            _owner.SwitchState(EStateType.Patrol);
        }
    }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws Exception
    {
        Sense(false);

        if(_owner.getAgentId() != 1)
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
