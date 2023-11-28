package com.lfcounago.gastoscompartidos.core;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        //Log.e("BalanceActivity", "Entramos en onBindViewHolder");

        // Verifica si la lista de IDs de saldo no está vacía
        if (member.getBalanceId() != null && !member.getBalanceId().isEmpty()) {
            // Obtén el total de deudas y créditos para este usuario en el grupo
            obtenerTotalDeudasCreditos(member.getBalanceId(), member.getUserId(),total->{
                // Muestra la información en el TextView
                if (total > 0) {
                    holder.tvMemberBalance.setText("Crédito: " + total);
                } else if (total < 0) {
                    holder.tvMemberBalance.setText("Deuda: " + Math.abs(total));
                } else {
                    holder.tvMemberBalance.setText("Sin deudas ni créditos");
                }
            });
        } else {
            holder.tvMemberBalance.setText("Sin deudas ni créditos");
        }
    }

    // Método para obtener el total de deudas y créditos para un usuario en el grupo
    private double obtenerTotalDeudasCreditos(List<String> balanceIds, String userId, OnTotalDeudasCreditosCallback callback) {
        AtomicInteger count = new AtomicInteger(balanceIds.size());
        AtomicReference<Double> total = new AtomicReference<>((double) 0);

        for (String balanceId : balanceIds) {
            fStore.collection("spends")
                    .document(balanceId)
                    .get()
                    .addOnCompleteListener(spendDocument ->{
                       if (spendDocument.isSuccessful()){
                           DocumentSnapshot spendResult = spendDocument.getResult();
                           if (spendResult.exists()){
                               //Obtener informacion del gasto
                               double amount = spendResult.getDouble("amount");
                               String payerId = spendResult.getString("payer");

                               if (userId.equals(payerId)){
                                   total.updateAndGet(v -> new Double((double) (v - amount)));
                               }else{
                                   total.updateAndGet(v -> new Double((double) (v + amount)));
                               }

                           }
                       }else{
                           Log.e("UserRecyclerViewAdapter", "No hay gasto");
                       }
                        if (count.decrementAndGet() == 0) {
                            callback.onTotalDeudasCreditos(total.get());
                        }
                    });
        }
        return total.get();

    }

    public interface OnTotalDeudasCreditosCallback {
        void onTotalDeudasCreditos(double total);
    }

    private void loadBalanceIdsForUsers(List<User> users, OnDataLoadedListener onDataLoadedListener) {
        AtomicInteger usersLoadedCount = new AtomicInteger(0);

        for (User user : users) {
            fStore.collection("spends")
                    .whereEqualTo("payer", user.getUserId())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        // Obtener los IDs de saldo y actualizar el objeto User
                        List<String> balanceIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Agregar el ID del gasto al saldo del usuario
                            balanceIds.add(document.getId());
                        }
                        user.setBalanceId(balanceIds);

                        // Incrementar el contador de usuarios cargados
                        int loadedCount = usersLoadedCount.incrementAndGet();

                        // Verificar si todos los usuarios han cargado sus IDs de saldo
                        if (loadedCount == users.size()) {
                            onDataLoadedListener.onDataLoaded();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Manejar errores al cargar los IDs de saldo
                        Log.e("UserRecyclerViewAdapter", "Error al cargar IDs de saldo", e);

                        // Incrementar el contador de usuarios cargados
                        int loadedCount = usersLoadedCount.incrementAndGet();

                        // Verificar si todos los usuarios han cargado sus IDs de saldo
                        if (loadedCount == users.size()) {
                            onDataLoadedListener.onDataLoaded();
                        }
                    });
        }
    }

    public interface OnDataLoadedListener {
        void onDataLoaded();
    }

    public interface OnDataLoadedCallback {
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
    }
}
