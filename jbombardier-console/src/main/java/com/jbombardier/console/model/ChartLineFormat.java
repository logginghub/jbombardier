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

package com.jbombardier.console.model;

import java.awt.Paint;
import java.awt.Stroke;

/**
 * Class to encapsulate the Paint and Stroke settings for a line on the chart.
 * We need this to spread the knowledge about each series paint between
 * different charts and controls to ensure everything has the same formatting
 * view.
 * 
 * @author James
 */
public class ChartLineFormat {
    private Paint paint;
    private Stroke stroke;
    
    public ChartLineFormat(Paint paint, Stroke stroke) {
        super();
        this.paint = paint;
        this.stroke = stroke;
    }
    
    public Paint getPaint() {
        return paint;
    }
    
    public Stroke getStroke() {
        return stroke;
    }
}
