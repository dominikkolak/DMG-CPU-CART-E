package cartridge.header;

import cartridge.header.enums.*;
import cartridge.constants.CartridgeConstants;

import java.util.Arrays;

public record Header(
        byte[] entry_point,            // 0x0100-0x0103
        byte[] nintendo_logo,          // 0x0104-0x0133: bitmap
        String title,                  // 0x0134-0x0143: variable length
        String manufacturer_code,      // 0x013F-0x0142
        CgbSupport cgb_support,        // 0x0143
        String new_licensee_code,      // 0x0144-0x0145
        SgbSupport sgb_support,        // 0x0146
        CartridgeType cartridge_type,  // 0x0147: MBC
        RomSize rom_size,              // 0x0148
        RamSize ram_size,              // 0x0149
        Destination destination,       // 0x014A
        int old_licensee_code,         // 0x014B
        int version_number,            // 0x014C
        int header_checksum,           // 0x014D
        int global_checksum            // 0x014E-0x014F: Global checksum (big-endian)
) {

    public static Header parse(byte[] rom_data) {
        if (rom_data == null || rom_data.length < CartridgeConstants.HEADER_END + 1) { throw new IllegalArgumentException("ROM too small to contain header"); }
        
        byte[] entry_point_tmp = extractBytes(rom_data, CartridgeConstants.ENTRY_POINT, 4);
        byte[] nintendo_logo_tmp = extractBytes(rom_data, CartridgeConstants.LOGO_START, 48);

        // CGB Flag determines title length
        int cgb_byte = Byte.toUnsignedInt(rom_data[CartridgeConstants.CGB_FLAG]);
        CgbSupport cgb_support_tmp = CgbSupport.fromByte(cgb_byte);
        
        String title_tmp;
        String manufacturer_code_tmp;

        if (cgb_support_tmp.isSupported()) {
            // CGB cartridge and manufacturer code present = shorter title
            title_tmp = extractString(rom_data, CartridgeConstants.TITLE_START, 11);
            manufacturer_code_tmp = extractString(rom_data, CartridgeConstants.MANUFACTURER_START, 4);
        } else {
            // DMG cartridge = full 15 character title
            title_tmp = extractString(rom_data, CartridgeConstants.TITLE_START, 15);
            manufacturer_code_tmp = "";
        }

        String new_licensee_code_tmp = extractString(rom_data, CartridgeConstants.LICENSEE_NEW_START, 2);
        SgbSupport sgb_support_tmp = SgbSupport.fromByte(Byte.toUnsignedInt(rom_data[CartridgeConstants.SGB_FLAG]));
        CartridgeType cartridge_type_tmp = CartridgeType.fromByte(Byte.toUnsignedInt(rom_data[CartridgeConstants.CARTRIDGE_TYPE]));
        RomSize rom_size_tmp = RomSize.fromByte(Byte.toUnsignedInt(rom_data[CartridgeConstants.ROM_SIZE]));
        RamSize ram_size_tmp = RamSize.fromByte(Byte.toUnsignedInt(rom_data[CartridgeConstants.RAM_SIZE]));
        Destination destination_tmp = Destination.fromByte(Byte.toUnsignedInt(rom_data[CartridgeConstants.DESTINATION]));
        int old_licensee_code_tmp = Byte.toUnsignedInt(rom_data[CartridgeConstants.LICENSEE_OLD]);
        int version_number_tmp = Byte.toUnsignedInt(rom_data[CartridgeConstants.VERSION]);
        int header_checksum_tmp = Byte.toUnsignedInt(rom_data[CartridgeConstants.HEADER_CHECKSUM]);
        int global_checksum_tmp = (Byte.toUnsignedInt(rom_data[CartridgeConstants.GLOBAL_CHECKSUM_HIGH]) << 8) | Byte.toUnsignedInt(rom_data[CartridgeConstants.GLOBAL_CHECKSUM_LOW]);

        return new Header(
                entry_point_tmp,
                nintendo_logo_tmp,
                title_tmp,
                manufacturer_code_tmp,
                cgb_support_tmp,
                new_licensee_code_tmp,
                sgb_support_tmp,
                cartridge_type_tmp,
                rom_size_tmp,
                ram_size_tmp,
                destination_tmp,
                old_licensee_code_tmp,
                version_number_tmp,
                header_checksum_tmp,
                global_checksum_tmp
        );
    }

    public boolean isHeaderChecksumValid(byte[] rom_data) {
        int calculated = calculateHeaderChecksum(rom_data);
        return calculated == header_checksum;
    }

    private int calculateHeaderChecksum(byte[] rom_data) {
        int checksum = 0;
        for (int addr = CartridgeConstants.TITLE_START; addr <= CartridgeConstants.VERSION; addr++) {
            checksum = (checksum - Byte.toUnsignedInt(rom_data[addr]) - 1) & 0xFF;
        }
        return checksum;
    }

    public boolean isNintendoLogoValid() {
        return Arrays.equals(nintendo_logo, CartridgeConstants.NINTENDO_LOGO);
    }

    public boolean isValid(byte[] rom_data) {
        return isHeaderChecksumValid(rom_data) && isNintendoLogoValid();
    }


    public boolean hasBattery() { return cartridge_type.has_battery; }

    public boolean hasRtc() { return cartridge_type.has_rtc; }

    public boolean hasRumble() { return cartridge_type.has_rumble; }

    public boolean hasRam() { return cartridge_type.has_ram || ram_size.size_in_bytes > 0; }

    public boolean supportsCgb() { return cgb_support.isSupported(); }

    public boolean isCgbOnly() { return cgb_support.isExclusive(); }

    public boolean supportsSgb() { return sgb_support.isSupported(); }

    public String getLicenseeCode() {
        if (old_licensee_code == 0x33) {
            return new_licensee_code;
        }
        return String.format("%02X", old_licensee_code);
    }

    public String getRegion() { return destination == Destination.JAPAN ? "Japan" : "International"; }

    private static byte[] extractBytes(byte[] data, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, offset, result, 0, length);
        return result;
    }

    private static String extractString(byte[] data, int offset, int max_length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max_length; i++) {
            byte b = data[offset + i];
            if (b == 0) break;
            if (b >= 0x20 && b <= 0x7E) {  // ASCII
                sb.append((char) b);
            }
        }
        return sb.toString().trim();
    }


    @Override
    public String toString() {
        return String.format(
                "header{title='%s', mbc=%s, rom=%s, ram=%s, cgb=%s, sgb=%s, region=%s, version=%d}",
                title,
                cartridge_type.name(),
                rom_size.name(),
                ram_size.name(),
                cgb_support.name(),
                sgb_support.name(),
                destination.name(),
                version_number
        );
    }
}
