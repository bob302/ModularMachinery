/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientItem;
import crafttweaker.api.item.IngredientStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import hellfirepvp.modularmachinery.common.crafting.PreparedRecipe;
import hellfirepvp.modularmachinery.common.crafting.RecipeRegistry;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementEnergy;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementItem;
import hellfirepvp.modularmachinery.common.lib.RequirementTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipePrimer
 * Created by HellFirePvP
 * Date: 02.01.2018 / 18:18
 */
@ZenRegister
@ZenClass("mods.modularmachinery.RecipePrimer")
public class RecipePrimer implements PreparedRecipe {

    private final ResourceLocation name, machineName;
    private final int tickTime, priority;
    private final boolean doesVoidPerTick;

    private List<ComponentRequirement> components = new LinkedList<>();
    private ComponentRequirement lastComponent = null;

    public RecipePrimer(ResourceLocation registryName, ResourceLocation owningMachine, int tickTime, int configuredPriority, boolean doesVoidPerTick) {
        this.name = registryName;
        this.machineName = owningMachine;
        this.tickTime = tickTime;
        this.priority = configuredPriority;
        this.doesVoidPerTick = doesVoidPerTick;
    }

    @ZenMethod
    public RecipePrimer setChance(float chance) {
        if(lastComponent != null) {
            if(lastComponent instanceof ComponentRequirement.ChancedRequirement) {
                ((ComponentRequirement.ChancedRequirement) lastComponent).setChance(chance);
            } else {
                CraftTweakerAPI.logWarning("Cannot set chance for not-chance-based Component: " + lastComponent.getClass().toString());
            }
        }
        return this;
    }

    @ZenMethod
    public RecipePrimer setTag(String selectorTag) {
        if(lastComponent != null) {
            lastComponent.setTag(new ComponentSelectorTag(selectorTag));
        }
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // Energy input & output
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public RecipePrimer addEnergyPerTickInput(int perTick) {
        requireEnergy(IOType.INPUT, perTick);
        return this;
    }

    @ZenMethod
    public RecipePrimer addEnergyPerTickOutput(int perTick) {
        requireEnergy(IOType.OUTPUT, perTick);
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // FLUID input & output
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public RecipePrimer addFluidInput(ILiquidStack stack) {
        requireFluid(IOType.INPUT, stack);
        return this;
    }

    @ZenMethod
    public RecipePrimer addFluidOutput(ILiquidStack stack) {
        requireFluid(IOType.OUTPUT, stack);
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // GAS input & output
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    @Optional.Method(modid = "mekanism")
    public RecipePrimer addGasInput(String gasName, int amount) {
        requireGas(IOType.INPUT, gasName, amount);
        return this;
    }

    @ZenMethod
    @Optional.Method(modid = "mekanism")
    public RecipePrimer addGasOutput(String gasName, int amount) {
        requireGas(IOType.OUTPUT, gasName, amount);
        return this;
    }

    //----------------------------------------------------------------------------------------------
    // ITEM input
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public RecipePrimer addItemInput(IIngredient input) {
        if (input instanceof IItemStack) {
            requireFuel(IOType.INPUT, (IItemStack) input);
        } else if (input instanceof IOreDictEntry) {
            requireFuel(IOType.INPUT, ((IOreDictEntry) input).getName(), 1);
        } else if (input instanceof IngredientStack && input.getInternal() instanceof IOreDictEntry) {
            requireFuel(IOType.INPUT, ((IOreDictEntry) input.getInternal()).getName(), input.getAmount());
        } else {
            CraftTweakerAPI.logError(String.format("[ModularMachinery] Invalid input type %s(%s)! Ignored.", input, input.getClass()));
        }

        return this;
    }

    @Deprecated
    @ZenMethod
    public RecipePrimer addItemInput(IOreDictEntry oreDict, int amount) {
        requireFuel(IOType.INPUT, oreDict.getName(), amount);
        CraftTweakerAPI.logWarning(String.format("[ModularMachinery] Deprecated method " +
                        "`addItemInput(<ore:%s>, %s)`! Consider using `addItemInput(<ore:%s> * %s)`",
                oreDict.getName(), amount, oreDict.getName(), amount)
        );
        return this;
    }

    @ZenMethod
    public RecipePrimer addItemInputs(IIngredient... inputs) {
        for (IIngredient input : inputs) {
            addItemInput(input);
        }
        return this;
    }

    @ZenMethod
    public RecipePrimer addFuelItemInput(int requiredTotalBurnTime) {
        requireFuel(requiredTotalBurnTime);
        return this;
    }
    //----------------------------------------------------------------------------------------------
    // ITEM output
    //----------------------------------------------------------------------------------------------

    @ZenMethod
    public RecipePrimer addItemOutput(IIngredient output) {
        if (output instanceof IItemStack) {
            requireFuel(IOType.OUTPUT, (IItemStack) output);
        } else if (output instanceof IOreDictEntry) {
            requireFuel(IOType.OUTPUT, ((IOreDictEntry) output).getName(), 1);
        } else if (output instanceof IngredientStack && output.getInternal() instanceof IOreDictEntry) {
            requireFuel(IOType.OUTPUT, ((IOreDictEntry) output.getInternal()).getName(), output.getAmount());
        } else {
            CraftTweakerAPI.logError(String.format("[ModularMachinery] Invalid output type %s(%s)! Ignored.", output, output.getClass()));
        }

        return this;
    }

    @Deprecated
    @ZenMethod
    public RecipePrimer addItemOutput(IOreDictEntry oreDict, int amount) {
        requireFuel(IOType.OUTPUT, oreDict.getName(), amount);
        CraftTweakerAPI.logWarning(String.format("[ModularMachinery] Deprecated method " +
                        "`addItemOutput(<ore:%s>, %s)`! Consider using `addItemOutput(<ore:%s> * %s)`",
                oreDict.getName(), amount, oreDict.getName(), amount)
        );
        return this;
    }

    @ZenMethod
    public RecipePrimer addItemOutputs(IIngredient... inputs) {
        for (IIngredient input : inputs) {
            addItemOutput(input);
        }

        return this;
    }

    //----------------------------------------------------------------------------------------------
    // Internals
    //----------------------------------------------------------------------------------------------
    private void requireEnergy(IOType ioType, long perTick) {
        appendComponent(new RequirementEnergy(ioType, perTick));
    }

    private void requireFluid(IOType ioType, ILiquidStack stack) {
        FluidStack mcFluid = CraftTweakerMC.getLiquidStack(stack);
        if(mcFluid == null) {
            CraftTweakerAPI.logError("FluidStack not found/unknown fluid: " + stack.toString());
            return;
        }
        if(stack.getTag() != null) {
            mcFluid.tag = CraftTweakerMC.getNBTCompound(stack.getTag());
        }
        RequirementFluid rf = new RequirementFluid(ioType, mcFluid);
        appendComponent(rf);
    }

    @Optional.Method(modid = "mekanism")
    private void requireGas(IOType ioType, String gasName, int amount) {
        Gas gas = GasRegistry.getGas(gasName);
        if (gas == null) {
            CraftTweakerAPI.logError("GasStack not found/unknown gas: " + gasName);
            return;
        }
        amount = Math.max(0, amount);
        GasStack gasStack = new GasStack(gas, amount);
        RequirementFluid req = RequirementFluid.createMekanismGasRequirement(RequirementTypesMM.REQUIREMENT_GAS, ioType, gasStack);
        appendComponent(req);
    }

    private void requireItem(IOType ioType, int requiredTotalBurnTime) {
        appendComponent(new RequirementItem(ioType, requiredTotalBurnTime));
    }

    private void requireItem(IOType ioType, IItemStack stack) {
        ItemStack mcStack = CraftTweakerMC.getItemStack(stack);
        if(mcStack.isEmpty()) {
            CraftTweakerAPI.logError("ItemStack not found/unknown item: " + stack.toString());
            return;
        }
        RequirementItem ri = new RequirementItem(ioType, mcStack);
        if(stack.getTag().length() > 0) {
            ri.tag = CraftTweakerMC.getNBTCompound(stack.getTag());
            ri.previewDisplayTag = CraftTweakerMC.getNBTCompound(stack.getTag());
        }
        appendComponent(ri);
    }

    private void requireItem(IOType ioType, String oreDictName, int amount) {
        appendComponent(new RequirementItem(ioType, oreDictName, amount));
    }

    private void requireFuel(int requiredTotalBurnTime) {
        appendComponent(new RequirementItem(IOType.INPUT, requiredTotalBurnTime));
    }

    private void requireFuel(IOType ioType, IItemStack stack) {
        ItemStack mcStack = CraftTweakerMC.getItemStack(stack);
        if (mcStack.isEmpty()) {
            CraftTweakerAPI.logError("[ModularMachinery] ItemStack not found/unknown item: " + stack.toString());
            return;
        }
        RequirementItem ri = new RequirementItem(ioType, mcStack);
        if (stack.getTag().length() > 0) {
            ri.tag = CraftTweakerMC.getNBTCompound(stack.getTag());
            ri.previewDisplayTag = CraftTweakerMC.getNBTCompound(stack.getTag());
        }
        appendComponent(ri);
    }

    private void requireFuel(IOType ioType, String oreDictName, int amount) {
        appendComponent(new RequirementItem(ioType, oreDictName, amount));
    }


    public void appendComponent(ComponentRequirement component) {
        this.components.add(component);
        this.lastComponent = component;
    }

    //----------------------------------------------------------------------------------------------
    // build
    //----------------------------------------------------------------------------------------------
    @ZenMethod
    public void build() {
        RecipeRegistry.getRegistry().registerRecipeEarly(this);
    }

    //----------------------------------------------------------------------------------------------
    // lingering stats
    //----------------------------------------------------------------------------------------------

    @Override
    public String getFilePath() {
        return "";
    }

    @Override
    public ResourceLocation getRecipeRegistryName() {
        return name;
    }

    @Override
    public ResourceLocation getAssociatedMachineName() {
        return machineName;
    }

    @Override
    public int getTotalProcessingTickTime() {
        return tickTime;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean voidPerTickFailure() {
        return doesVoidPerTick;
    }

    @Override
    public List<ComponentRequirement> getComponents() {
        return components;
    }

}
