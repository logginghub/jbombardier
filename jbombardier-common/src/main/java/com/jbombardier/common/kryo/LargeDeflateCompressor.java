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

package com.jbombardier.common.kryo;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Output;

// BOZO - Process in batches to avoid a large buffer size.

/**
 * Last class in the large compression hack.
 * @author James
 *
 */
public class LargeDeflateCompressor extends LargeByteArrayCompressor {
    private Deflater deflater;
    private Inflater inflater;

    public LargeDeflateCompressor (Serializer serializer) {
        this(serializer, 2048);
    }

    public LargeDeflateCompressor (Serializer serializer, int bufferSize) {
        super(serializer, bufferSize);
        this.deflater = new Deflater();
        this.inflater = new Inflater();
    }

    public void compress (byte[] inputBytes, int inputLength, ByteBuffer outputBuffer) {
        deflater.reset();
        deflater.setInput(inputBytes, 0, inputLength);
        deflater.finish();
        outputBuffer.position(deflater.deflate(outputBuffer.array()));
    }

    public void decompress (byte[] inputBytes, int inputLength, ByteBuffer outputBuffer) {
        inflater.reset();
        inflater.setInput(inputBytes, 0, inputLength);
        try {
            outputBuffer.position(inflater.inflate(outputBuffer.array()));
        } catch (DataFormatException ex) {
//            throw new SerializationException("Error inflating data.", ex);
        }
    }

    @Override public void write(Kryo arg0, Output arg1, Object arg2) {}
}
