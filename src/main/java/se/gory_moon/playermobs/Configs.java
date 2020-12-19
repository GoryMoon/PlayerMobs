package se.gory_moon.playermobs;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Configs {

    public static final Common COMMON;
    public static final ForgeConfigSpec commonSpec;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = pair.getRight();
        COMMON = pair.getKey();
    }

    public static class Common {

        public ForgeConfigSpec.BooleanValue attackTwin;
        public ForgeConfigSpec.BooleanValue openDoors;
        public ForgeConfigSpec.EnumValue<Difficulty> openDoorsDifficulty;
        public ForgeConfigSpec.DoubleValue pickupItemsChance;

        public ForgeConfigSpec.IntValue spawnWeight;
        public ForgeConfigSpec.IntValue spawnMinSize;
        public ForgeConfigSpec.IntValue spawnMaxSize;
        public ForgeConfigSpec.DoubleValue babySpawnChance;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionBlockList;

        public ForgeConfigSpec.ConfigValue<List<? extends String>> mobNames;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> nameLinks;
        public ForgeConfigSpec.IntValue nameLinksSyncTime;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            attackTwin = builder
                    .comment("If true the player mobs will attack all players",
                            "If false they will ignore players with the same name as themself")
                    .define("Attack Twin", true);

            openDoors = builder
                    .comment("If the player mobs should be able to open doors.")
                    .define("Open Doors", true);

            openDoorsDifficulty = builder
                    .comment("The difficulty and above that player mobs can open doors, if enabled above.")
                    .defineEnum("Open Doors Difficulty", Difficulty.HARD);

            pickupItemsChance = builder
                    .comment("The chance to use when spawned if a player mob can pickup items.",
                            "Set to -1 to disable.")
                    .defineInRange("Pickup Item Chance", 0.55D, -1D, 1D);

            builder.pop()
                    .comment("Configs related to spawning the mobs")
                    .push("spawning");

            spawnWeight = builder
                    .comment("The spawn weight of the mob compared to other mobs.",
                            "Higher values makes spawning more probable.",
                            "Default is similar to zombies")
                    .defineInRange("Spawn Weight", 100, 0, Integer.MAX_VALUE);

            spawnMinSize = builder
                    .comment("The minimum size of the group of mobs that will spawn.")
                    .defineInRange("Min Spawn Size", 4, 1, Integer.MAX_VALUE);

            spawnMaxSize = builder
                    .comment("The maximum size of the group of mobs that will spawn.")
                    .defineInRange("Max Spawn Size", 4, 1, Integer.MAX_VALUE);

            babySpawnChance = builder
                    .comment("Chance that a player mob will spawn as a baby.",
                            "Set to -1 to disable.")
                    .defineInRange("Baby Spawn Chance", 0.1D, -1D, 1D);

            dimensionBlockList = builder
                    .comment("The id of the dimensions to block spawning in.",
                            "The player mobs spawn where Zombies spawn, so no need to block dimensions that doesn't contain Zombies.",
                            "Example id: \"minecraft:overworld\"")
                    .defineList("Dimension Blocklist", ImmutableList.of(), Common::verifyDimension);

            builder.pop()
                    .comment("Configs related to the names of the mobs.")
                    .push("names");

            nameLinks = builder
                    .comment("A list of links to get names that the player mobs can have.",
                            "The names need to be separated by a newline.",
                            "Names from these links are combined with the named from below",
                            "As an example you have Twitch subs in the game by using https://whitelist.gorymoon.se")
                    .defineList("Name Links", ImmutableList.of(), Common::validString);

            nameLinksSyncTime = builder
                    .comment("The time interval in minutes when to reload the links (approximately, based on TPS)",
                            "If set to 0 it will only sync once on load.")
                    .defineInRange("Reload Interval", 60, 0, Integer.MAX_VALUE);

            mobNames = builder
                    .comment("A list of names that the player mobs can have.")
                    .defineList("Mob Names", DEFAULT_NAMES, Common::validString);

            builder.pop();
        }

        private static boolean validString(Object o) {
            return o instanceof String && !StringUtils.isEmpty((String) o);
        }

        @SuppressWarnings("ConstantConditions")
        public boolean isDimensionBlocked(DimensionType type) {
            return dimensionBlockList.get().contains(DynamicRegistries.func_239770_b_().getRegistry(Registry.DIMENSION_TYPE_KEY).getKey(type).toString());
        }

        private static DynamicRegistries.Impl dynamicRegistries;

        private static boolean verifyDimension(Object v) {
            if (v instanceof String) {
                ResourceLocation location = ResourceLocation.tryCreate((String) v);
                if (location == null) return false;

                if (dynamicRegistries == null) {
                    dynamicRegistries = DynamicRegistries.func_239770_b_();
                }

                return dynamicRegistries.getRegistry(Registry.DIMENSION_TYPE_KEY)
                                        .getOptionalValue(RegistryKey.getOrCreateKey(Registry.DIMENSION_TYPE_KEY, location))
                                        .isPresent();
            }
            return false;
        }

        @SubscribeEvent
        public static void onLoad(ModConfig.ModConfigEvent event) {
            dynamicRegistries = null;
            SpawnHandler.invalidateSpawner();
        }

        private static final List<String> DEFAULT_NAMES = ImmutableList.of(
                "Gory_Moon",
                "Darkosto",
                "LexManos",
                "cpw11",
                "vadis365",
                "Turkey2349",
                "Gen_Deathrow",
                "Sevadus",
                "direwolf20",
                "jeb_",
                "Dinnerbone",
                "Grumm",
                "Searge"
        );
    }
}
