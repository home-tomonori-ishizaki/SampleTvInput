package com.example.sampletvinput.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NhkService {
    public String id;
    public String name;

    @JsonProperty("logo_s")
    public Logo logo;

    public static class Logo {
        public String url;
        public int width;
        public int height;
    }
}
