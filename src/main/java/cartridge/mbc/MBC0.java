package cartridge.mbc;

import cartridge.interfaces.ExternalMemory;
import cartridge.interfaces.MemoryBankController;
import cartridge.interfaces.ReadOnlyMemory;
import cartridge.rom.ROM;

public class MBC0 implements MemoryBankController {

    private final ReadOnlyMemory rom_;
    private final ExternalMemory ram_;

    public MBC0(ReadOnlyMemory rom, ExternalMemory ram) {
        this.rom_ = rom;
        this.ram_ = ram;

        // MBC0 = 32KB (2 banks)
        if (rom.getSize() != 32768) {
            System.out.println("Warning: Size Mismatch MBC0");
        }

        // MBC0 no RAM enable register
        if (ram_ != null) {
            ram_.setEnabled(true);
        }
    }

    @Override
    public int readRom(int address) {
        // Direct mapping, no banking, just pass through
        return rom_.read(address);
    }

    @Override
    public int readRam(int address) {
        if (ram_ == null) {
            return 0xFF; // No RAM present
        }

        // Direct mapping
        return ram_.read(address);
    }

    @Override
    public void writeRom(int address, int value) {
        // no control registers
        // Writes to ROM ignored
    }

    @Override
    public void writeRam(int address, int value) {
        if (ram_ == null) {
            return; // No RAM
        }

        // Direct mapping
        ram_.write(address, value);
    }

    @Override
    public void reset() {
        // no state to reset
        // RAM state is self managed
        if (ram_ != null) {
            ram_.reset();
        }
    }

    @Override
    public void tick(int cycles) {
        // no time-based behavior
        // No real time clock
    }

    @Override
    public int getCurrentRomBank() {
        // no banking
        return 0;
    }

    @Override
    public int getCurrentRamBank() {
        // no RAM banking
        return 0;
    }

    @Override
    public boolean isRamEnabled() {
        // always enabled if present
        return ram_ != null;
    }

    @Override
    public String getComponentName() {
        return "MBC0";
    }

    @Override
    public String toString() {
        return String.format(
                "mbc0{rom=%s, ram=%s}",
                rom_.toString(),
                ram_ != null ? ram_.toString() : "none"
        );
    }
    
}
