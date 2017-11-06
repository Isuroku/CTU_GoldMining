package student.FSM;

import student.*;
import student.Messages.CMessageBase;
import student.Messages.CMessageTakeGold;
import student.Messages.EMessageType;

public class CFSMStatePatrol extends CFSMBaseState
{
    public CFSMStatePatrol(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.Patrol; }

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        super.OnMessage(inMessage);

        if(inMessage.MessageType() == EMessageType.TakeGold)
        {
            CFSMStateTakeGold state = (CFSMStateTakeGold) _owner.SwitchState(EStateType.TakeGold);
            CMessageTakeGold msg = (CMessageTakeGold)inMessage;
            state.SetGold(msg.GoldPos(), msg.Agent1(), msg.Agent2());
        }
    }

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
        Tuple<EStepResult, Integer> step_res = Mover().Step();
        if(step_res.Item1 == EStepResult.NoPath || step_res.Item1 == EStepResult.Obstacle)
            ChangeTarget();
    }



    private final Vector2D _dir = new Vector2D(1, 1);

    private Vector2D GetNexTarget(Rect2D inZone, Vector2D inPos)
    {
        if(inPos.x < inZone.Left)
            return new Vector2D(inZone.Left, inPos.y);
        if(inPos.x > inZone.Right)
            return new Vector2D(inZone.Right, inPos.y);


        Vector2D pos = Memory().GetFirstDarkPoint();
        if(pos != null)
        {
            Memory().CheckCoord(pos);
            return pos;
        }

        int x = inPos.x;
        int y = inPos.y;
        y += _dir.y;

        if(y < inZone.Top || y > inZone.Bottom)
        {
            _dir.y *= -1;
            y += _dir.y; //вернули на место

            x += _dir.x;
            if(x > inZone.Right)
            {
                Vector2D p = new Vector2D(inZone.Left, inZone.Top);
                Memory().CheckCoord(p);
                return p;
            }
        }
        Vector2D p = new Vector2D(x, y);
        Memory().CheckCoord(p);
        return p;
    }

    private void ChangeTarget() throws Exception
    {
        Vector2D our_pos = Memory().Position();
        Rect2D z = Memory().PatrolZone();

        Vector2D target = Mover().GetTarget();
        if(target == null)
        {
            if(z.InsideSoft(our_pos))
                target = our_pos;
            else
                target = new Vector2D(z.Left, our_pos.y);
        }

        Memory().CheckCoord(target);

        int circle_count = (z.Width() + 1) * (z.Height() + 1);

        while((!Memory().IsPassableCell(target, false) || our_pos.equals(target) || !Mover().SetTarget(target)) && circle_count > 0)
        {
            target = GetNexTarget(z, target);
            Memory().CheckCoord(target);
            circle_count--;
        }

        if(circle_count == 0)
        {
            _owner.log("Error ChangeTarget: Can't find new target in patrol!", true);
            return;
        }

        _owner.log(String.format("Change target: %s", target), false);
        //return true;
    }
}
