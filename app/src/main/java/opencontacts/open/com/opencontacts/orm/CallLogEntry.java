package opencontacts.open.com.opencontacts.orm;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by sultanm on 7/31/17.
 */

public class CallLogEntry extends SugarRecord implements Serializable {
    public int simId;
    public String name;
    public long contactId;
    public String phoneNumber;
    public String duration;
    public String callType;
    public String date; //in milliseconds from epoch type long;


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


}
