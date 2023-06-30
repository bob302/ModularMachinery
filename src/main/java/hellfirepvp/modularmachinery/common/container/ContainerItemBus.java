/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.container;

import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerItemBus
 * Created by HellFirePvP
 * Date: 09.07.2017 / 17:36
 */
public class ContainerItemBus extends ContainerBase<TileItemBus> {

    public ContainerItemBus(TileItemBus owner, EntityPlayer opening) {
        super(owner, opening);

        addInventorySlots(owner.getInventory().asGUIAccess(), owner.getSize());
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            boolean changed = false;
            if (index >= 0 && index < 36) {
                if(this.mergeItemStack(itemstack1, 36, inventorySlots.size(), false)) {
                    changed = true;
                }
            }

            if(!changed) {
                if (index >= 0 && index < 27) {
                    if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 27 && index < 36) {
                    if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    private void addInventorySlots(IItemHandlerModifiable itemHandler, ItemBusSize size) {
        addSlotToContainerWithAutoPosition(itemHandler, size);
    }

    private void addSlotToContainerWithAutoPosition(IItemHandlerModifiable itemHandler, ItemBusSize size) {
        int slots = size.getSlotCount();
        for (int zz = 0; zz < slots; zz++) {
            for (int xx = 0; xx < 9; xx++) {
                int index = zz * 9 + xx;
                if (!(index > slots - 1))
                    addSlotToContainer(new SlotItemHandler(itemHandler, index, 8 + xx * 18, 8 + zz * 18));
            }
        }
    }

}
