package com.lfcounago.gastoscompartidos.core;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lfcounago.gastoscompartidos.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Adaptador para la RecyclerView que muestra la lista de usuarios
public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.MemberViewHolder> {
    private List<User> memberList;
    private boolean showBalancesMode;

    public UserRecyclerViewAdapter(List<User> memberList, boolean showBalanceMode) {

        this.memberList = memberList;
        this.showBalancesMode = showBalanceMode;
    }

    // Método llamado cuando se necesita crear un nuevo ViewHolder
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new MemberViewHolder(view);
    }

    // Método llamado para mostrar datos en un ViewHolder específico
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = memberList.get(position);
        holder.tvMemberName.setText(member.getUserName());
        String currency = member.getCurrency();
        double balance = member.getTotalBalance();

        if (showBalancesMode){
            //Configurar el saldo directamente desde el objeto User y su currency
            holder.setBalance(member.getTotalBalance(), currency.substring(Math.max(0, currency.length() - 4)));
        }else{
            if (balance > 0){
                //Configurar la liquidacion directamente desde el objeto User y su currency
                holder.setLiquidations(member.getTotalBalance(), currency.substring(Math.max(0, currency.length() - 4)));
            }else{
                // Ocultar el elemento si no hay liquidaciones pendientes
                holder.itemView.setVisibility(View.GONE);
                ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                params.height = 0;
                holder.itemView.setLayoutParams(params);
            }
        }

        // Restablecer los márgenes para evitar problemas de reciclaje
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setMargins(0, 8, 0, 8);
        holder.itemView.setLayoutParams(layoutParams);
    }

    // Método para verificar si hay deudas en el grupo
    public boolean groupDebts(){
        if (showBalancesMode){
            return true;
        }else {
            for (User user : memberList) {
                if (user.getTotalBalance() > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    // Clase interna que representa el ViewHolder para cada elemento de la RecyclerView
    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        TextView tvMemberBalance;
        TextView tvMemberCurrency;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvUserName);
            tvMemberBalance = itemView.findViewById(R.id.tvUserBalance);
            tvMemberCurrency = itemView.findViewById(R.id.tvBalanceCurrency);
        }

        // Método para configurar el saldo en la interfaz de usuario
        public void setBalance(double totalBalance, String currency){
            // Crear un objeto DecimalFormat con el formato deseado
            DecimalFormat decimalFormat = new DecimalFormat("0.00");

            if (totalBalance > 0) {
                tvMemberBalance.setText(decimalFormat.format(totalBalance));
                tvMemberBalance.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));

                //Mostrar la currency
                tvMemberCurrency.setText(currency);
                tvMemberCurrency.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
            } else if (totalBalance < 0) {
                tvMemberBalance.setText(decimalFormat.format(totalBalance));
                tvMemberBalance.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.red));

                //Mostrar la currency
                tvMemberCurrency.setText(currency);
                tvMemberCurrency.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                tvMemberBalance.setText("0.00");
                tvMemberCurrency.setText(currency);
            }
        }

        // Método para configurar las liquidaciones en la interfaz de usuario
        public void setLiquidations(double totalLiquidations, String currency){
            // Crear un objeto DecimalFormat con el formato deseado
            DecimalFormat decimalFormat = new DecimalFormat("0.00");

            if (totalLiquidations > 0) {
                tvMemberBalance.setText(decimalFormat.format(totalLiquidations));
            } else if (totalLiquidations < 0) {
                tvMemberBalance.setText(decimalFormat.format(totalLiquidations));
            } else {
                tvMemberBalance.setText("0.00");
            }

            //Mostrar la currency
            tvMemberCurrency.setText(currency);
        }
    }
}
