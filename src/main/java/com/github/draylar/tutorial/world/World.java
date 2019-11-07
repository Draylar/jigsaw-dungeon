package com.github.draylar.tutorial.world;

import com.github.draylar.tutorial.JigsawDungeon;
import com.github.draylar.tutorial.world.features.jigsaw_dungeon.JigsawDungeonFeature;
import com.github.draylar.tutorial.world.features.jigsaw_dungeon.JigsawDungeonPiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.StructureFeature;

public class World {

    private World() {
        // NO-OP
    }

    public static final StructureFeature<DefaultFeatureConfig> FEATURE = Registry.register(
            Registry.FEATURE,
            new Identifier(JigsawDungeon.MODID, "jigsaw_dungeon_feature"),
            new JigsawDungeonFeature(DefaultFeatureConfig::deserialize)
    );

    public static final StructureFeature<DefaultFeatureConfig> STRUCTURE = Registry.register(
            Registry.STRUCTURE_FEATURE,
            new Identifier(JigsawDungeon.MODID, "jigsaw_dungeon_feature"),
            FEATURE
    );

    public static final StructurePieceType DUNGEON_PIECE = Registry.register(
            Registry.STRUCTURE_PIECE,
            new Identifier(JigsawDungeon.MODID, "jigsaw_dungeon_piece"),
            JigsawDungeonPiece::new
    );

    static {
        Feature.STRUCTURES.put("jigsawdungeon:jigsaw_dungeon", FEATURE);
    }

    static final StructurePoolElementType DUNGEON_POOL_ELEMENT = register("from_pool_element", DungeonPoolElement::new);

    public static void init() {

    }

    static StructurePoolElementType register(String name, StructurePoolElementType elementType) {
        return Registry.register(Registry.STRUCTURE_POOL_ELEMENT, JigsawDungeon.id(name), elementType);
    }
}
