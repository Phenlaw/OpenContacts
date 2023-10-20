package opencontacts.open.com.opencontacts.orm;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by sultanm on 7/31/17.
 */

public class CallLogEntry extends SugarRecord implements Serializable {
    int simId;
    public String name;
    public long contactId;
    String phoneNumber;
    String duration;
    String callType;
    String date; //in milliseconds from epoch type long;

    public int getSimId() {
        return simId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public CallLogEntry() {
        super();
    }

    public CallLogEntry(long id) {
        super();
        setId(id);
    }

    public CallLogEntry(String name, long contactId, String phoneNumber, String duration, String callType, String date, int simId) {
        this.name = name;
        this.contactId = contactId;
        this.phoneNumber = phoneNumber;
        this.duration = duration;
        this.callType = callType;
        this.date = date;
        this.simId = simId;
        if(this.phoneNumber == null) this.phoneNumber = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDuration() {
        return duration;
    }


    public String getCallType() {
        return callType;
    }


}
