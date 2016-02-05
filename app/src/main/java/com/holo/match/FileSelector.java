package com.holo.match;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.holo.match.fragment.AppSelectorFragment;
import com.holo.match.fragment.FileSelectorFragment;
import com.holo.match.fragment.ImagesSelectorFragment;
import com.holo.match.fragment.MusicSelectorFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class FileSelector extends AppCompatActivity {
    int tabs[] = {
            R.string.file_selector_app,
            R.string.file_selector_images,
            R.string.file_selector_music,
            R.string.file_selector_file
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewGroup tab = (ViewGroup) findViewById(R.id.file_selector_tab);
        tab.addView(LayoutInflater.from(this).inflate(R.layout.tab_file_selector_header, tab, false));
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_container);
        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);

        FragmentPagerItems pages = new FragmentPagerItems(this);
//        for (int titleResId : tabs) {
            pages.add(FragmentPagerItem.of(getString(tabs[0]), AppSelectorFragment.class));
            pages.add(FragmentPagerItem.of(getString(tabs[1]), ImagesSelectorFragment.class));
            pages.add(FragmentPagerItem.of(getString(tabs[2]), MusicSelectorFragment.class));
            pages.add(FragmentPagerItem.of(getString(tabs[3]), FileSelectorFragment.class));
//        }

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), pages);
        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case  R.id.action_settings:
                return true;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }
}
