package cartridge.util;

import cartridge.constants.CartridgeConstants;
import cartridge.exceptions.InvalidCartridgeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RomLoader {

    public static byte[] loadRom(String file_path) throws IOException {
        if (file_path == null || file_path.isEmpty()) { throw new IllegalArgumentException("file_path Empty"); }

        Path path = Paths.get(file_path);
        return loadRom(path);
    }

    public static byte[] loadRom(Path file_path) throws IOException {
        if (file_path == null) { throw new IllegalArgumentException("file_path null"); }
        if (!Files.exists(file_path)) { throw new IllegalArgumentException("ROM not found"); }
        if (!Files.isRegularFile(file_path)) { throw new IOException("Path is not File"); }
        if (!Files.isReadable(file_path)) { throw new IOException("ROM is not readable"); }

        byte[] rom_data = Files.readAllBytes(file_path);
        
        if (rom_data.length < CartridgeConstants.MIN_ROM_SIZE) { throw new InvalidCartridgeException("ROM file too small: " + rom_data.length + " < " + CartridgeConstants.MIN_ROM_SIZE); }

        if (rom_data.length < CartridgeConstants.HEADER_END + 1) { throw new InvalidCartridgeException("ROM file does not contain space for header"); }
        
        return rom_data;
    }
    
    public record romInfo(byte[] data, long file_size, String file_name, String absolute_path) {
        @Override
        public String toString() {
            return String.format("rom_info{filename='%s', size=%d bytes, path='%s'}", file_name, file_size, absolute_path);
        }
    }
    
    public static romInfo loadRomWithInfo(String file_path) throws IOException {
        Path path = Paths.get(file_path);
        return loadRomWithInfo(path);
    }

    public static romInfo loadRomWithInfo(Path file_path) throws IOException {
        byte[] rom_data = loadRom(file_path);
        long file_size = Files.size(file_path);
        String filename = file_path.getFileName().toString();
        String absolute_path = file_path.toAbsolutePath().toString();

        return new romInfo(rom_data, file_size, filename, absolute_path);
    }

    public static boolean isValidRomFile(String file_path) {
        try {
            Path path = Paths.get(file_path);
            return isValidRomFile(path);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidRomFile(Path filepath) {
        try {
            if (!Files.exists(filepath) || !Files.isRegularFile(filepath)) {
                return false;
            }

            long size = Files.size(filepath);
            if (size < CartridgeConstants.MIN_ROM_SIZE) {
                return false;
            }

            String filename = filepath.getFileName().toString().toLowerCase();
            return filename.endsWith(".gb") || filename.endsWith(".gbc");

        } catch (IOException e) {
            return false;
        }
    }

    public static String getFileExtension(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            return "";
        }

        int last_dot = filepath.lastIndexOf('.');
        if (last_dot == -1 || last_dot == filepath.length() - 1) {
            return "";
        }

        return filepath.substring(last_dot + 1).toLowerCase();
    }

    private RomLoader() {
        throw new AssertionError("No instantiation for Utility classes");
    }
    
}
