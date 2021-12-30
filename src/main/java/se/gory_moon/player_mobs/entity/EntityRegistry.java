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
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
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
            .spawnPlacement(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::checkMonsterSpawnRules)
            .properties(builder -> builder.sized(0.6F, 1.8F).clientTrackingRange(8))
            .defaultSpawnEgg(0xFFF144, 0x69DFDA)
            .loot((register, entityType) ->
                    register.add(entityType, LootTable.lootTable()
                            .withPool(LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(ItemLootEntry.lootTableItem(Items.BONE)).apply(SetCount.setCount(RandomValueRange.between(0, 3))).apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F))))
                            .withPool(LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(ItemLootEntry.lootTableItem(Items.ROTTEN_FLESH)).apply(SetCount.setCount(RandomValueRange.between(0, 3))).apply(LootingEnchantBonus.lootingMultiplier(RandomValueRange.between(0.0F, 1.0F))))
                    )
            )
            .register();

    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(PLAYER_MOB_ENTITY.get(), PlayerMobEntity.registerAttributes().build());
    }

    public static void init() {}
}
