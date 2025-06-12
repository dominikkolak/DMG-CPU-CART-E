package cartridge.interfaces;

public interface ReadOnlyMemory {
    int read(int address);
    int readBank(int bank, int offset);
    int readByte(int physical_address);
    int translateAddress(int bank, int offset);
    int getBankStartAddress(int bank);
    boolean isValidBank(int bank);
    boolean isValidAddress(int address);
    int getSize();
    int getBankCount();
    int getBankSize();
    byte[] getData();
    byte[] copyRegion(int start, int length);
    int getBankForAddress(int physical_address);
    int getOffsetInBank(int physical_address);
    byte[] readBytes(int start_address, int length);
    int readWord(int address);
}
