package se.gory_moon.player_mobs.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import se.gory_moon.player_mobs.Constants;
import se.gory_moon.player_mobs.entity.EntityRegistry;

public class PlayerMobsItemModelProvider extends ItemModelProvider {

    public PlayerMobsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(EntityRegistry.PLAYER_MOD_SPAWN_EGG.getRegisteredName(), mcLoc("item/template_spawn_egg"));
    }
}
