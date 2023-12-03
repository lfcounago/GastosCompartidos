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

import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LiquidationsActivity extends AppCompatActivity{
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
        setContentView(R.layout.activity_liquidations); //Establecer el layout correspondiente

        //Inicializar los atributos de la clase
        rvGroups = findViewById(R.id.rvGroupUsers);
        tvGroupName = findViewById(R.id.tvGroupName);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();

        groupList = new ArrayList<>();

        //Inicializar el adaptador
        groupRecyclerViewAdapter = new GroupRecyclerViewAdapter(groupList);

        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(groupRecyclerViewAdapter);

        //Obtener la referencia al LinearLayout que contiene el titulo "Liquidaciones"
        LinearLayout llLiquidaciones = findViewById(R.id.tvTituloLiquidaciones);

        //Añadir el titulo "Liquidaciones" al LinearLayout
        TextView tvTituloLiquidaciones = new TextView(this);
        tvTituloLiquidaciones.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvTituloLiquidaciones.setText("Liquidaciones");
        tvTituloLiquidaciones.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTituloLiquidaciones.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);
        tvTituloLiquidaciones.setTypeface(null, Typeface.BOLD);

        // Añadir el TextView al LinearLayout
        llLiquidaciones.addView(tvTituloLiquidaciones);

        //Llamar al método que obtiene los grupos
        getGroups();
    }


    public void onDataLoaded() {
        // Esta lógica se ejecutará una vez que los datos hayan sido cargados
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LiquidationsActivity.this, "Datos cargados exitosamente", Toast.LENGTH_SHORT).show();

                // Verifica si groupList tiene datos
                if (groupList != null && !groupList.isEmpty()) {
                    Log.e("LiquidationsActivity", "La lista de grupos no está vacía o nula");

                } else {
                    Log.e("LiquidationsActivity", "La lista de grupos está vacía o nula");
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

                        //Lista para llevar una cuenta de las tareas realizadas
                        List<Task<Void>> tasks = new ArrayList<>();

                        //Recorrer cada resultado del documento
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //Obtener los datos
                            String groupId = document.getId();
                            String groupName = document.getString("name");
                            String currency = document.getString("currency");
                            //Lista de usuarios del grupo
                            List<String> groupUsers = (List<String>) document.get("users");

                            //Comprobar si hay usuarios y si el usuario actual pertenece a ese grupo
                            if (groupUsers != null && groupUsers.contains(uid)) {
                                //Task para llevar cuenta de los grupos comprobados
                                Task<Void> groupTask = getGroupInfo(groupId, groupName, groupUsers, currency);
                                tasks.add(groupTask);
                            } else {
                                Log.e("LiquidationsActivity", "El usuario no pertenece a este grupo");
                            }
                        }
                        // Esperar a que todas las tareas se completen
                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(result -> {
                                    // Notificar al adaptador de los cambios realizados
                                    groupRecyclerViewAdapter.notifyDataSetChanged();
                                    onDataLoaded();
                                })
                                .addOnFailureListener(e -> Log.e("LiquidationsActivity", "Error al obtener información de grupos", e));

                    } else {
                        Log.e("LiquidationsActivity", "Error al obtener grupos", task.getException());
                    }
                });
    }

    private Task<Void> getGroupInfo(String groupId, String groupName, List<String> groupUsers, String currency){
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        List<User> users = new ArrayList<>();

        AtomicInteger userCount = new AtomicInteger(0);

        for (String userId : groupUsers) {
            //Filtrar el usuario actual
            if (!userId.equals(uid)){
                getGroupUsers(groupId, userId, currency, (user, totalBalance) -> {
                    //Añadir los usuarios a la lista de usuarios, excepto el currentUser
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
            }else{
                // Incrementar userCount para evitar que la condición de verificación se active antes de tiempo
                userCount.incrementAndGet();
            }
        }

        // Esperar a que todas las tareas de usuario se completen
        Tasks.whenAllSuccess(Collections.singletonList(taskCompletionSource.getTask()));

        return taskCompletionSource.getTask();
    }

    private void getGroupUsers(String groupId, String userId, String currency, UsersCallBack callBack) {
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
                                getUserLiquidations(groupId, userId, (spends, liquidations) ->{
                                    // Construir un objeto User con la información obtenida
                                    User user = new User(userId, userName,Collections.emptyList(),currency);

                                    // Configurar el saldo total del usuario
                                    user.setTotalBalance(liquidations);

                                    //Añadir el usuario a la lista de usuarios
                                    usersList.add(user);


                                    // Verificar si se han cargado todos los usuarios
                                    if (usersList.size() == 1) {
                                        //Notificar al adaptador de los cambios realizados
                                        groupRecyclerViewAdapter.notifyDataSetChanged();

                                        // Llamar al método de devolución de llamada con el usuario construido
                                        callBack.onUsuarioRecibido(usersList.get(0), liquidations);
                                    }
                                });

                        }
                    } else {
                        // Manejar errores al obtener información del usuario
                        Log.e("LiquidationsActivity", "Error al obtener información del usuario", userDocument.getException());
                    }
                });
    }

    private void getUserLiquidations(String groupId, String userId, BalancesCallback callback) {
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
                        double liquidation = calculateLiquidations(userId, groupId, spends);

                        // Llamar al método de devolución de llamada con la lista de balances
                        callback.onBalancesRecibidos(spends, liquidation);
                    } else {
                        // Manejar el error al obtener los balances
                        Log.e("LiquidationsActivity", "Error al obtener balances del usuario", task.getException());
                        callback.onBalancesRecibidos(Collections.emptyList(),0.0);
                    }
                });
    }

    private double calculateLiquidations(String userId, String groupId, List<ExpenseItem> spends){
       double totalDebt = 0.0;

        for (ExpenseItem spend : spends) {
            if (groupId.equals(spend.getGroupId())) {
                if (spend.getPayerId().equals(userId)) {
                    // El usuario es el pagador, calcular las deudas específicas del currentUser
                    double amount = spend.getAmount();
                    double debt = amount/spend.getSharedWith().size();

                    //Si el userId es quien paga, el currentUser le debe esa cantidad al userId
                    totalDebt += debt;

                } else{
                    //El userId no paga asi que el currentUser no le debe nada
                    totalDebt += 0.0;
                }
            }
        }

        return totalDebt;
    }

    interface UsersCallBack {

        void onUsuarioRecibido(User user, Double totalBalance);
    }

    // Interfaz de devolución de llamada para obtener balances del usuario
    interface BalancesCallback {
        void onBalancesRecibidos(List<ExpenseItem> balances, double liquidations);
    }
}


