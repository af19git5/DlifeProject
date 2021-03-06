package com.kang.Dlife.tb_page2.diary_view;

import java.io.Serializable;


public class PhotoSpot implements Serializable {
    private int sk;
    private int member_sk;
    private int diary_sk;
    private byte[] photo_img;
    private String post_date;

    public PhotoSpot(int sk, int member_sk, int diary_sk, byte[] photo_img, String post_date) {
        this.sk = sk;
        this.member_sk = member_sk;
        this.diary_sk = diary_sk;
        this.photo_img = photo_img;
        this.post_date = post_date;
    }
    public PhotoSpot(int sk){
        this.sk = sk;

    }


    public int getSk() {
        return sk;
    }

    public void setSk(int sk) {
        this.sk = sk;
    }

    public int getMember_sk() {
        return member_sk;
    }

    public void setMember_sk(int member_sk) {
        this.member_sk = member_sk;
    }

    public int getDiary_sk() {
        return diary_sk;
    }

    public void setDiary_sk(int diary_sk) {
        this.diary_sk = diary_sk;
    }

    public byte[] getPhoto_img() {
        return photo_img;
    }

    public void setPhoto_img(byte[] photo_img) {
        this.photo_img = photo_img;
    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }
}