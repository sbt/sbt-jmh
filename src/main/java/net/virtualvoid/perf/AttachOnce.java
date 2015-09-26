/*
 *   libperfmap: a JVM agent to create perf-<pid>.map files for consumption
 *               with linux perf-tools
 *   Copyright (C) 2013 Johannes Rudolph<johannes.rudolph@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along
 *   with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package net.virtualvoid.perf;

import java.io.File;

import com.sun.tools.attach.VirtualMachine;
import java.lang.management.ManagementFactory;
import java.util.Locale;

/**
 * Based on Agent loading code by Johannes Rudolph:
 * https://github.com/jrudolph/perf-map-agent
 *
 * Licensed under GPLv2
 */
public class AttachOnce {
    public static void main(String[] args) throws Exception {
        final String pid = args[0];

        String options = "";
        if (args.length > 1) options = args[1];

        loadAgent(pid, options);
    }

    static void loadAgent(String pid, String options) throws Exception {
        final VirtualMachine vm = VirtualMachine.attach(pid);
        try {
            final File lib = findSoFile();
            final String fullPath = lib.getAbsolutePath();
            if (!lib.exists()) {
                System.out.printf("Expected %s at '%s' but it didn't exist.\n", lib, fullPath);
                System.exit(1);
            }
            else vm.loadAgentPath(fullPath, options);
        } catch(com.sun.tools.attach.AgentInitializationException e) {
            // rethrow all but the expected exception
            if (!e.getMessage().equals("Agent_OnAttach failed")) throw e;
        } finally {
            vm.detach();
        }
    }

    private static File findSoFile() {
        final String os = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            if (os.contains("64")) return new File("libperfmap-64bit.so");
            else throw new RuntimeException("32bit systems are not supported!");
        } else {
            throw new RuntimeException(String.format("Running on not supported OS! Only Linux supported, was: [%s]", os));
        }
    }
}
