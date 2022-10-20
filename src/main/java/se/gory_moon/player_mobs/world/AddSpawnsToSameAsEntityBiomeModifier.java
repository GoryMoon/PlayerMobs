package se.gory_moon.player_mobs.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;

public record AddSpawnsToSameAsEntityBiomeModifier(EntityType<?> entityType, List<MobSpawnSettings.SpawnerData> spawners) implements BiomeModifier {

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD) {
            MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
            boolean foundEntity = false;

            // Check if this biome contains the specified entity
            for (MobCategory category : MobCategory.values())
            {
                if (spawnBuilder.getSpawner(category).stream().anyMatch(spawnerData -> spawnerData.type.equals(entityType)))
                {
                    foundEntity = true;
                    break;
                }
            }

            if (!foundEntity)
                return;

            for (MobSpawnSettings.SpawnerData spawner : this.spawners)
                spawnBuilder.addSpawn(spawner.type.getCategory(), spawner);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return BiomeModifierRegistry.ADD_SPAWN_AS_ENTITY_CODEC.get();
    }
}
