/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.util;

/**
 * A simple utility to check operating system type.
 *
 * @author Dennis Reedy
 */
public class OperatingSystemType {
    /**
     * Linux identifier
     */
    public static final String LINUX = "Linux";
    /**
     * Unix identifier
     */
    public static final String UNIX = "Unix";
    /**
     * Mac identifier
     */
    public static final String MACINTOSH = "Mac";
    /**
     * Windows identifier
     */
    public static final String WINDOWS = "Windows";

    private static final String OP_SYS = System.getProperty("os.name").toLowerCase();

    public static String get() {
        String opSysType;
        if (isWindows()) {
            opSysType = WINDOWS;
        } else if (isLinux()) {
            opSysType = LINUX;
        } else if (isUnix()) {
            opSysType = UNIX;
        } else if (isMac()) {
            opSysType = MACINTOSH;
        } else {
            throw new RuntimeException("Unrecognized operating system type: " + System.getProperty("os.name"));
        }
        return opSysType;
    }

    public static boolean isWindows() {
        return OP_SYS.contains("win");
    }

    public static boolean isLinux() {
        return OP_SYS.contains("linux");
    }

    public static boolean isUnix() {
        return OP_SYS.contains("nix") || OP_SYS.contains("nux") || OP_SYS.contains("aix");
    }

    public static boolean isMac() {
        return OP_SYS.contains("mac");
    }

}
