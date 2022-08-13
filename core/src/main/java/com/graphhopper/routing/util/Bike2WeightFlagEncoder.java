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
package com.graphhopper.routing.util;

import static com.graphhopper.routing.util.EncodingManager.getKey;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.*;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;

import static com.graphhopper.util.Helper.keepIn;

import java.util.*;

/**
 * Stores two speed values into an edge to support avoiding too much incline
 *
 * @author Peter Karich
 */
public class Bike2WeightFlagEncoder extends BikeFlagEncoder {
    protected IntEncodedValue netElevationGainEncoder;
    protected IntEncodedValue avgGradeEncoder;


    public Bike2WeightFlagEncoder() {
        this(new PMap());
        this.netElevationGainEncoder = new IntEncodedValueImpl(getKey(this, "ele_gain"), 16, 0, false ,true);
        this.avgGradeEncoder = new IntEncodedValueImpl(getKey(this, "avg_grade"), 16, -200, false, true);
    }

    public Bike2WeightFlagEncoder(PMap properties) {
        super(new PMap(properties).putObject("speed_two_directions", true).putObject("name", properties.getString("name", "bike2")));
        this.netElevationGainEncoder = new IntEncodedValueImpl(getKey(this, "ele_gain"), 16, 0, false, true);
        this.avgGradeEncoder = new IntEncodedValueImpl(getKey(this, "avg_grade"), 16, -200, false, true);
    }

    @Override
    public void createEncodedValues(List<EncodedValue> registerNewEncodedValue) {
        super.createEncodedValues(registerNewEncodedValue);
        registerNewEncodedValue.add(this.netElevationGainEncoder);
        registerNewEncodedValue.add(this.avgGradeEncoder);
    }

    @Override
    public void applyWayTags(ReaderWay way, EdgeIteratorState edge) {
        PointList pl = edge.fetchWayGeometry(FetchMode.ALL);
        if (!pl.is3D())
            throw new IllegalStateException(getName() + " requires elevation data to improve speed calculation based on it. Please enable it in config via e.g. graph.elevation.provider: srtm");

        IntsRef intsRef = edge.getFlags();
        if (way.hasTag("tunnel", "yes") || way.hasTag("bridge", "yes") || way.hasTag("highway", "steps"))
            // do not change speed
            // note: although tunnel can have a difference in elevation it is very unlikely that the elevation data is correct for a tunnel
            return;

        // Decrease the speed for ele increase (incline), and decrease the speed for ele decrease (decline). The speed-decrease
        // has to be bigger (compared to the speed-increase) for the same elevation difference to simulate losing energy and avoiding hills.
        // For the reverse speed this has to be the opposite but again keeping in mind that up+down difference.
        double incEleSum = 0, incDist2DSum = 0, decEleSum = 0, decDist2DSum = 0;
        double fullDist2D = edge.getDistance();

        // for short edges an incline makes no sense and for 0 distances could lead to NaN values for speed, see #432
        if (fullDist2D < 2)
            return;

        for(int i=1; i < pl.size(); i++) {
            double prevLat = pl.getLat(i-1);
            double prevLon = pl.getLon(i-1);
            double prevEle = pl.getEle(i-1);
            double nextLat = pl.getLat(i);
            double nextLon = pl.getLon(i);
            double nextEle = pl.getEle(i);
            double eleDelta = nextEle - prevEle;
            if (eleDelta > 0.1) {
                incEleSum += eleDelta;
                incDist2DSum += DistancePlaneProjection.DIST_PLANE.calcDist3D(prevLat, prevLon, prevEle, nextLat, nextLon, nextEle);
            } else if (eleDelta < -0.1) {
                decEleSum -= eleDelta;
                decDist2DSum += DistancePlaneProjection.DIST_PLANE.calcDist3D(prevLat, prevLon, prevEle, nextLat, nextLon, nextEle);;
            }
        }
        netElevationGainEncoder.setInt(false, intsRef, (int) Math.round(incEleSum));
        netElevationGainEncoder.setInt(true, intsRef, (int) Math.round(decEleSum));
        double endEle = pl.getEle(pl.size() - 1);
        double startEle = pl.getEle(0);
        avgGradeEncoder.setInt(false, intsRef, (int) Math.round((endEle - startEle) * 100/fullDist2D));
        avgGradeEncoder.setInt(true, intsRef, (int) Math.round((startEle - endEle) * 100/fullDist2D));



        // Calculate slop via tan(asin(height/distance)) but for rather smallish angles where we can assume tan a=a and sin a=a.
        // Then calculate a factor which decreases or increases the speed.
        // Do this via a simple quadratic equation where y(0)=1 and y(0.3)=1/4 for incline and y(0.3)=2 for decline
        double fwdIncline = incDist2DSum > 1 ? incEleSum / incDist2DSum : 0;
        double fwdDecline = decDist2DSum > 1 ? decEleSum / decDist2DSum : 0;
        double restDist2D = fullDist2D - incDist2DSum - decDist2DSum;
        double maxSpeed = getHighwaySpeed("cycleway");
        if (accessEnc.getBool(false, intsRef)) {
            // use weighted mean so that longer incline influences speed more than shorter
            double speed = avgSpeedEnc.getDecimal(false, intsRef);
            double fwdFaster = 1 + 2 * keepIn(fwdDecline, 0, 0.2);
            fwdFaster = fwdFaster * fwdFaster;
            double fwdSlower = 1 - 5 * keepIn(fwdIncline, 0, 0.2);
            fwdSlower = fwdSlower * fwdSlower;
            speed = speed * (fwdSlower * incDist2DSum + fwdFaster * decDist2DSum + 1 * restDist2D) / fullDist2D;
            setSpeed(false, intsRef, keepIn(speed, PUSHING_SECTION_SPEED / 2.0, maxSpeed));
        }

        if (accessEnc.getBool(true, intsRef)) {
            double speedReverse = avgSpeedEnc.getDecimal(true, intsRef);
            double bwFaster = 1 + 2 * keepIn(fwdIncline, 0, 0.2);
            bwFaster = bwFaster * bwFaster;
            double bwSlower = 1 - 5 * keepIn(fwdDecline, 0, 0.2);
            bwSlower = bwSlower * bwSlower;
            speedReverse = speedReverse * (bwFaster * incDist2DSum + bwSlower * decDist2DSum + 1 * restDist2D) / fullDist2D;
            setSpeed(true, intsRef, keepIn(speedReverse, PUSHING_SECTION_SPEED / 2.0, maxSpeed));
        }
        edge.setFlags(intsRef);
    }
}
