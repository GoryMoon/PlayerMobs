package se.gory_moon.player_mobs;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import se.gory_moon.player_mobs.names.NameManager;
import se.gory_moon.player_mobs.utils.ItemManager;
import se.gory_moon.player_mobs.utils.SpawnHandler;

import java.util.List;

public class Configs {

    public static final Common COMMON;
    public static final ForgeConfigSpec commonSpec;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = pair.getRight();
        COMMON = pair.getKey();
    }

    public static class Common {
        private static final DynamicRegistries.Impl dynamicRegistries = DynamicRegistries.func_239770_b_();

        public ForgeConfigSpec.BooleanValue attackTwin;
        public ForgeConfigSpec.BooleanValue openDoors;
        public ForgeConfigSpec.EnumValue<Difficulty> openDoorsDifficulty;
        public ForgeConfigSpec.DoubleValue pickupItemsChance;
        public ForgeConfigSpec.DoubleValue playerHeadDropChance;
        public ForgeConfigSpec.DoubleValue mobHeadDropChance;

        public ForgeConfigSpec.IntValue spawnWeight;
        public ForgeConfigSpec.IntValue spawnMinSize;
        public ForgeConfigSpec.IntValue spawnMaxSize;
        public ForgeConfigSpec.DoubleValue babySpawnChance;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionBlockList;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> mainItems;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> offhandItems;

        public ForgeConfigSpec.ConfigValue<List<? extends String>> mobNames;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> nameLinks;
        public ForgeConfigSpec.IntValue nameLinksSyncTime;
        public ForgeConfigSpec.BooleanValue useWhitelist;

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

            playerHeadDropChance = builder
                    .comment("The chance of players dropping a head with their texture.",
                            "Set to -1 to disable.")
                    .defineInRange("Player Head Drop Chance", 1, -1D, 1D);

            mobHeadDropChance = builder
                    .comment("The chance of player mobs dropping their head.",
                            "Set to -1 to disable.")
                    .defineInRange("Mob Head Drop Chance", 0.25D, -1D, 1D);

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
                    .defineList("Dimension Blocklist", ImmutableList.of(), Common::validString);

            mainItems = builder
                    .comment("A list of items that the player mobs can spawn with.",
                            "Default is 50% of getting a bow or a sword, then the swords are distributed after that.",
                            "There is a separated chance to spawn with an item at all, this is to pick what it to spawn when it does",
                            "Syntax is 'namespace:id-weight'")
                    .defineList("Spawn Items", DEFAULT_MAIN_HAND_ITEMS, Common::validString);

            offhandItems = builder
                    .comment("What item to be able to spawn in the offhand",
                            "It won't spawn an item in the offhand if it spawns with a bow like item.",
                            "There is a separated chance to spawn with an item at all, this is to pick what it to spawn when it does",
                            "Syntax is 'namespace:id-weight'")
                    .defineList("Spawn Items Offhand", DEFAULT_OFFHAND_ITEMS, Common::validString);

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

            useWhitelist = builder
                    .comment("If the names in the whitelist should be used for the player mobs.")
                    .define("Use Whitelist", true);

            builder.pop();
        }

        private static boolean validString(Object o) {
            return o instanceof String && !StringUtils.isEmpty((String) o);
        }

        @SuppressWarnings("ConstantConditions")
        public boolean isDimensionBlocked(DimensionType type) {
            return dimensionBlockList.get().contains(dynamicRegistries.func_230520_a_().getKey(type).toString());
        }

        @SubscribeEvent
        void onLoad(ModConfig.Reloading event) {
            configReload();
        }

        @SubscribeEvent
        void onLoad(ModConfig.Loading event) {
            configReload();
        }

        private static void configReload() {
            SpawnHandler.invalidateSpawner();
            NameManager.INSTANCE.configLoad();
            ItemManager.INSTANCE.configLoad();
        }

        private static final List<String> DEFAULT_MAIN_HAND_ITEMS = ImmutableList.of(
                "minecraft:bow-100",
                "minecraft:stone_sword-64",
                "minecraft:iron_sword-20",
                "minecraft:golden_sword-10",
                "minecraft:diamond_sword-5",
                "minecraft:netherite_sword-1"
        );

        private static final List<String> DEFAULT_OFFHAND_ITEMS = ImmutableList.of(
                "minecraft:shield-1"
        );

        private static final List<String> DEFAULT_NAMES = ImmutableList.of(
                "Gory_Moon",
                "Darkosto",
                "016Nojr",
                "BluSunrize",
                "Buuz135",
                "Darkere",
                "Darkhax",
                "Drullkus",
                "Ellpeck",
                "Emberwalker",
                "Gigabit101",
                "Kamefrede",
                "KnightMiner_",
                "Lat",
                "LexManos",
                "Mrbysco",
                "P3pp3rF1y",
                "Ray",
                "Ridanis",
                "SOTMead",
                "ShyNieke",
                "SkySom",
                "Soaryn",
                "TamasHenning",
                "ValkyrieofNight",
                "XCompWiz",
                "cpw11",
                "darkphan",
                "direwolf20",
                "dmodoomsirius",
                "dmodoomsirius",
                "malte0811",
                "nekosune",
                "neptunepink",
                "vadis365",
                "wyld",
                "paulsoaresjr",
                "Mhykol",
                "Vswe",
                "TurkeyDev",
                "Gen_Deathrow",
                "Sevadus",
                "jeb_",
                "Dinnerbone",
                "Grumm",
                "fry_"
        );
    }
}
