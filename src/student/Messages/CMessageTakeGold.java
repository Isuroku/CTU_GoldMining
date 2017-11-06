package student.Messages;

import student.Vector2D;

public class CMessageTakeGold extends CMessageBase
{
    public CMessageTakeGold(int sender_id)
    {
        super(sender_id);
    }

    public CMessageTakeGold(int sender_id, Vector2D gold_pos, int agent1, int agent2)
    {
        super(sender_id);
        _gold_pos = gold_pos;
        _agent1 = agent1;
        _agent2 = agent2;
    }

    @Override
    public EMessageType MessageType() { return EMessageType.TakeGold; }

    @Override
    public boolean Init(String inMsgBody)
    {
        String[] arr = inMsgBody.split("_");
        if(arr.length != 2)
            return false;

        _gold_pos = Vector2D.Parse(arr[0]);

        String[] arr2 = arr[1].split("-");
        _agent1 = Integer.parseInt(arr2[0]);
        _agent2 = Integer.parseInt(arr2[1]);

        return true;
    }

    @Override
    public String GetBodyCoding()
    {
        return String.format("%s_%d-%d", _gold_pos, _agent1, _agent2);
    }

    @Override
    public String toString() { return super.toString() + String.format("%s [%d-%d]", _gold_pos, _agent1, _agent2); }


    private Vector2D _gold_pos;
    private int _agent1;
    private int _agent2;

    public Vector2D GoldPos() { return _gold_pos; }
    public int Agent1() { return _agent1; }
    public int Agent2() { return _agent2; }
}