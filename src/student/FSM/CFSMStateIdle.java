package student.FSM;

import student.Agent;

import java.io.IOException;

public class CFSMStateIdle extends CFSMBaseState
{
    public CFSMStateIdle(Agent owner)
    {
        super(owner);
    }

    @Override
    public EStateType GetStateType() { return EStateType.Idle; }

    @Override
    public void OnEnter(CFSMBaseState inPrevState) throws Exception
    {
        Sense();
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
