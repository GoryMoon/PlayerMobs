package se.gory_moon.player_mobs;

public enum LangKeys {
    COMMANDS_RELOAD_START("commands.reload.start"),
    COMMANDS_RELOAD_DONE("commands.reload.done"),
    COMMANDS_SPAWN_SUCCESS("commands.spawn.success"),
    COMMANDS_SPAWN_FAILED("commands.spawn.failed"),
    COMMANDS_SPAWN_UUID("commands.spawn.uuid"),
    COMMANDS_SPAWN_INVALID_POS("commands.spawn.invalid_position"),

    PACK_DESCRIPTION("pack.player_mobs.description"),

    SOUND_PLAYER_MOB_HURT("subtitles.entity." + Constants.SOUND_PLAYER_MOB_HURT),
    SOUND_PLAYER_MOB_DEATH("subtitles.entity." + Constants.SOUND_PLAYER_MOB_DEATH);

    private final String key;

    LangKeys(String key) {
        this.key = Constants.MOD_ID + "." + key;
    }

    public String key() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}
