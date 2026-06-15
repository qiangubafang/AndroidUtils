/*
 * Copyright 2014-2026 Sayi
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
package org.tcshare.poi.util;

import java.util.Collections;
import java.util.List;

import org.apache.poi.util.Units;

/**
 * @author Sayi
 */
public final class UnitUtils {

    /**
     * cm to twips
     * 
     * @param cm
     * @return in twentieths of a point (1/1440 of an inch)
     */
    public static int cm2Twips(double cm) {
        return (int) (cm / 2.54 * 1440);
    }

    /**
     * point to twips
     * 
     * @param pt
     * @return in twentieths of a point (1/1440 of an inch)
     */
    public static int point2Twips(double pt) {
        return (int) (pt * 20);
    }

    /**
     * twips to point
     * 
     * @param twips
     * @return
     */
    public static double twips2Point(int twips) {
        return (twips / 20.0);
    }

    /**
     * cm to pixel
     * 
     * @param cm
     * @return pixel
     */
    public static int cm2Pixel(double cm) {
        return UnitUtils.pointsToPixel(cm / 2.54 * 1440 / 20.0);
    }
    public static int toEMU(double points) {
        return (int)Math.rint((double)12700.0F * points);
    }

    public static int pixelToEMU(int pixels) {
        return pixels * 9525;
    }

    public static double toPoints(long emu) {
        return emu == -1L ? (double)-1.0F : (double)emu / (double)12700.0F;
    }

    public static double fixedPointToDouble(int fixedPoint) {
        int i = fixedPoint >> 16;
        int f = fixedPoint & '\uffff';
        return (double)i + (double)f / (double)65536.0F;
    }

    public static int doubleToFixedPoint(double floatPoint) {
        double fractionalPart = floatPoint % (double)1.0F;
        double integralPart = floatPoint - fractionalPart;
        int i = (int)Math.floor(integralPart);
        int f = (int)Math.rint(fractionalPart * (double)65536.0F);
        return i << 16 | f & '\uffff';
    }

    public static double masterToPoints(int masterDPI) {
        double points = (double)masterDPI;
        points *= (double)72.0F;
        points /= (double)576.0F;
        return points;
    }

    public static int pointsToMaster(double points) {
        points *= (double)576.0F;
        points /= (double)72.0F;
        return (int)Math.rint(points);
    }

    public static int pointsToPixel(double points) {
        points *= (double)96.0F;
        points /= (double)72.0F;
        return (int)Math.rint(points);
    }

    public static double pixelToPoints(double pixel) {
        double points = pixel * (double)72.0F;
        points /= (double)96.0F;
        return points;
    }
    /**
     * twips to pixel
     * 
     * @param twips
     * @return pixel
     */
    public static int twips2Pixel(int twips) {
        return UnitUtils.pointsToPixel(twips / 20);
    }

    /**
     * average the width
     * 
     * @param width
     * @param col
     * @return
     */
    public static int[] average(int width, int col) {
        int colVal = (Integer.valueOf(width)) / col;
        List<Integer> nCopies = Collections.nCopies(col, colVal);
        return nCopies.stream().mapToInt(i -> i).toArray();
    }

}
