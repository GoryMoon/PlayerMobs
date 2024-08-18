package se.gory_moon.player_mobs.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import se.gory_moon.player_mobs.Constants;

import java.util.List;
import java.util.function.Function;

public class BiomeModifierRegistry {

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Constants.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddSpawnsToSameAsEntityBiomeModifier>> ADD_SPAWN_AS_ENTITY_CODEC = BIOME_MODIFIER_SERIALIZERS.register(Constants.BIOME_MODIFIER_ADD_SPAWN, () ->
            RecordCodecBuilder.mapCodec(builder -> builder.group(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(AddSpawnsToSameAsEntityBiomeModifier::entityType),
                    Codec.either(MobSpawnSettings.SpawnerData.CODEC.listOf(), MobSpawnSettings.SpawnerData.CODEC).xmap(
                            either -> either.map(Function.identity(), List::of), // convert list/singleton to list when decoding
                            list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list) // convert list to singleton/list when encoding
                    ).fieldOf("spawners").forGetter(AddSpawnsToSameAsEntityBiomeModifier::spawners)
            ).apply(builder, AddSpawnsToSameAsEntityBiomeModifier::new)));
}
