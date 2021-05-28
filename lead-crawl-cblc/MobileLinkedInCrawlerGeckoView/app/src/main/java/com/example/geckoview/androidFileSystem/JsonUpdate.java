package com.example.geckoview.androidFileSystem;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class JsonUpdate {

    public static final String DETAIL_JSON = "detail.txt";

    public static FileStructure getJson() {

        File filepath = LocalPath.getFile("/DCIM/MyAlbums/", DETAIL_JSON);
        String responce = null;
        JSONObject jsonObject = null;
        FileStructure fileStructure = null;

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filepath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            responce = stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObject = new JSONObject(responce);
            JSONArray jsonArray = jsonObject.getJSONArray("profile");
            ArrayList<String> arrayList = new ArrayList<>();
            JSONArray jArray = jsonObject.getJSONArray("profile");
            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++) {
                    arrayList.add(jArray.getString(i));
                }
            }

            fileStructure = new FileStructure(jsonObject.getInt("request_id"), jsonObject.getInt("url_count"), jsonObject.getString("sleep_count"), arrayList);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return fileStructure;

    }

    public static boolean setJson(FileStructure file) {

        File filepath = LocalPath.getFile("/DCIM/MyAlbums/", DETAIL_JSON);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("request_id", file.getRequestId());
            jsonObject.put("url_count", file.getUrlCount());

            jsonObject.put("sleep_count", file.getSleepCount());
            jsonObject.put("profile", new JSONArray(file.getProfile()));

            FileWriter fileWriter = new FileWriter(filepath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.close();
        } catch (Exception e) {
            Log.e("Json Object Error : ", e.getMessage());
        } finally {

        }
        return true;
    }
}
