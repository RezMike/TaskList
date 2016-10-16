package com.example.mike.tasklist;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class ReminderActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        UUID taskId = (UUID)getIntent().getSerializableExtra(TaskFragment.EXTRA_TASK_ID);
        return ReminderFragment.newInstance(taskId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.reminder_label);
    }
}
