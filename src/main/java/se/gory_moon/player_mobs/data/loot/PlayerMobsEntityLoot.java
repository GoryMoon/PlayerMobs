package se.gory_moon.player_mobs.data.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import se.gory_moon.player_mobs.entity.EntityRegistry;

import java.util.stream.Stream;

public class PlayerMobsEntityLoot extends EntityLootSubProvider {

    public PlayerMobsEntityLoot(HolderLookup.Provider provider) {
        super(FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
        return EntityRegistry.ENTITIES.getEntries().stream().map(DeferredHolder::get);
    }

    @Override
    public void generate() {

        add(EntityRegistry.PLAYER_MOB_ENTITY.get(), LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(
                                        LootItem.lootTableItem(Items.BONE)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 3)))
                                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)))
                                )
                )
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(
                                        LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 3)))
                                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)))
                                )
                )
        );
    }
}
