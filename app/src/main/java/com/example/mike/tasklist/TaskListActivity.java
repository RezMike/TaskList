package com.example.mike.tasklist;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.UUID;

public class TaskListActivity extends SingleFragmentActivity
        implements TaskListFragment.Callbacks, TaskFragment.Callbacks {
    public static final String PREF_DISPLAY = "display";
    private static final String OPENED_TASK_ID = "id";

    Task mOpenedTask;

    @Override
    protected Fragment createFragment() {
        return new TaskListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit()
                .putBoolean(PREF_DISPLAY, findViewById(R.id.detailFragmentContainer) != null)
                .commit();
        if (savedInstanceState != null){
            String stringId = savedInstanceState.getString(OPENED_TASK_ID);
            if (stringId != null) {
                UUID taskId = UUID.fromString(stringId);
                mOpenedTask = TaskLab.get(this).getTask(taskId);
            }
        }
        if (findViewById(R.id.detailFragmentContainer) != null) {
            if (mOpenedTask == null) {
                updateDetailFragment(new EmptyFragment());
            } else {
                updateDetailFragment(TaskFragment.newInstance(mOpenedTask.getId()));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mOpenedTask != null) {
            outState.putString(OPENED_TASK_ID, mOpenedTask.getId().toString());
        } else {
            outState.putString(OPENED_TASK_ID, null);
        }
    }

    @Override
    public void onTaskSelected(Task task) {
        if (findViewById(R.id.detailFragmentContainer) == null){
            Intent i = new Intent(this, TaskPagerActivity.class);
            i.putExtra(TaskFragment.EXTRA_TASK_ID, task.getId());
            startActivityForResult(i, 0);
        } else {
            mOpenedTask = task;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Fragment newDetail = TaskFragment.newInstance(task.getId());
                if (oldDetail != null){
                ft.remove(oldDetail);
            }
            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.commit();
        }
    }

    @Override
    public void onTaskUpdated(Task task) {
        updateUI();
    }

    @Override
    public void onDeleteTask(Task task) {
        if (findViewById(R.id.detailFragmentContainer) != null){
            TaskLab.get(this).deleteTask(task, this);
            mOpenedTask = null;
            updateDetailFragment(new EmptyFragment());
            updateUI();
        }
    }

    @Override
    public void onDeleteTasks(ArrayList<Task> deletingTasks) {
        if (findViewById(R.id.detailFragmentContainer) == null) {
            TaskLab taskLab = TaskLab.get(this);
            for (int i = 0; i < deletingTasks.size(); i++) {
                taskLab.deleteTask(deletingTasks.get(i), this);
            }
            updateUI();
        } else {
            TaskLab taskLab = TaskLab.get(this);
            for (int i = 0; i < deletingTasks.size(); i++) {
                Task task = deletingTasks.get(i);
                if (mOpenedTask != null && task.getId().equals(mOpenedTask.getId())) {
                    mOpenedTask = null;
                    updateDetailFragment(new EmptyFragment());
                }
                taskLab.deleteTask(task, this);
            }
            updateUI();
        }
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.detailFragmentContainer) != null && mOpenedTask != null){
            mOpenedTask = null;
            updateUI();
            updateDetailFragment(new EmptyFragment());
        } else {
            super.onBackPressed();
        }
    }

    private void updateDetailFragment(Fragment newFragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
        if (oldDetail != null) {
            ft.remove(oldDetail);
        }
        ft.add(R.id.detailFragmentContainer, newFragment);
        ft.commit();
    }

    public void updateUI(){
        FragmentManager fm = getSupportFragmentManager();
        TaskListFragment listFragment = (TaskListFragment) fm.findFragmentById(R.id.fragmentContainer);
        listFragment.updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUI();
    }
}
