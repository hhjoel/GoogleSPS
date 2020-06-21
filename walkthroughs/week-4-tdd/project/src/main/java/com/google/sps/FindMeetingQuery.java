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
  public static final int TOTAL_SLOTS = 24 * 60;
  public static final int DUMMY_DURATION = 1;

  public Collection<TimeRange> query1(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    Collection<TimeRange> answer = new ArrayList<>();

    if (duration > TOTAL_SLOTS) {
      return answer;
    }

    // build in O(PlogP) so contains() run in O(logP) instead of O(P), where P is the max number of Attendees per Event
    TreeSet<String> attendees = new TreeSet(request.getAttendees());

    // Represents whether each slot of 1 minute is occupied or not
    boolean[] isOccupied = new boolean[TOTAL_SLOTS];  // O(1)

    for (Event e : events) { // O(E), where E is the max number of Events supplied to the query
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
    for (int i = 0; i < TOTAL_SLOTS + 1; i++) { // O(1)
        if (i == TOTAL_SLOTS || isOccupied[i]) {
            if (cumulativeDuration >= duration) {
                // Add an available timeslot once we reach the end or hit an occupied slot,
                // and the previous cumulative unoccupied slots > duration
                answer.add(TimeRange.fromStartDuration(start, cumulativeDuration));
            }
            cumulativeDuration = 0;
            start = -1;
        } else {
            if (start == -1) {
                start = i; // Sets the start of the free block of timeslots
            }
            cumulativeDuration++;
        }
    }

    // Total Time Complexity = O((24*60)EPlogP) = O(EPlogP)
    return answer;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long duration = request.getDuration();
    Collection<TimeRange> answer = new ArrayList<>();

    if (duration > TOTAL_SLOTS) {
      return answer;
    }

    // build in O(PlogP) so contains() run in O(logP) instead of O(P), where P is the max number of Attendees per Event
    TreeSet<String> attendees = new TreeSet(request.getAttendees());

    // Invariant: this TreeSet will always consist of a number of non-overlapping TimeRange(s), sorted by start time
    TreeSet<TimeRange> occupiedSlots = new TreeSet<TimeRange>(
        (TimeRange t1, TimeRange t2) -> t1.start() - t2.start()
    );

    for (Event e : events) { // O(E), where E is the max number of Events supplied to the query
        TimeRange timeRange = e.getWhen();
        Set<String> participants = e.getAttendees();
        for (String p : participants) { // O(P)
            if (attendees.contains(p)) { // O(logP)
                TimeRange before = occupiedSlots.floor(timeRange); // O(logE)
                TimeRange after = occupiedSlots.floor(TimeRange.fromStartDuration(timeRange.end(), DUMMY_DURATION)); // O(logE)

                int toRemoveStart = before == null || before.end() <= timeRange.start() ? timeRange.start() : before.start(); // O(1)
                int toRemoveEnd = timeRange.end(); // O(1)
                remove(occupiedSlots, toRemoveStart, toRemoveEnd); // Amortized O(logE)

                int toInsertStart = before == null || before.end() <= timeRange.start() ? timeRange.start() : before.start(); // O(1)
                int toInsertEnd = after == null || after.end() <= timeRange.end() ? timeRange.end() : after.end(); // O(1)
                occupiedSlots.add(TimeRange.fromStartEnd(toInsertStart, toInsertEnd, false)); // O(logE)
            }
        }
    }

    int startTime = 0;
    TimeRange occupiedSlot;
    try {
        occupiedSlot = occupiedSlots.first();
    } catch (Exception e) {
        occupiedSlot = null; // empty tree
    }

    while (occupiedSlot != null) {
        int difference = occupiedSlot.start() - startTime;
        if (difference >= duration) {
            answer.add(TimeRange.fromStartDuration(startTime, difference));
        }
        startTime = occupiedSlot.end();
        // Get next occupied TimeRange in the tree (can use iterator, but this trick seems cleaner)
        occupiedSlot = occupiedSlots.ceiling(TimeRange.fromStartDuration(occupiedSlot.start() + 1, DUMMY_DURATION));
    }

    // Add the last free slot if it is >= duration
    if (TOTAL_SLOTS - startTime >= duration) {
        answer.add(TimeRange.fromStartEnd(startTime, TOTAL_SLOTS, false));
    }

    return answer;
  }

  // remove all elements in the tree that has starting time between start and end, both inclusive
  private void remove(TreeSet<TimeRange> tree, int start, int end) {
    TimeRange previous = TimeRange.fromStartDuration(start, DUMMY_DURATION);
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
