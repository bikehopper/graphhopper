/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util.shapes;

import org.locationtech.jts.geom.Point;

import java.util.Locale;

/**
 * @author Peter Karich
 */
public class GHPoint {
    public double lat = Double.NaN;
    public double lon = Double.NaN;

    public GHPoint() {
    }

    public GHPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public static GHPoint create(Point point) {
        return new GHPoint(point.getY(), point.getX());
    }

    public static GHPoint fromString(String str) {
        return fromString(str, false);
    }

    public static GHPoint fromStringLonLat(String str) {
        return fromString(str, true);
    }

    public static GHPoint fromJson(double[] xy) {
        return new GHPoint(xy[1], xy[0]);
    }

    private static GHPoint fromString(String str, boolean lonLatOrder) {
        String[] fromStrs = str.split(",");
        if (fromStrs.length != 2)
            throw new IllegalArgumentException("Cannot parse point '" + str + "'");

        try {
            double fromLat = Double.parseDouble(fromStrs[0]);
            double fromLon = Double.parseDouble(fromStrs[1]);
            if (lonLatOrder) {
                return new GHPoint(fromLon, fromLat);
            } else {
                return new GHPoint(fromLat, fromLon);
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Cannot parse point '" + str + "'");
        }
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public boolean isValid() {
        return !Double.isNaN(lat) && !Double.isNaN(lon);
    }

    @Override
    public String toString() {
        return lat + "," + lon;
    }

    public String toShortString() {
        return String.format(Locale.ROOT, "%.8f,%.8f", lat, lon);
    }

    /**
     * Attention: geoJson is LON,LAT
     */
    public Double[] toGeoJson() {
        return new Double[]{lon, lat};
    }
}
