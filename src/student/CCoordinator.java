package student;

import student.FSM.CFSMStateTakeGold;
import student.FSM.EStateType;
import student.Messages.*;

import java.util.HashSet;

public class CCoordinator
{
    private final Agent _owner;

    CCoordinator(Agent owner)
    {
        _owner = owner;
    }

    private CAgentMemory Memory() { return _owner.Memory; }
    //CAgentMover Mover() { return _owner.Mover; }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        if(inMessage.MessageType() == EMessageType.Hello)
            Memory().SetAgentCount(Memory().AgentCount() + 1);
        else if(inMessage.MessageType() == EMessageType.GoldPicked)
        {
            CMessageGoldPicked msg = (CMessageGoldPicked) inMessage;
            _owner.log(String.format("Coordinator unprocessed gold in %s", msg.GoldPos()), true);
            _processed_golds.remove(msg.GoldPos());
        }
    }

    public void Update(long inUpdateNumber) throws Exception
    {
        if(inUpdateNumber < 5)
            return;
        if(inUpdateNumber == 5)
        {
            _owner.SendBroadcastMessage(new CMessageAgentCount(_owner.getAgentId(), Memory().AgentCount()));
            _owner.SwitchState(EStateType.Patrol);
            return;
        }


        if(!Memory().IsDepotsPresent())
            return;

        if(_processed_golds.size() * 2 >= Memory().AgentCount())
            return;

        Vector2D[] gold_poses = Memory().GetGolds(); //ссылки на позиции в ячейках

        boolean agent_absent = false;
        for(int i = 0; i < gold_poses.length && !agent_absent; ++i)
        {
            Vector2D gold_pos = gold_poses[i];

            if(_processed_golds.contains(gold_pos))
                continue;

            Integer[] agents = Memory().ClosestFreeAgent(gold_pos);
            if(agents == null || agents.length < 2)
            {
                agent_absent = true;
                continue;
            }

            int a1 = agents[0];
            int a2 = agents[1];

            for(Integer agent_id : agents)
            {
                if(agent_id == _owner.getAgentId())
                {
                    CFSMStateTakeGold state = (CFSMStateTakeGold) _owner.SwitchState(EStateType.TakeGold);
                    state.SetGold(gold_pos, a1, a2);
                } else
                {
                    CMessageTakeGold msg = new CMessageTakeGold(_owner.getAgentId(), gold_pos, a1, a2);
                    _owner.SendMessage(agent_id, msg);
                    Memory().SetAgentState(agent_id, EStateType.TakeGold);
                }

                _processed_golds.add(gold_pos);
            }
        }

    }

    private final HashSet<Vector2D> _processed_golds = new HashSet<>();
}
