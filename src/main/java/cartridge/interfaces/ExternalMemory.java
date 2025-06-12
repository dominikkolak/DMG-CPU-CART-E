package cartridge.interfaces;

public interface ExternalMemory {
    int read(int address);

    void write(int address, int value);

    void selectBank(int bank);

    void setEnabled(boolean enabled);

    boolean isEnabled();

    int getCurrentBank();

    int getBankCount();

    int getSize();

    byte[] getData();

    void loadData(byte[] data);

    void reset();

    void clear();
}
