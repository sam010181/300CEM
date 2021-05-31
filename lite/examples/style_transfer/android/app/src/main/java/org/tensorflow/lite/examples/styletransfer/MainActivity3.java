package org.tensorflow.lite.examples.styletransfer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity3 extends AppCompatActivity {
    private static boolean setLanguage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanuage);

        if (!setLanguage) {
            setLanguage = true;
            showSaveLanguage(SpUserUtils.getString(this, "language"));
        }
    }

    public void nextPage(View view) {
        Intent intent = new Intent(this, Startpage.class);
        startActivity(intent);
    }

    public void changeToEnglish(View view) {
        showSaveLanguage(LanguageUtil.ENGLISH);
    }

    public void changeToChinese(View view) {
        showSaveLanguage(LanguageUtil.TRADITIONAL_CHINESE);
    }

    private void showSaveLanguage(String language){
        LanguageUtil.changeAppLanguage(this, language, MainActivity3.class);
        SpUserUtils.putString(this, "language", language);
    }
}