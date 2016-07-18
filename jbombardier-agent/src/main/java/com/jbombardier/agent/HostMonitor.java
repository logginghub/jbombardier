/*
 * Copyright (c) 2009-2016 Vertex Labs Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jbombardier.agent;

import java.io.IOException;

import com.logginghub.utils.InputStreamReaderThreadListener;
import com.logginghub.utils.ProcessWrapper;

public class HostMonitor {

    public static void main(String[] args) throws IOException {
        new HostMonitor().getMachineCPU();
    }

    public void getMachineCPU() throws IOException {

        if (isWindows()) {
            System.out.println("This is Windows");

            InputStreamReaderThreadListener outputHandler = new InputStreamReaderThreadListener() {
                public void onLine(String line) {
                    String[] split = line.split(",");
                    String timeString = split[0];
                    String valueString = split[1];
                    
                    if(timeString.contains("PDH-CSV")){
                        // Ignore this
                    }else{
                    
                    timeString = timeString.substring(1, timeString.length() - 1);
                    valueString = valueString.substring(1, valueString.length() -1);
                    
                    double value = Double.parseDouble(valueString);
                    System.out.println(value);
                    }
                }

                public void onCharacter(char c) {}
            };

            InputStreamReaderThreadListener errorHandler = new InputStreamReaderThreadListener() {
                public void onLine(String line) {}

                public void onCharacter(char c) {}
            };

            ProcessWrapper execute = ProcessWrapper.execute(outputHandler,
                                                            errorHandler,
                                                            "typeperf", "\"\\Processor(_Total)\\% Processor Time\"");
            execute.waitFor();

        }
        else if (isMac()) {
            System.out.println("This is Mac");
        }
        else if (isUnix()) {
            System.out.println("This is Unix or Linux");
        }
        else if (isSolaris()) {
            System.out.println("This is Solaris");
        }
        else {
            System.out.println("Your OS is not support!!");
        }

    }

    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        String os = System.getProperty("os.name").toLowerCase();
        // linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

    }

    public static boolean isSolaris() {

        String os = System.getProperty("os.name").toLowerCase();
        // Solaris
        return (os.indexOf("sunos") >= 0);

    }

}
