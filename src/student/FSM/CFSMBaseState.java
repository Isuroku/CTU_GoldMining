package student.FSM;

import mas.agents.task.mining.StatusMessage;
import student.*;
import student.Messages.CMessageBase;
import student.Messages.EMessageType;

public abstract class CFSMBaseState
{
    public abstract EStateType GetStateType();
    public abstract void OnEnter(CFSMBaseState inPrevState) throws Exception;
    public abstract void OnExit();
    public abstract void Update(long inUpdateNumber) throws Exception;

    CAgentMemory Memory() { return _owner.Memory; }
    CAgentMover Mover() { return _owner.Mover; }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        if(inMessage.MessageType() == EMessageType.LetPass)
            Mover().LetPass();
    }

    public StatusMessage Pick() throws Exception
    {
        StatusMessage sm = _owner.pick();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    public StatusMessage Drop() throws Exception
    {
        StatusMessage sm = _owner.drop();
        Memory().RefreshEnvironment(sm);
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
            Memory().RefreshEnvironment(sm);
        return sm;
    }

    CFSMBaseState(Agent owner)
    {
        _owner = owner;
    }

    protected Agent _owner;
}
