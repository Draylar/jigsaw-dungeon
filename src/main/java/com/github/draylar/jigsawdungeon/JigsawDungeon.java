package com.github.draylar.jigsawdungeon;

import com.github.draylar.jigsawdungeon.world.World;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.DecoratorConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

public class JigsawDungeon implements ModInitializer {

	public static final String MODID = "jigsawdungeon";

	@Override
	public void onInitialize() {
		World.init();

		Registry.BIOME.stream().filter(biome ->
				biome.getCategory() != Biome.Category.NETHER && biome.getCategory() != Biome.Category.THEEND && biome.getCategory() != Biome.Category.OCEAN
		).forEach(biome -> {
			biome.addFeature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, Biome.configureFeature(World.FEATURE, new DefaultFeatureConfig(), Decorator.NOPE, DecoratorConfig.DEFAULT));
			biome.addStructureFeature(World.FEATURE, new DefaultFeatureConfig());
		});
	}

	public static Identifier id(String name) {
		return new Identifier(MODID, name);
	}
}
