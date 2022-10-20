package se.gory_moon.player_mobs.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import se.gory_moon.player_mobs.Constants;

import java.util.List;
import java.util.function.Function;

public class BiomeModifierRegistry {

    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Constants.MOD_ID);

    public static final RegistryObject<Codec<AddSpawnsToSameAsEntityBiomeModifier>> ADD_SPAWN_AS_ENTITY_CODEC = BIOME_MODIFIER_SERIALIZERS.register(Constants.BIOME_MODIFIER_ADD_SPAWN, () ->
            RecordCodecBuilder.create(builder -> builder.group(
                    ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(AddSpawnsToSameAsEntityBiomeModifier::entityType),
                    new ExtraCodecs.EitherCodec<>(MobSpawnSettings.SpawnerData.CODEC.listOf(), MobSpawnSettings.SpawnerData.CODEC).xmap(
                            either -> either.map(Function.identity(), List::of), // convert list/singleton to list when decoding
                            list -> list.size() == 1 ? Either.right(list.get(0)): Either.left(list) // convert list to singleton/list when encoding
                    ).fieldOf("spawners").forGetter(AddSpawnsToSameAsEntityBiomeModifier::spawners)
            ).apply(builder, AddSpawnsToSameAsEntityBiomeModifier::new)));
}
