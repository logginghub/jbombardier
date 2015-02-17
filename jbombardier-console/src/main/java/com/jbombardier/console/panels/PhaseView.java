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

package com.jbombardier.console.panels;

import com.jbombardier.JBombardierController;
import com.jbombardier.JBombardierModel;
import com.jbombardier.console.model.PhaseModel;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Displays information about the current phase, and provides access to the controller methods to change the current
 * phase
 *
 * @author James
 */
public class PhaseView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel currentPhaseLabel;

    private JBombardierModel model;
    private JBombardierController controller;
    private final JButton previousPhaseButton;
    private final JButton nextPhaseButton;
    private final JLabel phaseDuration;
    private final JLabel phaseTimeRemaining;

    /**
     * Create the panel.
     */
    public PhaseView() {
        setLayout(new MigLayout("ins 0, gap 0", "[grow]", "[grow]"));

        currentPhaseLabel = new JLabel("Test not started");
        previousPhaseButton = new JButton("Previous");
        nextPhaseButton = new JButton("Next");
        phaseDuration = new JLabel("--:--");
        phaseTimeRemaining = new JLabel("--:--");

        add(new JLabel("Current Phase: "));
        add(currentPhaseLabel);
        add(new JLabel("Phase duration: "));
        add(phaseDuration, "wrap");
        add(previousPhaseButton);
        add(nextPhaseButton);
        add(new JLabel("Phase remaining: "));
        add(phaseTimeRemaining);

        previousPhaseButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                previousPhase();
            }
        });

        nextPhaseButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                nextPhase();
            }
        });

    }

    private void nextPhase() {
        if (controller != null) {
            controller.forceNextPhase();
        }
    }

    private void previousPhase() {
        if (controller != null) {
            controller.forcePreviousPhase();
        }
    }

    public void bind(final JBombardierController controller) {
        this.controller = controller;
        this.model = controller.getModel();

        nextPhaseButton.setEnabled(false);
        previousPhaseButton.setEnabled(false);

        final ObservablePropertyListener timeRemainingListener = new ObservablePropertyListener<Long>() {
            @Override public void onPropertyChanged(Long aLong, final Long t1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        phaseTimeRemaining.setText(TimeUtils.formatJustTime(t1));
                    }
                });
            }
        };

        final ObservablePropertyListener durationListener = new ObservablePropertyListener<Long>() {
            @Override public void onPropertyChanged(Long aLong, final Long t1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        phaseDuration.setText(TimeUtils.formatJustTime(t1));
                    }
                });
            }
        };


        model.getCurrentPhase().addListenerAndNotifyCurrent(new ObservablePropertyListener<PhaseModel>() {
            @Override public void onPropertyChanged(final PhaseModel previous, final PhaseModel newPhase) {

                if (previous != null) {
                    previous.getPhaseDuration().removeListener(durationListener);
                    previous.getPhaseRemainingTime().removeListener(timeRemainingListener);
                }

                if (newPhase != null) {
                    newPhase.getPhaseDuration().addListenerAndNotifyCurrent(durationListener);
                    newPhase.getPhaseRemainingTime().addListenerAndNotifyCurrent(timeRemainingListener);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            currentPhaseLabel.setText(controller.getCurrentPhaseName());
                            nextPhaseButton.setEnabled(controller.hasNextPhase());
                            previousPhaseButton.setEnabled(controller.hasPreviousPhase());

                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            currentPhaseLabel.setText("Not running");
                            nextPhaseButton.setEnabled(false);
                            previousPhaseButton.setEnabled(false);
                            phaseDuration.setText("--:--");
                            phaseTimeRemaining.setText("--:--");
                        }
                    });
                }
            }
        });


    }

}
