package com.example.mediaplayer.retrofit;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaplayer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainRetrofit extends AppCompatActivity {
    ProgressDialog progressDialog;
    CustomAdapter adapter;
    RecyclerView recyclerView;
    List<JsonData> jsonDataList=new ArrayList<JsonData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_retrofit);
        progressDialog = new ProgressDialog(MainRetrofit.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();


        /*Create handle for the RetrofitInstance interface*/
       /* GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        Call<List<RetroPhoto>> call = service.getAllPhotos();

        //enqueue is used to run the thread synchronously
        call.enqueue(new Callback<List<RetroPhoto>>() {
            @Override
            public void onResponse(Call<List<RetroPhoto>> call, Response<List<RetroPhoto>> response) {
                progressDialog.dismiss();
                generateDataList(response.body());
            }

            @Override
            public void onFailure(Call<List<RetroPhoto>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainRetrofit.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }

        });

        //post
        //1,2,mani,ram,tre
        RetroPhoto retroPhoto = new RetroPhoto(123, 1, "mani", "ramu", "madhu");
        Call<String> postcall = service.sendPhotos(retroPhoto);
        postcall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainRetrofit.this, "" + response.body(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainRetrofit.this, "" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/


        //json
        JSONObject jsonObject ;
        try {
            jsonObject = new JSONObject(getJsonData(getApplicationContext().getAssets()));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            JSONArray mediaList = jsonObject.getJSONArray("media");
            generateDataList(mediaList);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    //adding json data to jsonData class
    private void generateDataList(JSONArray mediaList) throws JSONException {
        for(int i=0;i<mediaList.length();i++){
            int m  = mediaList.length();


            JsonData data=new JsonData(mediaList.getJSONObject(i).getString("title"),mediaList.getJSONObject(i).getString("image"),mediaList.getJSONObject(i).getString("source"));
            jsonDataList.add(data);
            int n =mediaList.length();

        }
        recyclerAdapter(jsonDataList);


    }

    private String getJsonData(AssetManager assets) throws IOException {
        StringBuilder buf = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(assets.open("catalog.json")));
        String str;
        while ((str = in.readLine()) != null) {
            buf.append(str);
        }
        in.close();
        return buf.toString();
    }

  /*  private void generateDataList(List<RetroPhoto> photoList) {
        recyclerView = findViewById(R.id.customRecyclerView);
        adapter = new CustomAdapter(this, photoList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainRetrofit.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }*/

    private void recyclerAdapter(List<JsonData> jsonDataList) {
        recyclerView = findViewById(R.id.customRecyclerView);
        adapter = new CustomAdapter(this, jsonDataList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainRetrofit.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


}