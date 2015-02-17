package com.jbombardier.sample.virtualsystem;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;
import com.logginghub.logging.repository.SofString;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamReaderAbstraction;
import com.logginghub.utils.sof.StreamWriterAbstraction;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Created by James on 17/02/15.
 */
public class VirtualSystemClientTest extends PerformanceTestAdaptor {

    private String input;
    private StreamWriterAbstraction writer;
    private StreamReaderAbstraction reader;
    private SofConfiguration configuration;
    private long bytesRead = 0;

    @Override
    public void setup(TestContext pti) throws Exception {

        configuration = new SofConfiguration();
        configuration.registerType(SofString.class, 1);

        final Random random = new Random();
        int portStart = 16000;
        int portRange = 10;

        boolean connected = false;

        InputStream inputStream = null;
        OutputStream outputStream = null;

        Socket socket = null;
        while (!connected) {

            int portToTry = portStart + random.nextInt(portRange);

            try {
                socket = new Socket("localhost", portToTry);

                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                writer = new StreamWriterAbstraction(new BufferedOutputStream(outputStream));
                reader = new StreamReaderAbstraction(new BufferedInputStream(inputStream), Long.MAX_VALUE);

                connected = true;
            } catch (IOException e) {
            }
        }

    }

    @Override
    public void beforeIteration(TestContext pti) throws Exception {
        input = "This is a random string : " + StringUtils.randomString(100);
    }

    @Override
    public void runIteration(TestContext pti) throws Exception {
        SofSerialiser.write(writer, new SofString(input), configuration);
        writer.flush();
        SofString read = SofSerialiser.read(reader, configuration);
        bytesRead += read.getValue().length();
    }
}
