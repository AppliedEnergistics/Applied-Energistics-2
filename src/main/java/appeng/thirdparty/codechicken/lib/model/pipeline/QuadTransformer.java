/*
 * This file is part of CodeChickenLib.
 * Copyright (c) 2018, covers1624, All rights reserved.
 *
 * CodeChickenLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * CodeChickenLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CodeChickenLib. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.thirdparty.codechicken.lib.model.pipeline;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.ISmartVertexConsumer;
import appeng.thirdparty.codechicken.lib.model.Quad;

/**
 * Base class for a simple QuadTransformer. Operates on BakedQuads. QuadTransformers can be piped into each other at no
 * performance penalty.
 *
 * @author covers1624
 */
public abstract class QuadTransformer implements IVertexConsumer, ISmartVertexConsumer, IPipelineConsumer {

    protected CachedFormat format;
    protected IVertexConsumer consumer;
    protected Quad quad;

    /**
     * Used for the BakedPipeline.
     */
    protected QuadTransformer() {
        this.quad = new Quad();
    }

    public QuadTransformer(IVertexConsumer consumer) {
        this(consumer.getVertexFormat(), consumer);
    }

    public QuadTransformer(VertexFormat format, IVertexConsumer consumer) {
        this(CachedFormat.lookup(format), consumer);
    }

    public QuadTransformer(CachedFormat format, IVertexConsumer consumer) {
        this.format = format;
        this.consumer = consumer;
        this.quad = new Quad(format);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void reset(CachedFormat format) {
        this.format = format;
        this.quad.reset(format);
    }

    @Override
    public void setParent(IVertexConsumer parent) {
        this.consumer = parent;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void setInputQuad(Quad quad) {
        if (this.consumer instanceof IPipelineConsumer) {
            ((IPipelineConsumer) this.consumer).setInputQuad(quad);
        }
    }

    // @formatter:off
    @Override
    public VertexFormat getVertexFormat() {
        return this.format.format;
    }

    @Override
    public void setQuadTint(int tint) {
        this.quad.setQuadTint(tint);
    }

    @Override
    public void setQuadOrientation(Direction orientation) {
        this.quad.setQuadOrientation(orientation);
    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        this.quad.setApplyDiffuseLighting(diffuse);
    }

    @Override
    public void setTexture(TextureAtlasSprite texture) {
        this.quad.setTexture(texture);
    }
    // @formatter:on

    @Override
    public void put(int element, float... data) {
        this.quad.put(element, data);
        if (this.quad.full) {
            this.onFull();
        }
    }

    @Override
    public void put(Quad quad) {
        this.quad.put(quad);
        this.onFull();
    }

    /**
     * Called to transform the vertices.
     *
     * @return If the transformer should pipe the quad.
     */
    public abstract boolean transform();

    public void onFull() {
        if (this.transform()) {
            this.quad.pipe(this.consumer);
        }
    }

    // Should be small enough.
    private final static double EPSILON = 0.00001;

    public static boolean epsComp(float a, float b) {
        if (a == b) {
            return true;
        } else {
            return Math.abs(a - b) < EPSILON;
        }
    }
}
