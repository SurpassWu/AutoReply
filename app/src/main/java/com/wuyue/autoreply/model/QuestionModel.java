package com.wuyue.autoreply.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by wuyue on 2017/6/1.
 */

public class QuestionModel implements Parcelable {
    @SerializedName("question")
    private String mQuestion;

    public String getQuestion() {
        return mQuestion;
    }

    public void setQuestion(String mQuestion) {
        this.mQuestion = mQuestion;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mQuestion);
    }

    public QuestionModel() {
    }

    protected QuestionModel(Parcel in) {
        this.mQuestion = in.readString();
    }

    public static final Parcelable.Creator<QuestionModel> CREATOR = new Parcelable.Creator<QuestionModel>() {
        @Override
        public QuestionModel createFromParcel(Parcel source) {
            return new QuestionModel(source);
        }

        @Override
        public QuestionModel[] newArray(int size) {
            return new QuestionModel[size];
        }
    };

    public static List<QuestionModel> parse(JSONArray jsonArray) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<QuestionModel>>() {
        }.getType();
        return gson.fromJson(jsonArray.toString(), type);
    }
}
