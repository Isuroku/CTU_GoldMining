package student;

import mas.agents.StringMessage;
import mas.agents.task.mining.StatusMessage;
import student.Messages.CMessageLetPass;

public class CAgentMover
{
    CAgentMover(Agent owner)
    {
        _owner = owner;
    }

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

    Agent _owner;

    Vector2D[] _path;
    int _index;

    CAgentMemory Memory() { return _owner.Memory; }

    void ResetPath()
    {
        _path = null;
        _index = 0;
    }

    public EStepResult Step() throws Exception
    {
        if(_path == null || _index >= _path.length)
        {
            ResetPath();
            return EStepResult.NoPath;
        }

        for(int i = _index; i < _path.length; i++)
        {
            if(!Memory().IsPassableCell(_path[i], false))
            {
                ResetPath();
                return EStepResult.NoPath;
            }
        }

        Vector2D our_pos = _owner.Memory.Position();
        Vector2D step_pos = _path[_index];

        int agent_id = _owner.Memory.GetPassably(step_pos);

        if(agent_id == 0)
            return EStepResult.Obstacle;

        if(agent_id > 0)
        {
            if(agent_id > _owner.getAgentId())
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
                return EStepResult.AgentLower;
           //return EStepResult.AgentHigher;
        }

         return MakeStep(step_pos);
    }

    EStepResult MakeStep(Vector2D pos) throws Exception
    {
        Vector2D our_pos = _owner.Memory.Position();
        int dx = pos.x - our_pos.x;
        int dy = pos.y - our_pos.y;

        if(dx == 0 && dy == 0 || dx != 0 && dy != 0)
        {
            _owner.log( String.format("Error Step: dx %d, dy %d", dx, dy), true);
            return EStepResult.ErrorCalcNextPos;
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
            _owner.log(String.format("Step: change pos into %s", pos), true);

            res = EStepResult.StepDone;
            _index++;
        }
        else
            _owner.log(String.format("Step: can't do step into %s", pos), true);

        return res;
    }

    public StatusMessage Left() throws Exception
    {
        StatusMessage sm = _owner.left();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    public StatusMessage Right() throws Exception
    {
        StatusMessage sm = _owner.right();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    public StatusMessage Up() throws Exception
    {
        StatusMessage sm = _owner.up();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    public StatusMessage Down() throws Exception
    {
        StatusMessage sm = _owner.down();
        Memory().RefreshEnvironment(sm);
        return sm;
    }

    Vector2D GetFreePosition()
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

        EStepResult res = MakeStep(free_pos);
        if(res != EStepResult.StepDone)
            _owner.log("Error - LetPass: can't let pass!", true);
        else
            ResetPath();
    }
}
