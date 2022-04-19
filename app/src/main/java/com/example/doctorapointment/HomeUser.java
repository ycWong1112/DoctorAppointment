package com.example.doctorapointment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class HomeUser extends AppCompatActivity {

    RecyclerView recyclerView;
    DoctorListAdapter doctorListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        recyclerView = findViewById(R.id.rvDoctorList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Doctor> options =
                new FirebaseRecyclerOptions.Builder<Doctor>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("User Type"),Doctor.class)
                        .build();

        doctorListAdapter = new DoctorListAdapter(options);
        recyclerView.setAdapter(doctorListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        doctorListAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doctorListAdapter.stopListening();
    }
}