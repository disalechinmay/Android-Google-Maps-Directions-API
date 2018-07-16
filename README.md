# Android-Google-Maps-Directions-API
Demo on how to generate polylines on maps using Google Maps Directions API on Android. 

There is very minimal documentation regarding how to use Google Maps Directions API on Android using Java.
This repo will help you with using Directions API in your app.

    How does this work?
    
    0. MAKE SURE YOU HAVE ALL THE DEPENDENCIES!
    
    
    1. Communicate with Google servers using your API key and wait for a JSON response.
       JSON response is downloaded and managed using AsyncTask.
    
    
    2. Parse JSON response as required using JSONObject.
       For more information on this, refer to : 
       https://developer.android.com/reference/org/json/JSONObject
       
       
    3. Generate polylines on map.
