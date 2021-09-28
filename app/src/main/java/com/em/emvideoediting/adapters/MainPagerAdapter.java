package com.em.emvideoediting.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.em.emvideoediting.ui.AboutAppFragment;
import com.em.emvideoediting.ui.MergeFragment;
import com.em.emvideoediting.ui.TrimFragment;
import com.em.emvideoediting.ui.WaterMarkFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentManager fragmentManager,
                            @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return TrimFragment.newInstance(null, null);
            case 1:
                return MergeFragment.newInstance(null, null);
            case 2:
                return WaterMarkFragment.newInstance(null, null);
            case 3:
                return AboutAppFragment.newInstance(null, null);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
