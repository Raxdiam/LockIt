package com.raxdiam.lockables;

import com.raxdiam.lockables.commands.ShareCommand;
import com.raxdiam.lockables.commands.UnlockCommand;
import com.raxdiam.lockables.commands.UnshareCommand;
import com.raxdiam.lockables.util.CommandHelper;
import com.raxdiam.lockables.commands.LockCommand;
import com.raxdiam.lockables.text.PrefixedText;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LockablesMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Lockables");
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		PrefixedText.MOD_ID = "lockables";
		CommandHelper.register(new LockCommand());
		CommandHelper.register(new UnlockCommand());
		CommandHelper.register(new ShareCommand());
		CommandHelper.register(new UnshareCommand());

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SERVER = server;
		});
	}
}
