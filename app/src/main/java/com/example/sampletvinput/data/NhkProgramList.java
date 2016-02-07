package com.example.sampletvinput.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

public class NhkProgramList {
    public Map<String, List<NhkProgram>> list;

    public static class NhkProgram {
        public String id;
        public String event_id;
        public String start_time;
        public String end_time;
        @JsonIgnore
        public Object area;
        @JsonIgnore
        public Object service;
        public String title;
        @JsonIgnore
        public Object subtitle;
        public List<String> genres;
    }
}
