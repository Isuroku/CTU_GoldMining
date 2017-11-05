package student.Messages;

public class CMessageAgentCount extends CMessageBase
{
    CMessageAgentCount(int sender_id)
    {
        super(sender_id);
    }

    public CMessageAgentCount(int sender_id, int count)
    {
        super(sender_id);
        _count = count;
    }

    @Override
    public EMessageType MessageType() { return EMessageType.AgentCount; }

    @Override
    public boolean Init(String inMsgBody)
    {
        _count = Integer.parseInt(inMsgBody);
        return true;
    }

    @Override
    public String GetBodyCoding()
    {
        return String.format("%d", _count);
    }

    @Override
    public String toString() { return super.toString() + String.format("Count %d.", _count); }


    private int _count = 0;

    public int Count() { return _count; }
}
