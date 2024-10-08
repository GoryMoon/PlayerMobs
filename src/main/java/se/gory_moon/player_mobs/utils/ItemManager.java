package se.gory_moon.player_mobs.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gory_moon.player_mobs.Configs;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ItemManager {

    public static final ItemManager INSTANCE = new ItemManager();
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<WeightedEntry.Wrapper<ResourceLocation>> weightedMainItems = new CopyOnWriteArrayList<>();
    private final List<WeightedEntry.Wrapper<ResourceLocation>> weightedOffItems = new CopyOnWriteArrayList<>();

    private ItemManager() {
    }

    public void configLoad() {
        weightedMainItems.clear();
        weightedOffItems.clear();
        weightedMainItems.addAll(parseItems(Configs.COMMON.mainItems));
        weightedOffItems.addAll(parseItems(Configs.COMMON.offhandItems));
    }

    private List<WeightedEntry.Wrapper<ResourceLocation>> parseItems(ModConfigSpec.ConfigValue<List<? extends String>> items) {
        return items.get().stream().map(item -> {
            String[] parts = item.split("-");
            int weight = 1;
            if (parts.length == 2) {
                try {
                    weight = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    LOGGER.error(String.format("Failed to parse item weight: %s", parts[1]), e);
                }
            }
            ResourceLocation location = ResourceLocation.tryParse(parts[0]);
            if (location == null || !BuiltInRegistries.ITEM.containsKey(location)) {
                LOGGER.error("Failed to parse item id: {}", parts[0]);
                return null;
            }
            return WeightedEntry.wrap(location, weight);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public ItemStack getRandomMainHand(RandomSource randomSource) {
        return getRandomItem(weightedMainItems, randomSource);
    }

    public ItemStack getRandomOffHand(RandomSource randomSource) {
        return getRandomItem(weightedOffItems, randomSource);
    }

    private ItemStack getRandomItem(List<WeightedEntry.Wrapper<ResourceLocation>> items, RandomSource randomSource) {
        if (items.isEmpty())
            return ItemStack.EMPTY;

        return WeightedRandom
                .getRandomItem(randomSource, items)
                .map(resourceLocationWrapper -> new ItemStack(BuiltInRegistries.ITEM.get(resourceLocationWrapper.data())))
                .orElse(ItemStack.EMPTY);
    }
}
