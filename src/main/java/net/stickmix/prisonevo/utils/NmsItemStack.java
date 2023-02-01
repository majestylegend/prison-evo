package net.stickmix.prisonevo.utils;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.Consumer;

public class NmsItemStack {

    private final net.minecraft.server.v1_12_R1.ItemStack nmsItem;

    private NmsItemStack(ItemStack itemStack) {
        nmsItem = CraftItemStack.asNMSCopy(itemStack);
    }

    public static NmsItemStack from(ItemStack original) {
        return new NmsItemStack(original);
    }

    public boolean hasTag() {
        return nmsItem.hasTag();
    }

    public NBTTagCompound getTag() {
        return nmsItem.getTag();
    }

    public NBTTagCompound getOrCreateTag() {
        return Optional.ofNullable(getTag()).orElseGet(() -> {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            setTag(nbtTagCompound);
            return nbtTagCompound;
        });
    }

    public void setTag(NBTTagCompound nbtTag) {
        nmsItem.setTag(nbtTag);
    }

    public void setTag(Consumer<NBTTagCompound> tagConsumer) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        tagConsumer.accept(nbtTagCompound);
        setTag(nbtTagCompound);
    }

    public ItemStack toBukkitItem() {
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

}
