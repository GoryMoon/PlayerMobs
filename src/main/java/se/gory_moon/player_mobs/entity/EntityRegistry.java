package se.gory_moon.player_mobs.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import se.gory_moon.player_mobs.Constants;

import java.util.Optional;
import java.util.function.Supplier;

public class EntityRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Constants.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<PlayerMobEntity>> PLAYER_MOB_ENTITY = ENTITIES.register(
            Constants.PLAYER_MOB_ENTITY,
            () -> EntityType.Builder.<PlayerMobEntity>of(PlayerMobEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .eyeHeight(1.62F)
                    .passengerAttachments(2.0125F)
                    .vehicleAttachment(Player.DEFAULT_VEHICLE_ATTACHMENT)
                    .ridingOffset(-0.7F)
                    .clientTrackingRange(8)
                    .build(Constants.PLAYER_MOB_ENTITY)
    );

    public static final DeferredHolder<Item, DeferredSpawnEggItem> PLAYER_MOD_SPAWN_EGG = ITEMS.registerItem(
            Constants.PLAYER_MOB_SPAWN_EGG,
            properties -> new DeferredSpawnEggItem(PLAYER_MOB_ENTITY, 0xFFF144, 0x69DFDA, properties)
    );

    public static final Supplier<EntityDataSerializer<Optional<ResolvableProfile>>> RESOLVABLE_PROFILE_SERIALIZER = ENTITY_DATA_SERIALIZERS.register(
            "optional_resolvable_profile_serializer",
            () -> EntityDataSerializer.forValueType(ResolvableProfile.STREAM_CODEC.apply(ByteBufCodecs::optional))
    );

    public static void registerEntitySpawnPlacement(RegisterSpawnPlacementsEvent event) {
        event.register(PLAYER_MOB_ENTITY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
    }

    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(PLAYER_MOB_ENTITY.get(), PlayerMobEntity.registerAttributes().build());
    }
}
