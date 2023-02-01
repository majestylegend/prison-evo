package net.stickmix.prisonevo.utils.serialization;

public interface DataSerializer extends AutoCloseable {

    byte readByte();

    void writeByte(byte value);

    default boolean readBoolean() {
        return readByte() == 0;
    }

    default void writeBoolean(boolean value) {
        writeByte((byte) (value ? 0 : 1));
    }

    int readVarInt();

    void writeVarInt(int value);

    String readString(int maxStringLength);

    void writeString(String value);

    String readStringNullable(int maxStringLength);

    void writeStringNullable(String value);

    byte[] toBytes();

    void release();

    default void close() {
        release();
    }

}
