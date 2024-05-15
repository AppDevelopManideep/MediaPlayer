package com.example.mediaplayer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mediaplayer.folders.folder_music;
import com.example.mediaplayer.folders.folder_video;


public class TabPagerAdapter extends FragmentStateAdapter {

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new radio();
            case 1:
                return new folder_music();
            case 2:
                return new folder_video();
            default:
                return new radio();
        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

