package com.spaceapps.meatanagram.spaceappsproject;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by snowblack on 4/12/15.
 */

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String OWNER_KEY = "owner";
    public static final String LOCATION_KEY = "location";
    public static final String DATE_KEY = "date";


    public void setOwner(ParseUser owner) {
        this.put(OWNER_KEY, owner);
    }

    public ParseUser getOwner() {
        return (ParseUser) this.get(OWNER_KEY);
    }

    public void setGeoPoint(ParseGeoPoint geoPoint) {
        this.put(LOCATION_KEY, geoPoint);
    }

    public ParseGeoPoint getGeoPoint() { return (ParseGeoPoint) this.get(LOCATION_KEY); }

    public void setDate(Date date) {
        this.put(DATE_KEY, date);
    }

    public Date getDate() {
        return (Date) this.get(DATE_KEY);
    }


}
