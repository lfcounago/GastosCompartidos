package com.lfcounago.gastoscompartidos;

import com.lfcounago.gastoscompartidos.core.*;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Typeface;
import android.os.Bundle;

import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.util.TypedValue;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BalanceActivity extends AppCompatActivity{
    //Declarar los atributos de la clase
    private RecyclerView rvGroups;
    private TextView tvGroupName;
    private GroupRecyclerViewAdapter groupRecyclerViewAdapter;
    private List<Group> groupList;
    private String uid;
    private FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance); //Establecer el layout correspondiente

        //Inicializar los atributos de la clase
        rvGroups = findViewById(R.id.rvGroupUsers);
        tvGroupName = findViewById(R.id.tvGroupName);
        //uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //uid = "uivt6Bi7ZjapLuBKUXF8e052Oku2";
        uid = "58RJ5RVX0RPrxLu7NhtDacAdM563";
        fStore = FirebaseFirestore.getInstance();

        groupList = new ArrayList<>();

        //Inicializar el adaptador
        groupRecyclerViewAdapter = new GroupRecyclerViewAdapter(groupList);

        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(groupRecyclerViewAdapter);

        //Obtener la referencia al LinearLayout que contiene el titulo "Saldos"
        LinearLayout llSaldos = findViewById(R.id.tvTituloSaldos);

        //Añadir el titulo "Saldos" al LinearLayout
        TextView tvTituloSaldos = new TextView(this);
        tvTituloSaldos.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvTituloSaldos.setText("Saldos");
        tvTituloSaldos.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTituloSaldos.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);
        tvTituloSaldos.setTypeface(null, Typeface.BOLD);

        // Añadir el TextView al LinearLayout
        llSaldos.addView(tvTituloSaldos);


        tvGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
        Log.e("BalanceActivity", "Llamamos al getGroup");
        //Llamar al método que obtiene los grupos
        getGroups();
    }


    public void onDataLoaded() {
        // Esta lógica se ejecutará una vez que los datos hayan sido cargados
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Aquí puedes realizar cualquier acción que desees después de cargar los datos
                // Por ejemplo, actualizar la interfaz de usuario, mostrar un mensaje, etc.
                Toast.makeText(BalanceActivity.this, "Datos cargados exitosamente", Toast.LENGTH_SHORT).show();

                // Verifica si groupList tiene datos
                if (groupList != null && !groupList.isEmpty()) {
                    Log.e("BalanceActivity", "La lista de grupos no está vacía o nula");

                } else {
                    Log.e("BalanceActivity", "La lista de grupos está vacía o nula");
                }

            }
        });
    }

    //Método que obtiene los miembros del grupo
    private void getGroups() {
        Log.e("BalanceActivity", "Entra en getGroups");
        //Realizar consulta a la coleccion "groups" de la base de datos de Firestore
        fStore.collection("groups").get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Limpiar los grupos antes de agregar nuevos
                groupList.clear();

                int groupCount = task.getResult().size();
                AtomicInteger userCount = new AtomicInteger(0);

                //Recorrer cada resultado del documento
                for (QueryDocumentSnapshot document : task.getResult()) {
                    //Obtener los datos
                    String groupId = document.getId();
                    String groupName = document.getString("name");
                    //Lista de usuarios del grupo
                    List<String> groupUsers = (List<String>) document.get("users");

                    Log.e("BalanceActivity", "Procesando grupo: " + groupName + ", ID: " + groupId);

                    //Comprobar si hay usuarios y si el usuario actual pertenece a ese grupo
                    if (groupUsers != null && groupUsers.contains(uid)) {
                        List<User> users = new ArrayList<>();
                        List<String> balanceIds = new ArrayList<>();

                        for (String userId : groupUsers) {
                            getGroupUsers(groupId, userId, (user,totalBalance) -> {
                                users.add(user);

                                // Verificar si se han cargado todos los usuarios
                                if (userCount.incrementAndGet() == groupUsers.size()) {
                                    Group group = new Group(groupId, groupName, users);
                                    groupList.add(group);

                                    // Verificar si se han cargado todos los grupos
                                    if (groupList.size() == groupCount) {
                                        onDataLoaded();
                                    }
                                }
                            });
                        }
                    } else {
                        Log.e("BalanceActivity", "El usuario no pertenece a este grupo");
                    }
                }
                //Notificar al adaptador de los cambios realizados
                groupRecyclerViewAdapter.notifyDataSetChanged();

            } else {
                Log.e("BalanceActivity", "Error al obtener grupos", task.getException());
            }
        });
    }

    private void getGroupUsers(String groupId, String userId, UsersCallBack callBack) {
        Log.e("BalanceActivity", "Entramos en getGroupUsers");
        List<User> usersList = new ArrayList<>();

        // Realizar consulta a la colección "users" con la referencia del ID del usuario
        fStore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(userDocument -> {
                    if (userDocument.isSuccessful()) {
                       // Obtener resultado de la consulta de Firestore
                       DocumentSnapshot result = userDocument.getResult();

                       if (result.exists()) {
                          // Obtener el nombre del usuario
                          String userName = result.getString("fName");

                           Log.e("BalanceActivity", "Procesando usuario: " + userName+ ", ID: " + userId);

                          //Obtener informacion de cada gasto asociado al usuario
                          getUserSpends(groupId, userId, (spends, totalBalance) ->{
                               // Construir un objeto User con la información obtenida
                               User user = new User(userId, userName,Collections.emptyList());

                              // Configurar el saldo total del usuario
                               user.setTotalBalance(totalBalance);

                               usersList.add(user);

                              // Verificar si se han cargado todos los usuarios
                              if (usersList.size() == 1) {
                                  // Llamar al método de devolución de llamada con el usuario construido
                                  callBack.onUsuarioRecibido(usersList.get(0), totalBalance);

                              }
                              //Notificar al adaptador de los cambios realizados
                              groupRecyclerViewAdapter.notifyDataSetChanged();
                          });

                       }
                    } else {
                        // Manejar errores al obtener información del usuario
                        Log.e("BalanceActivity", "Error al obtener información del usuario", userDocument.getException());
                    }
                });
    }

    private void getUserSpends(String groupId, String userId, BalancesCallback callback) {
        // Realizar consulta a la colección "spends" para obtener la información de los balances asociados al usuario
        fStore.collection("spends")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ExpenseItem> spends = new ArrayList<>();

                        for (QueryDocumentSnapshot spendDocument : task.getResult()) {
                            String spendId = spendDocument.getId();
                            String payerId = spendDocument.getString("payer");
                            double amount = spendDocument.getDouble("amount");
                            List<String> sharedWith = (List<String>) spendDocument.get("sharedWith");

                            // Crear instancia de Balance y agregar a la lista de balances
                            ExpenseItem balance = new ExpenseItem(spendId, groupId, payerId, amount, sharedWith);
                            spends.add(balance);

                        }
                        // Calcular el saldo total del usuario en el grupo
                        double totalBalance = calculateTotalBalance(userId, spends);

                        // Mostrar el saldo total del usuario en el grupo
                        Log.e("BalanceActivity", "El saldo total del usuario: " + userId + " en el grupo es: " + totalBalance);


                        // Llamar al método de devolución de llamada con la lista de balances
                        callback.onBalancesRecibidos(spends, totalBalance);
                    } else {
                        // Manejar el error al obtener los balances
                        Log.e("BalanceActivity", "Error al obtener balances del usuario", task.getException());
                        callback.onBalancesRecibidos(Collections.emptyList(),0.0);
                    }
                });
    }

    private double calculateTotalBalance(String userId, List<ExpenseItem> spends){
        double totalBalance = 0.0;

        //Calcular el saldo total del usuario en el grupo
        for (ExpenseItem spend: spends){
            if (spend.getPayerId().equals(userId)){
                // El usuario pagó, resta el amount del gasto
                totalBalance -= spend.getAmount();
            } else if (spend.getSharedWith().contains(userId)) {
                // El usuario compartió el gasto, suma su parte al saldo
                totalBalance += spend.getAmount() / spend.getSharedWith().size();
            }
        }
        return totalBalance;
    }

    interface UsersCallBack {

        void onUsuarioRecibido(User user, Double totalBalance);
    }

    // Interfaz de devolución de llamada para obtener balances del usuario
    interface BalancesCallback {
        void onBalancesRecibidos(List<ExpenseItem> balances, double spend);
    }
}
    /*
        private void getExpensesForUser(String groupId, String userId){
            fStore.collection("spends")
                    .whereEqualTo("groupId",groupId)
                    .whereArrayContains("sharedWith",userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            List<ExpenseItem> expenses = new ArrayList<>();

                            for(QueryDocumentSnapshot spendDocument : task.getResult()){
                                String payerId = spendDocument.getString("payer");
                                double amount = spendDocument.getDouble("amount");

                                // Crear instancia de ExpenseItem y agregar a la lista de gastos
                                ExpenseItem expenseItem = new ExpenseItem(payerId,amount);
                                expenseItem.setPayerId(payerId);
                                expenseItem.setAmount(amount);
                                expenses.add(expenseItem);
                            }

                            // Obtener el grupo actual de manera asíncrona
                            getGroupById(groupId, group -> {
                                if (group != null) {
                                    // Actualizar los gastos del usuario en el grupo
                                    group.updateUserExpenses(userId, expenses);
                                    lvGroups.setAdapter(adapter);
                                    // Notificar al adaptador que los datos han cambiado
                                    adapter.notifyDataSetChanged();
                                } else {
                                    // Manejar el caso en que no se pudo obtener el grupo
                                    Log.e("BalanceActivity", "No se pudo obtener el grupo");
                                }
                            });
                        } else {
                            // Manejar el error al obtener los gastos
                            Log.e("BalanceActivity", "Error al obtener gastos", task.getException());
                        }
                    });
        }

        private void getGroupById(String groupId, GroupCallback callback) {
            fStore.collection("groups").document(groupId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // El documento existe, puedes construir un objeto Group con los datos
                                String groupName = document.getString("name");
                                List<User> groupUsers = (List<User>) document.get("users");

                                // Construir el objeto Group
                                Group group = new Group(groupId, groupName, groupUsers);

                                // Llamar al método de devolución de llamada con el grupo construido
                                // callback.onGroupReceived(group);
                            } else {
                                // El documento no existe
                                Log.d("BalanceActivity", "No existe un grupo con ID: " + groupId);

                                // Llamar al método de devolución de llamada con null, indicando que no se encontró el grupo
                                //callback.onGroupReceived(null);
                            }
                        } else {
                            // Manejar el error al obtener el documento
                            Log.e("BalanceActivity", "Error al obtener el grupo con ID: " + groupId, task.getException());
                        }
                    });
        }

        // Interfaz de devolución de llamada para obtener el grupo por ID
        interface GroupCallback {
            void onGroupReceived(Group group);
        }

     */

