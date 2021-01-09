package com.henry.RandomAddressGenerator;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.AsynchUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;

@DesignerComponent(
        version = 1,
        description = "A simple extension that gets random address from randomuser.me<br/>Developed by Henry Richard J",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "")

@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
        "android.permission.WRITE_EXTERNAL_STORAGE," +
        "android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "json-simple.jar")
public final class RandomAddressGenerator extends AndroidNonvisibleComponent {
    public String inline="";
    public final Activity activity;


    public RandomAddressGenerator(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    @SimpleFunction(description = "Function to get random information")
    public void getRandomInformation(final String gender,final String nationality){


        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                String inline="";
                final StringBuilder sb = new StringBuilder();
                try{
                    final String furl = "https://randomuser.me/api/?gender="+gender+"&nat="+nationality;
                    URL url = new URL(furl);
                    HttpURLConnection httpURLConnection = null;
                    InputStream inputStream = null;


                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();
                    inputStream = httpURLConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    while ((inline=br.readLine()) != null) {
                        //System.out.println(read);
                        sb.append(inline);
                    }

                    br.close();

                    httpURLConnection.disconnect();

                    inputStream.close();
                    activity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            getName(sb.toString());
                        }
                    } );


                } catch (Exception e) {
                    //Some Error
                }
            }
        });


    }

    @SimpleEvent(description = "Event Handler after getting random data")
    public void gotData(String Name, String Street, String City, String State, String Country, String Zipcode,String picUrl ){
        EventDispatcher.dispatchEvent(this,"gotData",Name,Street,City,State,Country,Zipcode,picUrl);
    }

    public void getName(String JsonObj){

        try {

            JSONParser parse = new JSONParser();
            JSONObject obj = (JSONObject) parse.parse(JsonObj);
            JSONArray arr = (JSONArray) obj.get("results");

            JSONObject resultsArray = (JSONObject) arr.get(0);
            JSONObject nameJobject = (JSONObject) resultsArray.get("name");

            JSONObject locationObject;
            locationObject = (JSONObject)resultsArray.get("location");
            JSONObject streetObject = (JSONObject)locationObject.get("street");

            JSONObject pictureObject = (JSONObject)resultsArray.get("picture");

            String firstName = (String) nameJobject.get("first");
            String lastName = (String) nameJobject.get("last");

            String streetNumber = String.valueOf(streetObject.get("number"));
            String streetName = (String) streetObject.get("name");
            String city = (String)locationObject.get("city");
            String state = (String)locationObject.get("state");
            String country = (String)locationObject.get("country");
            String zipCode = String.valueOf(locationObject.get("postcode"));

            String pictureUrl = (String) pictureObject.get("large");

            gotData(firstName + " " + lastName,streetNumber+" "+streetName,city,state,country,zipCode,pictureUrl);
        }
        catch (Exception e)

        {
            //Fetch Error
        }

    }
}

