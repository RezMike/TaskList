package com.example.mike.tasklist;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.graphics.BitmapCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

public class TaskFragment extends Fragment {
    public static final String EXTRA_TASK_ID = "com.example.mike.tasklist.task_id";

    private static final String DIALOG_DATE = "date";
    private static final String DIALOG_TIME = "time";
    private static final String DIALOG_IMAGE = "image";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_IMAGE = 3;
    private static final int REQUEST_REMINDER = 4;

    private Task mTask;
    private File mPhotoFile;
    private EditText mTitleField;
    private EditText mDescriptionField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mReminderCheckBox;
    private TextView mReminderText;
    private Button mReminderButton;
    private Button mReminderDeleteButton;
    private ImageButton mPhotoButton;
    private ImageButton mImageButton;
    private ImageView mPhotoView;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onTaskUpdated(Task task);
        void onDeleteTask(Task task);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static TaskFragment newInstance(UUID taskId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TASK_ID, taskId);
        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void updateTask() {
        if (mTask.getReminder() != null) {
            mTask.setHasReminder(NotificationService.checkAlarm(mTask.getReminder().getTime()));
        }
        TaskLab.get(getActivity()).updateTask(mTask);
        mCallbacks.onTaskUpdated(mTask);
    }

    private void updateDate() {
        mDateButton.setText(mTask.getDateToString());
    }

    private void updateTime() {
        mTimeButton.setText(mTask.getTimeToString());
    }

    private void updatePhotoView(int width, int height) {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setEnabled(false);
        } else {
            if (width != 0 && height != 0) {
                Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), width, height);
                mPhotoView.setImageBitmap(bitmap);
            } else {
                Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
                mPhotoView.setImageBitmap(bitmap);
            }
            mPhotoView.setEnabled(true);
        }
        updateTask();
    }

    private void updateReminder() {
        mReminderDeleteButton.setVisibility(View.VISIBLE);
        String reminderString = getString(R.string.task_reminder_string) + mTask.getTimeReminderToString();
        mReminderText.setText(reminderString);
        mReminderText.setVisibility(View.VISIBLE);
        mReminderButton.setEnabled(true);
        mReminderButton.setText(R.string.task_reset_reminder_label);
        mReminderDeleteButton.setEnabled(true);
    }

    private void cleanReminder() {
        mReminderText.setVisibility(View.GONE);
        mReminderDeleteButton.setVisibility(View.GONE);
        mReminderButton.setText(R.string.task_add_reminder_label);
        mReminderButton.setEnabled(false);
        mTask.setHasReminder(false);
        mReminderCheckBox.setChecked(false);
    }

    private void setReminder(boolean add) {
        Intent intent = new Intent(getActivity(), NotificationService.class);
        intent.putExtra(NotificationService.DATE, mTask.getReminder().getTime());
        if (add) {
            intent.addFlags(NotificationService.ADD);
        } else {
            intent.addFlags(NotificationService.DELETE);
        }
        getActivity().startService(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID taskId = (UUID) getArguments().getSerializable(EXTRA_TASK_ID);
        mTask = TaskLab.get(getActivity()).getTask(taskId);
        if (mTask != null) mPhotoFile = TaskLab.get(getActivity()).getPhotoFile(mTask);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTitleField = (EditText) v.findViewById(R.id.task_title);
        mTitleField.setText(mTask.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mTask.setTitle(c.toString());
                updateTask();
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable c) {

            }
        });

        mDescriptionField = (EditText) v.findViewById(R.id.task_description);
        mDescriptionField.setText(mTask.getDescription());
        mDescriptionField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mTask.setDescription(c.toString());
                updateTask();
            }

            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable c) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.task_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mTask.getDate(), getString(R.string.task_date_label));
                dialog.setTargetFragment(TaskFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.task_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mTask.getTime(), getString(R.string.task_time_label));
                dialog.setTargetFragment(TaskFragment.this, REQUEST_TIME);
                dialog.show(fm, DIALOG_TIME);
            }
        });

        mReminderButton = (Button) v.findViewById(R.id.task_reminder_button);
        mReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ReminderActivity.class);
                i.putExtra(EXTRA_TASK_ID, mTask.getId());
                startActivityForResult(i, REQUEST_REMINDER);
            }
        });

        mReminderDeleteButton = (Button) v.findViewById(R.id.task_reminder_delete);
        mReminderDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReminder(false);
                mTask.setReminder(null);
                cleanReminder();
                updateTask();
            }
        });

        mReminderText = (TextView) v.findViewById(R.id.task_reminder_text);
        if (mTask.getReminder() != null) {
            mTask.setHasReminder(NotificationService.checkAlarm(mTask.getReminder().getTime()));
        }

        mReminderCheckBox = (CheckBox) v.findViewById(R.id.task_reminder);
        mReminderCheckBox.setChecked(mTask.hasReminder());
        mReminderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTask.setHasReminder(isChecked);
                if (isChecked) {
                    mReminderText.setTextColor(Color.BLACK);
                    mReminderButton.setEnabled(true);
                    mReminderDeleteButton.setEnabled(true);
                } else {
                    mReminderText.setTextColor(Color.GRAY);
                    mReminderButton.setEnabled(false);
                    mReminderDeleteButton.setEnabled(false);
                }
                if (mTask.getReminder() != null) {
                    setReminder(isChecked);
                }
                updateTask();
            }
        });

        if (mTask.hasReminder() && mTask.getReminder() != null) {
            updateReminder();
            mReminderText.setTextColor(Color.BLACK);
        } else {
            if (mTask.getReminder() != null) {
                updateReminder();
            } else {
                cleanReminder();
            }
            mReminderText.setTextColor(Color.GRAY);
            mReminderButton.setEnabled(false);
            mReminderDeleteButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton)v.findViewById(R.id.task_photoButton);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getActivity().getPackageManager();
                boolean canTakePhoto = captureImage.resolveActivity(packageManager) != null;
                if (canTakePhoto && mPhotoFile != null) {
                    Uri uri = Uri.fromFile(mPhotoFile);
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mImageButton = (ImageButton)v.findViewById(R.id.task_imageButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryImage = new Intent(Intent.ACTION_PICK);
                galleryImage.setType("image/*");
                startActivityForResult(galleryImage, REQUEST_IMAGE);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.task_imageView);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                ImageFragment.newInstance(mPhotoFile.getPath()).show(fm, DIALOG_IMAGE);
            }
        });
        registerForContextMenu(mPhotoView);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.menu_item_delete_task_fragment:
                mCallbacks.onDeleteTask(mTask);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.task_photo_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_photo:
                mTask.deletePhoto(getActivity());
                PictureUtils.cleanImageView(mPhotoView);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTask.getReminder() == null) {
            mTask.setHasReminder(false);
            updateTask();
        } else {
            TaskLab.get(getActivity()).updateTask(mTask);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        PictureUtils.cleanImageView(mPhotoView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REMINDER && resultCode != Activity.RESULT_OK && mTask.getReminder() == null) {
            cleanReminder();
        }
        if (resultCode != Activity.RESULT_OK) {
            updateTask();
            return;
        }
        switch (requestCode) {
            case REQUEST_DATE:
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mTask.setDate(date);
                updateDate();
                updateTask();
                break;
            case REQUEST_TIME:
                Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                mTask.setTime(time);
                updateTime();
                updateTask();
                break;
            case REQUEST_PHOTO:
                updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
                break;
            case REQUEST_IMAGE:
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = Media.getBitmap(getActivity().getContentResolver(), imageUri);
                    saveBitmap(bitmap);
                    updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_REMINDER:
                Date reminder = (Date) data.getSerializableExtra(ReminderFragment.EXTRA_REMINDER);
                mTask.setReminder(reminder);
                mTask.setHasReminder(true);
                setReminder(true);
                updateReminder();
                updateTask();
                break;
        }
    }

    private void saveBitmap(Bitmap bitmap) {
        try {
            mTask.deletePhoto(getActivity());
            OutputStream stream = new FileOutputStream(mPhotoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}