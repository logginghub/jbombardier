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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.jbombardier.common.AgentClassRequest;
import com.jbombardier.common.AgentClassResponse;
import com.logginghub.messaging2.api.MessagingInterface;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.logging.Logger;

/**
 * Provides standard remote class loading features across the Messaging2 framework.
 * 
 * @author James
 * 
 */
public class Messaging2ClassLoader extends ClassLoader {
    private static final Logger logger = Logger.getLoggerFor(Messaging2ClassLoader.class);

    private final MessagingInterface client;
    private final String serverDestinationID;
    private Map<String, Class> classes = new HashMap<String, Class>();
    private ClassCache cache = new ClassCache(new File("./classcache/"));
    private List<String> dontCachePackages = new ArrayList<String>();
    private Map<String, byte[]> resourceCache = new HashMap<String, byte[]>();

    private int classRequestTimeout = Integer.getInteger("agent.classRequestTimeoutSeconds", 60);
    private final static byte[] doesntExist = new byte[] {};

    // TODO : need to factor the request/response code so it is shared between
    // client and hub!
    public Messaging2ClassLoader(MessagingInterface client, String serverDestinationID, ClassLoader classLoader) {
        super(classLoader);
        this.client = client;
        this.serverDestinationID = serverDestinationID;
        dontCachePackages.add("com.logginghub");
    }

    @Override public synchronized InputStream getResourceAsStream(String name) {
        logger.trace("LRCL looking for resource (as stream) : {}", name);
        InputStream stream = super.getResourceAsStream(name);

        if (stream == null) {
            logger.trace("Parent stream was null, trying to load the resource remotely");

            byte[] cachedResourceData = resourceCache.get(name);
            if (cachedResourceData == null) {
                try {
                    byte[] loadClassData = loadClassData(name, false);
                    if (loadClassData.length > 0) {
                        stream = new ByteArrayInputStream(loadClassData);
                        resourceCache.put(name, loadClassData);
                    }
                    else {
                        logger.trace("Remote stream was empty, resource '{}' does not exist on the server ", name);
                        resourceCache.put(name, doesntExist);
                        stream = null;
                    }
                }
                catch (IOException e) {
                    logger.warn(e,"Failed to load resource [{}]", name);
                    stream = null;
                }
                catch (ClassNotFoundException e) {
                    logger.warn(e,"Failed to load resource [{}]", name);
                    stream = null;
                }
            }
            else {
                if (cachedResourceData == doesntExist) {
                    stream = null;
                }
                else {
                    stream = new ByteArrayInputStream(cachedResourceData);
                }
            }
        }

        return stream;
    }

    public synchronized Class loadClass(String className) throws ClassNotFoundException {
        return findClass(className);
    }

    @Override synchronized public Class findClass(String className) throws ClassNotFoundException {
        logger.trace("Loading class [{}]", className);

        byte classBytes[];
        Class result = null;
        result = (Class) classes.get(className);
        if (result != null) {
            if(result == DoesntExistOnServer.class) {
                // This is our marker class to throw a class not found
                throw new ClassNotFoundException(String.format("Class '%s' wasn't found - we found a marker in the cache saying this class wasn't available on the server last time we checked",
                                                               className));
            }
            
            logger.trace("Loading class [{}] from the cache", className);
            return result;
        }

        try {
            logger.trace("Attempting to load system class");
            Class<?> findSystemClass = findSystemClass(className);
            logger.trace("Loading class [{}] from the system class loader", className);
            classes.put(className, findSystemClass);
            return findSystemClass;
        }
        catch (Exception e) {
            logger.trace("System class failed, it must be a normal class");
        }

        try {
            if (isCacheable(className)) {
                classBytes = cache.load(className);
                if (classBytes != null) {
                    logger.trace("Loading class [{}] from the disk cache", className);
                    result = defineClass(className, classBytes, 0, classBytes.length, null);
                    classes.put(className, result);
                    return result;
                }
            }
        }
        catch (IOException e) {
            logger.debug("Failed to load class [{}] from the disk cache, will try and find it remotely", className, e);
        }

        try {
            classBytes = loadClassData(className, true);
            if (classBytes.length == 0) {
                // Cache the fact the server couldn't find it, sometimes this might be normal
                // operation and we dont want to slow the world down with a server round trip each
                // time
                classes.put(className, DoesntExistOnServer.class);
                throw new ClassNotFoundException(String.format("Class '%s' wasn't found - the remote class provider returned a zero byte data array, which means it couldn't be found on the server. Please check to see if you have specified an incorrect classname somewhere?",
                                                               className));
            }

            result = defineClass(className, classBytes, 0, classBytes.length, null);

            classes.put(className, result);
            if (isCacheable(className)) {
                cache.cache(className, classBytes);
            }
            return result;
        }
        catch (IOException e) {
            logger.warn(e, "Failed to load class [{}]", className);
            return null;
        }
    }

    private boolean isCacheable(String className) {
        return false;
        /*
         * boolean isCacheable = true;
         * 
         * List<String> dontCachePackages = m_dontCachePackages; for (String string :
         * dontCachePackages) { if (className.startsWith(string)) { isCacheable = false; break; } }
         * return isCacheable;
         */
    }

    private synchronized byte[] loadClassData(String className, boolean classNotResource) throws IOException, ClassNotFoundException {
        logger.trace("Attempting remote load of class {} via kryo messaging", className);

        final Bucket<AgentClassResponse> responseBucket = new Bucket<AgentClassResponse>();
        Listener listener = new Listener() {
            @Override public void received(Connection connection, Object object) {
                super.received(connection, object);
                if (object instanceof AgentClassResponse) {
                    responseBucket.add((AgentClassResponse) object);
                }
            }
        };

        AgentClassRequest acr = new AgentClassRequest();
        acr.setClassName(className);
        acr.setClassNotResource(classNotResource);

        AgentClassResponse response = client.sendRequest(serverDestinationID, acr, classRequestTimeout, TimeUnit.SECONDS);
        byte[] data = response.getData();

        logger.debug("Reposonse received, class '{}' data was {} bytes", className, data.length);
        return data;
    }

    public synchronized void clearClassCache() {
        classes.clear();
    }

}
