package cartridge.interfaces;

import shared.Component;

public interface MemoryBankController extends Component{

    int readRom(int address);

    int readRam(int address);

    void writeRom(int address, int value);

    void writeRam(int address, int value);

    int getCurrentRomBank();

    int getCurrentRamBank();

    boolean isRamEnabled();

}
