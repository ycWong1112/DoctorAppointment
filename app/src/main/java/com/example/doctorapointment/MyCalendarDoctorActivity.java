package com.example.doctorapointment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyCalendarDoctorActivity extends AppCompatActivity {
    public EditText PickDate;
    public EditText PickTime;
    private Button SetSlotBtn;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private TimeSlotAdapter timeSlotAdapter;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_calendar_doctor);

        progressDialog = new ProgressDialog(this);
        PickDate = findViewById(R.id.etPickDate);
        PickTime = findViewById(R.id.etPickTime);
        SetSlotBtn = findViewById(R.id.btnsetSlot);
        recyclerView = findViewById(R.id.recycle_time_slot1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int min = calendar.get(Calendar.MINUTE);

        FirebaseRecyclerOptions<TimeSlot> options =
                new FirebaseRecyclerOptions.Builder<TimeSlot>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("User Type").child(FirebaseAuth.getInstance().getUid()).child("Time Slot"),TimeSlot.class)
                        .build();
        timeSlotAdapter = new TimeSlotAdapter(options);

        recyclerView.setAdapter(timeSlotAdapter);

        final DatabaseReference myRef = firebaseDatabase.getReference("User Type").child(firebaseAuth.getUid()).child("TimeSlot");
        SetSlotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Waiting for save");
                progressDialog.show();
                if(!PickDate.getText().toString().matches("")|| !PickTime.getText().toString().matches("")) {
                    DatabaseReference databaseReference = myRef.push();
                    databaseReference.child("date").setValue(PickDate.getText().toString());
                    databaseReference.child("time").setValue(PickTime.getText().toString());
                    databaseReference.child("availableSlot").setValue(true);
                    databaseReference.child("patientName").setValue("");
                    progressDialog.dismiss();
                }
                else{
                    Toast.makeText(getApplication(),"Please fill in the emoty",Toast.LENGTH_SHORT).show();
                }
            }
        });

        PickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MyCalendarDoctorActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month);
                        c.set(Calendar.DAY_OF_MONTH, day);
                        String format = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
                        PickDate.setText(format);
                    }
                }, year, month, day);
            datePickerDialog.show();
            }
        });
        PickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MyCalendarDoctorActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int min) {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY,hour);
                        c.set(Calendar.MINUTE, min);
                        //picked time for 30 mins slot
                        String format = new SimpleDateFormat("h:mm a").format(c.getTime());
                        long halfH = c.getTimeInMillis() + 1800000;
                        String format1 = new SimpleDateFormat("h:mm a").format(halfH);
                        PickTime.setText(format + "-" + format1);
                    }
                }, hour, min,false);
                timePickerDialog.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        timeSlotAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        timeSlotAdapter.stopListening();
    }
}

