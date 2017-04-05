package com.mascotworld.yandextranstlate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.mascotworld.yandextranstlate.R.id.intranslang;

public class MainActivity extends AppCompatActivity {
    private List<languages> language;
    private List<history_translate> historytranslate;
    DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        // work_write_data();
        //work_delete_data();
        historytranslate = new ArrayList<>();
        historytranslate.add(new history_translate("ForFun", "ForFun", "RU-EN"));
        final TextView yourTextView = (TextView) findViewById(R.id.TranslateText);
        yourTextView.setMovementMethod(new ScrollingMovementMethod());
        final Button intranslang = (Button) findViewById(R.id.intranslang);
        final Button outranslang = (Button) findViewById(R.id.outranslang);
        getlanguages();
        Button changelang = (Button) findViewById(R.id.changelang);

        final EditText inputedtext = (EditText) findViewById(R.id.inputedtext);
        Button buttonclearedit = (Button) findViewById(R.id.clearedit);


        View.OnClickListener changelanguage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String substr;

                substr = intranslang.getText().toString();
                if (substr.equals("Определение") || substr.equals(outranslang.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Невозможно", Toast.LENGTH_LONG).show();
                } else {

                    intranslang.setText(outranslang.getText().toString());
                    outranslang.setText(substr);
                }
                translate();


            }

        };

        View.OnClickListener clearedit = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputedtext.setText("");
                TextView translatedtext = (TextView) findViewById(R.id.TranslateText);
                translatedtext.setText("Переведённый текст...");
            }
        };

        buttonclearedit.setOnClickListener(clearedit);
        changelang.setOnClickListener(changelanguage);


        final Button favorite = (Button) findViewById(R.id.favorite);
        final View.OnClickListener setFavorite = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView translatedtext = (TextView) findViewById(R.id.TranslateText);
                if (!translatedtext.getText().toString().equals("")) {
                    if (!translatedtext.getText().toString().equals("Переведённый текст...")) {
                        int checkwork = 0, i = 0, position = 0;
                        while (checkwork == 0) {
                            if (historytranslate.get(i).translatelanguage.equalsIgnoreCase(translatedtext.getText().toString())) {
                                checkwork = 1;
                                position = i;
                            }
                            i++;
                        }
                        historytranslate.get(position).setFavorite();
                        if (historytranslate.get(position).isFavorite()) {
                            favorite.setBackground(getDrawable(R.mipmap.ic_favorite_black_36dp));
                        } else
                            favorite.setBackgroundResource(R.mipmap.ic_favorite_border_black_36dp);
                        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);

                        rv.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        };
        favorite.setOnClickListener(setFavorite);

        Button copyClip = (Button) findViewById(R.id.copybutton);
        View.OnClickListener setCopy = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((ClipboardManager) getApplicationContext().getSystemService(getApplicationContext().CLIPBOARD_SERVICE))
                        .setText(yourTextView.getText().toString());

                Toast.makeText(MainActivity.this, "Скопировано", Toast.LENGTH_LONG).show();
            }
        };
        copyClip.setOnClickListener(setCopy);

        inputedtext.addTextChangedListener(new TextWatcher() {


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }


            private Timer timer = new Timer();
            private final long DELAY = 500; // milliseconds

            @Override
            public void afterTextChanged(final Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        favorite.setBackgroundResource(R.mipmap.ic_favorite_border_black_36dp);
                                    }
                                });

                                translate();
                            }
                        },
                        DELAY
                );
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


        });

        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);
        rv.addItemDecoration(new SpacesItemDecoration(5));
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(rv);
        initializeAdapter();
        work_read_data();
    }

    public void work_write_data(String defword, String tranword, String wtr) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("defword", defword);
        cv.put("tranword", tranword);
        cv.put("wtr", wtr);
        cv.put("favorite", "test");
        // вставляем запись и получаем ее ID
        long rowID = db.insert("mytable", null, cv);

    }

    public void work_read_data() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("mytable", null, null, null, null, null, null);

        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int defwordind = c.getColumnIndex("defword");
            int tranwordind = c.getColumnIndex("tranword");
            int wtrind = c.getColumnIndex("wtr");

            do {
                // получаем значения по номерам столбцов и пишем все в лог
                Log.d("testdb", "ID = " + c.getString(defwordind) + ", name = " + c.getString(tranwordind) +
                        ", email = " + c.getString(wtrind));
                historytranslate.add(new history_translate(c.getString(defwordind), c.getString(tranwordind), c.getString(wtrind)));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());

        } else
            Log.d("testdb", "0 rows");
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.getAdapter().notifyDataSetChanged();

        c.close();
    }

    public void work_delete_data(int i) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d("testdb", "--- Clear mytable: ---");
        db.delete("mytable", "id = " + i, null);
    }

    private ItemTouchHelper.Callback createHelperCallback() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        deleteItem(viewHolder.getAdapterPosition());
                    }
                };
        return simpleItemTouchCallback;
    }

    private void moveItem(int oldPos, int newPos) {
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        //ListItem item = (ListItem) listData.get(oldPos);
        // listData.remove(oldPos);
        // listData.add(newPos, item);
        String def1, def2, def3;
        def1 = historytranslate.get(oldPos).defaultlanguage;
        def2 = historytranslate.get(oldPos).translatelanguage;
        def3 = historytranslate.get(oldPos).wtr;
        boolean def4 = historytranslate.get(oldPos).isFavorite();
        historytranslate.get(oldPos).defaultlanguage = historytranslate.get(newPos).defaultlanguage;
        historytranslate.get(oldPos).translatelanguage = historytranslate.get(newPos).translatelanguage;
        historytranslate.get(oldPos).wtr = historytranslate.get(newPos).wtr;
        historytranslate.get(oldPos).swapFavorite(historytranslate.get(newPos).isFavorite());

        historytranslate.get(newPos).defaultlanguage = def1;
        historytranslate.get(newPos).translatelanguage = def2;
        historytranslate.get(newPos).wtr = def3;
        historytranslate.get(newPos).swapFavorite(def4);


        rv.getAdapter().notifyItemMoved(oldPos, newPos);
    }

    private void deleteItem(final int position) {
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        historytranslate.remove(position);
        work_delete_data(position);
        rv.getAdapter().notifyItemRemoved(position);
    }

    public void clickcvhist(View view) {
        int position = (int) view.getTag();
        Button button = (Button) findViewById(R.id.intranslang);
        Button button1 = (Button) findViewById(R.id.outranslang);

        String def = historytranslate.get(position).wtr.substring(0, 2);
        String def2 = historytranslate.get(position).wtr.substring(3, 5);

        int checkwork = 0, i = 0;
        while (checkwork == 0) {
            if (def.equalsIgnoreCase(language.get(i).lang)) {
                button.setText(language.get(i).language);
                checkwork = 1;
            } else i++;
        }

        checkwork = 0;
        i = 0;
        while (checkwork == 0) {
            if (def2.equalsIgnoreCase(language.get(i).lang)) {
                button1.setText(language.get(i).language);
                checkwork = 1;
            } else i++;
        }


        Button button2 = (Button) findViewById(R.id.favorite);
        if (historytranslate.get(position).isFavorite()) {
            button2.setBackgroundResource(R.mipmap.ic_favorite_black_36dp);
        } else button2.setBackgroundResource(R.mipmap.ic_favorite_border_black_36dp);


        EditText editText = (EditText) findViewById(R.id.inputedtext);
        editText.setText(historytranslate.get(position).defaultlanguage);

        TextView textView = (TextView) findViewById(R.id.TranslateText);
        textView.setText(historytranslate.get(position).translatelanguage);


    }

    public void getlanguages() {
        //https://translate.yandex.net/api/v1.5/tr.json/getLangs?ui=en&key=trnsl.1.1.20170315T201529Z.d15600fa87da8a7a.fbd42ba86db793ee772d23843d0b8a05126d0143

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://translate.yandex.net/api/v1.5/tr.json/getLangs?ui=ru&key=trnsl.1.1.20170315T201529Z.d15600fa87da8a7a.fbd42ba86db793ee772d23843d0b8a05126d0143")
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
                            jObject = jObject.getJSONObject("langs");
                            JSONArray rec = jObject.names();
                            language = new ArrayList<>();
                            for (int i = 0; i < rec.length(); i++) {
                                language.add(new languages(rec.getString(i), jObject.getString(rec.getString(i))));
                            }
                            for (int i = 0; i < rec.length(); i++) {
                                Log.d("Languages list", Integer.toString(i) + " | " + language.get(i).lang + " | " + language.get(i).language);
                            }
                            createPopupMenu();

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
        final Button outranslang = (Button) findViewById(R.id.outranslang);
        final Button intranslang = (Button) findViewById(R.id.intranslang);
        int checkwork = 0, langtotrans = 0, langtotrans1 = 0;
        final String lang;
        final String langch;
        final String translated;
        final RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);

        if (!transtext.getText().toString().equals("")) {

            while (checkwork == 0) {
                if (language.get(langtotrans).language.equals(outranslang.getText().toString())) {
                    checkwork = 1;
                } else
                    langtotrans++;
            }

            if (intranslang.getText().toString().equals("Определение")) {
                lang = (language.get(langtotrans).lang);
            } else {
                checkwork = 0;
                while (checkwork == 0) {
                    if (language.get(langtotrans1).language.equals(intranslang.getText().toString())) {
                        checkwork = 1;
                    } else
                        langtotrans1++;
                }
                lang = (language.get(langtotrans1).lang) + "-" + (language.get(langtotrans).lang);
            }
            langch = lang;
            translated = transtext.getText().toString();
            Request request = new Request.Builder()
                    .url("https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170315T201529Z.d15600fa87da8a7a.fbd42ba86db793ee772d23843d0b8a05126d0143" +
                            "&text=" + translated +
                            "&lang=" + lang +
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
                                String code = jObject.getString("code");
                                if (code.equals("200")) {
                                    String lang = jObject.getString("lang");
                                    int ilang = 0, i = 0;

                                    ilang = lang.indexOf("-");
                                    String sublang = lang;
                                    lang = lang.substring(0, ilang);
                                    String text = jObject.getString("text");
                                    text = text.substring(2, text.length() - 2);
                                    if (!lang.equals(langch)) {
                                        historytranslate.add(new history_translate(translated, text, sublang));
                                        work_write_data(translated, text, sublang);
                                        rv.getAdapter().notifyDataSetChanged();
                                        translatedtext.setText(text);
                                    }
                                    ilang = 0;
                                    while (ilang == 0) {
                                        if (lang.equals(language.get(i).lang)) {
                                            lang = language.get(i).language;
                                            ilang = 1;
                                        } else i++;
                                    }
                                    intranslang.setText(lang);
                                } else
                                    Toast.makeText(MainActivity.this, "Нет соеденения или ключ не активен", Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            });
        }
    }

    public void createPopupMenu() {

        final Button intranslang = (Button) findViewById(R.id.intranslang);
        final Button outranslang = (Button) findViewById(R.id.outranslang);

        final PopupMenu popup = new PopupMenu(MainActivity.this, intranslang);
        popup.getMenuInflater().inflate(R.menu.popupmenu, popup.getMenu());

        popup.getMenu().add("Определение");

        for (int i = 0; i < language.size(); i++) {
            popup.getMenu().add(language.get(i).language);
        }

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                intranslang.setText(item.getTitle());
                translate();
                return true;
            }
        });


        View.OnClickListener inlanguage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        };

        final PopupMenu popup1 = new PopupMenu(MainActivity.this, outranslang);
        popup1.getMenuInflater().inflate(R.menu.popupmenu, popup1.getMenu());
        for (int i = 0; i < language.size(); i++) {
            popup1.getMenu().add(language.get(i).language);
        }

        //registering popup with OnMenuItemClickListener
        popup1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                outranslang.setText(item.getTitle());
                translate();
                return true;
            }
        });

        final View.OnClickListener outlanguage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup1.show();
            }
        };

        intranslang.setOnClickListener(inlanguage);
        outranslang.setOnClickListener(outlanguage);

    }

    private void initializeAdapter() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        historytranslate = new ArrayList<>();
        Adapter_History adapter = new Adapter_History(historytranslate);
        rv.setAdapter(adapter);
    }

}
