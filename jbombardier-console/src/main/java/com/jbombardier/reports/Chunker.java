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

package com.jbombardier.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chunker
{

    class ChunkMap extends HashMap<Long, Chunk>
    {};

    private Map<String, ChunkMap> chunks = new HashMap<String, ChunkMap>();
    private long chunkerInterval = 1000;

    public void onNewResult(String key, long time, double value)
    {
        long chunkedTime = time - (time % chunkerInterval);

        ChunkMap chunkMap = chunks.get(key);
        if (chunkMap == null)
        {
            chunkMap = new ChunkMap();
            chunks.put(key, chunkMap);
        }

        Chunk chunk = chunkMap.get(chunkedTime);
        if (chunk == null)
        {
            chunk = new Chunk();
            chunk.setChunkStart(chunkedTime);
            chunk.setResultID(key);
            chunkMap.put(chunkedTime, chunk);
        }

        chunk.getStatistics().addValue(value);
    }

    public List<Chunk> getTimeOrderedResults(String transactionName)
    {
        List<Chunk> orderedChunks = new ArrayList<Chunk>();
        ChunkMap chunkMap = chunks.get(transactionName);
        if (chunkMap != null)
        {
            orderedChunks.addAll(chunkMap.values());
        }

        sort(orderedChunks);
        
        return orderedChunks;
    }

    private void sort(List<Chunk> orderedChunks)
    {
        Collections.sort(orderedChunks, new Comparator<Chunk>()
        {
            public int compare(Chunk o1, Chunk o2)
            {
                return Long.valueOf(o1.getChunkStart()).compareTo(Long.valueOf(o2.getChunkStart()));
            }
        });
    }

}
