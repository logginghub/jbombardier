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

import java.text.NumberFormat;

import com.logginghub.utils.TimeUtils;

public class VelocityUtils {
    
    private NumberFormat numberFormat = NumberFormat.getInstance();
    
    public String format(double number) {
        return numberFormat.format(number);
    }

    private String format(int decimalPlaces, double number) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(decimalPlaces);
        numberFormat.setMinimumFractionDigits(decimalPlaces);
        return numberFormat.format(number);
    }
    
    public String formatWithSign(double number) {
        String formatted = format(number);
        if(number > 0) {
            formatted = "+" + formatted;
        }
        return formatted;
    }

    
    public String formatDelta(double number) {        
        String formatted = format(number);
        if(number > 0) {
            formatted = "+" + formatted;
        }
        else if(formatted.equals("0")) {
            formatted = "";
        }
        
        return formatted;
    }
    
    public String formatDelta(int decimalPlaces, double number) {        
        String formatted = format(decimalPlaces, number);
        if(number > 0) {
            formatted = "+" + formatted;
        }
        else if(formatted.equals("0")) {
            formatted = "";
        }
        
        return formatted;
    }
    
    
    public double div(double a, double b){
        return a/b;
    }
    
    public int integer(double value) {
        return (int)value;
    }

    public String toDateTimeString(long value) {
        return TimeUtils.toDateTimeString(value);
    }
    
    public String htmlify(String string) {
        return string.replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
    }

}
