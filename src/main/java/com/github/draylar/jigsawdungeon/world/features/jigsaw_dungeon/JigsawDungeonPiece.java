package com.github.draylar.jigsawdungeon.world.features.jigsaw_dungeon;

import com.github.draylar.jigsawdungeon.world.World;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;

public class JigsawDungeonPiece extends PoolStructurePiece {

    JigsawDungeonPiece(StructureManager structureManager, StructurePoolElement structurePoolElement_1, BlockPos blockPos_1, int int_1, BlockRotation blockRotation_1, MutableIntBoundingBox mutableIntBoundingBox_1) {
        super(World.DUNGEON_PIECE, structureManager, structurePoolElement_1, blockPos_1, int_1, blockRotation_1, mutableIntBoundingBox_1);
    }

    public JigsawDungeonPiece(StructureManager structureManager, CompoundTag tag) {
        super(structureManager, tag, World.DUNGEON_PIECE);
    }

    @Override
    public void translate(int x, int y, int z) {
        super.translate(x, getBoundingBox().minY + getGroundLevelDelta() + 25, z);
    }
}
