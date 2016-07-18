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

import com.esotericsoftware.kryo.Serializer;

/**
 * Another hack for large compressed messages.
 * @author James
 */
public abstract class LargeByteArrayCompressor extends LargeCompressor {
    public LargeByteArrayCompressor (Serializer serializer) {
        super(serializer);
    }

    public LargeByteArrayCompressor (Serializer serializer, int bufferSize) {
        super(serializer, bufferSize);
    }

    public void compress (ByteBuffer inputBuffer, Object object, ByteBuffer outputBuffer) {
//        Context context = Kryo.getContext();
//        int inputLength = inputBuffer.remaining();
//        byte[] inputBytes = context.getBuffer(Math.max(inputLength, bufferSize)).array();
//        inputBuffer.get(inputBytes, 0, inputLength);
//        compress(inputBytes, inputLength, outputBuffer);
    }

    /**
     * Implementations should read the specified number of input bytes and write compressed data to the output buffer.
     * @param outputBuffer A non-direct buffer.
     */
    abstract public void compress (byte[] inputBytes, int inputLength, ByteBuffer outputBuffer);

    public void decompress (ByteBuffer inputBuffer, Class type, ByteBuffer outputBuffer) {
//        Context context = Kryo.getContext();
//        int inputLength = inputBuffer.remaining();
//        byte[] inputBytes = context.getBuffer(Math.max(inputLength, bufferSize)).array();
//        inputBuffer.get(inputBytes, 0, inputLength);
//        decompress(inputBytes, inputLength, outputBuffer);
    }

    /**
     * Implementations should read the specified number of input bytes and write decompressed data to the output bytes.
     * @param outputBuffer A non-direct buffer.
     */
    abstract public void decompress (byte[] inputBytes, int inputLength, ByteBuffer outputBuffer);
}
