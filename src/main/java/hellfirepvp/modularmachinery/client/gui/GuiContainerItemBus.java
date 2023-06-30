/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import hellfirepvp.modularmachinery.common.container.ContainerItemBus;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.lwjgl.opengl.GL11;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiContainerItemBus
 * Created by HellFirePvP
 * Date: 09.07.2017 / 18:42
 */
public class GuiContainerItemBus extends GuiContainerBase<ContainerItemBus> {

    private TileItemBus itemBus;

    public GuiContainerItemBus(TileItemBus itemBus, EntityPlayer opening) {
        super(new ContainerItemBus(itemBus, opening));
        this.itemBus = itemBus;
    }

    private ResourceLocation getTextureInventory() {
        return new ResourceLocation(ModularMachinery.MODID, "textures/gui/itembus_inventory.png");
    }

    @Override
    protected void setWidthHeight() {}

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(getTextureInventory());
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        GuiUtils.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize, 1);

        int slots = itemBus.getSize().getSlotCount();

        for (int zz = 0; zz < slots; zz++) {
            for (int xx = 0; xx < 9; xx++) {
                int index = zz * 9 + xx;
                if (!(index > size.getSlotCount() - 1))
                    GuiUtils.drawTexturedModalRect(i + 8 + xx * 18, j +8 + zz * 18, 176, 0, 16, 16, 2);
            }
        }
    }


    ItemBusSize size = this.container.getOwner().getSize();
}
