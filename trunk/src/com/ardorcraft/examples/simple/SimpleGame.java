/**
 * Copyright (c) 2008-2010 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardorcraft.examples.simple;

import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.FogState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardorcraft.base.ArdorCraftGame;
import com.ardorcraft.base.CanvasRelayer;
import com.ardorcraft.control.FlyControl;
import com.ardorcraft.generators.LayerDataGenerator;
import com.ardorcraft.player.PlayerBase;
import com.ardorcraft.world.BlockWorld;

/**
 * A simple example showing a textured and lit box spinning.
 */
public class SimpleGame implements ArdorCraftGame {

    private BlockWorld blockWorld;
    private final int subMeshSize = 16;
    private final int gridSize = 16;
    private final int width = subMeshSize * gridSize;
    private final int height = 64;
    private final double farPlane = (gridSize - 1) / 2 * subMeshSize;

    private final ReadOnlyColorRGBA fogColor = new ColorRGBA(0.9f, 0.9f, 1.0f, 1.0f);
    private Node root;
    private Camera camera;
    private PlayerBase player;

    @Override
    public void update(final ReadOnlyTimer timer) {
        camera.setLocation(player.getPosition());
        camera.setDirection(player.getDirection());
        camera.setUp(player.getUp());
        camera.setLeft(player.getLeft());

        // The infinite world update
        blockWorld.updatePosition(player.getPosition());
        blockWorld.update(timer);
    }

    @Override
    public void render(final Renderer renderer) {
        root.draw(renderer);
    }

    @Override
    public void init(final Node root, final CanvasRelayer canvas, final LogicalLayer logicalLayer,
            final PhysicalLayer physicalLayer, final MouseManager mouseManager) {
        this.root = root;

        canvas.setTitle("Simple");
        canvas.getCanvasRenderer().getRenderer().setBackgroundColor(fogColor);

        camera = canvas.getCanvasRenderer().getCamera();
        camera.setFrustumPerspective(75.0, (float) camera.getWidth() / (float) camera.getHeight(), 0.1, farPlane);

        setupFog();

        // Create player object
        player = new PlayerBase();
        player.getPosition().set(0, 50, 0);
        FlyControl.setupTriggers(player, logicalLayer, Vector3.UNIT_Y, true);

        // Create block world
        blockWorld = new BlockWorld(width, height, subMeshSize, simpleSineGenerator, null);
        root.attachChild(blockWorld.getWorldNode());

        blockWorld.startThreads();
    }

    private void setupFog() {
        final FogState fogState = new FogState();
        fogState.setDensity(1.0f);
        fogState.setEnabled(true);
        fogState.setColor(fogColor);
        fogState.setEnd((float) farPlane);
        fogState.setStart((float) farPlane / 3.0f);
        fogState.setDensityFunction(FogState.DensityFunction.Linear);
        fogState.setQuality(FogState.Quality.PerPixel);
        root.setRenderState(fogState);
    }

    @Override
    public void destroy() {}

    @Override
    public void resize(final int newWidth, final int newHeight) {}

    LayerDataGenerator simpleSineGenerator = new LayerDataGenerator(1, 0) {
        @Override
        public boolean isCave(final int x, final int y, final int z) {
            return false;
        }

        @Override
        public int getLayerType(final int layer, final int x, final int z) {
            return 4;
        }

        @Override
        public int getLayerHeight(final int layer, final int x, final int z) {
            return (int) (Math.abs(Math.sin(x * 0.1) * Math.cos(z * 0.1)) * height / 2);
        }
    };
}
