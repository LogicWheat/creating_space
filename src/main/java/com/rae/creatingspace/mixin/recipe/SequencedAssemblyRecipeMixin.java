package com.rae.creatingspace.mixin.recipe;

import com.rae.creatingspace.recipes.IMoreNbtConditions;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = SequencedAssemblyRecipe.class)
public class SequencedAssemblyRecipeMixin implements IMoreNbtConditions {
    @Unique
    public ArrayList<String> nbtKeys = new ArrayList<>();
    @Unique
    public boolean cS_1_19_2$matchNbt = false;

    public void setKeepNbt(ArrayList<String> nbtKeys) {
        this.nbtKeys = nbtKeys;
    }

    public void setMachNbt(boolean value) {
        cS_1_19_2$matchNbt = value;
    }

    @Override
    public boolean isKeepNbt() {
        return !nbtKeys.isEmpty();
    }

    @Override
    public boolean isMachNbt() {
        return cS_1_19_2$matchNbt;
    }
    //issue will using either creatingspace:range_int or forge:intersection and a transitional item different that the first ingredient
    //possibly related to getRecipes

    /*@ModifyVariable(method = "advance", at = @At(value = "LOAD", ordinal = 0),name = "itemTag")
    private CompoundTag addNbt(CompoundTag value){
        return value;
    }*/
    @Inject(method = "advance", at = @At(value = "RETURN"), cancellable = true, remap = false)
    public void addTagBack(ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (isKeepNbt()) {
            ItemStack advancedItem = cir.getReturnValue();
            CompoundTag itemTag = advancedItem.getOrCreateTag();
            CompoundTag toKeepTag = input.getOrCreateTag();
            for (String key : nbtKeys) {
                if (toKeepTag.get(key) != null) {
                    itemTag.put(key, toKeepTag.get(key));
                }
            }
            advancedItem.setTag(itemTag);
            cir.setReturnValue(advancedItem);
        }
    }
}
