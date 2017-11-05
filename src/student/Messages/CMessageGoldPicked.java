package student.Messages;

import student.Vector2D;

public class CMessageGoldPicked extends CMessageBase
{
    public CMessageGoldPicked(int sender_id)
    {
        super(sender_id);
    }

    public CMessageGoldPicked(int sender_id, Vector2D gold_pos)
    {
        super(sender_id);
        _gold_pos = gold_pos;
    }

    @Override
    public EMessageType MessageType() { return EMessageType.GoldPicked; }

    @Override
    public boolean Init(String inMsgBody)
    {
        _gold_pos = Vector2D.Parse(inMsgBody);
        return true;
    }

    @Override
    public String GetBodyCoding()
    {
        return String.format("%s", _gold_pos);
    }

    @Override
    public String toString() { return super.toString() + String.format("%s", _gold_pos); }


    private Vector2D _gold_pos;

    public Vector2D GoldPos() { return _gold_pos; }
}