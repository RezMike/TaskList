package com.example.mike.tasklist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.UUID;

public class TaskPagerActivity extends FragmentActivity implements TaskFragment.Callbacks{
    private ViewPager mViewPager;
    private ArrayList<Task> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.ViewPager);
        setContentView(mViewPager);

        mTasks = TaskLab.get(this).getTasks();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mTasks.size();
            }

            @Override
            public Fragment getItem(int position) {
                Task task = mTasks.get(position);
                return TaskFragment.newInstance(task.getId());
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            public void onPageScrollStateChanged(int state){

            }

            public void onPageScrolled(int pos, float posOffset, int posOffsetPixels){

            }

            public void onPageSelected(int pos){
                Task task = mTasks.get(pos);
                if (task.getTitle().equals("")) {
                    setTitle(R.string.task_no_title);
                } else {
                    setTitle(task.getTitle());
                }
            }
        });

        UUID taskId = (UUID) getIntent().getSerializableExtra(TaskFragment.EXTRA_TASK_ID);
        for (int i = 0; i < mTasks.size(); i++) {
            Task task = mTasks.get(i);
            if (task.getId().equals(taskId)){
                mViewPager.setCurrentItem(i);
                if (task.getTitle().equals("")) {
                    setTitle(R.string.task_no_title);
                } else {
                    setTitle(task.getTitle());
                }
                break;
            }
        }
    }

    @Override
    public void onTaskUpdated(Task task) {

    }

    @Override
    public void onDeleteTask(Task task) {
        TaskLab.get(this).deleteTask(task, this);
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
