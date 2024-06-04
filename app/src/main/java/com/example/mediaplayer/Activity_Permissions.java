package com.example.mediaplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mediaplayer.Streaming.MainStreaming;

public class Activity_Permissions extends AppCompatActivity {

    Button allow_btn;
    public static final int STORAGE_PERMISSION=1;
    public static final int REQUEST_PERMISSION_SETTING=12;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        preferences=getSharedPreferences("AllowAccess",MODE_PRIVATE);
        String value=preferences.getString("Allow","");//taking value from shared preferences
        if(value.equals("OK")){
            //navigate to next screen
        }
        else{
            //again this screen only
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Allow","OK");
        editor.apply();
        allow_btn=findViewById(R.id.allow_access);
        allow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    startActivity(new Intent(Activity_Permissions.this, MainStreaming.class));
                    finish();
                }
                else{
                    ActivityCompat.requestPermissions(Activity_Permissions.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
                }
              //  Toast.makeText(Activity_Permissions.this, "Allow Access", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==STORAGE_PERMISSION){
            for(int i=0;i<permissions.length;i++){
                String per = permissions[i];
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    boolean showRationale=shouldShowRequestPermissionRationale(per);
                    if(!showRationale){


                        //user clicked on never ask means
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("App Permission")
                                .setMessage("For Playing videos,music ,you must allow this"
                                +"\n\n"+"Now follow the below steps"+"\n\n"+
                                        "Open Settings from below button"+"\n"
                                +"Click on Permissions" + "\n"+ "Allow access for storage")
                                .setPositiveButton("open Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                                        intent.setData(uri);
                                        startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
                                    }
                                }).create().show();


                    }
                    else{
                        //user clicked on deny
                        ActivityCompat.requestPermissions(Activity_Permissions.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
                    }
                }
                else{
                    startActivity(new Intent(Activity_Permissions.this,MainStreaming.class));
                    finish();
                }
            }
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            startActivity(new Intent(Activity_Permissions.this,MainActivity.class));
            finish();
        }
    }
}