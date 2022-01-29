package se.gory_moon.player_mobs.sound;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.sounds.SoundEvent;
import se.gory_moon.player_mobs.PlayerMobs;
import se.gory_moon.player_mobs.utils.CustomRegistrate;

import static se.gory_moon.player_mobs.Constants.*;

public class SoundRegistry {

    private static final CustomRegistrate REGISTRATE = PlayerMobs.getRegistrate();

    public static final RegistryEntry<SoundEvent> PLAYER_MOB_AMBIENT = REGISTRATE.object(SOUND_PLAYER_MOB_AMBIENT)
            .sound()
            .sound((definition, provider) -> definition.with(provider.createSound("mob/ambient")))
            .register();

    public static final RegistryEntry<SoundEvent> PLAYER_MOB_HURT = REGISTRATE.object(SOUND_PLAYER_MOB_HURT)
            .sound()
            .entitySound((definition, provider) -> definition.with(
                    provider.createMcSound("damage/hit1"),
                    provider.createMcSound("damage/hit2"),
                    provider.createMcSound("damage/hit3")
            ), "Player Mob hurts")
            .register();

    public static final RegistryEntry<SoundEvent> PLAYER_MOB_DEATH = REGISTRATE.object(SOUND_PLAYER_MOB_DEATH)
            .sound()
            .entitySound((definition, provider) -> definition.with(
                    provider.createMcSound("damage/hit1"),
                    provider.createMcSound("damage/hit2"),
                    provider.createMcSound("damage/hit3")
            ), "Player Mob dies")
            .register();

    public static void init() {}
}
