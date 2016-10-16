package com.example.mike.tasklist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ReminderFragment extends Fragment{
    public static final String EXTRA_REMINDER = "com.example.mike.tasklist.reminder";
    public static final String EXTRA_TASK_ID = "com.example.mike.tasklist.task_id";

    private static final String DIALOG_DATE = "date";
    private static final String DIALOG_TIME = "time";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;

    private Date mDate;
    private Button mDateButton;
    private Button mTimeButton;

    public static ReminderFragment newInstance(UUID taskId){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TASK_ID, taskId);
        ReminderFragment fragment = new ReminderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID taskId = (UUID) getArguments().getSerializable(EXTRA_TASK_ID);
        Task task = TaskLab.get(getActivity()).getTask(taskId);
        if (task.getReminder() != null){
            mDate = task.getReminder();
        } else {
            mDate = task.getDate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reminder, container, false);

        mDateButton = (Button)v.findViewById(R.id.reminder_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mDate, getString(R.string.reminder_date_label));
                dialog.setTargetFragment(ReminderFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mTimeButton = (Button)v.findViewById(R.id.reminder_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mDate, getString(R.string.reminder_time_label));
                dialog.setTargetFragment(ReminderFragment.this, REQUEST_TIME);
                dialog.show(fm, DIALOG_TIME);
            }
        });

        Button buttonOk = (Button)v.findViewById(R.id.reminder_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra(EXTRA_REMINDER, mDate);
                getActivity().setResult(Activity.RESULT_OK, i);
                getActivity().finish();
            }
        });

        Button buttonCancel = (Button)v.findViewById(R.id.reminder_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });

        return v;
    }

    private void updateDate(){
        SimpleDateFormat format1 = new SimpleDateFormat("d MMMM yyyy, E");
        mDateButton.setText(format1.format(mDate));
    }

    private void updateTime(){
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm");
        mTimeButton.setText(format1.format(mDate));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_DATE:
                mDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                updateDate();
                break;
            case REQUEST_TIME:
                mDate = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                updateTime();
                break;
        }
    }
}
