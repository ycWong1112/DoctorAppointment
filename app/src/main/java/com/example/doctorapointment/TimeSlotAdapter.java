package com.example.doctorapointment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class TimeSlotAdapter extends FirebaseRecyclerAdapter<TimeSlot,TimeSlotAdapter.TimeSlotViewHolder> {

    public TimeSlotAdapter(@NonNull FirebaseRecyclerOptions<TimeSlot> options) {
        super(options);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position,@NonNull TimeSlot model) {
        holder.time.setText(model.getTime());
        holder.slotAvailable.setText(model.getAvailableSlot());
    }

    @NonNull
    @Override
    public TimeSlotAdapter.TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.time_slot,parent,false);
        return new TimeSlotViewHolder(view);
    }


    class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        TextView time,slotAvailable;

        public TimeSlotViewHolder(@NonNull View itemView){
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.timeSlot);
            slotAvailable = (TextView) itemView.findViewById(R.id.time_slot_description);
        }
    }
}
