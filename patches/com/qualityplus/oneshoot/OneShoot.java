package com.qualityplus.oneshoot;

import com.qualityplus.assistant.lib.eu.okaeri.injector.annotation.Inject;
import com.qualityplus.assistant.lib.eu.okaeri.platform.bukkit.annotation.Delayed;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Scan;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.plan.ExecutionPhase;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.plan.Planned;
import com.qualityplus.assistant.okaeri.OkaeriSilentPlugin;
import com.qualityplus.oneshoot.api.IOneShootAPI;
import com.qualityplus.oneshoot.api.box.Box;
import com.qualityplus.oneshoot.api.loader.OneShootArenaLoader;
import com.qualityplus.oneshoot.persistance.data.OneShootPlayer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@Scan(deep = true)
public final class OneShoot extends OkaeriSilentPlugin {

    @Inject
    private static IOneShootAPI api;
    private static OneShoot instance;

    @Planned(value = ExecutionPhase.PRE_STARTUP)
    public void setup() {
        redirectDataFolder(this);
        instance = this;
    }

    /**
     * Point data folder at plugins/GenesiCore/games/OneShot (Paper getDataFolder is final).
     */
    private static void redirectDataFolder(JavaPlugin plugin) {
        Plugin core = Bukkit.getPluginManager().getPlugin("GenesiCore");
        File genesiCore = core != null ? core.getDataFolder() : new File("plugins", "GenesiCore");
        File target = new File(new File(genesiCore, "games"), plugin.getName());
        //noinspection ResultOfMethodCallIgnored
        target.mkdirs();
        try {
            Field field = JavaPlugin.class.getDeclaredField("dataFolder");
            field.setAccessible(true);
            field.set(plugin, target);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Failed to redirect OneShot data folder to " + target.getAbsolutePath());
            e.printStackTrace();
        }
    }

    @Planned(value = ExecutionPhase.PRE_SHUTDOWN)
    @SuppressWarnings("unchecked")
    private void whenStop(Box box) {
        api.getBox().getOneShootService().getGames().forEach(game -> {
            List<OneShootPlayer> playerList = game.getPlayers();
            for (OneShootPlayer player : playerList) {
                api.getBox().getOneShootService().finishGame(game, player, false);
            }
        });
        Bukkit.getOnlinePlayers().stream().forEach(p -> api.getPlayerService().unloadPlayerDataSync(p));
    }

    @Delayed(time = 1)
    public void initLoaders(OneShootArenaLoader arenaLoader) {
        arenaLoader.load();
    }

    public static IOneShootAPI getApi() {
        return api;
    }

    public static OneShoot getInstance() {
        return instance;
    }
}
