package student.FSM;

import mas.agents.task.mining.StatusMessage;
import student.*;
import student.Messages.CMessageBase;
import student.Messages.EMessageType;

import java.util.ArrayList;
import java.util.Collections;

public abstract class CFSMBaseState
{
    public abstract EStateType GetStateType();
    public abstract void OnEnter(CFSMBaseState inPrevState) throws Exception;
    public abstract void OnExit();
    public abstract void Update(long inUpdateNumber) throws Exception;

    CAgentMemory Memory() { return _owner.Memory; }
    CAgentMover Mover() { return _owner.Mover; }

    private int _let_pass_count = 0;

    public void OnMessage(CMessageBase inMessage) throws Exception
    {
        if(inMessage.MessageType() == EMessageType.LetPass)
        {
            if(_let_pass_count == 0)
            {
                ArrayList<Vector2D> poses = Memory().GetNeighbourhoodPoses(5, true);
                if(poses.isEmpty())
                    poses = Memory().GetNeighbourhoodPoses(5, false);

                if(poses.isEmpty())
                    Mover().LetPass();
                else
                {
                    Collections.shuffle(poses);
                    Mover().SetTarget(poses.get(0));
                }


            }
            else if(_let_pass_count > 5)
            {
                _let_pass_count = 0;
            }
            else
                _let_pass_count++;
        }
    }

    StatusMessage Pick() throws Exception
    {
        StatusMessage sm = _owner.pick();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    StatusMessage Drop() throws Exception
    {
        StatusMessage sm = _owner.drop();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    StatusMessage Sense() throws Exception
    {
        return Sense(true);
    }

    StatusMessage Sense(boolean send) throws Exception
    {
        StatusMessage sm = _owner.sense();
        Memory().RefreshEnvironment(sm, send);
        return sm;
    }

    CFSMBaseState(Agent owner)
    {
        _owner = owner;
    }

    final Agent _owner;
}
