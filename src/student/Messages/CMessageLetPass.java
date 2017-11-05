package student.Messages;

public class CMessageLetPass extends CMessageBase
{
    public CMessageLetPass(int sender_id)
    {
        super(sender_id);
    }

    @Override
    public EMessageType MessageType() { return EMessageType.LetPass; }

    @Override
    public boolean Init(String inMsgBody) { return true; }

    @Override
    public String GetBodyCoding() { return ""; }

}

