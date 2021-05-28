package com.example.webload;

import android.os.Bundle;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;


public class TestActivity extends AppCompatActivity {







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        File path =       Environment.getExternalStoragePublicDirectory
                (
                        //Environment.DIRECTORY_PICTURES
                        Environment.DIRECTORY_DCIM + "/MyAlbums/Profile/"
                );
        File file = new File(path, "profile.txt");
        FileInputStream fis = null;
        System.out.println("checkpoint 2");
        try {
            fis=new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while((text = br.readLine())!=null)
            {
                sb.append(text).append("\n");
            }
            String url = sb.toString();
            String urls[]= url.split("\n");
            System.out.println(Arrays.toString(urls));

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
