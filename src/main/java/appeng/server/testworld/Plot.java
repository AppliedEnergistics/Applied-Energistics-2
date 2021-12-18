package appeng.server.testworld;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

class Plot implements PlotBuilder {
    private final ResourceLocation id;

    private final List<BuildAction> buildActions = new ArrayList<>();

    private Test test;

    public BoundingBox getBounds() {
        return BoundingBox.encapsulatingBoxes(buildActions.stream().map(BuildAction::getBoundingBox).toList())
                .orElseThrow();
    }

    private static final Pattern RANGE = Pattern.compile("\\[(-?\\d+),(-?\\d+)]");

    public Plot(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addBuildAction(BuildAction action) {
        buildActions.add(action);
    }

    @Override
    public PlotBuilder transform(Function<BoundingBox, BoundingBox> transform) {
        return new TransformingPlotBuilder(this, transform);
    }

    // Format: x y z
    // Each number can be a range in the form of [from,to]
    public BoundingBox bb(String def) {
        var parts = def.split("\\s+");
        var p = new int[6];
        Preconditions.checkArgument(parts.length * 2 == p.length);
        for (int i = 0; i < parts.length; i++) {
            var part = parts[i];
            var rangeMatch = RANGE.matcher(part);
            if (rangeMatch.matches()) {
                p[i * 2] = Integer.parseInt(rangeMatch.group(1));
                p[i * 2 + 1] = Integer.parseInt(rangeMatch.group(2));
            } else {
                p[i * 2] = p[i * 2 + 1] = Integer.parseInt(part);
            }
        }
        Preconditions.checkArgument(p[0] <= p[1], "Invalid bb: %s", def);
        Preconditions.checkArgument(p[2] <= p[3], "Invalid bb: %s", def);
        Preconditions.checkArgument(p[4] <= p[5], "Invalid bb: %s", def);
        return new BoundingBox(p[0], p[2], p[4], p[1], p[3], p[5]);
    }

    public void build(ServerLevel level, Player player, BlockPos origin) {
        for (var action : buildActions) {
            action.build(level, player, origin);
        }
    }

    public Test getTest() {
        return test;
    }

    @Override
    public Test test(Consumer<PlotTestHelper> testFunction) {
        this.test = new Test(testFunction);
        return this.test;
    }
}
