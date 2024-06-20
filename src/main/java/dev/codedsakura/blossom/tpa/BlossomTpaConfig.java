package dev.codedsakura.blossom.tpa;


import dev.codedsakura.blossom.lib.config.BlossomConfig;
import dev.codedsakura.blossom.lib.polyfill.PlaySound;
import dev.codedsakura.blossom.lib.teleport.TeleportConfig;
import dev.codedsakura.blossom.lib.utils.gson.SerializeNull;

public class BlossomTpaConfig extends BlossomConfig {
    TeleportConfig teleportation = new TeleportConfig.Builder()
            .setStandStill(3)
            .setCooldown(30)
            .build();

    int timeout = 60;

    CommandOverrides commandOverrides = new CommandOverrides();

    @SerializeNull
    PlaySound.Sound requestRecipientSound = null;
    @SerializeNull
    PlaySound.Sound cancelRecipientSound = null;
    @SerializeNull
    PlaySound.Sound acceptInitiatorSound = null;
    @SerializeNull
    PlaySound.Sound denyInitiatorSound = null;
    @SerializeNull
    PlaySound.Sound timeoutRecipientSound = null;
    @SerializeNull
    PlaySound.Sound timeoutInitiatorSound = null;

    @Deprecated
    int standStill = 3;
    @Deprecated
    int cooldown = 30;

    @Override
    protected int getLatestVersion() {
        return 1;
    }

    @Override
    public boolean update() {
        if (version == null) {
            teleportation = new TeleportConfig();
            teleportation.standStill = standStill;
            teleportation.cooldown = cooldown;
            commandOverrides.accept = "tpaaccept";
        }
        return super.update();
    }

    static class CommandOverrides {
        String accept = "tpaccept";
        String deny = "tpadeny";
        String cancel = "tpacancel";
    }
}
