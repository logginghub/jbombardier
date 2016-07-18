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

package com.jbombardier.console;

import com.jbombardier.JBombardierModel;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.model.PhaseModel;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservablePropertyListener;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofStreamSerialiser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by james on 06/02/15.
 */
public class CapturedStatisticsHelper {

    private static final Logger logger = Logger.getLoggerFor(CapturedStatisticsHelper.class);
    private File resultsFolder;

    private BufferedOutputStream statisticsStream;

    private SofConfiguration sofConfig;

    public CapturedStatisticsHelper(JBombardierModel model) {
        sofConfig = new SofConfiguration();
        sofConfig.registerType(CapturedStatistic.class, 0);

        model.getCurrentPhase().addListenerAndNotifyCurrent(new ObservablePropertyListener<PhaseModel>() {
            @Override public void onPropertyChanged(PhaseModel phaseModel, PhaseModel t1) {
                if(t1 != null) {
                    try {
                        closeStreamingFiles();
                        openStreamingFilesForPhase(t1.getPhaseName().get());
                    } catch (IOException e) {
                        logger.warn(e, "Failed to open file for captured statistics");
                    }
                }
            }
        });
    }

    public void addCapturedStatistic(CapturedStatistic statistic) {
        logger.info("Captured stat : {}", statistic);
        try {
            SofStreamSerialiser.write(statisticsStream, statistic, sofConfig);
        } catch (SofException e) {
            logger.warn(e, "Failed to serialise captured statistic '{}'", statistic);
        }
    }

    public void openStreamingFilesForPhase(String phaseName) throws IOException {
        File file = getStreamingFile(phaseName);
        logger.info("Opening streaming file for captured statistics '{}'", file.getAbsolutePath());
        statisticsStream = new BufferedOutputStream(new FileOutputStream(file));
    }

    private File getStreamingFile(String phaseName) {
        return new File(resultsFolder, "statistics." + phaseName + ".sof");
    }

    public void closeStreamingFiles() {
        if (statisticsStream != null) {
            FileUtils.closeQuietly(statisticsStream);
            logger.info("Closed streaming file for captured statistics");
            statisticsStream = null;
        }
    }

    public void visitStreamingFile(String phaseName, final Destination<CapturedStatistic> destination) {
        File streamingFile = getStreamingFile(phaseName);
        if (streamingFile.exists()) {
            BufferedInputStream bis = null;

            try {
                bis = new BufferedInputStream(new FileInputStream(streamingFile));
                SofStreamSerialiser.visit(bis, streamingFile.length(), sofConfig, new Destination<SerialisableObject>() {
                    @Override public void send(SerialisableObject serialisableObject) {
                        destination.send((CapturedStatistic) serialisableObject);
                    }
                });
            } catch (IOException e) {
                logger.warn(e, "Failed to read serialised captured statistics file");
            } catch (SofException e) {
                logger.warn(e, "Failed to read serialised captured statistics file");
            } finally {
                FileUtils.closeQuietly(bis);
            }
        }
    }

    public void flush() {
        if (statisticsStream != null) {
            try {
                statisticsStream.flush();
            } catch (IOException e) {
            }
        }

    }

}
