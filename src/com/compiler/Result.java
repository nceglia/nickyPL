package com.compiler;

import java.util.ArrayList;

public class Result {
    public ResultType kind;
    public Integer value;
    public Integer address;
    public Integer register;
    public Integer condition;
    public Integer fixuplocation;
    public ArrayList<String> offset = new ArrayList<>();
    public String message;
    public String name;
    public ArrayList<Integer> dimensions = new ArrayList();
}
