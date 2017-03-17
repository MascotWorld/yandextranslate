package com.mascotworld.yandextranstlate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String lang = "ru";
    String[][] languages = new String[50][2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button changelang = (Button) findViewById(R.id.changelang);
        CardView cardView = (CardView) findViewById(R.id.cardView);
        cardView.setVisibility(View.INVISIBLE);
        final EditText inputedtext = (EditText) findViewById(R.id.inputedtext);
        Button buttonclearedit = (Button) findViewById(R.id.clearedit);



        View.OnClickListener changelanguage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        View.OnClickListener clearedit = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputedtext.setText("");
            }
        };
        buttonclearedit.setOnClickListener(clearedit);
        changelang.setOnClickListener(changelanguage);

        inputedtext.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (inputedtext.getText().toString().equals("")) {
                    CardView cardView = (CardView) findViewById(R.id.cardView);
                    cardView.setVisibility(View.INVISIBLE);
                } else
                    translate();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

    }

    public void getlanguages(){
        //https://translate.yandex.net/api/v1.5/tr.json/getLangs?ui=en&key=trnsl.1.1.20170315T201529Z.d15600fa87da8a7a.fbd42ba86db793ee772d23843d0b8a05126d0143

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://translate.yandex.net/api/v1.5/tr.json/getLangs?ui=en&key=trnsl.1.1.20170315T201529Z.d15600fa87da8a7a.fbd42ba86db793ee772d23843d0b8a05126d0143")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                final String responseData = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = null;
                        try {
                            
                            JSONObject jObject = new JSONObject(responseData);
                            JSONArray jsonArray = jsonObject.getJSONArray("code");



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

    }

    public void translate() {
        EditText transtext = (EditText) findViewById(R.id.inputedtext);
        final TextView translatedtext = (TextView) findViewById(R.id.TranslateText);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170315T201529Z.d15600fa87da8a7a.fbd42ba86db793ee772d23843d0b8a05126d0143" +
                        "&text=" + transtext.getText() +
                        "&lang=en" +
                        "&[options=1]")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                final String responseData = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = null;
                        try {
                            JSONObject jObject = new JSONObject(responseData); // json
                            String code = jObject.getString("code"); // get the name from data.
                            String lang = jObject.getString("lang");
                            String text = jObject.getString("text");
                            text = text.substring(2, text.length() - 2);
                            translatedtext.setText(text);
                            CardView cardView = (CardView) findViewById(R.id.cardView);
                            cardView.setVisibility(View.VISIBLE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

    }
}
