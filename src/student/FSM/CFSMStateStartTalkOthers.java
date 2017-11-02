package student.FSM;

import mas.agents.Message;
import mas.agents.StringMessage;
import student.Agent;

import java.io.IOException;

public class CFSMStateStartTalkOthers extends CFSMBaseState
{
    public CFSMStateStartTalkOthers(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.TalkOthers; }

    public void OnMessage(Message inMessage) throws Exception
    {
        super.OnMessage(inMessage);

        String msg_text = inMessage.stringify();

        if(msg_text.startsWith("AgentCount"))
        {
            String s = msg_text.substring("AgentCount".length() + 1);
            _owner._agent_count = Integer.parseInt(s);
            _owner.SwitchState(this, EStateType.Idle);
        }
    }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws IOException
    {
        _owner.sendMessage(1, new StringMessage("Hello"));
    }

    @Override
    public void OnExit()
    {

    }

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {
    }
}

