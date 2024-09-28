package com.example.sfc_front.ui.library;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class JsonFileManager {
    private static final String JSON_FILE_NAME = "data.json";

    public static void createJsonFile(Context context) {
        try {
            // 创建一个新的JSON对象
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("IncorrectPasswordAttempts", 0);
            jsonObject.put("UAC", "");
            jsonObject.put("Name", "");
            jsonObject.put("M/F", "");
            jsonObject.put("PhoneNumber", "");
            jsonObject.put("UCK", "");
            jsonObject.put("UBox", "");
            // 将JSON对象写入文件
            File jsonFile = new File(context.getFilesDir(), JSON_FILE_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(jsonFile);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.close();
            Log.d("JsonFileManager", "JSON file created.");
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject readJsonFile(Context context) {
        try {
            File jsonFile = new File(context.getFilesDir(), JSON_FILE_NAME);
            FileInputStream fileInputStream = new FileInputStream(jsonFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonString.append(line);
            }
            bufferedReader.close();
            return new JSONObject(jsonString.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateJsonKey(Context context, String key, String newValue) {
        JSONObject jsonObject = readJsonFile(context);
        if (jsonObject != null) {
            try {
                jsonObject.put(key, newValue);
                writeJsonToFile(context, jsonObject);
                Log.d("JsonFileManager", "JSON key updated.");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteJsonKey(Context context, String key) {
        JSONObject jsonObject = readJsonFile(context);
        if (jsonObject != null) {
            jsonObject.remove(key);
            writeJsonToFile(context, jsonObject);
            Log.d("JsonFileManager", "JSON key deleted.");
        }
    }

    private static void writeJsonToFile(Context context, JSONObject jsonObject) {
        try {
            File jsonFile = new File(context.getFilesDir(), JSON_FILE_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(jsonFile);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

