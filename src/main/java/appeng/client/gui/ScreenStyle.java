package appeng.client.gui;

import appeng.client.gui.layout.SlotGridLayout;
import appeng.container.SlotSemantic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ScreenStyle {

    public static final Gson GSON = new GsonBuilder().create();

    private final Map<SlotSemantic, SlotPosition> slots = new EnumMap<>(SlotSemantic.class);

    private final List<String> includes = new ArrayList<>();

    public List<String> getIncludes() {
        return includes;
    }

    public Map<SlotSemantic, SlotPosition> getSlots() {
        return slots;
    }

    public ScreenStyle merge(ScreenStyle otherStyle) {
        ScreenStyle merged = new ScreenStyle();
        // Slots are merged by simply overwriting
        merged.slots.putAll(this.slots);
        merged.slots.putAll(otherStyle.slots);
        return merged;
    }

    public static class SlotPosition {

        @Nullable
        private SlotGridLayout grid;

        @Nullable
        private Integer left;

        @Nullable
        private Integer top;

        @Nullable
        private Integer right;

        @Nullable
        private Integer bottom;

        @Nullable
        public SlotGridLayout getGrid() {
            return grid;
        }

        public void setGrid(@Nullable SlotGridLayout grid) {
            this.grid = grid;
        }

        public Integer getLeft() {
            return left;
        }

        public void setLeft(Integer left) {
            this.left = left;
        }

        public Integer getTop() {
            return top;
        }

        public void setTop(Integer top) {
            this.top = top;
        }

        public Integer getRight() {
            return right;
        }

        public void setRight(Integer right) {
            this.right = right;
        }

        public Integer getBottom() {
            return bottom;
        }

        public void setBottom(Integer bottom) {
            this.bottom = bottom;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();


            return result.toString();
        }
    }

}
