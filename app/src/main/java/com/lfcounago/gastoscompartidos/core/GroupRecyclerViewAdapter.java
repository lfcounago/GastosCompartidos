package com.lfcounago.gastoscompartidos.core;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lfcounago.gastoscompartidos.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class GroupRecyclerViewAdapter extends RecyclerView.Adapter<GroupRecyclerViewAdapter.GroupViewHolder> {
    private List<Group> groupList;
    private UserRecyclerViewAdapter.OnDataLoadedListener onDataLoadedListener;

    public GroupRecyclerViewAdapter(List<Group> groupList) {

        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_balance, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.tvGroupName.setText(group.getGroupName());

        // Configura el RecyclerView interno para los miembros del grupo
        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.rvGroupMembers.getContext());
        holder.rvGroupMembers.setLayoutManager(layoutManager);

        UserRecyclerViewAdapter memberAdapter = new UserRecyclerViewAdapter(group.getUsers());
        holder.rvGroupMembers.setAdapter(memberAdapter);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        RecyclerView rvGroupMembers;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            rvGroupMembers = itemView.findViewById(R.id.rvGroupUsers);
        }
    }

    // Interfaz de devolución de llamada para la lógica después de cargar los datos
    public interface OnDataLoadedListener {
        void onDataLoaded();
    }

}
