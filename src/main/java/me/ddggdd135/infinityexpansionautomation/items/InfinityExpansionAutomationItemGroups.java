package me.ddggdd135.infinityexpansionautomation.items;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.infinityexpansionautomation.InfinityExpansionAutomation;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class InfinityExpansionAutomationItemGroups {
    public static final ItemStack MAIN_ITEM_GROUP_CURSOR =
            new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS, "&c无&6尽&e自&a动&9化");
    public static final ItemGroup MAIN_ITEM_GROUP =
            new ItemGroup(new NamespacedKey(InfinityExpansionAutomation.getInstance(), "main"), MAIN_ITEM_GROUP_CURSOR);
}
