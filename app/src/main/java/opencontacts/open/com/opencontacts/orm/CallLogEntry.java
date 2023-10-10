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
    } //da ottimizzare

    public void setSimId(int simId) {
        this.simId = simId;
    } //inutilizzato

    public String getDate() {
        return date;
    } //da ottimizzare

    public void setDate(String date) {
        this.date = date;
    } // da ottimizzare

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
    } //da ottimizzare

    public String getName() {
        return name;
    } //da ottimizzare

    public void setName(String name) {
        this.name = name;
    }//da ottimizzare

    public long getContactId() {
        return contactId;
    } //da ottimizzare

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }//da ottimizzare

    public String getPhoneNumber() {
        return phoneNumber;
    } //da ottimizzare

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    //da ottimizzare
    public String getDuration() {
        return duration;
    } //da ottimizzare

    public void setDuration(String duration) {
        this.duration = duration;
    } //inutilizzati

    public String getCallType() {
        return callType;
    } //da ottimizzare

    public void setCallType(String callType) {
        this.callType = callType;
    } //inutilizzato

}
