package com.lfcounago.gastoscompartidos.core;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lfcounago.gastoscompartidos.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.MemberViewHolder> {
    private List<User> memberList;
    private FirebaseFirestore fStore;
    private OnDataLoadedListener onDataLoadedListener;


    public UserRecyclerViewAdapter(List<User> memberList) {

        this.memberList = memberList;
        //this.onDataLoadedListener = onDataLoadedListener;

        // Llamada a esta función para cargar los IDs de saldo y luego actualizar la interfaz de usuario
        //loadBalanceIdsForUsers(memberList, onDataLoadedListener);

    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new MemberViewHolder(view);
    }

    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = memberList.get(position);
        holder.tvMemberName.setText(member.getUserName());

        //Configurar el saldo directamente desde el objeto User
        holder.setBalance(member.getTotalBalance());

        // Restablecer los márgenes para evitar problemas de reciclaje
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setMargins(0, 8, 0, 8);
        holder.itemView.setLayoutParams(layoutParams);
    }


    public interface OnDataLoadedListener {
        void onDataLoaded();
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        TextView tvMemberBalance;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvUserName);
            tvMemberBalance = itemView.findViewById(R.id.tvUserBalance);
        }

        public void setBalance(double totalBalance){
            if (totalBalance > 0) {
                tvMemberBalance.setText(String.valueOf(totalBalance));
                tvMemberBalance.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
            } else if (totalBalance < 0) {
                tvMemberBalance.setText(String.valueOf(totalBalance));
                tvMemberBalance.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                tvMemberBalance.setText("Sin deudas ni créditos");
            }
        }
    }
}
