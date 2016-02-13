package com.example.sampletvinput.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NhkProgram {
    public long id;
    public String event_id;
    public String start_time;
    public NhkService service;
    public String end_time;
    public String title;
    public List<String> genres;
    public NhkLogo program_logo;
    public String program_url;
}
