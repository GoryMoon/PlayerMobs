package se.gory_moon.player_mobs.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gory_moon.player_mobs.Configs;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ItemManager {

    public static final ItemManager INSTANCE = new ItemManager();
    private static final Logger LOGGER = LogManager.getLogger();

    private final List<WeightedItem> weightedMainItems = new CopyOnWriteArrayList<>();
    private final List<WeightedItem> weightedOffItems = new CopyOnWriteArrayList<>();

    private ItemManager() {
    }

    public void configLoad() {
        weightedMainItems.clear();
        weightedOffItems.clear();
        weightedMainItems.addAll(parseItems(Configs.COMMON.mainItems));
        weightedOffItems.addAll(parseItems(Configs.COMMON.offhandItems));
    }

    private List<WeightedItem> parseItems(ForgeConfigSpec.ConfigValue<List<? extends String>> offhandItems) {
        return offhandItems.get().stream().map(item -> {
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
            if (location == null || !ForgeRegistries.ITEMS.containsKey(location)) {
                LOGGER.error(String.format("Failed to parse item id: %s", parts[0]));
                return null;
            }
            return new WeightedItem(location, weight);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public ItemStack getRandomMainHand(Random rand) {
        return getRandomItem(weightedMainItems, rand);
    }

    public ItemStack getRandomOffHand(Random rand) {
        return getRandomItem(weightedOffItems, rand);
    }

    private ItemStack getRandomItem(List<WeightedItem> items, Random rand) {
        if (items.size() <= 0) {
            return ItemStack.EMPTY;
        }
        WeightedItem item = WeightedRandom.getRandomItem(rand, items);
        return new ItemStack(ForgeRegistries.ITEMS.getValue(item.id));
    }

    private static class WeightedItem extends WeightedRandom.Item {

        private final ResourceLocation id;

        public WeightedItem(ResourceLocation id, int itemWeightIn) {
            super(itemWeightIn);
            this.id = id;
        }
    }
}
