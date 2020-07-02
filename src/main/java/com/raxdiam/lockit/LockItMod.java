package com.raxdiam.lockit;

import com.raxdiam.lockit.util.CommandHelper;
import com.raxdiam.lockit.commands.LockCommand;
import com.raxdiam.lockit.text.PrefixedText;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LockItMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Lockables");

	@Override
	public void onInitialize() {
		PrefixedText.MOD_ID = "lockables";
		CommandHelper.register(new LockCommand());
		/*CommandHelper.register(new UnlockCommand());
		CommandHelper.register(new ShareCommand());
		CommandHelper.register(new UnshareCommand());*/
	}
}
