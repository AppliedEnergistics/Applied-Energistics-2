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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.ISmartVertexConsumer;
import appeng.thirdparty.codechicken.lib.model.Quad;

/**
 * The BakedPipeline! Basically this allows us to efficiently transform a BakedQuad, the Pipeline has Elements, each
 * element has a name, state and a transformer, you can enable and disable elements easily, you can also grab the
 * underlying transformer for the element if you need to set its state before rendering.
 * <p>
 * The BakedPipeline is final once created, you cannot add or remove elements, you should not need to add or remove them
 * runtime, enable and disable exist.
 * <p>
 * You must use the Builder class to construct a BakedPipeline, see {@link #builder}
 * <p>
 * Transformers run on a mutable state inside each transformer, allowing for easy reuse. It is recommended to store your
 * pipeline inside a ThreadLocal because 'minecraft'.
 * <p>
 * Each Transformer should be smart enough to expand itself for each newly sized VertexFormat it comes across, meaning
 * that the internal states for the transformers can be safely shared across VertexFormats, this reduces array
 * creations, and generally makes the system as efficient as it is.
 * <p>
 * To use the system: Grab any elements you need to set state data on first, using {@link #getElement(String, Class)}
 * transformers should NOT clear their state on pipeline Reset's so set any global data on elements now. Assuming you
 * are looping over a set of quads to transform, next you need to {@link #reset} the pipeline, Now you should disable /
 * enable any optional elements that are needed, NOTE: Element states are reset when resetting the pipeline. Now you
 * will need to call {@link #prepare(IVertexConsumer)} on the pipeline, here you will pass your collector, usually this
 * is some form of (Unpacked)BakedQuadBuilder, See {@link QuadBuilder} for a simple and fast implementation for standard
 * BakedQuads, and {@link UnpackedBakedQuad.Builder} for UnpackedBakedQuads. Now final step, simply pipe the quad you
 * want to transform INTO the pipeline 'quad.pipe(pipeline)' And that's it! hell, Pipe a pipeline into each other for
 * all i care, the system is efficient enough that there would be no performance penalty for doing so.
 *
 * @author covers1624
 */
public class BakedPipeline implements ISmartVertexConsumer {

    private PipelineElement[] elements;
    private Map<String, PipelineElement> nameLookup;
    private IPipelineConsumer first;

    private Quad unpacker = new Quad();

    private BakedPipeline(PipelineElement[] elements) {
        this.elements = elements;
        this.nameLookup = Arrays.stream(elements).collect(Collectors.toMap(e -> e.name, e -> e));
    }

    /**
     * Used to create a BakedPipeline.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Used to reset the pipeline for the next quad. MUST be called between quads.
     *
     * @param format The format.
     */
    public void reset(VertexFormat format) {
        this.reset(CachedFormat.lookup(format));
    }

    /**
     * Used to reset the pipeline for the next quad. MUST be called between quads.
     *
     * @param format The format.
     */
    public void reset(CachedFormat format) {
        this.unpacker.reset(format);
        for (PipelineElement element : this.elements) {
            element.reset(format);
        }
        this.first = null;
    }

    /**
     * Get an element from the pipeline.
     *
     * @param name  The name of the element.
     * @param clazz The Class of the element, used to safe cast.
     * @return The element.
     */
    public <T extends IPipelineConsumer> T getElement(String name, Class<T> clazz) {
        PipelineElement element = this.nameLookup.get(name);
        if (element != null) {
            if (!clazz.isAssignableFrom(element.consumer.getClass())) {
                throw new IllegalArgumentException(
                        "Element with name " + name + " is not assignable from reference class.");
            }
            return clazz.cast(element.consumer);
        }
        throw new IllegalArgumentException("Element with name " + name + " does not exist.");
    }

    /**
     * Used to enable an element on the pipeline with the specified name.
     *
     * @param name The elements name.
     */
    public void enableElement(String name) {
        this.setElementState(name, true);
    }

    /**
     * Used to disable an element on the pipeline with the specified name.
     *
     * @param name The elements name.
     */
    public void disableElement(String name) {
        this.setElementState(name, false);
    }

    /**
     * Used to set the state of an element on the pipeline.
     *
     * @param name    The name of the element.
     * @param enabled The state to set it to.
     */
    public void setElementState(String name, boolean enabled) {
        PipelineElement element = this.nameLookup.get(name);
        if (element != null) {
            element.isEnabled = enabled;
            return;
        }
        throw new IllegalArgumentException("Element with name " + name + " does not exist.");
    }

    /**
     * Call when you are ready to use the pipeline. This builds the internal state of the Elements getting things ready
     * to transform.
     *
     * @param collector The IVertexConsumer that should collect the transformed quad.
     */
    public void prepare(IVertexConsumer collector) {
        IPipelineConsumer next = null;
        for (PipelineElement element : this.elements) {
            if (element.isEnabled) {
                if (this.first == null) {
                    this.first = element.consumer;
                } else {
                    next.setParent(element.consumer);
                }
                next = element.consumer;
            }
        }
        next.setParent(collector);
    }

    @Override
    public VertexFormat getVertexFormat() {
        this.check();
        return this.first.getVertexFormat();
    }

    @Override
    public void setQuadTint(int tint) {
        this.check();
        this.unpacker.setQuadTint(tint);
    }

    @Override
    public void setQuadOrientation(Direction orientation) {
        this.check();
        this.unpacker.setQuadOrientation(orientation);
    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        this.check();
        this.unpacker.setApplyDiffuseLighting(diffuse);
    }

    @Override
    public void setTexture(TextureAtlasSprite texture) {
        this.check();
        this.unpacker.setTexture(texture);
    }

    @Override
    public void put(int element, float... data) {
        this.check();
        this.unpacker.put(element, data);
        if (this.unpacker.full) {
            this.onFull();
        }
    }

    @Override
    public void put(Quad quad) {
        this.check();
        this.unpacker.put(quad);
    }

    private void check() {
        if (this.first == null) {
            throw new IllegalStateException("Pipeline used before prepare was called.");
        }
    }

    private void onFull() {
        this.first.setInputQuad(this.unpacker);
        this.first.put(this.unpacker);
    }

    /**
     * Internal class, used to hold a PipelineElement's state.
     */
    public static class PipelineElement<T extends IPipelineConsumer> {

        public String name;
        public boolean defaultState;
        public T consumer;
        public boolean isEnabled;

        public void reset(CachedFormat format) {
            this.isEnabled = this.defaultState;
            this.consumer.setParent(null);
            this.consumer.reset(format);
        }
    }

    /**
     * The builder associated with the BakedPipeline. You must create a BakedPipeline with this, once created a pipeline
     * cannot be modified, modifying should not be needed as you can enable and disable elements with ease.
     */
    public static class Builder {

        private LinkedList<PipelineElement> elements = new LinkedList<>();

        /**
         * Inserts an element to the front of the list, Useful if you have a more complex system and each system need to
         * be independent from each other, but this element must be first.
         *
         * @param name    The name to identify this element, used as an identifier when setting state, and retrieving
         *                the element.
         * @param factory The factory used to create the Transformer.
         * @return The same builder.
         */
        public Builder addFirst(String name, IPipelineElementFactory<?> factory) {
            return this.addFirst(name, factory, true);
        }

        /**
         * Inserts an element to the front of the list, Useful if you have a more complex system and each system need to
         * be independent from each other, but this element must be first.
         *
         * @param name         The name to identify this element, used as an identifier when setting state, and
         *                     retrieving the element.
         * @param factory      The factory used to create the Transformer.
         * @param defaultState The default state for this element.
         * @return The same builder.
         */
        public Builder addFirst(String name, IPipelineElementFactory<?> factory, boolean defaultState) {
            return this.addFirst(name, factory, defaultState, e -> {
            });
        }

        /**
         * Inserts an element to the front of the list, Useful if you have a more complex system and each system need to
         * be independent from each other, but this element must be first.
         *
         * @param name           The name to identify this element, used as an identifier when setting state, and
         *                       retrieving the element.
         * @param factory        The factory used to create the Transformer.
         * @param defaultState   The default state for this element.
         * @param defaultsSetter A callback used to set any defaults on the transformer.
         * @return The same builder.
         */
        public <T extends IPipelineConsumer> Builder addFirst(String name, IPipelineElementFactory<T> factory,
                boolean defaultState, Consumer<T> defaultsSetter) {
            PipelineElement<T> element = this.makeElement(name, factory, defaultState);
            defaultsSetter.accept(element.consumer);
            this.elements.addFirst(element);
            return this;
        }

        /**
         * Adds an element at the end of the transform list, Suitable for 99% of cases.
         *
         * @param name    The name to identify this element, used as an identifier when setting state, and retrieving
         *                the element.
         * @param factory The factory used to create the Transformer.
         * @return The same builder.
         */
        public Builder addElement(String name, IPipelineElementFactory<?> factory) {
            return this.addElement(name, factory, true);
        }

        /**
         * Adds an element at the end of the transform list, Suitable for 99% of cases.
         *
         * @param name         The name to identify this element, used as an identifier when setting state, and
         *                     retrieving the element.
         * @param factory      The factory used to create the Transformer.
         * @param defaultState The default state for this element.
         * @return The same builder.
         */
        public Builder addElement(String name, IPipelineElementFactory<?> factory, boolean defaultState) {
            return this.addElement(name, factory, defaultState, e -> {
            });
        }

        /**
         * Adds an element at the end of the transform list, Suitable for 99% of cases.
         *
         * @param name           The name to identify this element, used as an identifier when setting state, and
         *                       retrieving the element.
         * @param factory        The factory used to create the Transformer.
         * @param defaultState   The default state for this element.
         * @param defaultsSetter A callback used to set any defaults on the transformer.
         * @return The same builder.
         */
        public <T extends IPipelineConsumer> Builder addElement(String name, IPipelineElementFactory<T> factory,
                boolean defaultState, Consumer<T> defaultsSetter) {
            PipelineElement<T> element = this.makeElement(name, factory, defaultState);
            defaultsSetter.accept(element.consumer);
            this.elements.add(element);
            return this;
        }

        // Internal method, used to construct the PipelineElement class.
        private <T extends IPipelineConsumer> PipelineElement<T> makeElement(String name,
                IPipelineElementFactory<T> factory, boolean defaultState) {
            if (this.elements.stream().anyMatch(p -> p.name.equals(name))) {
                throw new IllegalArgumentException("Unable to add element with duplicate name: " + name);
            }
            PipelineElement<T> element = new PipelineElement<>();
            element.name = name;
            element.consumer = factory.create();
            element.defaultState = defaultState;
            return element;
        }

        /**
         * Call this once you are finished to build your BakedPipeline!
         *
         * @return The new Pipeline.
         */
        public BakedPipeline build() {
            return new BakedPipeline(this.elements.toArray(new PipelineElement[0]));
        }
    }
}
