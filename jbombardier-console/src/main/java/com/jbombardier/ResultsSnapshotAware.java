package com.jbombardier;

import java.util.List;

/**
 * Created by james on 17/03/15.
 */
public interface ResultsSnapshotAware {
    List<ControllerResultSnapshot> getResultsVsBaselineEfficiency();
}
