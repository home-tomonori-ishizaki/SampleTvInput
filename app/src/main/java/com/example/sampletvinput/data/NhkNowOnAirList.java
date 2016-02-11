package com.example.sampletvinput.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown=true)
public class NhkNowOnAirList {
    public Map<String, Map<String, NhkProgram>> nowonair_list;
}
