package cartridge.header.enums;

public enum RomSize {

    // Official sizes
    KB_32(0x00, 32768, 2),          // 32 KiB, 2 banks
    KB_64(0x01, 65536, 4),          // 64 KiB, 4 banks
    KB_128(0x02, 131072, 8),        // 128 KiB, 8 banks
    KB_256(0x03, 262144, 16),       // 256 KiB, 16 banks
    KB_512(0x04, 524288, 32),       // 512 KiB, 32 banks
    MB_1(0x05, 1048576, 64),        // 1 MiB, 64 banks
    MB_2(0x06, 2097152, 128),       // 2 MiB, 128 banks
    MB_4(0x07, 4194304, 256),       // 4 MiB, 256 banks
    MB_8(0x08, 8388608, 512),       // 8 MiB, 512 banks

    // Unofficial sizes
    MB_1_1(0x52, 1179648, 72),      // 1.1 MiB, 72 banks
    MB_1_2(0x53, 1310720, 80),      // 1.2 MiB, 80 banks
    MB_1_5(0x54, 1572864, 96);      // 1.5 MiB, 96 banks

    // Unofficial sizes are very rare in production cartridges but can be found in homebrew

    public final int value;
    public final int size_in_bytes;
    public final int bank_count;

    RomSize(int value, int sizeInBytes, int bank_count) {
        this.value = value;
        this.size_in_bytes = sizeInBytes;
        this.bank_count = bank_count;
    }

    public static RomSize fromByte(int b) {
        for (RomSize size : values()) {
            if (size.value == b) {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown ROM size");
    }

}
