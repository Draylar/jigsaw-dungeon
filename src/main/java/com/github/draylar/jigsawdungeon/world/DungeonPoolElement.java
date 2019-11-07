package com.github.draylar.jigsawdungeon.world;

import com.github.draylar.jigsawdungeon.util.GenerationUtils;
import com.github.draylar.jigsawdungeon.util.SpawnerUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.JigsawReplacementStructureProcessor;
import net.minecraft.structure.processor.NopStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DynamicDeserializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.loot.LootTables;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DungeonPoolElement extends StructurePoolElement {

    private final Identifier location;
    private final ImmutableList<StructureProcessor> processors;

    public DungeonPoolElement(String string_1, List<StructureProcessor> list_1) {
        this(string_1, list_1, StructurePool.Projection.RIGID);
    }

    public DungeonPoolElement(String string_1, List<StructureProcessor> list_1, StructurePool.Projection structurePool$Projection_1) {
        super(structurePool$Projection_1);
        this.location = new Identifier(string_1);
        this.processors = ImmutableList.copyOf(list_1);
    }

    public DungeonPoolElement(Dynamic<?> dynamic_1) {
        super(dynamic_1);
        this.location = new Identifier(dynamic_1.get("location").asString(""));
        this.processors = ImmutableList.copyOf(dynamic_1.get("processors").asList((dynamic_1x) -> {
            return (StructureProcessor) DynamicDeserializer.deserialize(dynamic_1x, Registry.STRUCTURE_PROCESSOR, "processor_type", NopStructureProcessor.INSTANCE);
        }));
    }

    @Override
    public List<Structure.StructureBlockInfo> getStructureBlockInfos(StructureManager structureManager_1, BlockPos blockPos_1, BlockRotation blockRotation_1, Random random_1) {
        Structure structure_1 = structureManager_1.getStructureOrBlank(this.location);
        List<Structure.StructureBlockInfo> list_1 = structure_1.method_15165(blockPos_1, (new StructurePlacementData()).setRotation(blockRotation_1), Blocks.JIGSAW, true);
        Collections.shuffle(list_1, random_1);
        return list_1;
    }

    @Override
    public MutableIntBoundingBox getBoundingBox(StructureManager structureManager_1, BlockPos blockPos_1, BlockRotation blockRotation_1) {
        Structure structure_1 = structureManager_1.getStructureOrBlank(this.location);
        return structure_1.calculateBoundingBox((new StructurePlacementData()).setRotation(blockRotation_1), blockPos_1);
    }

    @Override
    public boolean generate(StructureManager structureManager, IWorld world, BlockPos pos, BlockRotation rotation, MutableIntBoundingBox bounds, Random random) {
        Structure structure = structureManager.getStructureOrBlank(this.location);
        StructurePlacementData structurePlacementData = this.getDefaultPlacementData(rotation, bounds);

        // generate
        if (!structure.method_15172(world, pos, structurePlacementData, 18)) {
            return false;
        } else {
            List<Structure.StructureBlockInfo> list_1 = structure.method_16445(pos, structurePlacementData, Blocks.STRUCTURE_BLOCK);
            Iterator var6 = list_1.iterator();

            while(var6.hasNext()) {
                Structure.StructureBlockInfo blockInfo = (Structure.StructureBlockInfo)var6.next();
                if (blockInfo.tag != null) {
                    StructureBlockMode structureBlockMode_1 = StructureBlockMode.valueOf(blockInfo.tag.getString("mode"));
                    if (structureBlockMode_1 == StructureBlockMode.DATA) {
                        method_16756(world, blockInfo, blockInfo.pos, rotation, random, bounds);
                    }
                }
            }

            return true;
        }
    }

    // handleMetadata
    @Override
    public void method_16756(IWorld world, Structure.StructureBlockInfo info, BlockPos pos, BlockRotation rotation, Random random, MutableIntBoundingBox bounds) {
        String metadata = info.tag.getString("metadata");
        switch(metadata) {
            case "medium_room_spawner": {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 3);
                world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 3);

                if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
                    SpawnerUtils.setSpawnerType(world, pos);
                }

                break;
            }

            case "end_chest": {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

                BlockEntity blockEntity = world.getBlockEntity(pos.down());
                if (blockEntity instanceof ChestBlockEntity) {
                    ((ChestBlockEntity) blockEntity).setLootTable(LootTables.SIMPLE_DUNGEON_CHEST, random.nextLong());
                }

                break;
            }

            case "room_corner": {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                int chance = world.getRandom().nextInt(6);

                // chance for chest
                if(chance < 2) {
                    world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 3);
                    BlockEntity blockEntity = world.getBlockEntity(pos);

                    if (blockEntity instanceof ChestBlockEntity) {
                        ((ChestBlockEntity) blockEntity).setLootTable(LootTables.ABANDONED_MINESHAFT_CHEST, random.nextLong());
                    }
                }

                // chance for spawner
                else if (chance < 3)  {
                    world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 3);

                    if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
                        SpawnerUtils.setSpawnerType(world, pos);
                    }
                }

                else if(chance < 4) {
                    GenerationUtils.placeGroundPool(world, pos.down());
                } else {
                    GenerationUtils.placeGroundPoolWithFall(world, pos.down());
                }

                break;
            }

            case "room_top_corner": {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                int rand = world.getRandom().nextInt(3);

                if(rand < 2)
                    GenerationUtils.placeVines(world, pos);

                break;
            }
        }

        super.method_16756(world, info, pos, rotation, random, bounds);
    }

    private StructurePlacementData getDefaultPlacementData(BlockRotation rotation, MutableIntBoundingBox bounds) {
        StructurePlacementData structurePlacementData_1 = new StructurePlacementData();
        structurePlacementData_1.setBoundingBox(bounds);
        structurePlacementData_1.setRotation(rotation);
        structurePlacementData_1.method_15131(true);
        structurePlacementData_1.setIgnoreEntities(false);
        structurePlacementData_1.addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR);
        structurePlacementData_1.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
        this.processors.forEach(structurePlacementData_1::addProcessor);
        this.getProjection().getProcessors().forEach(structurePlacementData_1::addProcessor);
        return structurePlacementData_1;
    }

    @Override
    public StructurePoolElementType getType() {
        return World.DUNGEON_POOL_ELEMENT;
    }

    @Override
    public <T> Dynamic<T> method_16625(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(ops.createString("location"), ops.createString(this.location.toString()), ops.createString("processors"), ops.createList(this.processors.stream().map((structureProcessor_1) -> structureProcessor_1.method_16771(ops).getValue())))));
    }

    @Override
    public String toString() {
        return "DungeonSingle[" + this.location + "]";
    }
}
