package student;

import mas.agents.task.mining.StatusMessage;
import student.FSM.EStateType;
import student.Messages.CMessageLetPass;

public class CAgentMover
{
    CAgentMover(Agent owner)
    {
        _owner = owner;
    }

    private final Agent _owner;

    private Vector2D[] _path;
    private int _index;
    private int _agent_lower = 0;

    private CAgentMemory Memory() { return _owner.Memory; }

    public Vector2D GetTarget()
    {
        if(_path != null)
            return _path[_path.length - 1];
        return null;
    }

    public boolean SetTarget(Vector2D inTarget)
    {
        if(_path == null || !_path[_path.length - 1].equals(inTarget))
        {
            _path = _owner.Memory.GetPath(inTarget);
            _index = 0;
        }

        return _path != null;
    }

    private void ResetPath()
    {
        _path = null;
        _index = 0;
        _agent_lower = 0;
    }

    public Tuple<EStepResult, Integer> Step() throws Exception
    {
        if(_path == null || _index >= _path.length)
        {
            ResetPath();
            return new Tuple<>(EStepResult.NoPath, 0);
        }

        for(int i = _index; i < _path.length; i++)
        {
            if(!Memory().IsPassableCell(_path[i], false))
            {
                ResetPath();
                return new Tuple<>(EStepResult.NoPath, 0);
            }
        }

        Vector2D our_pos = _owner.Memory.Position();
        Vector2D step_pos = _path[_index];

        int agent_id = _owner.Memory.GetPassably(step_pos);

        if(agent_id == 0)
        {
            ResetPath();
            return new Tuple<>(EStepResult.Obstacle, 0);
        }

        if(agent_id > 0)
        {
            int pO = GetPrioritize(agent_id);
            int pT = GetPrioritize(_owner.getAgentId());
            if(pO > pT)
            {
                Vector2D free_pos = GetFreePosition();
                if(free_pos != null)
                {
                    step_pos = free_pos;
                    ResetPath();
                }
                else
                    _owner.SendMessage(agent_id, new CMessageLetPass(_owner.getAgentId()));
            }
            else
            {
                _agent_lower++;
                if(_agent_lower > 7)
                    _owner.SendMessage(agent_id, new CMessageLetPass(_owner.getAgentId()));
                return new Tuple<>(EStepResult.AgentLower, agent_id);
            }
           //return EStepResult.AgentHigher;
        }

         return MakeStep(step_pos);
    }

    private int GetPrioritize(int inAgentId)
    {
        EStateType state = Memory().GetAgentState(inAgentId);
        int p = inAgentId;
        switch(state)
        {
            case TakeGold: p *= 10; break;
            case GoldToDepot: p *= 100; break;
        }
        return p;
    }

    private Tuple<EStepResult, Integer> MakeStep(Vector2D pos) throws Exception
    {
        Vector2D our_pos = _owner.Memory.Position();
        int dx = pos.x - our_pos.x;
        int dy = pos.y - our_pos.y;

        if(dx == 0 && dy == 0 || dx != 0 && dy != 0)
        {
            _owner.log( String.format("Error Step: dx %d, dy %d", dx, dy), false);
            return new Tuple<>(EStepResult.ErrorCalcNextPos, 0);
        }

        EStepResult res = EStepResult.ErrorStep;
        if(dx > 0)
            Right();
        else if(dx < 0)
            Left();
        else if(dy < 0)
            Up();
        else if(dy > 0)
            Down();

        if(Memory().Position().equals(pos))
        {
            _owner.log(String.format("Step: change pos into %s", pos), false);

            res = EStepResult.StepDone;
            _index++;
        }
        else if(!Memory().Position().IsNeighbour(pos))
            _owner.log(String.format("Step: can't do step into %s", pos), true);

        return new Tuple<>(res, 0);
    }

    private StatusMessage Left() throws Exception
    {
        StatusMessage sm = _owner.left();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    private StatusMessage Right() throws Exception
    {
        StatusMessage sm = _owner.right();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    private StatusMessage Up() throws Exception
    {
        StatusMessage sm = _owner.up();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    private StatusMessage Down() throws Exception
    {
        StatusMessage sm = _owner.down();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    private Vector2D GetFreePosition()
    {
        Vector2D[] neighbours = Memory().Position().GetNeighbours(Memory().GetMapRect());

        Vector2D pos = null;
        for(int i = 0; i < neighbours.length && pos == null; ++i)
        {
            Vector2D p = neighbours[i];
            if(Memory().IsPassableCell(p, true))
                pos = p;
        }

        return pos;
    }

    public void LetPass() throws Exception
    {
        Vector2D free_pos = GetFreePosition();
        if(free_pos == null)
        {
            _owner.log("Error - LetPass: can't find free pos!", true);
            return;
        }

        Tuple<EStepResult, Integer> res = MakeStep(free_pos);
        if(res.Item1 != EStepResult.StepDone)
            _owner.log("Error - LetPass: can't let pass!", true);
        else
            ResetPath();
    }
}
