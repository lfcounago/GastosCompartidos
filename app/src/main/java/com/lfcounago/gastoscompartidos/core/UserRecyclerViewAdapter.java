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
import java.util.List;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.MemberViewHolder> {
    private List<User> memberList;

    public UserRecyclerViewAdapter(List<User> memberList) {
        this.memberList = memberList;
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
        String currency = member.getCurrency();

        //Configurar el saldo directamente desde el objeto User y su currency
        holder.setBalance(member.getTotalBalance(), currency.substring(Math.max(0, currency.length() - 4)));

        // Restablecer los márgenes para evitar problemas de reciclaje
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        layoutParams.setMargins(0, 8, 0, 8);
        holder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

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

        public void setBalance(double totalBalance, String currency){
            // Crear un objeto DecimalFormat con el formato deseado
            DecimalFormat decimalFormat = new DecimalFormat("#.00");

            if (totalBalance > 0) {
                tvMemberBalance.setText(decimalFormat.format(totalBalance));
                tvMemberBalance.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
            } else if (totalBalance < 0) {
                tvMemberBalance.setText(decimalFormat.format(totalBalance));
                tvMemberBalance.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                tvMemberBalance.setText("0.00");
            }

            //Mostrar la currency
            tvMemberCurrency.setText(currency);
        }
    }
}
