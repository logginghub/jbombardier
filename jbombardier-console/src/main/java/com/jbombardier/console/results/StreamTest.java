/*
 * Copyright (c) 2009-2015 Vertex Labs Limited.
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

package com.jbombardier.console.results;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class StreamTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        Thread t1 = new Thread() {
            @Override public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(9111);
                    Socket socket = new Socket("localhost", 9111);

                    System.out.println("Start Socket");
                    byte[] buffer = new byte[4096];
                    socket.getInputStream().read(buffer);
                    System.out.println("End Socket");
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        Thread t2 = new Thread() {
            @Override public void run() {
                try {
                    PipedOutputStream pop = new PipedOutputStream();
                    PipedInputStream pip = new PipedInputStream(pop);

                    System.out.println("Start Pipe");
                    byte[] buffer = new byte[4096];
                    pip.read(buffer);
                    System.out.println("End Pipe");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        t1.start();
        t2.start();
        t2.join();
    }
}
