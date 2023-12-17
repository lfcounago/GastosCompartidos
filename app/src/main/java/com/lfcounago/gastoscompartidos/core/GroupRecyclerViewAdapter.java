package com.lfcounago.gastoscompartidos.core;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lfcounago.gastoscompartidos.*;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

// Adaptador para la RecyclerView que muestra la lista de grupos
public class GroupRecyclerViewAdapter extends RecyclerView.Adapter<GroupRecyclerViewAdapter.GroupViewHolder> {
    private List<Group> groupList;
    private boolean showBalancesMode;

    public GroupRecyclerViewAdapter(List<Group> groupList, boolean showBalancesMode) {

        this.groupList = groupList;
        this.showBalancesMode = showBalancesMode;
    }

    // Método llamado cuando se necesita crear un nuevo ViewHolder
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Selecciona el diseño según el modo de visualización
        int layoutRes = showBalancesMode ? R.layout.activity_balance : R.layout.activity_liquidations;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new GroupViewHolder(view);
    }

    // Método llamado para mostrar datos en un ViewHolder específico
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.tvGroupName.setText(group.getGroupName());

        // Configura el RecyclerView interno para los miembros del grupo
        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.rvGroupMembers.getContext());
        holder.rvGroupMembers.setLayoutManager(layoutManager);

        UserRecyclerViewAdapter memberAdapter = new UserRecyclerViewAdapter(group.getUsers(), showBalancesMode);
        holder.rvGroupMembers.setAdapter(memberAdapter);

        // Si el modo de visualización es liquidaciones, verifica si hay deudas en el grupo
        if (!showBalancesMode) {
            if (holder.tvNoDebtsMessage != null) {
                //Verificar si hay deudas en el grupo
                if (memberAdapter.groupDebts()) {
                    holder.tvNoDebtsMessage.setVisibility(View.GONE);
                } else {
                    holder.tvNoDebtsMessage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // Método para actualizar el modo de visualización
    public void setShowBalancesMode(boolean showBalancesMode) {
        this.showBalancesMode = showBalancesMode;
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    // Clase interna que representa el ViewHolder para cada elemento de la RecyclerView
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        TextView tvNoDebtsMessage;
        RecyclerView rvGroupMembers;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvNoDebtsMessage = itemView.findViewById(R.id.tvNoDebtsMessage);
            rvGroupMembers = itemView.findViewById(R.id.rvGroupUsers);
        }
    }
}
