package se.gory_moon.player_mobs.entity;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.functions.LootingEnchantBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.world.gen.Heightmap;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.PlayerMobs;
import se.gory_moon.player_mobs.client.render.PlayerMobRenderer;
import se.gory_moon.player_mobs.utils.CustomRegistrate;

public class EntityRegistry {

    private static final CustomRegistrate REGISTRATE = PlayerMobs.getRegistrate();

    public static final EntityEntry<PlayerMobEntity> PLAYER_MOB_ENTITY = REGISTRATE.object(Constants.PLAYER_MOB_ENTITY)
            .<PlayerMobEntity>entity(PlayerMobEntity::new, EntityClassification.MONSTER)
            .lang("Player Mob")
            .renderer(() -> PlayerMobRenderer::new)
            .spawnPlacement(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::canMonsterSpawnInLight)
            .properties(builder -> builder.size(0.6F, 1.8F).trackingRange(8).func_233608_b_(5))
            .defaultSpawnEgg(0xFFF144, 0x69DFDA)
            .loot((register, entityType) ->
                    register.registerLootTable(entityType, LootTable.builder()
                            .addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.BONE)).acceptFunction(SetCount.builder(RandomValueRange.of(0, 3))).acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
                            .addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(Items.ROTTEN_FLESH)).acceptFunction(SetCount.builder(RandomValueRange.of(0, 3))).acceptFunction(LootingEnchantBonus.builder(RandomValueRange.of(0.0F, 1.0F))))
                    )
            )
            .register();

    public static void registerEntityAttributes() {
        GlobalEntityTypeAttributes.put(PLAYER_MOB_ENTITY.get(), PlayerMobEntity.registerAttributes().create());
    }

    public static void init() {}
}
