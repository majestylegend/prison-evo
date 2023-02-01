package net.stickmix.prisonevo.utils.serialization;


public interface Serializator {

    /**
     * Получить реализацию DataSerializer без каких-либо данных внутри.
     *
     * @return пустую реализацию DataSerializer.
     */
    DataSerializer createEmptyDataSerializer();

    /**
     * Получить реализацию DataSerializer с внутренними данными в виде переданного массива байт.
     *
     * @param data данные, поверх которых нужно построить DataSerializer.
     * @return реализацию DataSerializer поверх переданного массива байт.
     */
    DataSerializer createDataSerializer(byte[] data);


    ItemStackSerializer getItemStackSerializer();


    LocationsSerializer getLocationsSerializer();

}
