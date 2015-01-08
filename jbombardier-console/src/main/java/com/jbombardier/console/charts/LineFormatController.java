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

package com.jbombardier.console.charts;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.plot.DefaultDrawingSupplier;

/**
 * Controls the line format used for chart series to ensure the same formats can
 * be used for equivalent series in different charts.
 * 
 * @author James
 * 
 */
public class LineFormatController {
    
    private Map<String, Paint> seriesPaints = new HashMap<String, Paint>();
    private Map<String, Stroke> seriesStrokes = new HashMap<String, Stroke>();
    private DefaultDrawingSupplier defaultDrawingSupplier = new DefaultDrawingSupplier();
    
    public synchronized Paint allocateColour(String seriesName){
        Paint paint = seriesPaints.get(seriesName);
        if(paint == null){
            paint = defaultDrawingSupplier.getNextPaint();
            seriesPaints.put(seriesName, paint);
        }
        
        return paint;
    }
    
   
    public void setPaint(String seriesName, Paint paint) {
        seriesPaints.put(seriesName, paint);
    }

    public void setStroke(String seriesName, BasicStroke stroke) {
        seriesStrokes.put(seriesName, stroke);
    }

    public Stroke getStroke(String label) {
        Stroke stroke = seriesStrokes.get(label);
        if(stroke == null){
            stroke = new BasicStroke(1);
            seriesStrokes.put(label, stroke);
        }
        return stroke;
         
    }
    
}
