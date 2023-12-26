package com.example.ntasks;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private RadioGroup radioGroupPriority;
    private RadioButton radioButtonHigh;
    private RadioButton radioButtonMedium;
    private RadioButton radioButtonLow;
    private TextInputEditText editTextDeadline;
    private Button buttonAddTask;
    private TextInputEditText editTextTaskName;
    private TextInputEditText editTextTaskDescription;
    private Spinner spinnerAssignedUser1;
    private Spinner spinnerAssignedUser2;
    private Spinner spinnerAssignedUser3;
    private CheckBox checkBoxSendToAll;
    private Button buttonSendToAll;
    private ProgressDialog progressDialog;

    DatabaseReference taskRef;
    DatabaseReference userRef;
    private Button addbtn;

    ArrayList<String> userList = new ArrayList<>();

    ////initializations for attachments
    private static final int PICK_FILE_REQUEST = 1; // You can use any integer value
    private static final String TAG = "Main";
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    // Variable to store the file URI
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        taskRef = FirebaseDatabase.getInstance().getReference().child("Taskdata");
        userRef = FirebaseDatabase.getInstance().getReference().child("Registered Users");

        // Initialize views
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        editTextTaskName = findViewById(R.id.editTextTaskName);
        radioGroupPriority = findViewById(R.id.radioGroupPriority);
        radioButtonHigh = findViewById(R.id.radioButtonHigh);
        radioButtonMedium = findViewById(R.id.radioButtonMedium);
        radioButtonLow = findViewById(R.id.radioButtonLow);
        editTextDeadline = findViewById(R.id.editTextDeadline);
        buttonAddTask = findViewById(R.id.addTaskButton);
        spinnerAssignedUser1 = findViewById(R.id.spinnerAssignedUser1);
        spinnerAssignedUser2 = findViewById(R.id.spinnerAssignedUser2);
        spinnerAssignedUser3 = findViewById(R.id.spinnerAssignedUser3);
        checkBoxSendToAll = findViewById(R.id.checkBoxSendToAll);
        buttonSendToAll = findViewById(R.id.buttonSendToAll);
        addbtn = findViewById(R.id.btn_attach);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Task...");
        progressDialog.setCancelable(false);


        setupSpinnerWithUserNames();

        ///button for attach files
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddTaskActivity.this, "Select any type of file", Toast.LENGTH_LONG).show();

                // Use Intent to open a file picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE_REQUEST);
            }
        });

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Get a reference to the Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Get a reference to the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("Attachments");

        // Set click listener for the Deadline field
        editTextDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        checkBoxSendToAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonSendToAll.setVisibility(View.VISIBLE);
                } else {
                    buttonSendToAll.setVisibility(View.GONE);
                }
            }
        });

        buttonSendToAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTaskToAllUsers();
            }
        });

        // Set click listener for the Add Task button
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show progress dialog
                progressDialog.show();

                // Check if a file is attached
                if (fileUri != null) {
                    // Upload the file to Firebase Storage
                    uploadFileToStorage(fileUri);
                } else {
                    // No file attached, proceed to insert task data without file
                    insertTaskData();
                }
            }
        });
    }

    private void setupSpinnerWithUserNames() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> userNames = new ArrayList<>();

                userNames.add("Select User");

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Assuming each user node has a 'name' field
                    String userName = userSnapshot.child("name").getValue(String.class);
                    if (userName != null) {
                        userNames.add(userName);
                    }
                }

                // Update the spinner adapter with user names
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTaskActivity.this, android.R.layout.simple_spinner_item, userNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAssignedUser1.setAdapter(adapter);
                spinnerAssignedUser2.setAdapter(adapter);
                spinnerAssignedUser3.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private List<String> insertTaskData() {
        // Insert task data to Firebase
        String taskNamedb = editTextTaskName.getText().toString();
        String taskDescriptiondb = editTextTaskDescription.getText().toString();
        String prioritydb = getSelectedPriority();
        String deadlinedb = editTextDeadline.getText().toString();
        String assignedUserdb1 = spinnerAssignedUser1.getSelectedItem().toString();
        String assignedUserdb2 = spinnerAssignedUser2.getSelectedItem().toString();
        String assignedUserdb3 = spinnerAssignedUser3.getSelectedItem().toString();
        String assignerdb = getCurrentUser();
        String defaultStatus = "ASSIGNED";

        if (TextUtils.isEmpty(taskNamedb) || TextUtils.isEmpty(taskDescriptiondb) || TextUtils.isEmpty(prioritydb) || TextUtils.isEmpty(deadlinedb) || assignedUserdb1.equals("Select User")) {
            Toast.makeText(this, "Please fill all the necessary fields", Toast.LENGTH_SHORT).show();
            return Collections.emptyList();
        }

        List<String> taskIds = new ArrayList<>();

        // Add a new task for each selected user
        if (!assignedUserdb1.equals("Select User")) {
            taskIds.add(addTaskForUser(taskNamedb, taskDescriptiondb, prioritydb, deadlinedb, defaultStatus, assignedUserdb1));
        }
        if (!assignedUserdb2.equals("Select User")) {
            taskIds.add(addTaskForUser(taskNamedb, taskDescriptiondb, prioritydb, deadlinedb, defaultStatus, assignedUserdb2));
        }
        if (!assignedUserdb3.equals("Select User")) {
            taskIds.add(addTaskForUser(taskNamedb, taskDescriptiondb, prioritydb, deadlinedb, defaultStatus, assignedUserdb3));
        }

        Toast.makeText(AddTaskActivity.this, "Task sent!", Toast.LENGTH_SHORT).show();
        Log.d("TaskData", "Name: " + assignerdb);
        finish();

        return taskIds;
    }

    /*private void insertTaskData(Map<String, Object> fileMetadata) {
        // Insert task data to Firebase
        String taskNamedb = editTextTaskName.getText().toString();
        String taskDescriptiondb = editTextTaskDescription.getText().toString();
        String prioritydb = getSelectedPriority();
        String deadlinedb = editTextDeadline.getText().toString();
        String assignedUserdb1 = spinnerAssignedUser1.getSelectedItem().toString();
        String assignedUserdb2 = spinnerAssignedUser2.getSelectedItem().toString();
        String assignedUserdb3 = spinnerAssignedUser3.getSelectedItem().toString();
        String assignerdb = getCurrentUser();
        String defaultStatus = "ASSIGNED";

        if (TextUtils.isEmpty(taskNamedb) || TextUtils.isEmpty(taskDescriptiondb) || TextUtils.isEmpty(prioritydb) || TextUtils.isEmpty(deadlinedb) || assignedUserdb1.equals("Select User")) {
            Toast.makeText(this, "Please fill all the necessary fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add a new task for each selected user
        if (!assignedUserdb1.equals("Select User")) {
            String taskID1 = addTaskForUser(taskNamedb, taskDescriptiondb, prioritydb, deadlinedb, defaultStatus, assignedUserdb1);
        }
        if (!assignedUserdb2.equals("Select User")) {
            String taskID2 = addTaskForUser(taskNamedb, taskDescriptiondb, prioritydb, deadlinedb, defaultStatus, assignedUserdb2);
        }
        if (!assignedUserdb3.equals("Select User")) {
            String taskID3 = addTaskForUser(taskNamedb, taskDescriptiondb, prioritydb, deadlinedb, defaultStatus, assignedUserdb3);
        }

        // Add log statements to check the data
        Log.d("TaskData", "Name: " + assignerdb);

        // If file metadata is available, add it to the task data
        if (fileMetadata != null) {
            // Assuming the key in fileMetadata is "url"
            String fileUrl = (String) fileMetadata.get("url");
            // Add file URL to the task data or handle it as needed
            // For demonstration purposes, it's simply logged here
            Log.d("TaskData", "File URL: " + fileUrl);
        }

        Toast.makeText(AddTaskActivity.this, "Task sent!", Toast.LENGTH_SHORT).show();
        finish();
    }*/

    private String getSelectedPriority() {
        int selectedId = radioGroupPriority.getCheckedRadioButtonId();
        if (selectedId == R.id.radioButtonHigh) {
            return "High";
        } else if (selectedId == R.id.radioButtonMedium) {
            return "Medium";
        } else if (selectedId == R.id.radioButtonLow) {
            return "Low";
        }
        return "";
    }

    private void sendTaskToAllUsers() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userName = userSnapshot.child("name").getValue(String.class);
                    if (userName != null) {
                        userList.add(userName);
                    }
                }

                // Now userList contains all user names, including "Select User"

                for (int i = 0; i < userList.size(); i++) {
                    String userName = userList.get(i);
                    if (!userName.equals("Select User")) {
                        // Get task details
                        String taskName = editTextTaskName.getText().toString();
                        String taskDescription = editTextTaskDescription.getText().toString();
                        String priority = getSelectedPriority();
                        String deadline = editTextDeadline.getText().toString();
                        String assignedUser = userName;
                        String assignerdb = getCurrentUser();

                        if (TextUtils.isEmpty(taskName) || TextUtils.isEmpty(taskDescription) || TextUtils.isEmpty(priority) || TextUtils.isEmpty(deadline)) {
                            Toast.makeText(AddTaskActivity.this, "Please fill all the necessary fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Add task for the user
                        addTaskForUser(taskName, taskDescription, priority, deadline, "ASSIGNED", assignedUser);
                    }
                }
                Toast.makeText(AddTaskActivity.this, "Tasks sent to all users", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Toast.makeText(AddTaskActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String addTaskForUser(String taskNamedb, String taskDescriptiondb, String prioritydb,
                                  String deadlinedb, String defaultStatus, String assignedUserdb) {
        String assignerdb = getCurrentUser();
        Log.d("TaskData", "Assigner: " + assignerdb);
        DatabaseReference newTaskRef = taskRef.push();
        newTaskRef.setValue(new taskdb(taskNamedb, taskDescriptiondb, prioritydb, deadlinedb, defaultStatus, assignedUserdb, assignerdb));
        return newTaskRef.getKey();
    }

    private String getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getDisplayName(); // Assuming you have set the display name during authentication
        } else {
            // Handle the case when the user is not authenticated
            return "Unknown User";
        }
    }

    // ... (Your existing methods remain unchanged)

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Update the Deadline field with the selected date
                        String formattedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year);
                        editTextDeadline.setText(formattedDate);
                    }
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }
    // ... (Your existing methods remain unchanged)

    // ... (Your existing methods remain unchanged)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the home button click
                Intent intent = new Intent(this, Master.class);
                startActivity(intent);
                finish(); // Optional: Close the current activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    ////code for attach file out of on create
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the selected file URI
            fileUri = data.getData();
            Toast.makeText(this, "File selected: " + fileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFileToStorage(Uri fileUri) {
        // Create a storage reference
        StorageReference fileRef = storageRef.child("uploads/" + fileUri.getLastPathSegment());

        // Upload file to Firebase Storage
        UploadTask uploadTask = fileRef.putFile(fileUri);

        // Handle success or failure
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // File uploaded successfully, get download URL
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Insert task data and get list of task IDs
                List<String> taskIds = insertTaskData();

                // Update the corresponding entries in the "Attachments" node with the file URL and task IDs
                storeFileMetadata(uri.toString(), taskIds);

                // Hide progress dialog
                progressDialog.dismiss();
            });
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful upload
            Log.e(TAG, "Upload failed: " + exception.getMessage());

            // Hide progress dialog
            progressDialog.dismiss();
        });
    }

    private void storeFileMetadata(String downloadUrl, List<String> taskIds) {
        // Loop through the list of task IDs
        for (String taskId : taskIds) {
            // Create a unique key for the file in the Realtime Database
            String fileId = databaseRef.push().getKey();

            // Create a map to store metadata
            Map<String, Object> fileMetadata = new HashMap<>();
            fileMetadata.put("url", downloadUrl);
            fileMetadata.put("timestamp", System.currentTimeMillis());

            // Include the task ID in the file metadata
            fileMetadata.put("taskId", taskId);

            // Store file metadata in Realtime Database
            databaseRef.child(fileId).setValue(fileMetadata);
        }
    }

}

