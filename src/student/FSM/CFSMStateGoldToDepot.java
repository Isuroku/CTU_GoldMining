package student.FSM;

import student.Agent;
import student.EStepResult;
import student.Messages.CMessageBase;
import student.Vector2D;

public class CFSMStateGoldToDepot extends CFSMBaseState
{
    Vector2D _depot_pos;

    public CFSMStateGoldToDepot(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.GoldToDepot; }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        super.OnMessage(inMessage);
    }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws Exception
    {
        _depot_pos = Memory().GetNearestDepot();
        Mover().SetTarget(_depot_pos);
    }


    @Override
    public void OnExit()
    {

    }

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {
        EStepResult step_res = Mover().Step();
        if(step_res != EStepResult.StepDone)
            Mover().SetTarget(_depot_pos);

        if(Memory().Position().equals(_depot_pos))
        {
            Drop();
            _owner.SwitchState(EStateType.Patrol);
        }
    }
}