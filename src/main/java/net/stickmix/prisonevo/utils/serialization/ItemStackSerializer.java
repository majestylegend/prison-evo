package net.stickmix.prisonevo.utils.serialization;

import org.bukkit.inventory.ItemStack;

public interface ItemStackSerializer {

    /**
     * Сериализовать переданный предмет в массив байт.
     * Сохраняются лишь тип, прочность (дата), название, описание, зачарования и то, может ли предмет ломаться.
     *
     * @param itemStack предмет для сериализации.
     * @return сериализованный предмет в виде массива байт.
     */
    byte[] serialize(ItemStack itemStack);

    /**
     * Десериализовать предмет из массива байт.
     *
     * @param data сериализованный предмет в виде массива байт.
     * @return десериализованный предмет.
     */
    ItemStack unserialize(byte[] data);

    /**
     * Сериализовать массив предметов в массив байт.
     * Этот метод оптимальнее, чем последовательная сериализация каждого из предметов массива по отдельности.
     *
     * @param itemStacks предметы для сериализации.
     * @return сериализованные предметы в виде единого массива байт.
     */
    byte[] serializeArray(ItemStack[] itemStacks);

    /**
     * Десериализовать массив предметов из массива байт.
     *
     * @param data сериализованные предметы в виде массива байт.
     * @return десериализованный массив предметов.
     */
    ItemStack[] unserializeArray(byte[] data);

}
