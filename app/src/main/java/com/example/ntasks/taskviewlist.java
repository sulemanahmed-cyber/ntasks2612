package com.example.ntasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class taskviewlist extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference dbref;
    MyAdapter myAdapter;
    ArrayList<Userlist> list;

    String assignerdbf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskviewlist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the notification channel
        createNotificationChannel();

        assignerdbf = getCurrentUser();
        Log.d("TaskData", "Name: " + assignerdbf);
        recyclerView = findViewById(R.id.taskrecy);
        dbref = FirebaseDatabase.getInstance().getReference("Taskdata");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        myAdapter = new MyAdapter(this, list);
        recyclerView.setAdapter(myAdapter);

        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Userlist userlist) {
                openTaskDetailsActivity(userlist);
            }
        });

        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String currentUserName = currentUser.getDisplayName();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String assignedUserdb = dataSnapshot.child("assignedUserdb").getValue(String.class);

                        if (assignedUserdb != null && assignedUserdb.equals(currentUserName)) {
                            String taskID = dataSnapshot.getKey();
                            String taskName = dataSnapshot.child("taskNamedb").getValue(String.class);
                            String taskDesc = dataSnapshot.child("taskDescriptiondb").getValue(String.class);
                            String priority = dataSnapshot.child("prioritydb").getValue(String.class);
                            String deadline = dataSnapshot.child("deadlinedb").getValue(String.class);
                            String statusdb = dataSnapshot.child("statusdb").getValue(String.class);
                            assignedUserdb = dataSnapshot.child("assignedUserdb").getValue(String.class);
                            String assignerdb = dataSnapshot.child("assignerdb").getValue(String.class);

                            // Create an instance of TaskModel
                            TaskModel taskModel = new TaskModel(taskID, taskName, taskDesc, priority, deadline, statusdb, assignedUserdb, assignerdb);

                            Userlist userlist = new Userlist();
                            userlist.setTaskID(taskID);
                            userlist.setTaskName(taskName);
                            userlist.setTaskDesc(taskDesc);
                            userlist.setTaskprio(priority);
                            userlist.setTaskdeadl(deadline);
                            userlist.setStatusdb(statusdb);
                            userlist.setAssignedUserdb(assignedUserdb);
                            userlist.setAssignerdb(assignerdb);
                            list.add(0, userlist);

                            // Example date string, replace with your actual date string
                            String dateString = "2023-12-25T17:03:30Z";

                            // Retrieve the Date object from the TaskModel instance
                            Date date = taskModel.convertStringToDate(dateString);

                            // Set the time part to 1:35 PM
                            ///date = setTimeTo1_35(date);

                            // Schedule a notification for each task based on its date
                            if (date != null && date.after(new Date())) {
                                String title = taskModel.getTaskName();
                                String message = taskModel.getTaskDescription();

                                // Call sendImmediateNotification with the context
                                Toast.makeText(taskviewlist.this, "1144", Toast.LENGTH_SHORT).show();
                                NotificationScheduler.sendImmediateNotification(taskviewlist.this, title, message);
                            }
                        }
                    }

                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data", error.toException());
                Toast.makeText(taskviewlist.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void openTaskDetailsActivity(Userlist userlist) {
        Intent intent = new Intent(taskviewlist.this, TaskDetailsActivity.class);
        intent.putExtra("task", new TaskModel(userlist.getTaskID(), userlist.getTaskName(), userlist.getTaskDesc(), userlist.getTaskprio(), userlist.getTaskdeadl(), userlist.getStatusdb(), userlist.getAssignedUserdb(), userlist.getAssignerdb()));
        startActivity(intent);
    }

    private Date setTimeTo1_35(Date date) {
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            calendar.set(Calendar.MINUTE, 21);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        }
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Your Notification Channel";
            String description = "Channel for your app notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("taskrem", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {


                notificationManager.createNotificationChannel(channel);

                Log.d("Notification", "Channel created successfully"); // Add this log statement

            }
        }
    }

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
}
