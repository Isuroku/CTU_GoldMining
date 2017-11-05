package student.Messages;

public class CMessageHello extends CMessageBase
{
    public CMessageHello(int sender_id)
    {
        super(sender_id);
    }

    @Override
    public EMessageType MessageType() { return EMessageType.Hello; }

    @Override
    public boolean Init(String inMsgBody) { return true; }

    @Override
    public String GetBodyCoding() { return ""; }

}
