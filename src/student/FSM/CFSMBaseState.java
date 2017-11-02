package student.FSM;

import mas.agents.Message;
import student.Agent;

import java.io.IOException;

public abstract class CFSMBaseState
{
    public abstract EStateType GetStateType();
    public abstract void OnEnter(CFSMBaseState inPrevState) throws IOException, Exception;
    public abstract void OnExit();
    public abstract void Update(long inUpdateNumber) throws Exception;

    public void OnMessage(Message inMessage) throws Exception
    {
        String s =  String.format("Agent %d have received %s" , _owner.getAgentId(), inMessage.stringify());
        _owner.log(s);
    }

    CFSMBaseState(Agent owner)
    {
        _owner = owner;
    }

    protected Agent _owner;
}
