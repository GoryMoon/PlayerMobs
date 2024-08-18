package se.gory_moon.player_mobs.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import se.gory_moon.player_mobs.LangKeys;
import se.gory_moon.player_mobs.sound.SoundRegistry;

import static se.gory_moon.player_mobs.Constants.MOD_ID;

public class PlayerMobsSoundDefinitionsProvider extends SoundDefinitionsProvider {

    public PlayerMobsSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(SoundRegistry.PLAYER_MOB_AMBIENT, definition()
                .with(
                        sound(ResourceLocation.fromNamespaceAndPath(MOD_ID, "mob/ambient"))
                )
        );

        add(SoundRegistry.PLAYER_MOB_HURT, definition()
                .subtitle(LangKeys.SOUND_PLAYER_MOB_HURT.key())
                .with(
                        sound("damage/hit1"),
                        sound("damage/hit2"),
                        sound("damage/hit3")
                )
        );

        add(SoundRegistry.PLAYER_MOB_DEATH, definition()
                .subtitle(LangKeys.SOUND_PLAYER_MOB_DEATH.key())
                .with(
                        sound("damage/hit1"),
                        sound("damage/hit2"),
                        sound("damage/hit3")
                )
        );
    }
}
