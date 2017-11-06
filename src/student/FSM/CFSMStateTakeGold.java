package student.FSM;

import student.Agent;
import student.EStepResult;
import student.Messages.*;
import student.Tuple;
import student.Vector2D;

public class CFSMStateTakeGold extends CFSMBaseState
{
    public CFSMStateTakeGold(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.TakeGold; }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        super.OnMessage(inMessage);
        if(inMessage.MessageType() == EMessageType.GoldPicked)
        {
            CMessageGoldPicked msg = (CMessageGoldPicked) inMessage;
            if(_gold_pos.equals(msg.GoldPos()))
                _owner.SwitchState(EStateType.Patrol);
        }
        else if(inMessage.MessageType() == EMessageType.PickUp)
        {
            if(_gold_pos.equals(Memory().Position()))
            {
                if(Memory().IsAgentAroundMePresent())
                {
                    Pick();
                    Memory().DeleteGold(_gold_pos);
                    _owner.SendBroadcastMessage(new CMessageGoldPicked(_owner.getAgentId(), _gold_pos));
                    _owner.SwitchState(EStateType.GoldToDepot);
                }
            }
        }
    }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws Exception
    {
        Sense(false);
    }


    @Override
    public void OnExit()
    {

    }

    private int _around_me_counter = 0;

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {
        Sense(false);
        if(_gold_pos.equals(Memory().Position()))
            return;

        int AgentOnGold = Memory().IsOtherAgentOnCell(_gold_pos);
        if(AgentOnGold > 0)
        {
            if(AgentOnGold == _friend_id && _target_pos == null)
            {
                _target_pos = Memory().GetFreeNeighbourCell(_gold_pos);
                if(_target_pos != null)
                    Mover().SetTarget(_target_pos);
            } else if(AgentOnGold != _friend_id && _target_pos != null)
            {
                _target_pos = null;
                Mover().SetTarget(_gold_pos);
            }
        }

        if(!_gold_pos.IsNeighbour(Memory().Position()) || AgentOnGold == 0)
        {
            _around_me_counter = 0;
            Tuple<EStepResult, Integer> step_res = Mover().Step();
            if(step_res.Item1 == EStepResult.NoPath || step_res.Item1 == EStepResult.Obstacle)
                Mover().SetTarget(_gold_pos);
        }
        else if(AgentOnGold != _friend_id)
        {
            _around_me_counter = 0;
            _owner.SendMessage(AgentOnGold, new CMessageLetPass(_owner.getAgentId()));
        }
        else if(AgentOnGold == _friend_id)
        {
            _around_me_counter++;
            if(_around_me_counter > 2)
                _owner.SendMessage(_friend_id, new CMessagePickUp(_owner.getAgentId()));
        }
    }

    private Vector2D _gold_pos;
    private Vector2D _target_pos;
    private int _friend_id;

    public void SetGold(Vector2D gold_pos, int inAgent1, int inAgent2) throws Exception
    {
        if(inAgent1 == _owner.getAgentId())
            _friend_id = inAgent2;
        else
            _friend_id = inAgent1;

        _gold_pos = gold_pos;

        _owner.log(String.format("SetGold: %s, Agent1 %d, Agent2 %d", _gold_pos, inAgent1, inAgent2), true);

        Mover().SetTarget(gold_pos);
    }
}