package appeng.me.pathfinding;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.me.Grid;
import com.mojang.logging.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CheckingPathingCalculation implements IPathingCalculation {
    private final IGrid grid;
    private final Supplier<? extends IPathingCalculation> first;
    private final Map<IPathItem, Integer> pathItemChannelsFirst = new HashMap<>();
    private final Supplier<? extends IPathingCalculation> second;

    public CheckingPathingCalculation(IGrid grid, Supplier<? extends IPathingCalculation> first, Supplier<? extends IPathingCalculation> second) {
        this.grid = grid;
        this.first = first;
        this.second = second;
    }

    @Override
    public void step() {
        if (!first.get().isFinished()) {
            first.get().step();
            if (first.get().isFinished()) {
                grid.getPivot().beginVisit(new IGridConnectionVisitor() {
                    @Override
                    public void visitConnection(IGridConnection n) {
                        pathItemChannelsFirst.put((IPathItem) n, ((IPathItem)n).getUsedChannelCount());
                    }

                    @Override
                    public boolean visitNode(IGridNode n) {
                        pathItemChannelsFirst.put((IPathItem) n, ((IPathItem)n).getUsedChannelCount());
                        return true;
                    }
                });
            }
            return;
        }

        second.get().step();
        if (second.get().isFinished()) {
            // Compare ;)
            int firstChannelsByBlocks = first.get().getChannelsByBlocks();
            int secondChannelsByBlocks = second.get().getChannelsByBlocks();
            if (firstChannelsByBlocks != secondChannelsByBlocks) {
                LogUtils.getLogger().warn("Channels by blocks mismatch: {} != {}", firstChannelsByBlocks, secondChannelsByBlocks);
            }

            int firstChannelsInUse = first.get().getChannelsInUse();
            int secondChannelsInUse = second.get().getChannelsInUse();
            if (firstChannelsInUse != secondChannelsInUse) {
                LogUtils.getLogger().warn("Channels in use mismatch: {} != {}", firstChannelsInUse, secondChannelsInUse);
            }

            grid.getPivot().beginVisit(new IGridConnectionVisitor() {
                @Override
                public void visitConnection(IGridConnection n) {
                    int firstChannels = pathItemChannelsFirst.get(n);
                    int secondChannels = ((IPathItem) n).getUsedChannelCount();
                    if (firstChannels != secondChannels) {
                        LogUtils.getLogger().warn("Channels mismatch for {}: {} != {}", n, firstChannels, secondChannels);
                    }
                }

                @Override
                public boolean visitNode(IGridNode n) {
                    int firstChannels = pathItemChannelsFirst.get(n);
                    int secondChannels = ((IPathItem) n).getUsedChannelCount();
                    if (firstChannels != secondChannels) {
                        LogUtils.getLogger().warn("Channels mismatch for {}: {} != {}", n, firstChannels, secondChannels);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean isFinished() {
        return first.get().isFinished() && second.get().isFinished();
    }

    @Override
    public int getChannelsByBlocks() {
        return first.get().getChannelsByBlocks();
    }

    @Override
    public int getChannelsInUse() {
        return first.get().getChannelsInUse();
    }
}
