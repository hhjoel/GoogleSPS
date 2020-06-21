// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class FindMeetingQuery {
  public static final int MAX_SLOTS = 24 * 60;

  public Collection<TimeRange> query1(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    Collection<String> attendeesTemp = request.getAttendees();
    TreeSet<String> attendees = new TreeSet(attendeesTemp); // build in O(PlogP), check contains() in O(logP) instead of O(P)

    Collection<TimeRange> answer = new ArrayList<>();
    if (duration > MAX_SLOTS) {
      return answer;
    }

    boolean[] isOccupied = new boolean[MAX_SLOTS];  // O(1)

    for (Event e : events) { // O(E)
        TimeRange timeRange = e.getWhen();
        Set<String> participants = e.getAttendees();
        for (String p : participants) { // O(P)
            if (attendees.contains(p)) { // O(logP)
                for (int i = timeRange.start(); i < timeRange.end(); i++) { // O(1)
                    isOccupied[i] = true; // O(1)
                }
            }
        }
    }

    int cumulativeDuration = 0;
    int start = -1;
    for (int i = 0; i < MAX_SLOTS; i++) { // O(1)
        if (isOccupied[i]) {
            if (cumulativeDuration >= duration) {
                answer.add(TimeRange.fromStartDuration(start, cumulativeDuration));
            }
            cumulativeDuration = 0;
            start = -1;
        } else {
            if (start == -1) {
                start = i;
            }
            cumulativeDuration++;
        }
    }

    if (cumulativeDuration >= duration) {
        answer.add(TimeRange.fromStartDuration(start, cumulativeDuration));
    }

    // O((24*60)EPlogP) = O(EPlogP)
    return answer;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    Collection<String> attendeesTemp = request.getAttendees();
    TreeSet<String> attendees = new TreeSet(attendeesTemp); // build in O(PlogP), check contains() in O(logP) instead of O(P)

    Collection<TimeRange> answer = new ArrayList<>();
    if (duration > MAX_SLOTS) {
      return answer;
    }

    TreeSet<TimeRange> occupiedSlots = new TreeSet<TimeRange>(
        (TimeRange t1, TimeRange t2) -> t1.start() - t2.start()
    );

    for (Event e : events) { // O(E)
        TimeRange timeRange = e.getWhen();
        Set<String> participants = e.getAttendees();
        for (String p : participants) { // O(P)
            if (attendees.contains(p)) { // O(logP)
                if (occupiedSlots.size() == 0) {
                    occupiedSlots.add(TimeRange.fromStartDuration(timeRange.start(), timeRange.duration()));
                } else {
                    TimeRange before = occupiedSlots.floor(timeRange);
                    if (before == null || before.end() <= timeRange.start()) {
                        TimeRange after = occupiedSlots.floor(TimeRange.fromStartDuration(timeRange.end(), 1));
                        if (after == null || after.end() <= timeRange.end()) {
                            remove(occupiedSlots, timeRange.start(), timeRange.end());
                            occupiedSlots.add(TimeRange.fromStartDuration(timeRange.start(), timeRange.duration()));
                        } else {
                            remove(occupiedSlots, timeRange.start(), timeRange.end());
                            occupiedSlots.add(TimeRange.fromStartEnd(timeRange.start(), after.end(), false));
                        }
                    } else {
                        TimeRange after = occupiedSlots.floor(TimeRange.fromStartDuration(timeRange.end(), 1));
                        if (after == null || after.end() <= timeRange.end()) {
                            remove(occupiedSlots, before.start(), timeRange.end());
                            occupiedSlots.add(TimeRange.fromStartEnd(before.start(), timeRange.end(), false));
                        } else {
                            remove(occupiedSlots, before.start(), timeRange.end());
                            occupiedSlots.add(TimeRange.fromStartEnd(before.start(), after.end(), false));
                        }
                    }
                }
            }
        }
    }

    int startTime = 0;
    TimeRange occupiedSlot;

    try {
        occupiedSlot = occupiedSlots.first();
    } catch (Exception e) {
        occupiedSlot = null;
    }

    while (true) {
        if (occupiedSlot == null) {
            if (MAX_SLOTS - startTime >= duration) {
                answer.add(TimeRange.fromStartEnd(startTime, MAX_SLOTS, false));
            }
            break;
        }
        int difference = occupiedSlot.start() - startTime;
        if (difference >= duration) {
            answer.add(TimeRange.fromStartDuration(startTime, difference));
        }
        startTime = occupiedSlot.end();
        occupiedSlot = occupiedSlots.ceiling(TimeRange.fromStartDuration(occupiedSlot.start() + 1, 1));
    }

    return answer;
  }

  // remove all elements in the tree that has starting time between start and end, both inclusive
  private void remove(TreeSet<TimeRange> tree, int start, int end) {
    TimeRange previous = TimeRange.fromStartDuration(start, 1);
    while (true) {
        TimeRange element = tree.ceiling(previous);
        if (element != null && element.start() <= end) {
            tree.remove(element);
            previous = element;
        } else {
            break;
        }
    }
  }
}
