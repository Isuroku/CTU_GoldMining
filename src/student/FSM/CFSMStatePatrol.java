package student.FSM;

import student.Agent;
import student.EStepResult;
import student.Rect2D;
import student.Vector2D;

public class CFSMStatePatrol extends CFSMBaseState
{
    public CFSMStatePatrol(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.Patrol; }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws Exception
    {
        Sense();
        ChangeTarget();
    }


    @Override
    public void OnExit()
    {

    }

    @Override
    public void Update(long inUpdateNumber) throws Exception
    {


        EStepResult step_res = Mover().Step();
        if(step_res == EStepResult.NoPath || step_res == EStepResult.Obstacle)
            ChangeTarget();
    }



    private Vector2D _dir = new Vector2D(1, 1);

    private Vector2D GetNexTarget(Rect2D inZone, Vector2D inPos)
    {
        if(inPos.x < inZone.Left)
            return new Vector2D(inZone.Left, inPos.y);
        if(inPos.x > inZone.Right)
            return new Vector2D(inZone.Right, inPos.y);

        int x = inPos.x;
        int y = inPos.y;
        y += _dir.y;

        if(y < inZone.Top || y > inZone.Bottom)
        {
            _dir.y *= -1;
            y += _dir.y; //вернули на место

            x += _dir.x;
            if(x > inZone.Right)
                return new Vector2D(inZone.Left, inZone.Top);
        }
        return new Vector2D(x, y);
    }

    private void ChangeTarget() throws Exception
    {
        Vector2D our_pos = Memory().Position();
        Rect2D z = Memory().PatrolZone();

        Vector2D target = Mover().GetTarget();
        if(target == null)
            if(z.InsideSoft(our_pos))
                target = our_pos;
            else
                target = new Vector2D(z.Left, our_pos.y);

        int circle_count = (z.Width() + 1) * (z.Height() + 1);

        while((!Memory().IsPassableCell(target, false) || our_pos.equals(target) || !Mover().SetTarget(target)) && circle_count > 0)
        {
            target = GetNexTarget(z, target);
            circle_count--;
        }

        if(circle_count == 0)
        {
            _owner.log("Error ChangeTarget: Can't find new target in patrol!", true);
            return;
        }

        _owner.log(String.format("Change target: %s", target), true);
        //return true;
    }
}
