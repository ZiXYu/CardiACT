package com.example.osorekoxuan.cardiact;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


@ParseClassName("AED_DATA")
public class AED extends ParseObject {
  public String getName() {
    return getString("ADD_FULL");
  }

  public void setName(String value) {
    put("ADD_FULL", value);
  }

  public ParseGeoPoint getLocation() {
    return getParseGeoPoint("LOCATION");
  }

  public void setLong(Number value) {
    put("LONGITUDE", value);
  }

  public static ParseQuery<AED> getQuery() { return ParseQuery.getQuery(AED.class);
  }
}
