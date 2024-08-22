package com.example.mediaplayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity  {
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    TabPagerAdapter viewPageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout=findViewById(R.id.tablayout);
        viewPager2 = findViewById(R.id.viewPager);
        viewPageAdapter=new TabPagerAdapter(this);//tab adapter class to operate based on positions
        viewPager2.setAdapter(viewPageAdapter);
        //when tab is selected

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //when u selected tab
                viewPager2.setCurrentItem(tab.getPosition());

                tab.getCustomView().findViewById(R.id.tabBackground).setBackgroundColor(getResources().getColor(R.color.selected_tab_color));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //for deselect tab
                tab.getCustomView().findViewById(R.id.tabBackground).setBackgroundColor(getResources().getColor(R.color.unselected_tab_color));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // when screen is swipping
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            //when u swipe
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

    }


}