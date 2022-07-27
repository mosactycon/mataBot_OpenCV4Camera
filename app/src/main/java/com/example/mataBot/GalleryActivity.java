package com.example.mataBot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    ViewPager mviewPager;
    ArrayList<String> filepath=new ArrayList<>();
    ViewPageAdapter viewPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        File folder=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/mataBot");

        createFileArray(folder);

        mviewPager=(ViewPager)findViewById(R.id.viewPagerMain);
        viewPageAdapter=new ViewPageAdapter(GalleryActivity.this,filepath);
        mviewPager.setAdapter(viewPageAdapter);
    }

    private void createFileArray(File folder) {
        File listFile[]=folder.listFiles();
        if(listFile != null){
            for (int i = 0;i<listFile.length;i++){
                filepath.add(listFile[i].getAbsolutePath());
            }
        }
    }
}