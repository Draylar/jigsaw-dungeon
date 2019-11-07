package com.github.draylar.jigsawdungeon.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.ArrayList;

public class SpawnerUtils {

    private static ArrayList<EntityType> mobSpawns = new ArrayList<>();

    static {
        mobSpawns.add(EntityType.ZOMBIE);
        mobSpawns.add(EntityType.SKELETON);
        mobSpawns.add(EntityType.SPIDER);
        mobSpawns.add(EntityType.CAVE_SPIDER);
        mobSpawns.add(EntityType.CREEPER);
        mobSpawns.add(EntityType.WITCH);
    }

    private SpawnerUtils() {
        // NO-OP
    }

    public static void setSpawnerType(IWorld world, BlockPos spawnerPos) {
        BlockEntity blockEntity_1 = world.getBlockEntity(spawnerPos);
        if (blockEntity_1 instanceof MobSpawnerBlockEntity) {
            ((MobSpawnerBlockEntity)blockEntity_1).getLogic().setEntityId(EntityType.ZOMBIE);
        }
    }
}
