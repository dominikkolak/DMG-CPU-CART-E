package cartridge.header.enums;

public enum RamSize {

    NONE(0x00, 0, 0),           // No RAM
    UNUSED(0x01, 0, 0),         // Listed in docs but not used in production Cartridges!
    KB_8(0x02, 8192, 1),        // 8 KiB, 1 bank
    KB_32(0x03, 32768, 4),      // 32 KiB, 4 banks
    KB_128(0x04, 131072, 16),   // 128 KiB, 16 banks
    KB_64(0x05, 65536, 8);      // 64 KiB, 8 banks

    public final int value;
    public final int size_in_bytes;
    public final int bank_count;

    RamSize(int value, int sizeInBytes, int bank_count) {
        this.value = value;
        this.size_in_bytes = sizeInBytes;
        this.bank_count = bank_count;
    }

    public static RamSize fromByte(int b) {
        for (RamSize size : values()) {
            if (size.value == b) {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown RAM size");
    }

}
