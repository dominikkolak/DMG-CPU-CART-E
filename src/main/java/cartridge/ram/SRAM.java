package cartridge.ram;

import cartridge.constants.CartridgeConstants;
import cartridge.interfaces.ExternalMemory;

public class SRAM implements ExternalMemory {
    private final byte[] data_;
    private final int total_size_;
    private final int bank_size_;
    private final int bank_count_;

    private int current_bank_;
    private boolean enabled_;

    public SRAM(int size_in_bytes) {
        if (size_in_bytes <= 0) { throw new IllegalArgumentException("SRAM Negative"); }

        // power of 2
        if ((size_in_bytes & (size_in_bytes - 1)) != 0) { throw new IllegalArgumentException("SRAM size must be power of 2"); }

        this.total_size_ = size_in_bytes;
        this.data_ = new byte[size_in_bytes];
        this.bank_size_ = CartridgeConstants.RAM_BANK_SIZE;
        this.bank_count_ = size_in_bytes / bank_size_;

        this.current_bank_ = 0;
        this.enabled_ = false;

        // init to 0xFF, uninitialized ram behavior needs verification
        clear();
    }

    @Override
    public int read(int address) {
        if (!enabled_) {
            return 0xFF; // open bus
        }

        // 0xA000-0xBFFF => 0x0000-0x1FFF: 8kb mask
        int offset = address & 0x1FFF;

        int physical_address = (current_bank_ * bank_size_) + offset;

        if (physical_address >= total_size_) {
            return 0xFF;
        }

        return Byte.toUnsignedInt(data_[physical_address]);
    }

    @Override
    public void write(int address, int value) {
        if (!enabled_) {
            return; // ignore
        }

        // 0xA000-0xBFFF => 0x0000-0x1FFF: 8kb mask
        int offset = address & 0x1FFF;

        int physical_address = (current_bank_ * bank_size_) + offset;

        if (physical_address >= total_size_) {
            return; // ignore
        }

        data_[physical_address] = (byte) (value & 0xFF);
    }

    @Override
    public void selectBank(int bank) {
        if (bank_count_ > 0) {
            current_bank_ = bank % bank_count_;
        } else {
            current_bank_ = 0;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled_ = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled_;
    }

    @Override
    public int getCurrentBank() {
        return current_bank_;
    }

    @Override
    public int getBankCount() {
        return bank_count_;
    }

    @Override
    public int getSize() {
        return total_size_;
    }

    @Override
    public byte[] getData() {
        return data_;
    }

    @Override
    public void loadData(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data null");
        }

        System.arraycopy(data, 0, data_, 0, total_size_);
    }

    @Override
    public void reset() {
        current_bank_ = 0;
        enabled_ = false;
    }

    @Override
    public void clear() {
        // uninit ram behavior. Needs to be checked especially with open bus
        for (int i = 0; i < total_size_; i++) {
            data_[i] = (byte) 0xFF;
        }
    }

    public boolean isDirty() {
        for (byte b : data_) {
            if (b != (byte) 0xFF) {
                return true;
            }
        }
        return false;
    }

    public int readBank(int bank, int offset) {
        if (bank < 0 || bank >= bank_count_) {
            return 0xFF;
        }

        if (offset < 0 || offset >= bank_size_) {
            return 0xFF;
        }

        int physical_address = (bank * bank_size_) + offset;

        if (physical_address >= total_size_) {
            return 0xFF;
        }

        return Byte.toUnsignedInt(data_[physical_address]);
    }

    public void writeBank(int bank, int offset, int value) {
        if (bank < 0 || bank >= bank_count_) {
            return;
        }

        if (offset < 0 || offset >= bank_size_) {
            return;
        }

        int physical_address = (bank * bank_size_) + offset;

        if (physical_address >= total_size_) {
            return;
        }

        data_[physical_address] = (byte) (value & 0xFF);
    }

    @Override
    public String toString() {
        return String.format(
                "sram{size=%d bytes, banks=%d, current_bank=%d, enabled=%s, dirty=%s}",
                total_size_,
                bank_count_,
                current_bank_,
                enabled_,
                isDirty()
        );
    }
}
