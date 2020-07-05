package com.raxdiam.lockit;

import com.raxdiam.lockit.commands.LockItCommand;
import com.raxdiam.lockit.util.CommandHelper;
import com.raxdiam.lockit.text.PrefixedText;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LockItMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("LockIt");

	@Override
	public void onInitialize() {
		PrefixedText.MOD_ID = "lockit";
		CommandHelper.register(new LockItCommand());
	}
}
