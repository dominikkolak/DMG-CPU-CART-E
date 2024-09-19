package cartridge.header.enums;

public enum SgbSupport {

    UNSUPPORTED(0x00),
    SUPPORTED(0x03);

    public final int value;

    SgbSupport(int value) {
        this.value = value;
    }

    public static SgbSupport fromByte(int b) {
        return b == 0x03 ? SUPPORTED : UNSUPPORTED;
    }

    public boolean isSupported() {
        return this == SUPPORTED;
    }
}
