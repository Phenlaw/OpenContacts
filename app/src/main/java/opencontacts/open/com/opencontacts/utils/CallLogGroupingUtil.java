package opencontacts.open.com.opencontacts.utils;

import static java.util.Calendar.MINUTE;

import android.util.Log;

import androidx.core.util.Pair;

import com.github.underscore.U;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.GroupedCallLogEntry;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;

public class CallLogGroupingUtil {

    public static final int OFFSET_TIME_IN_MINUTES_FOR_GROUPING = 60;

    public static List<GroupedCallLogEntry> group(List<CallLogEntry> callLogEntries) {
        ArrayList<GroupedCallLogEntry> groupedCallLogEntries = new ArrayList<>();
        if (U.isEmpty(callLogEntries)) {
            return groupedCallLogEntries;
        }

        GroupedCallLogEntryWithCache groupedCallLogEntryWithCache = createGroupedCallLogEntryWithCache(callLogEntries.get(0));
        groupedCallLogEntries.add(groupedCallLogEntryWithCache.groupedCallLogEntry);

        return U.reduce(U.drop(callLogEntries, 1), (accumulator, callLogEntry) -> {
            GroupedCallLogEntry lastGroup = U.last(accumulator.first);
            if (canBeGrouped(accumulator.second, callLogEntry))
                lastGroup.callLogEntries = U.concat(lastGroup.callLogEntries, Collections.singletonList(callLogEntry));
            else {
                GroupedCallLogEntryWithCache cachedGroupCallLogEntry = createGroupedCallLogEntryWithCache(callLogEntry);
                accumulator.first.add(cachedGroupCallLogEntry.groupedCallLogEntry);
                return new Pair<>(accumulator.first, cachedGroupCallLogEntry);
            }
            return accumulator;
        }, new Pair<List<GroupedCallLogEntry>, GroupedCallLogEntryWithCache>(groupedCallLogEntries, groupedCallLogEntryWithCache)).first;
    }

    private static boolean canBeGrouped(GroupedCallLogEntryWithCache groupedCallLogEntryWithCache, CallLogEntry callLogEntry) {
        if (callLogEntry.contactId != -1 && groupedCallLogEntryWithCache.groupedCallLogEntry.latestCallLogEntry.contactId != callLogEntry.contactId)
            return false;
        Log.i("G&S","Modificato");
        Log.i("G&S","Modificato");
        if (callLogEntry.contactId == -1 && !U.isEqual(groupedCallLogEntryWithCache.groupedCallLogEntry.latestCallLogEntry.phoneNumber, callLogEntry.phoneNumber))
            return false;
        Log.i("G&S","Modificato");
        return (Common.getCalendarInstanceAt(Long.parseLong(callLogEntry.date)).after(groupedCallLogEntryWithCache.groupingTimeOffsetCalendarInstance));
    }

    private static GroupedCallLogEntryWithCache createGroupedCallLogEntryWithCache(CallLogEntry callLogEntry) {
        GroupedCallLogEntry groupedCallLogEntry = new GroupedCallLogEntry(Collections.singletonList(callLogEntry), callLogEntry);
        Log.i("G&S","Modificato");
        Calendar hourOffsetCalendarInstance = Common.getCalendarOffset(-OFFSET_TIME_IN_MINUTES_FOR_GROUPING, MINUTE, new Date(Long.parseLong(callLogEntry.date)));

        return new GroupedCallLogEntryWithCache(groupedCallLogEntry, hourOffsetCalendarInstance);
    }

    private static class GroupedCallLogEntryWithCache {
        public GroupedCallLogEntry groupedCallLogEntry;
        public Calendar groupingTimeOffsetCalendarInstance;

        public GroupedCallLogEntryWithCache(GroupedCallLogEntry groupedCallLogEntry, Calendar groupingTimeOffsetCalendarInstance) {
            this.groupedCallLogEntry = groupedCallLogEntry;
            this.groupingTimeOffsetCalendarInstance = groupingTimeOffsetCalendarInstance;
        }
    }

}
