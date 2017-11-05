package student.FSM;

import student.Agent;
import student.EStepResult;
import student.Messages.CMessageBase;
import student.Messages.CMessageGoldPicked;
import student.Messages.EMessageType;
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

    int _around_me_counter = 0;

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {
        Sense(false);
        if(_gold_pos.equals(Memory().Position()))
        {
            if(Memory().IsAgentAroundMePresent())
            {
                _around_me_counter++;
                if(_around_me_counter > 3)
                {
                    Pick();
                    _owner.SendBroadcastMessage(new CMessageGoldPicked(_owner.getAgentId(), _gold_pos));
                    _owner.SwitchState(EStateType.GoldToDepot);
                }
            }
            else
                _around_me_counter = 0;
        }
        else
        {
            if(Memory().IsOtherAgentOnCell(_gold_pos))
            {
                if(_target_pos == null)
                {
                    _target_pos = Memory().GetFreeNeighbourCell(_gold_pos);
                    if(_target_pos != null)
                        Mover().SetTarget(_target_pos);
                }
            }
            else if(_target_pos != null)
            {
                _target_pos = null;
                Mover().SetTarget(_gold_pos);
            }

            if(_target_pos == null || !Memory().Position().IsNeighbour(_gold_pos))
            {
                EStepResult step_res = Mover().Step();
                if(step_res == EStepResult.NoPath || step_res == EStepResult.Obstacle)
                    Mover().SetTarget(_gold_pos);
            }
        }
    }

    Vector2D _gold_pos;
    Vector2D _target_pos;

    public void SetGold(Vector2D gold_pos) throws Exception
    {
        _gold_pos = gold_pos;

        _owner.log(String.format("SetGold: %s", _gold_pos), true);

        Mover().SetTarget(gold_pos);
    }
}