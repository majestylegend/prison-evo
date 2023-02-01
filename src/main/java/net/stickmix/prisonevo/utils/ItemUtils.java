package net.stickmix.prisonevo.utils;

import lombok.experimental.UtilityClass;
import net.stickmix.game.api.util.ChatUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.StringJoiner;

@UtilityClass
public class ItemUtils {

    public final static String SAFE_ITEM = ChatUtil.colorize("&a&lПредмет не выпадает при смерти");

    public String itemToBase64(ItemStack itemStack) {
        if (itemStack == null) {
            return "null";
        }
        return Base64.getEncoder().encodeToString(itemToBytes(itemStack));
    }

    public ItemStack itemFromBase64(String data) {
        if ("null".equals(data)) {
            return null;
        }
        return itemFromBytes(Base64.getDecoder().decode(data));
    }

    public byte[] itemToBytes(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        byte[] itemBytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(itemStack);
            itemBytes = outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected exception due write ItemStack to BukkitObjectOutputStream", e);
        }
        return itemBytes;
    }

    public ItemStack itemFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException("Unexpected exception due read ItemStack from BukkitObjectInputStream", e);
        }
    }

    public boolean nullOrAir(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    public static ItemStack[] stringToArray(String items, ItemStack[] array) {
        if (items == null || items.isEmpty()) {
            return array;
        }
        String[] split = items.split(" ");
        for (int i = 0; i < split.length; i++) {
            array[i] = ItemUtils.itemFromBase64(split[i]);
        }
        return array;
    }

    public static String arrayToString(ItemStack[] array) {
        StringJoiner joiner = new StringJoiner(" ");
        for (ItemStack stack : array) {
            joiner.add(ItemUtils.itemToBase64(stack));
        }
        return joiner.toString();
    }

}