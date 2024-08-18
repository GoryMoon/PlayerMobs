package se.gory_moon.player_mobs;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import se.gory_moon.player_mobs.utils.ItemManager;
import se.gory_moon.player_mobs.utils.NameManager;
import se.gory_moon.player_mobs.utils.ThreadUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

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
        public ForgeConfigSpec.DoubleValue playerHeadDropChance;
        public ForgeConfigSpec.DoubleValue mobHeadDropChance;
        public ForgeConfigSpec.DoubleValue babySpawnChance;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionBlocklistStrings;
        public final List<ResourceKey<Level>> dimensionBlocklist = new CopyOnWriteArrayList<>();
        public ForgeConfigSpec.ConfigValue<List<? extends String>> mainItems;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> offhandItems;
        public ForgeConfigSpec.EnumValue<Difficulty> offhandDifficultyLimit;
        public ForgeConfigSpec.DoubleValue offhandSpawnChance;
        public ForgeConfigSpec.BooleanValue allowTippedArrows;
        public ForgeConfigSpec.BooleanValue forceSpawnItem;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> tippedArrowBlocklistStrings;
        public final List<ResourceLocation> tippedArrowBlocklist = new CopyOnWriteArrayList<>();

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
                    .comment("The chance of the mob getting the ability to pickup items, it's used on mob spawn.",
                            "Set to -1 to disable.")
                    .defineInRange("Pickup Item Chance", 0.55D, -1D, 256D);

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

            babySpawnChance = builder
                    .comment("Chance that a player mob will spawn as a baby.",
                            "Set to -1 to disable.")
                    .defineInRange("Baby Spawn Chance", 0.1D, -1D, 1D);

            dimensionBlocklistStrings = builder
                    .comment("The id of the dimensions to block spawning in.",
                            "The player mobs spawn where Zombies spawn, so no need to block dimensions that doesn't contain Zombies.",
                            "Example id: \"minecraft:overworld\"")
                    .defineList("Dimension Blocklist", ImmutableList.of(), Common::validResourceLocation);

            forceSpawnItem = builder
                    .comment("Force the mobs to spawn holding items.")
                    .define("Force Items Spawn", false);

            mainItems = builder
                    .comment("A list of items that the player mobs can spawn with.",
                            "Default is 40% for a bow, 10% for a crossbow and 50% for a sword, then the swords are distributed after that.",
                            "There is a separated chance to spawn with an item at all, this is to pick what to spawn when it does",
                            "Syntax is \"namespace:id-weight\"")
                    .defineList("Spawn Items", DEFAULT_MAIN_HAND_ITEMS, Common::validString);

            offhandItems = builder
                    .comment("What item to be able to spawn in the offhand",
                            "Offhand items can only spawn when on hard difficulty",
                            "It won't spawn an item in the offhand if it spawns with a bow like item.",
                            "There is a separated chance to spawn with an item at all, this is to pick what it to spawn when it does",
                            "Syntax is \"namespace:id-weight\"")
                    .defineList("Spawn Items Offhand", DEFAULT_OFFHAND_ITEMS, Common::validString);

            offhandDifficultyLimit = builder
                    .comment("The difficulty and above that player mobs can spawn with items in their offhand.")
                    .defineEnum("Offhand Spawn Difficulty", Difficulty.HARD);

            offhandSpawnChance = builder
                    .comment("The chance of items spawning in the offhand",
                            "If holding a projectile weapon this can spawn a tipped arrow if allowed",
                            "Else it will spawn from the offhand item list",
                            "Set to -1 to disable.")
                    .defineInRange("Offhand Spawn Chance", 0.5D, -1D, 1D);

            allowTippedArrows = builder
                    .comment("Allow for a change to spawn a random tipped arrow when the mob is holding a projectile weapon")
                    .define("Spawn Tipped Arrows", true);

            tippedArrowBlocklistStrings = builder
                    .comment("A list of potion \"namespace:id\" to block from getting applied to tipped arrows")
                    .defineList("Tipped Arrow Blocklist", DEFAULT_BLOCKED_POTIONS, Common::validResourceLocation);

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

        private static boolean validResourceLocation(Object o) {
            return validString(o) && ResourceLocation.tryParse((String) o) != null;
        }

        public boolean isDimensionBlocked(ResourceKey<Level> type) {
            return dimensionBlocklist.contains(type);
        }

        @SubscribeEvent
        void onLoad(ModConfigEvent.Loading event) {
            configReload();
        }

        @SubscribeEvent
        void onReload(ModConfigEvent.Reloading event) {
            configReload();
        }

        private void configReload() {
            ThreadUtils.tryRunOnMain(() -> {
                dimensionBlocklist.clear();
                dimensionBlocklist.addAll(dimensionBlocklistStrings.get().stream()
                        .map(ResourceLocation::tryParse)
                        .filter(Objects::nonNull)
                        .map(s -> ResourceKey.create(Registries.DIMENSION, s))
                        .toList());
                tippedArrowBlocklist.clear();
                tippedArrowBlocklist.addAll(tippedArrowBlocklistStrings.get().stream()
                        .map(ResourceLocation::tryParse)
                        .filter(Objects::nonNull)
                        .toList());
                NameManager.INSTANCE.configLoad();
                ItemManager.INSTANCE.configLoad();
            });
        }

        private static final List<String> DEFAULT_MAIN_HAND_ITEMS = ImmutableList.of(
                "minecraft:bow-90",
                "minecraft:crossbow-10",
                "minecraft:stone_sword-64",
                "minecraft:iron_sword-20",
                "minecraft:golden_sword-10",
                "minecraft:diamond_sword-5",
                "minecraft:netherite_sword-1"
        );

        private static final List<String> DEFAULT_OFFHAND_ITEMS = ImmutableList.of(
                "minecraft:shield-1",
                "minecraft:air-4"
        );

        private static final List<String> DEFAULT_BLOCKED_POTIONS = ImmutableList.of(
                "minecraft:awkward",
                "minecraft:empty",
                "minecraft:fire_resistance",
                "minecraft:healing",
                "minecraft:invisibility",
                "minecraft:leaping",
                "minecraft:long_fire_resistance",
                "minecraft:long_invisibility",
                "minecraft:long_leaping",
                "minecraft:long_night_vision",
                "minecraft:long_regeneration",
                "minecraft:long_strength",
                "minecraft:long_swiftness",
                "minecraft:long_turtle_master",
                "minecraft:long_water_breathing",
                "minecraft:luck",
                "minecraft:mundane",
                "minecraft:night_vision",
                "minecraft:regeneration",
                "minecraft:strength",
                "minecraft:strong_healing",
                "minecraft:strong_leaping",
                "minecraft:strong_regeneration",
                "minecraft:strong_strength",
                "minecraft:strong_swiftness",
                "minecraft:strong_turtle_master",
                "minecraft:swiftness",
                "minecraft:thick",
                "minecraft:turtle_master",
                "minecraft:water",
                "minecraft:water_breathing"
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
