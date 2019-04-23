package com.blank.writotext.utils;

public class Fyle {

    private int id;
    private String FileName;
    private int FileImg;
    private String FilePath;

    public Fyle() {
    }

    public Fyle(int id, String FileName, int FileImg, String FilePath) {
        this.id = id;
        this.FileName = FileName;
        this.FileImg = FileImg;
        this.FilePath = FilePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return FilePath;
    }
    public void setFilePath(String FilePath) {
        this.FilePath = FilePath;
    }


    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fname) {
        FileName = fname;
    }
    public int getFileImg() {
        return FileImg;
    }
    public void setFileImg(int fimg) {
        FileImg = fimg;
    }
}