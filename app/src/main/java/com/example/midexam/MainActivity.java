package com.example.midexam;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper db;
    ListView listView;
    FloatingActionButton fabAdd;
    ArrayList<StudentModel> studentList;
    StudentAdapter adapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DatabaseHelper(this);
        listView = findViewById(R.id.listViewStudents);
        fabAdd = findViewById(R.id.fabAdd);

        loadData();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFormDialog(false, "", "", "", 0);
            }
        });
    }

    private void loadData() {
        studentList = new ArrayList<>();
        Cursor cursor = db.getAllStudents();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String studentId = cursor.getString(1);
                String name = cursor.getString(2);
                int enrolled = cursor.getInt(3);
                studentList.add(new StudentModel(id, studentId, name, enrolled));
            }
        }

        adapter = new StudentAdapter(this, studentList);
        listView.setAdapter(adapter);
    }

    private void showFormDialog(boolean isEdit, String primaryKeyId, String currentStudentId, String currentName, int currentEnrolled) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_form, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etStudentName);
        EditText etStudentId = dialogView.findViewById(R.id.etStudentId);
        CheckBox cbEnrolled = dialogView.findViewById(R.id.cbHasEnrolled);

        if (isEdit) {
            etStudentId.setText(currentStudentId);
            etName.setText(currentName);
            cbEnrolled.setChecked(currentEnrolled == 1);
        }

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = etName.getText().toString().trim();
                String studentId = etStudentId.getText().toString().trim();
                int enrolled = cbEnrolled.isChecked() ? 1 : 0;

                if (name.isEmpty() || studentId.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEdit) {
                    boolean success = db.updateStudent(Integer.parseInt(primaryKeyId), studentId, name, enrolled);
                    if (success) {
                        Toast.makeText(MainActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(MainActivity.this, "Update failed. Student ID might already exist.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    boolean success = db.insertStudent(studentId, name, enrolled);
                    if (success) {
                        Toast.makeText(MainActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(MainActivity.this, "Error: Student ID already inserted", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public static class StudentModel {
        String id, studentId, name;
        int enrolled;

        public StudentModel(String id, String studentId, String name, int enrolled) {
            this.id = id;
            this.studentId = studentId;
            this.name = name;
            this.enrolled = enrolled;
        }
    }

    public class StudentAdapter extends ArrayAdapter<StudentModel> {
        public StudentAdapter(Context context, ArrayList<StudentModel> students) {
            super(context, 0, students);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.student_item, parent, false);
            }

            StudentModel student = getItem(position);

            TextView tvName = convertView.findViewById(R.id.tvName);
            TextView tvStudentId = convertView.findViewById(R.id.tvStudentId);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);
            Button btnView = convertView.findViewById(R.id.btnView);
            Button btnEdit = convertView.findViewById(R.id.btnEdit);
            Button btnDelete = convertView.findViewById(R.id.btnDelete);

            tvName.setText(student.name);
            tvStudentId.setText("ID: " + student.studentId);

            if (student.enrolled == 1) {
                tvStatus.setText("Enrolled");
                tvStatus.setBackgroundColor(Color.parseColor("#D1E7DD"));
                tvStatus.setTextColor(Color.parseColor("#0F5132"));
            } else {
                tvStatus.setText("Not Enrolled");
                tvStatus.setBackgroundColor(Color.parseColor("#F8D7DA"));
                tvStatus.setTextColor(Color.parseColor("#842029"));
            }

            btnView.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Student Details")
                        .setMessage("Name: " + student.name + "\nStudent ID: " + student.studentId + "\nStatus: " + (student.enrolled == 1 ? "Enrolled" : "Not Enrolled"))
                        .setPositiveButton("OK", null)
                        .show();
            });

            btnEdit.setOnClickListener(v -> {
                showFormDialog(true, student.id, student.studentId, student.name, student.enrolled);
            });

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Student")
                        .setMessage("Are you sure you want to delete this student data?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            db.deleteStudent(student.id);
                            Toast.makeText(MainActivity.this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                            loadData();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

            return convertView;
        }
    }
}