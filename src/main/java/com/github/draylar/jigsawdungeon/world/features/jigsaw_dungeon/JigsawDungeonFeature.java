package com.github.draylar.jigsawdungeon.world.features.jigsaw_dungeon;

import com.mojang.datafixers.Dynamic;
import net.minecraft.world.gen.feature.AbstractTempleFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

import java.util.function.Function;

public class JigsawDungeonFeature extends AbstractTempleFeature<DefaultFeatureConfig> {

    public JigsawDungeonFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> config) {
        super(config);
    }

    @Override
    protected int getSeedModifier() {
        return 13371337;
    }

    @Override
    public StructureStartFactory getStructureStartFactory() {
        return JigsawDungeonStructureStart::new;
    }

    @Override
    public String getName() {
        return "Jigsaw Dungeon";
    }

    @Override
    public int getRadius() {
        return 8;
    }
}
