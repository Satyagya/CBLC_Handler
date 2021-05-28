package com.example.webload.androidFileSystem;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter

public class FileStructure {
    int requestId;
    int urlCount;
    String sleepCount;
    ArrayList<String> profile;

    public FileStructure() {
        profile = new ArrayList<>();

        requestId = 0;
        urlCount = 0;
        sleepCount = "0_0_0";

    }

    public FileStructure(int requestId, int urlCount, String sleepCount, ArrayList<String> profile) {
        this.requestId = requestId;
        this.urlCount = urlCount;
        this.sleepCount = sleepCount;
        this.profile = profile;
    }

}
