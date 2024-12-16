package appeng.me.pathfinding;

public interface IPathingCalculation {
    void step();

    boolean isFinished();

    int getChannelsByBlocks();
    int getChannelsInUse();
}
