package me.ddggdd135.infinityexpansionautomation;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import java.util.Objects;
import me.ddggdd135.infinityexpansionautomation.items.InfinityExpansionAutomationItems;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InfinityExpansionAutomation extends JavaPlugin implements SlimefunAddon {

    private static InfinityExpansionAutomation instance;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        if (!Bukkit.getPluginManager().isPluginEnabled("InfinityExpansion")) {
            getPluginLoader()
                    .enablePlugin(
                            Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("InfinityExpansion")));
        }
        InfinityExpansionAutomationItems.onSetup(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        instance = null;
    }

    @NotNull @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable @Override
    public String getBugTrackerURL() {
        return null;
    }

    public static InfinityExpansionAutomation getInstance() {
        return instance;
    }
}
