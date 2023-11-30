package com.lfcounago.gastoscompartidos;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
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
    private TextView tvUserBalance;
    private GroupRecyclerViewAdapter groupRecyclerViewAdapter;
    private UserRecyclerViewAdapter userRecyclerViewAdapter;
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
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //uid = "uivt6Bi7ZjapLuBKUXF8e052Oku2";
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

        //Llamar al método que obtiene los grupos
        getGroups();
    }


    public void onDataLoaded() {
        // Esta lógica se ejecutará una vez que los datos hayan sido cargados
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        //Realizar consulta a la coleccion "groups" de la base de datos de Firestore
        fStore.collection("groups").get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Limpiar los grupos antes de agregar nuevos
                groupList.clear();

                List<Task<Void>> tasks = new ArrayList<>();
                int groupCount = task.getResult().size();
                AtomicInteger userCount = new AtomicInteger(0);

                //Recorrer cada resultado del documento
                for (QueryDocumentSnapshot document : task.getResult()) {
                    //Obtener los datos
                    String groupId = document.getId();
                    String groupName = document.getString("name");
                    //Lista de usuarios del grupo
                    List<String> groupUsers = (List<String>) document.get("users");

                    //Comprobar si hay usuarios y si el usuario actual pertenece a ese grupo
                    if (groupUsers != null && groupUsers.contains(uid)) {
                        Task<Void> groupTask = getGroupInfo(groupId, groupName, groupUsers);
                        tasks.add(groupTask);
                    } else {
                        Log.e("BalanceActivity", "El usuario no pertenece a este grupo");
                    }
                }
            // Esperar a que todas las tareas se completen
                Tasks.whenAllSuccess(tasks)
                        .addOnSuccessListener(result -> {
                            // Notificar al adaptador de los cambios realizados
                            groupRecyclerViewAdapter.notifyDataSetChanged();
                            onDataLoaded();
                        })
                        .addOnFailureListener(e -> Log.e("BalanceActivity", "Error al obtener información de grupos", e));

            } else {
                Log.e("BalanceActivity", "Error al obtener grupos", task.getException());
            }
        });
    }

    private Task<Void> getGroupInfo(String groupId, String groupName, List<String> groupUsers){
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        List<User> users = new ArrayList<>();

        AtomicInteger userCount = new AtomicInteger(0);

        for (String userId : groupUsers) {
            getGroupUsers(groupId, userId, (user, totalBalance) -> {
                //Añadir los usuarios a la lista de usuarios
                users.add(user);

                // Verificar si se han cargado todos los usuarios
                if (userCount.incrementAndGet() == groupUsers.size()) {
                    // Construir un objeto Group con la información obtenida
                    Group group = new Group(groupId, groupName, users);

                    //Añadir el grupo a la lista de grupos
                    groupList.add(group);

                    taskCompletionSource.trySetResult(null);
                }
            });
        }

        // Esperar a que todas las tareas de usuario se completen
        Tasks.whenAllSuccess(Collections.singletonList(taskCompletionSource.getTask()));

        return taskCompletionSource.getTask();
    }

    private void getGroupUsers(String groupId, String userId, UsersCallBack callBack) {
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

                          //Obtener informacion de cada gasto asociado al usuario
                          getUserSpends(groupId, userId, (spends, totalBalance) ->{
                               // Construir un objeto User con la información obtenida
                               User user = new User(userId, userName,Collections.emptyList());

                              // Configurar el saldo total del usuario
                               user.setTotalBalance(totalBalance);

                               //Añadir el usuario a la lista de usuarios
                               usersList.add(user);

                              // Verificar si se han cargado todos los usuarios
                              if (usersList.size() == 1) {
                                  //Notificar al adaptador de los cambios realizados
                                  groupRecyclerViewAdapter.notifyDataSetChanged();

                                  // Llamar al método de devolución de llamada con el usuario construido
                                  callBack.onUsuarioRecibido(usersList.get(0), totalBalance);
                              }
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
                .whereEqualTo("groupID", groupId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ExpenseItem> spends = new ArrayList<>();

                        for (QueryDocumentSnapshot spendDocument : task.getResult()) {
                            //Obtener la informacion necesaria de los gastos
                            String spendId = spendDocument.getId();
                            String payerId = spendDocument.getString("payer");
                            double amount = spendDocument.getDouble("amount");
                            List<String> sharedWith = (List<String>) spendDocument.get("sharedWith");

                            // Crear instancia de Balance y agregar a la lista de balances
                            ExpenseItem balance = new ExpenseItem(spendId, groupId, payerId, amount, sharedWith);
                            spends.add(balance);

                        }
                        // Calcular el saldo total del usuario en el grupo
                        double totalBalance = calculateTotalBalance(userId,groupId, spends);

                        // Llamar al método de devolución de llamada con la lista de balances
                        callback.onBalancesRecibidos(spends, totalBalance);
                    } else {
                        // Manejar el error al obtener los balances
                        Log.e("BalanceActivity", "Error al obtener balances del usuario", task.getException());
                        callback.onBalancesRecibidos(Collections.emptyList(),0.0);
                    }
                });
    }

    private double calculateTotalBalance(String userId, String groupId, List<ExpenseItem> spends){
        double totalBalance = 0.0;

        //Calcular el saldo total del usuario en el grupo
        for (ExpenseItem spend: spends){
            //Comprobar que el groupId que se pasa sea el mismo que el del gasto
            if (groupId.equals(spend.getGroupId())){
                //Comrpobar que el userId que se pasa sea igual al payer del gasto
                if (spend.getPayerId().equals(userId)) {
                    //Obtener informacion de cada gasto
                    double amount = spend.getAmount();
                    List<String> sharedWith = spend.getSharedWith();
                    int numShared = sharedWith.size();

                    // El usuario pagó, se divide entre el numero de usuarios con los que se comparte el gasto
                    totalBalance += (amount / numShared) * (numShared - 1);

                }else if (spend.getSharedWith().contains(userId)) {
                    // El usuario no pagó pero comparte el gasto con el payer
                    totalBalance -= spend.getAmount() / spend.getSharedWith().size();
                }
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

