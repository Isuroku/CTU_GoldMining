package student.FSM;

public class CFreeFSM
{
    CFSMBaseState _state; //current state

    public CFSMBaseState state() { return _state; }

    public CFreeFSM(CFSMBaseState state) throws Exception
    {
        _state = state;
    }

    public void Switch(CFSMBaseState inNewState) throws Exception
    {
        CFSMBaseState old = _state;
        _state = inNewState;

        old.OnExit();
        _state.OnEnter(old);
    }
}
