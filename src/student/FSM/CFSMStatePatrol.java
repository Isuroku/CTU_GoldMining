package student.FSM;

import student.Agent;

import java.io.IOException;

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
        sense();
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
