package se.gory_moon.player_mobs.sound;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.util.SoundEvent;
import se.gory_moon.player_mobs.PlayerMobs;
import se.gory_moon.player_mobs.utils.CustomRegistrate;

import static se.gory_moon.player_mobs.Constants.*;

public class SoundRegistry {

    private static final CustomRegistrate REGISTRATE = PlayerMobs.getRegistrate();

    public static final RegistryEntry<SoundEvent> PLAYER_MOB_LIVING = REGISTRATE.object(SOUND_PLAYER_MOB_LIVING).sound().register();
    public static final RegistryEntry<SoundEvent> PLAYER_MOB_HURT = REGISTRATE.object(SOUND_PLAYER_MOB_HURT).sound().register();
    public static final RegistryEntry<SoundEvent> PLAYER_MOB_DEATH = REGISTRATE.object(SOUND_PLAYER_MOB_DEATH).sound().register();

    public static void init() {}
}
