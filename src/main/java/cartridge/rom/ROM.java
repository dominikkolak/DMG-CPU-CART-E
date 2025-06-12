package cartridge.rom;

import cartridge.constants.CartridgeConstants;
import cartridge.interfaces.ReadOnlyMemory;

public class ROM implements ReadOnlyMemory {

    private final byte[] data_;
    private final int total_size_;
    private final int bank_size_;
    private final int bank_count_;

    public ROM(byte[] data) {
        if (data == null || data.length == 0) { throw new IllegalArgumentException("ROM data null or empty"); }

        this.data_ = data;
        this.total_size_ = data.length;
        this.bank_size_ = CartridgeConstants.ROM_BANK_SIZE;
        this.bank_count_ = total_size_ / bank_size_;

        // multiple of bank size
        if (total_size_ % bank_size_ != 0) { throw new IllegalArgumentException( "ROM size must be multiple of " + bank_size_); }

        // min 2 banks = 32KB
        if (total_size_ < CartridgeConstants.MIN_ROM_SIZE) { throw new IllegalArgumentException("Minimum Rom Size: " + CartridgeConstants.MIN_ROM_SIZE); }

    }

    public int read(int address) {
        if (address < 0 || address >= total_size_) {
            return 0xFF; // Open bus (behavior needs to be verified with docs!)
        }
        return Byte.toUnsignedInt(data_[address]);
    }

    public int readBank(int bank, int offset) {
        if (!isValidBank(bank)) {
            return 0xFF;
        }

        if (offset < 0 || offset >= bank_size_) {
            return 0xFF;
        }

        int physical_address = (bank * bank_size_) + offset;
        return Byte.toUnsignedInt(data_[physical_address]);
    }

    public int readByte(int physical_address) {
        return read(physical_address);
    }

    public int translateAddress(int bank, int offset) {
        if (!isValidBank(bank) || offset < 0 || offset >= bank_size_) {
            return -1;
        }
        return (bank * bank_size_) + offset;
    }

    public int getBankStartAddress(int bank) {
        if (!isValidBank(bank)) {
            return -1;
        }
        return bank * bank_size_;
    }

    public boolean isValidBank(int bank) {
        return bank >= 0 && bank < bank_count_;
    }

    public boolean isValidAddress(int address) {
        return address >= 0 && address < total_size_;
    }

    public int getSize() {
        return total_size_;
    }

    public int getBankCount() {
        return bank_count_;
    }

    public int getBankSize() {
        return bank_size_;
    }

    public byte[] getData() {
        return data_;
    }

    public byte[] copyRegion(int start, int length) {
        if (start < 0 || start + length > total_size_) {
            throw new IllegalArgumentException( "Invalid region");
        }

        byte[] region = new byte[length];
        System.arraycopy(data_, start, region, 0, length);
        return region;
    }

    public int getBankForAddress(int physical_address) {
        if (!isValidAddress(physical_address)) {
            return -1;
        }
        return physical_address / bank_size_;
    }

    public int getOffsetInBank(int physical_address) {
        if (!isValidAddress(physical_address)) {
            return -1;
        }
        return physical_address % bank_size_;
    }

    public byte[] readBytes(int start_address, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) read(start_address + i);
        }
        return result;
    }

    // 16 bit
    public int readWord(int address) {
        int low = read(address);
        int high = read(address + 1);
        return (high << 8) | low;
    }

    @Override
    public String toString() {
        return String.format(
                "rom{size=%d bytes, banks=%d, bank_size=%d bytes}",
                total_size_,
                bank_count_,
                bank_size_
        );
    }

}
