package cartridge.mbc;

import cartridge.interfaces.ExternalMemory;
import cartridge.interfaces.MemoryBankController;
import cartridge.interfaces.ReadOnlyMemory;
import cartridge.rom.ROM;

public class MBC1 implements MemoryBankController {

    private final ReadOnlyMemory rom_;
    private final ExternalMemory ram_;

    // Banking state
    private int rom_bank_;          // Lower 5 bits (0x01-0x1F)
    private int ram_bank_;          // Upper 2 bits (0x00-0x03)
    private boolean ram_enabled_;   // RAM enable flag
    private boolean banking_mode_;  // ROM mode (0) / RAM mode (1)

    public MBC1(ReadOnlyMemory rom, ExternalMemory ram) {
        this.rom_ = rom;
        this.ram_ = ram;

        reset();
    }

    @Override
    public int readRom(int address) {
        if (address < 0x4000) {
            // Bank 0, in RAM banking mode upper bits affect bank 0
            int bank = banking_mode_ ? (ram_bank_ << 5) : 0;
            return rom_.readBank(bank, address);
        } else {
            // Bank N
            int bank = calculateRomBank();
            int offset = address - 0x4000;
            return rom_.readBank(bank, offset);
        }
    }

    @Override
    public int readRam(int address) {
        if (!ram_enabled_ || ram_ == null) {
            return 0xFF;
        }

        // RAM bank only used in RAM banking mode
        int bank = banking_mode_ ? ram_bank_ : 0;
        ram_.selectBank(bank);
        return ram_.read(address);
    }

    @Override
    public void writeRom(int address, int value) {
        if (address < 0x2000) {
            // 0x0A enables RAM everything else disables it
            ram_enabled_ = (value & 0x0F) == 0x0A;

            if (ram_ != null) {
                ram_.setEnabled(ram_enabled_);
            }

        } else if (address < 0x4000) {
            // ROM Bank Number = lower 5 bits
            rom_bank_ = value & 0x1F;

            // Bank 0 not selectable
            if (rom_bank_ == 0) {
                rom_bank_ = 1;
            }

        } else if (address < 0x6000) {
            // RAM Bank Number | ROM Bank upper bits
            ram_bank_ = value & 0x03;

        } else {
            // 0 = ROM | 1 = RAM
            banking_mode_ = (value & 0x01) == 1;
        }
    }

    @Override
    public void writeRam(int address, int value) {
        if (!ram_enabled_ || ram_ == null) {
            return;
        }

        // RAM bank only used in RAM banking mode
        int bank = banking_mode_ ? ram_bank_ : 0;
        ram_.selectBank(bank);
        ram_.write(address, value);
    }

    @Override
    public void reset() {
        rom_bank_ = 1; // bank 0 not selectable
        ram_bank_ = 0;
        ram_enabled_ = false;
        banking_mode_ = false; // ROM by default

        if (ram_ != null) {
            ram_.reset();
        }
    }

    @Override
    public void tick(int cycles) {
        // no time-based behavior
    }

    private int calculateRomBank() {
        // lower 5 bits from rom_bank_ with upper 2 bits ram_bank_
        int bank = rom_bank_ | (ram_bank_ << 5);

        // Banks 0x00, 0x20, 0x40, 0x60
        // IMPORTANT: These banks cannot be selected, they map to next bank
        // This is some weird hardware behavior!
        if ((bank & 0x1F) == 0) {
            bank++;
        }

        int max_bank = rom_.getBankCount();
        return bank % max_bank;
    }

    @Override
    public int getCurrentRomBank() {
        return calculateRomBank();
    }

    @Override
    public int getCurrentRamBank() {
        return banking_mode_ ? ram_bank_ : 0;
    }

    @Override
    public boolean isRamEnabled() {
        return ram_enabled_;
    }

    @Override
    public String getComponentName() {
        return "MBC1";
    }

    public String getBankingMode() {
        return banking_mode_ ? "RAM" : "ROM";
    }

    @Override
    public String toString() {
        return String.format(
                "mbc1{rom=%s, ram=%s, rom_bank=0x%02X, ram_bank=%d, mode=%s, ram_enabled=%s}",
                rom_.toString(),
                ram_ != null ? ram_.toString() : "none",
                calculateRomBank(),
                getCurrentRamBank(),
                getBankingMode(),
                ram_enabled_
        );
    }

}
