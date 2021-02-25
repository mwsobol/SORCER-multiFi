package sorcer.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperatingSystemTypeTest {

    @Test
    public void verifyOpSys() {
        String type = OperatingSystemType.get();
        if (OperatingSystemType.isMac()) {
            assertEquals(OperatingSystemType.MACINTOSH, type);
        }
        if (OperatingSystemType.isLinux()) {
            assertEquals(OperatingSystemType.LINUX, type);
        }
        if (OperatingSystemType.isUnix()) {
            assertEquals(OperatingSystemType.UNIX, type);
        }
        if (OperatingSystemType.isWindows()) {
            assertEquals(OperatingSystemType.WINDOWS, type);
        }
    }

}