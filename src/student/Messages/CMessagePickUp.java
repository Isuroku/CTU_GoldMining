package student.Messages;

public class CMessagePickUp extends CMessageBase
{
    public CMessagePickUp(int sender_id)
    {
        super(sender_id);
    }

    @Override
    public EMessageType MessageType() { return EMessageType.PickUp; }

    @Override
    public boolean Init(String inMsgBody) { return true; }

    @Override
    public String GetBodyCoding() { return ""; }

}
