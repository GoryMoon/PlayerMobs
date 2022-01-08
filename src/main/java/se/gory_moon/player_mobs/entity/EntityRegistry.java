package se.gory_moon.player_mobs.entity;

import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.PlayerMobs;
import se.gory_moon.player_mobs.client.render.PlayerMobRenderer;
import se.gory_moon.player_mobs.utils.CustomRegistrate;

public class EntityRegistry {

    private static final CustomRegistrate REGISTRATE = PlayerMobs.getRegistrate();

    public static final EntityEntry<PlayerMobEntity> PLAYER_MOB_ENTITY = REGISTRATE.object(Constants.PLAYER_MOB_ENTITY)
            .<PlayerMobEntity>entity(PlayerMobEntity::new, MobCategory.MONSTER)
            .lang("Player Mob")
            .renderer(() -> PlayerMobRenderer::new)
            .spawnPlacement(SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules)
            .properties(builder -> builder.sized(0.6F, 1.8F).clientTrackingRange(8))
            .loot((register, entityType) ->
                    register.add(entityType, LootTable.lootTable()
                            .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(Items.BONE)).apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 3))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
                            .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(Items.ROTTEN_FLESH)).apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 3))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
                    )
            )
            .register();

    public static final ItemEntry<ForgeSpawnEggItem> PLAYER_MOD_SPAWN_EGG = REGISTRATE.object(Constants.PLAYER_MOB_SPAWN_EGG)
            .item(p -> new ForgeSpawnEggItem(PLAYER_MOB_ENTITY, 0xFFF144, 0x69DFDA, p))
            .properties(p -> p.tab(CreativeModeTab.TAB_MISC))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("item/template_spawn_egg")))
            .register();

    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(PLAYER_MOB_ENTITY.get(), PlayerMobEntity.registerAttributes().build());
    }

    public static void init() {}
}
