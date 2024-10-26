package com.example.stablediffusion.api;

import java.util.List;

public class ImageResponse {
    private String status;
    private double generationTime;
    private int id;
    private List<String> output;

    public List<String> getOutput() {
        return output;
    }

}

