package student.Messages;

import student.FSM.EStateType;

public class CMessageChangeState extends CMessageBase
{
    public CMessageChangeState(int sender_id)
    {
        super(sender_id);
    }

    public CMessageChangeState(int sender_id, EStateType state)
    {
        super(sender_id);
        _state = state;
    }

    @Override
    public EMessageType MessageType() { return EMessageType.ChangeState; }

    @Override
    public boolean Init(String inMsgBody)
    {
        _state = EStateType.valueOf(inMsgBody);
        return true;
    }

    @Override
    public String GetBodyCoding()
    {
        return String.format("%s", _state);
    }

    @Override
    public String toString() { return super.toString() + String.format("State %s.", _state); }


    private EStateType _state = EStateType.Idle;

    public EStateType State() { return _state; }
}
