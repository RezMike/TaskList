package com.example.mike.tasklist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class TaskListFragment extends ListFragment{
    private Callbacks mCallbacks;
    private ArrayList<Task> mDeletingTasks;

    public interface Callbacks{
        void onTaskSelected(Task task);
        void onDeleteTasks(ArrayList<Task> deletingTasks);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void updateUI(){
        ArrayList<Task> tasks = TaskLab.get(getActivity()).getTasks();
        TaskAdapter adapter = new TaskAdapter(tasks);
        setListAdapter(adapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.tasks_title);
        updateUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_task, container, false);

        Button newTaskButton = (Button)v.findViewById(R.id.new_task_button);
        newTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task task = new Task();
                TaskLab.get(getActivity()).addTask(task);
                mCallbacks.onTaskSelected(task);
                updateUI();
            }
        });

        ListView listView = (ListView)v.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.task_list_item_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_delete_task:
                        mDeletingTasks = new ArrayList<>();
                        TaskAdapter adapter = (TaskAdapter) getListAdapter();
                        for (int i = adapter.getCount()-1; i >= 0; i--) {
                            if (getListView().isItemChecked(i)) {
                                mDeletingTasks.add(adapter.getItem(i));
                            }
                        }
                        if (!mDeletingTasks.isEmpty()) {
                            mCallbacks.onDeleteTasks(mDeletingTasks);
                        }
                        mDeletingTasks = null;
                        updateUI();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setEmptyView(view.findViewById(android.R.id.empty));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_new_task:
                Task task = new Task();
                TaskLab.get(getActivity()).addTask(task);
                updateUI();
                mCallbacks.onTaskSelected(task);
                return true;
            case R.id.menu_list_information:
                Intent i = new Intent(getActivity(), InformationActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.task_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_task:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
                int position = info.position;
                TaskAdapter adapter = (TaskAdapter)getListAdapter();
                Task task = adapter.getItem(position);
                mDeletingTasks = new ArrayList<>();
                mDeletingTasks.add(task);
                mCallbacks.onDeleteTasks(mDeletingTasks);
                mDeletingTasks = null;
                updateUI();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Task c = ((TaskAdapter) getListAdapter()).getItem(position);
        mCallbacks.onTaskSelected(c);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateUI();
    }

    private class TaskAdapter extends ArrayAdapter<Task> {
        public TaskAdapter(ArrayList<Task> tasks){
            super(getActivity(), 0, tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_task, null);
            }

            Task task = getItem(position);

            TextView titleTextView = (TextView) convertView.findViewById(R.id.task_list_item_titleTextView);
            String title = task.getTitle();
            if (title.equals("")){
                title = "<Без названия>";
            }
            titleTextView.setText(title);

            TextView dateTextView = (TextView) convertView.findViewById(R.id.task_list_item_dateTextView);
            String date = task.getDateToString() + ",  " + task.getTimeToString();
            dateTextView.setText(date);

            if (task.getReminder() != null){
                task.setHasReminder(NotificationService.checkAlarm(task.getReminder().getTime()));
            }
            ImageView alarmImageView = (ImageView) convertView.findViewById(R.id.task_list_item_imageAlarmOn);
            if (task.hasReminder() && task.getReminder() != null){
                alarmImageView.setImageResource(R.mipmap.alarm_on);
            } else {
                alarmImageView.setImageDrawable(null);
            }

            TaskListActivity activity = (TaskListActivity) getActivity();
            Task openedTask = activity.mOpenedTask;
            if (getActivity().findViewById(R.id.detailFragmentContainer) == null){
                convertView.setBackground(activity.getResources().getDrawable(R.drawable.item_simple));
            } else {
                if (openedTask != null && openedTask.getId().equals(task.getId()))
                    convertView.setBackground(activity.getResources().getDrawable(R.drawable.item_opened));
                else
                    convertView.setBackground(activity.getResources().getDrawable(R.drawable.item_not_opened));
            }

            return convertView;
        }
    }
}
