package com.ora.calmwaters.data.localstorage;

public enum DiaryFileFormat {
    HEADER("SubjectID;Visit Number; Case Type;Symptom Type;Left Eye Rating Value; Right Eye Rating Value\n");

    public final String value;
    DiaryFileFormat(String value) {
        this.value = value;
    }
}
