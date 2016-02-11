package com.example.sampletvinput.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NhkProgramList {
    public Map<String, List<NhkProgram>> list;

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class NhkProgram {
        public String id;
        public String event_id;
        public String start_time;
        public String end_time;
        public String title;
        public List<String> genres;
    }
}
