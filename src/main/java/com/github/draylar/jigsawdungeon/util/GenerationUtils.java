package com.github.draylar.jigsawdungeon.util;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;

import java.util.ArrayList;

public class GenerationUtils {

    private GenerationUtils() {
        // NO-OP
    }

    public static void placeVines(IWorld world, BlockPos pos) {
        world.setBlockState(pos, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 3);

        for(Direction direction : Direction.values()) {
            if(direction.getAxis() != Direction.Axis.Y) {
                int randomHeight = world.getRandom().nextInt(5);

                for(int i = 0; i < randomHeight; i++) {
                    world.setBlockState(pos.down(i).offset(direction), Blocks.VINE.getDefaultState(), 3);
                }
            }
        }
    }

    public static void placeGroundPool(IWorld world, BlockPos pos) {
        ArrayList<BlockPos> placed = new ArrayList<>();
        ArrayList<BlockPos> toAdd = new ArrayList<>();
        placed.add(pos);

        for(int tries = 0; tries < 3; tries++) {
            for (BlockPos position : placed) {
                for (Direction direction : Direction.values()) {
                    if (direction.getAxis() != Direction.Axis.Y) {
                        if (world.getRandom().nextInt(2) == 0) {
                            toAdd.add(position.offset(direction));
                        }
                    }
                }

                if (world.getRandom().nextInt(3 - tries) == 0) {
                    toAdd.add(position.down());
                }
            }

            placed.addAll(toAdd);
        }

        placed.forEach(position -> {
            world.setBlockState(position, Blocks.WATER.getDefaultState(), 3);

            if(world.getBlockState(position.down()).getFluidState().isEmpty() && world.getRandom().nextInt(4) == 0) {
                world.setBlockState(position, Blocks.KELP.getDefaultState(), 3);
            }
        });
    }

    public static void placeGroundPoolWithFall(IWorld world, BlockPos pos) {
        ArrayList<BlockPos> placed = new ArrayList<>();
        ArrayList<BlockPos> toAdd = new ArrayList<>();
        placed.add(pos);

        for(int tries = 0; tries < 3; tries++) {
            for (BlockPos position : placed) {
                for (Direction direction : Direction.values()) {
                    if (direction.getAxis() != Direction.Axis.Y) {
                        if (world.getRandom().nextInt(2) == 0) {
                            toAdd.add(position.offset(direction));
                        }
                    }
                }

                if (world.getRandom().nextInt(3 - tries) == 0) {
                    toAdd.add(position.down());
                }
            }

            placed.addAll(toAdd);
        }

        placed.forEach(position -> {
            world.setBlockState(position, Blocks.WATER.getDefaultState(), 3);

            if(world.getBlockState(position.down()).getFluidState().isEmpty() && world.getRandom().nextInt(4) == 0) {
                world.setBlockState(position, Blocks.KELP.getDefaultState(), 3);
            }
        });

        world.setBlockState(pos.up(6), Blocks.WATER.getDefaultState(), 3);
    }
}
