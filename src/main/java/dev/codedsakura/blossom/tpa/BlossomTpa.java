package dev.codedsakura.blossom.tpa;

import dev.codedsakura.blossom.lib.BlossomConfig;
import dev.codedsakura.blossom.lib.CustomLogger;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.core.Logger;

public class BlossomTpa implements ModInitializer {
    static BlossomTpaConfig CONFIG = BlossomConfig.load(BlossomTpaConfig.class, "BlossomTpa.json");
    public static final Logger LOGGER = CustomLogger.createLogger("BlossomTpa");

    @Override
    public void onInitialize() {
        // BlossomLib.addCommand();
    }
}
