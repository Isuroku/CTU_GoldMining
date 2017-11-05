package student.Messages;

import mas.agents.Message;
import mas.agents.StringMessage;

public abstract class CMessageBase
{
    protected int _sender_id;

    public CMessageBase(int sender_id)
    {
        _sender_id = sender_id;
    }

    public int Sender() { return _sender_id; }

    @Override
    public String toString() { return String.format("Msg %s from Agent %d. ", MessageType(), _sender_id); }

    public abstract EMessageType MessageType();
    public abstract boolean Init(String inMsgBody);

    public static CMessageBase CreateMessage(int SenderId, String inMsgStr)
    {
        for(EMessageType mt : EMessageType.values())
        {
            String header = mt.toString();
            if(inMsgStr.startsWith(header))
            {
                String body = inMsgStr.substring(header.length() + 1);
                return CreateMessage(SenderId, mt, body);
            }
        }
        throw new IllegalArgumentException(String.format("SenderId %d, inMsgStr %s", SenderId, inMsgStr));
    }

    public static CMessageBase CreateMessage(int SenderId, EMessageType inMsgType, String inMsgBody)
    {
        CMessageBase msg = null;
        switch(inMsgType)
        {
            case Hello: msg = new CMessageHello(SenderId); break;
            case AgentCount: msg = new CMessageAgentCount(SenderId); break;
            case LetPass: msg = new CMessageLetPass(SenderId); break;
            case REnv: msg = new CMessageRefreshEnvironment(SenderId); break;
        }

        if(msg != null && msg.Init(inMsgBody))
            return msg;

        throw new IllegalArgumentException(String.format("Can't CreateMessage: type %s, body %s", inMsgBody, inMsgType));
    }

    public Message CodeToMessage()
    {
        String str = String.format("%s:%s", MessageType(), GetBodyCoding());
        return new StringMessage(str);
    }

    public abstract String GetBodyCoding();
}
