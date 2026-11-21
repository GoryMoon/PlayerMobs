# Local patch notes

This is a modified build of PlayerMobs based on upstream tag `v5.1.2`.

Changed on 2026-11-21:

- Arm model selection now reads the model from the same current `PlayerSkin` used for skin and cape rendering. The UUID-only model cache was removed so unresolved or refreshed skin data cannot leave the renderer stuck on a stale arm model.
- When EMF has injected a custom player cape model, PlayerMobs now renders that model directly using EMF's cape anchor transform. This avoids EMF resetting the vanilla cape physics pose and then skipping its custom cape for non-`Player` entities such as PlayerMobEntity. Without the EMF-injected model, the existing vanilla-style cape renderer remains in use.
- Corrected the Gradle mod license metadata to `GPL-3.0`, matching the included `LICENSE` text. Corrected the README's misspelled license label.

This is not an official PlayerMobs release. The modified project remains under GNU GPL version 3; see `LICENSE`.
