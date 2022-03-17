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

import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A simple wrapper around two Maps that stores directionally-aware priority
 * values.
 */
public class TwoDirectionsPriorityMap {
  private TreeMap<Double, Integer> forward;
  private TreeMap<Double, Integer> backward;

  TwoDirectionsPriorityMap() {
    this.forward = new TreeMap<>();
    this.backward = new TreeMap<>();
  }

  public void put(boolean reverse, Double key, Integer value) {
    if (reverse) {
      this.backward.put(key, value);
    } else {
      this.forward.put(key, value);
    }
  }

  public void put(Double key, Integer value) {
    this.forward.put(key, value);
    this.backward.put(key, value);
  }

  public Entry<Double, Integer> lastEntry(boolean reverse) {
    if (reverse) {
      return this.backward.lastEntry();
    }
    return this.forward.lastEntry();
  }
}
