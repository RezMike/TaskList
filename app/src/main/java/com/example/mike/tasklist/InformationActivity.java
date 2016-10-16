package com.example.mike.tasklist;

import android.support.v4.app.Fragment;

public class InformationActivity extends SingleFragmentActivity{
    @Override
    protected Fragment createFragment() {
        return new InformationFragment();
    }
}
