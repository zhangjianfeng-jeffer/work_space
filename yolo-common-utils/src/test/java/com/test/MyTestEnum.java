package com.test;

public enum MyTestEnum {

    TEST_ONE("TEST_ONE"),
    TEST_TWO("TEST_TWO"),
    TEST_THREE("TEST_THREE");

    private String test;

    MyTestEnum(String test){
        this.test = test;
    }
}
