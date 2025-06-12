package cartridge;

import cartridge.exceptions.InvalidCartridgeException;
import cartridge.header.Header;
import cartridge.interfaces.ExternalMemory;
import cartridge.interfaces.MemoryBankController;
import cartridge.interfaces.ReadOnlyMemory;
import cartridge.ram.SRAM;
import cartridge.rom.ROM;
import shared.Addressable;
import cartridge.mbc.*;
import cartridge.header.enums.*;

public class Cartridge implements Addressable {
    private final Header header_;
    private final ReadOnlyMemory rom_;
    private final MemoryBankController mbc_;
    private final ExternalMemory ram_;

    public Cartridge(byte[] rom_data) {
        this.rom_ = new ROM(rom_data);
        this.header_ = Header.parse(rom_data);

        if (!header_.isHeaderChecksumValid(rom_data)) { throw new InvalidCartridgeException("Header checksum invalid"); }
        if (!header_.isNintendoLogoValid()) { throw new InvalidCartridgeException("Logo invalid"); }

        this.ram_ = createRam(header_);
        this.mbc_ = createMbc(header_, rom_, ram_);

        // TODO: RTC

        // TODO: Battery
    }

    private ExternalMemory createRam(Header h) {
        if (!h.hasRam()) { return null; }

        int ram_size = h.ram_size().size_in_bytes;

        if (ram_size == 0) { return null; }

        // Only Standard Ram
        // TODO: Handle otehr Ram types for the other Memory Bank Controllers

        return new SRAM(ram_size);
    }

    private MemoryBankController createMbc(Header h, ReadOnlyMemory rom, ExternalMemory ram) {
        CartridgeType type = h.cartridge_type();

        return switch (type) {
            case ROM_ONLY, ROM_RAM, ROM_RAM_BATTERY -> new MBC0(rom, ram);
            case MBC1, MBC1_RAM, MBC1_RAM_BATTERY -> new MBC1(rom, ram);

            // TODO: Implement other MBCs

            default -> throw new InvalidCartridgeException("Unsupported MBC type");
        };
    }

    @Override
    public int read(int address) {
        if (address >= 0x0000 && address <= 0x7FFF) {
            return mbc_.readRom(address);
        }
        else if (address >= 0xA000 && address <= 0xBFFF) {
            return mbc_.readRam(address);
        }

        // not a cartridge address
        return 0xFF;
    }

    @Override
    public void write(int address, int value) {
        if (address >= 0x0000 && address <= 0x7FFF) {
            // ROM W to MBC
            mbc_.writeRom(address, value);
        }
        else if (address >= 0xA000 && address <= 0xBFFF) {
            mbc_.writeRam(address, value);
        }

        // not a cartridge address
    }

    @Override
    public boolean accepts(int address) {
        // (0x0000-0x7FFF) and (0xA000-0xBFFF)
        return (address >= 0x0000 && address <= 0x7FFF) || (address >= 0xA000 && address <= 0xBFFF);
    }

    @Override
    public void tick(int cycles) {
        mbc_.tick(cycles);
        // TODO: Tick RTC for mbc3
    }

    @Override
    public void reset() {
        mbc_.reset();

        if (ram_ != null) {
            ram_.reset();
        }

        // TODO: Reset RTC
    }

    @Override
    public String getComponentName() {
        return "Cartridge[" + header_.title() + "]";
    }

    // TODO: Implement when battery ready
    public void save() {

    }

    // TODO: Implement when battery ready
    public void load() {

    }

    public Header getHeader() {
        return header_;
    }

    public String getTitle() {
        return header_.title();
    }

    public String getMbcType() {
        return mbc_.getComponentName();
    }

    public ReadOnlyMemory getRom() {
        return rom_;
    }

    public ExternalMemory getRam() {
        return ram_;
    }

    public MemoryBankController getMbc() {
        return mbc_;
    }

    @Override
    public String toString() {
        return String.format(
                "cartridge{title='%s', mbc=%s, rom=%d KB, ram=%d KB}",
                header_.title(),
                mbc_.getComponentName(),
                header_.rom_size().size_in_bytes / 1024,
                header_.ram_size().size_in_bytes / 1024
        );
    }

}
