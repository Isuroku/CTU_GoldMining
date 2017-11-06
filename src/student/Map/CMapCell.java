package student.Map;

import student.Vector2D;

class CMapCell implements Comparable<CMapCell>
{
    final Vector2D Pos;

    boolean IsPassable(boolean agent_obstacle)
    {
        if(agent_obstacle)
            return _passable && AgentId == 0;
        return _passable;
    }

    int GetPassably()
    {
        if(AgentId != 0)
            return AgentId;
        if(!_passable)
            return 0;
        return -1;
    }

    boolean IsObstacleFree() { return _passable ; }
    void SetObstacle() { _passable = false; }

    private boolean _passable = true;

    int AgentId = 0;

    boolean Gold = false;
    boolean Depot = false;

    int WaveLength = 0;
    long WaveNumber = Long.MIN_VALUE;
    CMapCell Prev;

    CMapCell(Vector2D inPos) { Pos = inPos; }

    @Override
    public String toString() {
        return String.format("%s: %s%s%s agent %d [wl:%d, wn: %d, p: %s]",
                Pos,
                _passable ? " passable" : "",
                Gold ? " gold" : "",
                Depot ? " depot" : "",
                AgentId,
                WaveLength, WaveNumber, Prev == null ? "none" : Prev.Pos);
    }

    @Override
    public int compareTo(CMapCell o)
    {
        return WaveLength - o.WaveLength;
    }
}