package com.example.ntasks;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskModel implements Parcelable {

    private String taskID;
    private String taskName;
    private String taskDescription;
    private String priority;
    private String deadline;
    private String status;
    private String assignedUser;
    private String assignerdb;
    private String dateString;

    public TaskModel() {
        // Default constructor required for calls to DataSnapshot.getValue(TaskModel.class)
    }

    public TaskModel(String taskID, String taskName, String taskDescription, String priority, String deadline, String status, String assignedUser, String assignerdb) {
        this.taskID = taskID;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.priority = priority;
        this.deadline = deadline;
        this.status = status;
        this.assignedUser = assignedUser;
        this.assignerdb = assignerdb;
    }

    protected TaskModel(Parcel in) {
        taskID = in.readString();
        taskName = in.readString();
        taskDescription = in.readString();
        priority = in.readString();
        deadline = in.readString();
        status = in.readString();
        assignedUser = in.readString();
        assignerdb = in.readString();
        dateString = in.readString();
    }

    public static final Creator<TaskModel> CREATOR = new Creator<TaskModel>() {
        @Override
        public TaskModel createFromParcel(Parcel in) {
            return new TaskModel(in);
        }

        @Override
        public TaskModel[] newArray(int size) {
            return new TaskModel[size];
        }
    };

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public Date convertStringToDate(String dateString) {
        try {
            if (dateString != null && !dateString.isEmpty()) {
                // Assuming date string is in a specific format, adjust as needed
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                return sdf.parse(dateString);
            } else {
                // Handle the case when dateString is null or empty
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void main(String[] args) {
        // Creating an instance of TaskModel
        TaskModel taskInstance = new TaskModel();

        // Assuming you have a String date from your database
        taskInstance.setDateString("2023-01-01T12:00:00Z");

        // Use the convertStringToDate method on the instance
        // Example usage in taskviewlist class
        String dateString = "2023-12-31T12:00:00Z"; // Replace with your actual date string
        Date deadline = taskInstance.convertStringToDate(dateString);

        // Now you can use the 'deadline' variable as needed
        System.out.println("Deadline: " + deadline);
    }

    public String getAssignerdb() {
        return assignerdb;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public void setAssignerdb(String assignerdb) {
        this.assignerdb = assignerdb;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getPriority() {
        return priority;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public String getStatus() {
        return status;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskID);
        dest.writeString(taskName);
        dest.writeString(taskDescription);
        dest.writeString(priority);
        dest.writeString(deadline);
        dest.writeString(status);
        dest.writeString(assignedUser);
        dest.writeString(assignerdb);
        dest.writeString(dateString);
    }
}
