package student.Messages;

import student.Vector2D;

import java.util.ArrayList;

public class CMessageRefreshEnvironment extends CMessageBase
{
    CMessageRefreshEnvironment(int sender_id)
    {
        super(sender_id);
    }

    public CMessageRefreshEnvironment(int sender_id, Vector2D inPos, ArrayList<Vector2D> inObstacles, ArrayList<Vector2D> inGold, ArrayList<Vector2D> inDepots)
    {
        super(sender_id);
        _pos = inPos;
        _obstacles = inObstacles;
        _golds = inGold;
        _depots = inDepots;
    }

    @Override
    public EMessageType MessageType() { return EMessageType.REnv; }

    @Override
    public String toString()
    {
        return super.toString() + String.format("Data: %s", GetBodyCoding());
    }

    @Override
    public boolean Init(String inMsgBody)
    {
        String[] arr = inMsgBody.split(";");

        for(String data : arr)
            LoadData(data);

        return true;
    }

    private void LoadData(String inData)
    {
        for(EDataType dt : EDataType.values())
        {
            String header = dt.toString();
            if(inData.startsWith(header))
            {
                String body = inData.substring(header.length());
                body = body.substring(1, body.length() - 1);

                if(dt == EDataType.Pos)
                {
                    _pos = Vector2D.Parse(body);
                }
                else if(dt == EDataType.Obst)
                {
                    _obstacles = Vector2D.StringToList(body);
                }
                else if(dt == EDataType.Depot)
                {
                    _depots = Vector2D.StringToList(body);
                }
                else if(dt == EDataType.Gold)
                {
                    _golds = Vector2D.StringToList(body);
                }
                return;
            }
        }

        throw new IllegalArgumentException(String.format("Can't find header in inData: %s", inData));
    }

    @Override
    public String GetBodyCoding()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(EDataType.Pos).append("[").append(_pos).append("];");

        if(!_obstacles.isEmpty())
            sb.append(EDataType.Obst).append(_obstacles).append(";");
        if(!_golds.isEmpty())
            sb.append(EDataType.Gold).append(_golds).append(";");
        if(!_depots.isEmpty())
            sb.append(EDataType.Depot).append(_depots).append(";");

        return sb.toString();
    }

    private Vector2D _pos;
    public Vector2D Position() { return _pos; }

    private ArrayList<Vector2D> _obstacles = new ArrayList<>();
    public Iterable<Vector2D> Obstacles() { return _obstacles; }

    private ArrayList<Vector2D> _golds = new ArrayList<>();
    public Iterable<Vector2D> Golds() { return _golds; }

    private ArrayList<Vector2D> _depots = new ArrayList<>();
    public Iterable<Vector2D> Depots() { return _depots; }

    enum EDataType { Pos, Obst, Gold, Depot }

}