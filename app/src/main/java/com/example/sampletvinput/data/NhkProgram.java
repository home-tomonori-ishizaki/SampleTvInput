package com.example.sampletvinput.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NhkProgram {
    public String id;
    public String event_id;
    public String start_time;
    public String end_time;
    public String title;
    public List<String> genres;
}
