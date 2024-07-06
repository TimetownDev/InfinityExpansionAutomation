package me.ddggdd135.infinityexpansionautomation.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.mooy1.infinityexpansion.infinitylib.machines.MachineRecipeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.List;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.utils.ItemUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InfinityCrafter extends TickingBlock
        implements InventoryBlock, EnergyNetComponent, MachineProcessHolder<CraftingOperation> {
    private static final ItemStack TARGET_SLOT =
            new SlimefunItemStack("_UI_TARGET_SLOT", Material.RED_STAINED_GLASS_PANE, "&c目标输入槽");
    public static final int[] targetBorder = new int[] {0, 1, 2, 9, 11, 18, 19, 20};
    public static final int targetSlot = 10;
    public static final int[] inputBorder = new int[] {27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 45, 53};
    public static final int[] inputSlots = new int[] {37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 52};
    public static final int[] outputBorder = new int[] {3, 4, 5, 6, 7, 8, 12, 17, 21, 22, 23, 24, 25, 26};
    public static final int[] outputSlot = new int[] {13, 14, 15, 16};
    public static final int processSlot = 44;
    private final MachineProcessor<CraftingOperation> processor = new MachineProcessor<>(this);
    private int energyCapacity = -1;
    private MachineRecipeType machineRecipeType;

    public InfinityCrafter(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            MachineRecipeType machineRecipeType) {
        super(itemGroup, item, recipeType, recipe);
        this.machineRecipeType = machineRecipeType;

        processor.setProgressBar(getProgressBar());
        createPreset(this);
        addItemHandler(new BlockBreakHandler(false, true) {
            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack itemStack, List<ItemStack> list) {
                processor.endOperation(e.getBlock());
                BlockMenu blockMenu = StorageCacheUtils.getMenu(e.getBlock().getLocation());
                if (blockMenu == null) return;

                blockMenu.dropItems(e.getBlock().getLocation(), getInputSlots());
                blockMenu.dropItems(e.getBlock().getLocation(), getOutputSlots());
                blockMenu.dropItems(e.getBlock().getLocation(), targetSlot);
            }

            @Override
            public void onExplode(Block block, List<ItemStack> drops) {
                processor.endOperation(block);
                BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (blockMenu == null) return;

                for (int slot : inputSlots) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null && !itemStack.getType().isAir())
                        block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                }

                for (int slot : outputSlot) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack != null && !itemStack.getType().isAir())
                        block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                }

                ItemStack itemStack = blockMenu.getItemInSlot(targetSlot);
                if (itemStack != null && !itemStack.getType().isAir())
                    block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
            }
        });
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(
            @Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData slimefunBlockData) {
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return;
        CraftingOperation craftingOperation = processor.getOperation(block);
        if (takeCharge(block.getLocation())) {
            if (craftingOperation == null) {
                ItemStack target = blockMenu.getItemInSlot(targetSlot);
                if (target == null || target.getType().isAir()) return;
                SlimefunItem item = SlimefunItem.getByItem(target);
                if (item == null || item.getRecipeType() != machineRecipeType) return;
                ItemStack[] recipe = item.getRecipe();
                ItemHashMap<Integer> itemHashMap = ItemUtils.getAmounts(recipe);
                recipe = ItemUtils.createItems(itemHashMap);
                if (!ItemUtils.contains(blockMenu, getInputSlots(), recipe)
                        || !InvUtils.fits(blockMenu.getInventory(), item.getRecipeOutput(), getOutputSlots())) return;
                ItemUtils.tryTakeItem(blockMenu, itemHashMap, getInputSlots());
                craftingOperation = new CraftingOperation(recipe, new ItemStack[] {item.getRecipeOutput()}, 30);
                processor.startOperation(block, craftingOperation);
                processor.updateProgressBar(blockMenu, processSlot, craftingOperation);
            } else if (craftingOperation.isFinished()
                    && InvUtils.fitAll(blockMenu.getInventory(), craftingOperation.getResults(), getOutputSlots())) {
                for (ItemStack result : craftingOperation.getResults()) {
                    blockMenu.pushItem(result, getOutputSlots());
                }
                processor.endOperation(block);
                blockMenu.replaceExistingItem(
                        processSlot, new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " "));
            } else if (!craftingOperation.isFinished()) {
                craftingOperation.addProgress(1);
                processor.updateProgressBar(blockMenu, processSlot, craftingOperation);
            }
        }
    }

    @Override
    public int[] getInputSlots() {
        return inputSlots;
    }

    @Override
    public int[] getOutputSlots() {
        return outputSlot;
    }

    @Override
    public void init(@Nonnull BlockMenuPreset blockMenuPreset) {
        for (int slot : targetBorder) {
            blockMenuPreset.addItem(slot, TARGET_SLOT, ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : inputBorder) {
            blockMenuPreset.addItem(slot, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : outputBorder) {
            blockMenuPreset.addItem(slot, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : getOutputSlots()) {
            blockMenuPreset.addMenuClickHandler(slot, new ChestMenu.AdvancedMenuClickHandler() {

                @Override
                public boolean onClick(Player player, int slot, ItemStack cursor, ClickAction action) {
                    return false;
                }

                @Override
                public boolean onClick(
                        InventoryClickEvent e, Player player, int slot, ItemStack cursor, ClickAction action) {
                    return cursor == null || cursor.getType().isAir();
                }
            });
        }

        blockMenuPreset.addItem(
                processSlot,
                new AdvancedCustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " "),
                ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        blockMenu.addMenuClickHandler(targetSlot, (player, slot, cursor, action) -> {
            ItemStack itemStack = blockMenu.getItemInSlot(targetSlot);
            if (cursor == null || cursor.getType().isAir() || cursor.isSimilar(itemStack)) return true;
            SlimefunItem item = SlimefunItem.getByItem(cursor);
            return item != null && item.getRecipeType() == machineRecipeType;
        });
    }

    @Nonnull
    @Override
    public MachineProcessor<CraftingOperation> getMachineProcessor() {
        return processor;
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return energyCapacity;
    }

    public int getEnergyConsumption() {
        return energyCapacity;
    }

    @Nonnull
    public InfinityCrafter setEnergyConsumption(int energyCapacity) {
        this.energyCapacity = energyCapacity;
        return this;
    }

    protected boolean takeCharge(@Nonnull Location l) {
        Validate.notNull(l, "Can't attempt to take charge from a null location!");

        if (isChargeable()) {
            int charge = getCharge(l);

            if (charge < getEnergyConsumption()) {
                return false;
            }

            setCharge(l, charge - getEnergyConsumption());
            return true;
        } else {
            return true;
        }
    }

    public ItemStack getProgressBar() {
        return new ItemStack(Material.IRON_PICKAXE);
    }
}
