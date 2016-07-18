package com.jbombardier.console;

import com.jbombardier.JBombardierController;

/**
 * Created by james on 13/03/15.
 */
public interface PhaseController {
    void start(JBombardierController controller);
    void stop();

    void configure(String configuration);
}
