package se.gory_moon.player_mobs.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AddSpawnsToSameAsEntityBiomeModifier(EntityType<?> entityType, List<MobSpawnSettings.SpawnerData> spawners) implements BiomeModifier {

    @Override
    public void modify(@NotNull Holder<Biome> biome, @NotNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        if (phase == Phase.ADD) {
            MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
            boolean foundEntity = false;

            // Check if this biome contains the specified entity
            for (MobCategory category : MobCategory.values()) {
                if (spawnBuilder.getSpawner(category).stream().anyMatch(spawnerData -> spawnerData.type.equals(entityType))) {
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
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return BiomeModifierRegistry.ADD_SPAWN_AS_ENTITY_CODEC.get();
    }
}
