package com.example.doctorapointment;

import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorListAdapter extends FirebaseRecyclerAdapter<Doctor,DoctorListAdapter.DoctorListViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public DoctorListAdapter(@NonNull FirebaseRecyclerOptions<Doctor> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull DoctorListViewHolder holder, int position, @NonNull Doctor model) {
        holder.name.setText(model.getUserName());
        holder.specialist.setText(model.getUserAddress());
        holder.email.setText(model.getUserEmail());

        Glide.with(holder.imgDoctor.getContext())
                .load(model.getUserImage())
                .placeholder(R.drawable.common_google_signin_btn_icon_dark)
                .circleCrop()
                .error(R.drawable.common_google_signin_btn_icon_dark_normal)
                .into(holder.imgDoctor);
    }

    @NonNull
    @Override
    public DoctorListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        return new DoctorListViewHolder(view);
    }

    class DoctorListViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imgDoctor;
        TextView name, specialist, email;

        public DoctorListViewHolder(@NonNull View itemView){
            super(itemView);

            imgDoctor = (CircleImageView) itemView.findViewById(R.id.imageDoctor);
            name = (TextView) itemView.findViewById(R.id.tvDoctorName);
            specialist= (TextView) itemView.findViewById(R.id.tvSpecialist);
            email = (TextView) itemView.findViewById(R.id.tvEmail);
        }
    }
    /*Context context;
    ArrayList<Doctor>list;

    public DoctorListAdapter(Context context, ArrayList<Doctor> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DoctorListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item,parent,false);
        return new DoctorListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorListViewHolder holder, int position) {
        Doctor doctor = list.get(position);
        holder.userName.setText(doctor.getUserName());
        holder.userAddress.setText(doctor.getUserAddress());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DoctorListViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userAddress;

        public DoctorListViewHolder(@NonNull View itemView){
           super(itemView);

            userName = itemView.findViewById(R.id.tvDoctorName);
            userAddress = itemView.findViewById(R.id.tvSpecialist);
            //experience = itemView.findViewById(R.id.tvExperience);
        }
    }*/
}
