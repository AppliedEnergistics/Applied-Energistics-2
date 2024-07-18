package appeng.client.guidebook.scene.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.util.FastColor;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Creates a new animation sprite-sheet for sprite-sheets that have interpolation enabled. Interpolating frames is too
 * hard on the web, so we pre-bake the interpolation data.
 */
final class InterpolatedSpriteBuilder {

    private InterpolatedSpriteBuilder() {
    }

    public static InterpolatedResult interpolate(NativeImage frameSheet,
            int frameWidth,
            int frameHeight,
            int frameRowCount,
            List<SpriteContents.FrameInfo> frames) {

        // ALl unique frames in this animation in the form (from, to, alpha) where non-interpolated frames
        // are represented as (index, index, 0), and otherwise alpha is a float ([0, 1]) for lerping between
        // the from and to frame.
        var interpFrames = new ArrayList<InterpolatedFrame>();
        var interpolated = new IntArrayList();
        for (int i = 0; i < frames.size(); i++) {
            var frame = frames.get(i);
            var nextFrame = frames.get((i + 1) % frames.size());

            for (var j = 0; j < frame.time; j++) {
                var f = j / (float) frame.time;

                var interpFrame = new InterpolatedFrame(frame.index, nextFrame.index, f);
                var newIndex = interpFrames.indexOf(interpFrame);
                if (newIndex != -1) {
                    interpolated.add(newIndex);
                } else {
                    interpolated.add(interpFrames.size());
                    interpFrames.add(interpFrame);
                }
            }
        }

        var height = interpFrames.size() * frameHeight;
        var nativeImage = new NativeImage(frameSheet.format(), frameWidth, height, false);

        for (int i = 0; i < interpFrames.size(); i++) {
            var destX = 0;
            var destY = i * frameHeight;

            var interpFrame = interpFrames.get(i);
            var srcIndexA = interpFrame.aIndex;
            var srcIndexB = interpFrame.bIndex;
            var srcX = (srcIndexA % frameRowCount) * frameWidth;
            var srcY = (srcIndexA / frameRowCount) * frameHeight;

            // Copy frame as-is
            frameSheet.copyRect(nativeImage, srcX, srcY, destX, destY, frameWidth, frameHeight, false, false);

            // Mix frame b into it
            if (srcIndexA != srcIndexB) {
                blend(
                        frameSheet,
                        (srcIndexB % frameRowCount) * frameWidth,
                        (srcIndexB / frameRowCount) * frameHeight,
                        nativeImage,
                        destX,
                        destY,
                        frameWidth,
                        frameHeight,
                        interpFrame.f);
            }
        }

        return new InterpolatedResult(
                nativeImage,
                interpolated.toIntArray(),
                interpFrames.size(),
                1);
    }

    private static void blend(NativeImage source,
            int srcX,
            int srcY,
            NativeImage dest,
            int destX,
            int destY,
            int frameWidth,
            int frameHeight,
            float alpha) {
        for (var y = 0; y < frameHeight; y++) {
            for (var x = 0; x < frameWidth; x++) {
                var srcColor = source.getPixelRGBA(srcX + x, srcY + y);
                var destColor = dest.getPixelRGBA(destX + x, destY + y);
                dest.setPixelRGBA(destX + x, destY + y, FastColor.ARGB32.lerp(alpha, destColor, srcColor));
            }
        }
    }

    record InterpolatedResult(NativeImage frames, int[] indices, int frameCount, int frameRowSize) {
    }

    record InterpolatedFrame(int aIndex, int bIndex, float f) {
        public InterpolatedFrame(int frame) {
            this(frame, frame, 0);
        }

        InterpolatedFrame(int aIndex, int bIndex, float f) {
            // Normalize cases where one or the other is the primary frame
            if (Math.abs(f) < 0.01f) {
                f = 0.0f;
                bIndex = aIndex;
            } else if (Math.abs(1 - f) < 0.01f) {
                f = 0;
                aIndex = bIndex;
            }

            if (bIndex < aIndex) {
                this.aIndex = bIndex;
                this.bIndex = aIndex;
                this.f = 1 - f;
            } else {
                this.aIndex = aIndex;
                this.bIndex = bIndex;
                this.f = f;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            InterpolatedFrame that = (InterpolatedFrame) o;
            return aIndex == that.aIndex && bIndex == that.bIndex && Math.abs(f - that.f) < 0.01f;
        }

        @Override
        public int hashCode() {
            return Objects.hash(aIndex, bIndex, f);
        }
    }

}
