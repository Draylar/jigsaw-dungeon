package com.github.draylar.jigsawdungeon.world.features.jigsaw_dungeon;

import com.github.draylar.jigsawdungeon.JigsawDungeon;
import com.github.draylar.jigsawdungeon.world.BTStructurePoolBasedGenerator;
import com.github.draylar.jigsawdungeon.world.DungeonPoolElement;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;

public class JigsawDungeonStructureStart extends StructureStart {

    private static final Identifier START = JigsawDungeon.id("start");
    private static final Identifier ANY = JigsawDungeon.id("any");
    private static final Identifier ANY_NO_STAIRS = JigsawDungeon.id("any_non_empty_non_stairs");
    private static final Identifier TERMINATORS = JigsawDungeon.id("terminators");
    public static final Identifier HALLWAY_TERMINATORS = JigsawDungeon.id("hallway_terminators");

    JigsawDungeonStructureStart(StructureFeature<?> feature, int x, int z, Biome biome, MutableIntBoundingBox box, int int_3, long seed) {
        super(feature, x, z, biome, box, int_3, seed);
    }

    @Override
    public void initialize(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int x, int z, Biome biome) {
        // start pool, size, piece (only 1 piece needed)
        BTStructurePoolBasedGenerator.addPieces(START, 6, JigsawDungeonPiece::new, chunkGenerator, structureManager, new BlockPos(x * 16, 0, z * 16), children, random);
        setBoundingBoxFromChildren();
    }

    static {
        Identifier EMPTY = new Identifier("empty");

        ImmutableList<StructureProcessor> RULE = ImmutableList.of(
                createBlockProcessor(Blocks.STONE_BRICKS, .3f, Blocks.MOSSY_STONE_BRICKS.getDefaultState()),
                createBlockProcessor(Blocks.STONE_BRICKS, .3f, Blocks.CRACKED_STONE_BRICKS.getDefaultState())
        );

        BTStructurePoolBasedGenerator.REGISTRY.add(
                new StructurePool(
                        START,
                        EMPTY,
                        ImmutableList.of(
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("medium_room").toString(), RULE), 1)
                        ),
                        StructurePool.Projection.RIGID
                )
        );

        BTStructurePoolBasedGenerator.REGISTRY.add(
                new StructurePool(
                        ANY,
                        TERMINATORS,
                        ImmutableList.of(
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("medium_room").toString(), RULE), 4),
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("large_stairs").toString(), RULE), 1),
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("medium_tunnel").toString(), RULE), 8)
                        ),
                        StructurePool.Projection.RIGID
                )
        );

        BTStructurePoolBasedGenerator.REGISTRY.add(
                new StructurePool(
                        ANY_NO_STAIRS,
                        TERMINATORS,
                        ImmutableList.of(
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("medium_room").toString(), RULE), 1),
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("medium_tunnel").toString(), RULE), 1)
                        ),
                        StructurePool.Projection.RIGID
                )
        );

        BTStructurePoolBasedGenerator.REGISTRY.add(
                new StructurePool(
                        TERMINATORS,
                        EMPTY,
                        ImmutableList.of(
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("end").toString(), RULE), 5),
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("end_chest").toString(), RULE), 1)
                        ),
                        StructurePool.Projection.RIGID
                )
        );

        BTStructurePoolBasedGenerator.REGISTRY.add(
                new StructurePool(
                        HALLWAY_TERMINATORS,
                        TERMINATORS,
                        ImmutableList.of(
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("medium_room").toString(), RULE), 2),
                                Pair.of(new DungeonPoolElement(JigsawDungeon.id("end_chest").toString(), RULE), 1)
                        ),
                        StructurePool.Projection.RIGID
                )
        );
    }

    static RuleStructureProcessor createBlockProcessor(Block initialBlock, float chance, BlockState targetState) {
        return new RuleStructureProcessor(
                ImmutableList.of(
                        new StructureProcessorRule(
                                new RandomBlockMatchRuleTest(initialBlock, chance),
                                AlwaysTrueRuleTest.INSTANCE,
                                targetState
                        )
                )
        );
    }
}
