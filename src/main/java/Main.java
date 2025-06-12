import cartridge.Cartridge;
import cartridge.util.RomLoader;

public class Main {
    public static void main(String[] args) {

        String rom_path = "src/main/java/data/rom_test/cpu_instrs/cpu_instrs.gb";

        try {


            byte[] rom_data = RomLoader.loadRom(rom_path);
            Cartridge cart = new Cartridge(rom_data);

            System.out.println(cart);
            System.out.println(cart.getTitle());
            System.out.println(cart.getMbcType());

            System.out.println("-----------------------------");

            System.out.println("0x0100: " + cart.accepts(0x0100));
            System.out.println("0xA000: " + cart.accepts(0xA000));
            System.out.println("0x8000: " + cart.accepts(0x8000));

            System.out.println("-----------------------------");

            System.out.printf("0x0100: 0x%02X\n", cart.read(0x0100));
            System.out.printf("0x0150: 0x%02X\n", cart.read(0x0150));
            System.out.printf("0x4000: 0x%02X\n", cart.read(0x4000));

            System.out.println("-----------------------------");

            System.out.printf("0x4000 = 0x%02X\n", cart.read(0x4000)); // bank 1
            cart.write(0x2000, 0x02); // switch
            System.out.printf("0x4000 = 0x%02X\n", cart.read(0x4000)); // bank 2
            cart.write(0x2000, 0x03); // switch
            System.out.printf("0x4000 = 0x%02X\n", cart.read(0x4000)); // bank 3

            System.out.println("-----------------------------");

            System.out.println(cart.getComponentName());
            cart.tick(1);
            cart.reset();
            System.out.println("TICK / RESET");

            System.out.println("-----------------------------");

            System.out.println("TEST COMPLETE");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
