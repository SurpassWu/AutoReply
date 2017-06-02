package com.wuyue.autoreply.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.wuyue.autoreply.utils.StringUtil;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by wuyue on 2017/6/1.
 */

public class AutoReply implements Parcelable {

    @SerializedName("auto_reply")
    private String mAutoReply;

    @SerializedName("question")
    private String mQuestion;

    public String getQuestion() {
        return mQuestion;
    }

    public void setQuestion(String mQuestion) {
        this.mQuestion = mQuestion;
    }

    public String getAutoReply() {
        return mAutoReply;
    }

    public void setAutoReply(String mAutoReply) {
        this.mAutoReply = mAutoReply;
    }

    public AutoReply() {
    }


    public static List<AutoReply> parse(JSONArray jsonArray) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<AutoReply>>() {
        }.getType();
        return gson.fromJson(jsonArray.toString(), type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAutoReply);
        dest.writeString(this.mQuestion);
    }

    protected AutoReply(Parcel in) {
        this.mAutoReply = in.readString();
        this.mQuestion = in.readString();
    }

    public static final Creator<AutoReply> CREATOR = new Creator<AutoReply>() {
        @Override
        public AutoReply createFromParcel(Parcel source) {
            return new AutoReply(source);
        }

        @Override
        public AutoReply[] newArray(int size) {
            return new AutoReply[size];
        }
    };
}
