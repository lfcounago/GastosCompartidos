package com.lfcounago.gastoscompartidos;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.lfcounago.gastoscompartidos.core.*;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LiquidationsActivity extends AppCompatActivity{
    //Declarar los atributos de la clase
    private RecyclerView rvGroups;
    private GroupRecyclerViewAdapter groupRecyclerViewAdapter;
    private List<Group> groupList;
    private List<User> usersListSp;
    private String uid, lastSelectedUserId;
    private Spinner spUsers;
    private TextView tvUsers;
    private FirebaseFirestore fStore;
    private boolean isDataLoaded;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liquidations); //Establecer el layout correspondiente

        //Inicializar los atributos de la clase
        rvGroups = findViewById(R.id.rvGroupUsers);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        tvUsers = findViewById(R.id.tvUsers);
        isDataLoaded = false;

        //Inicializar las listas
        groupList = new ArrayList<>();
        usersListSp = new ArrayList<>();

        //Inicializar el adaptador
        groupRecyclerViewAdapter = new GroupRecyclerViewAdapter(groupList,false);

        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(groupRecyclerViewAdapter);

        //Añadir texto al TextView del spinner
        tvUsers.setText("Filtrado por usuario:");


        //Obtener la referencia al LinearLayout que contiene el titulo "Liquidaciones"
        LinearLayout llLiquidaciones = findViewById(R.id.tvTituloLiquidaciones);
        ////Obtener la referencia al LinearLayout que contiene el boton
        LinearLayout llButtom = findViewById(R.id.llButton);
        //Obtener la referencia al LinearLayout que contiene el spinner de usuarios
        LinearLayout llSpUsers = findViewById(R.id.llspUsers);

        //Parametros generales
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //Añadir el titulo "Liquidaciones" al LinearLayout
        TextView tvTituloLiquidaciones = new TextView(this);
        tvTituloLiquidaciones.setLayoutParams(layoutParams);
        tvTituloLiquidaciones.setText("Liquidaciones");
        tvTituloLiquidaciones.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTituloLiquidaciones.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);
        tvTituloLiquidaciones.setTypeface(null, Typeface.BOLD);

        //Añadir el boton al LinearLayout
        fab = new FloatingActionButton(this);
        fab.setLayoutParams(layoutParams);
        fab.setImageResource(R.drawable.ic_return);

        //Añadir el spinner al LinearLayout
        spUsers = new Spinner(this);
        spUsers.setLayoutParams(layoutParams);
        spUsers.setAccessibilityPaneTitle("Filtrar por usuario");

        // Añadir al LinearLayout
        llLiquidaciones.addView(tvTituloLiquidaciones);
        llButtom.addView(fab);
        llSpUsers.addView(spUsers);

        //Indicar que se trata de las liquidaciones
        groupRecyclerViewAdapter.setShowBalancesMode(false);



        //Llamar al método que obtiene los grupos
        getGroups();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToListUserGroups(v);
            }
        });

    }

    public void onDataLoaded() {
        // Esta lógica se ejecutará una vez que los datos hayan sido cargados
        if (!isDataLoaded) {
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
            isDataLoaded = true;
        }
    }

    //Método para listar los grupos una vez seleccionado un usuario del filtro
    private void getGroupsForUserSpinner(String userId) {
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
                            if (groupUsers != null && groupUsers.contains(uid) && groupUsers.contains(userId)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Task para llevar cuenta de los grupos comprobados
                                        Task<Void> groupTask = getGroupInfo(groupId, groupName, groupUsers, currency);
                                        if (groupTask != null){
                                            tasks.add(groupTask);
                                        }else{
                                            Log.e("LiquidationsActivitySpinner", "La tarea es null para el grupo: " + groupId);
                                        }
                                    }
                                });
                            } else {
                                Log.e("LiquidationsActivity", "El usuario no pertenece a este grupo, funcion del spinner");
                            }
                        }
                        // Esperar a que todas las tareas se completen
                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(result -> {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Notificar al adaptador de los cambios realizados
                                            groupRecyclerViewAdapter.notifyDataSetChanged();
                                            onDataLoaded();
                                        }
                                    });

                                })
                                .addOnFailureListener(e -> Log.e("LiquidationsActivity", "Error al obtener información de grupos", e));
                    } else {
                        Log.e("LiquidationsActivity", "Error al obtener grupos", task.getException());
                    }
                });
    }

    // Método para configurar el spinner de usuarios
    private void setupUserSpinner() {
        //Configurar el adaptador del spinner de usuarios
        ArrayAdapter<User> userAdapter = new ArrayAdapter<>(LiquidationsActivity.this, android.R.layout.simple_spinner_item, usersListSp);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUsers.setAdapter(userAdapter);

        spUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < parent.getCount()) {
                    //Seleccionar el usuarios escogido por el user
                    User selectedUser = (User) parent.getItemAtPosition(position);
                    lastSelectedUserId = selectedUser.getUserId();

                    //Ejecutar la funcion específica para el spinner de usuarios
                    getGroupsForUserSpinner(lastSelectedUserId);

                    //Coger el usuario seleccionado para mostrar un mensaje
                    String selection = parent.getItemAtPosition(position).toString();
                    Toast.makeText(LiquidationsActivity.this, "Selección actual: " + selection, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada en este caso
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
                        //Limpiar la lista de usuarios antes de agregar nuevos
                        usersListSp.clear();

                        //Lista para llevar una cuenta de las tareas realizadas
                        List<Task<Void>> tasks = new ArrayList<>();

                        //Recorrer cada resultado del documento
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document != null) {
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

                                    // Verificar si la tarea no es null antes de agregarla a la lista
                                    if (groupTask != null) {
                                        tasks.add(groupTask);
                                    } else {
                                        Log.e("LiquidationsActivity", "La tarea es null para el grupo: " + groupId);
                                    }
                                } else {
                                    Log.e("LiquidationsActivity", "El usuario no pertenece a este grupo");
                                }
                            }else{
                                Log.e("LiquidationsActivity", "El document es null");
                            }
                        }

                        // Esperar a que todas las tareas se completen
                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(taskResult ->{
                                    Log.d("LiquidationsActivity", "Todas las tareas completadas con éxito.");
                                    // Notificar al adaptador de los cambios realizados
                                    groupRecyclerViewAdapter.notifyDataSetChanged();
                                    onDataLoaded();

                                    // Configurar el adapter y listener para el spinner de usuarios
                                    setupUserSpinner();
                                })
                                .addOnFailureListener(e->{
                                    Log.e("LiquidationsActivity", "Error al obtener información de grupos", e);
                                });
                    } else {
                        Log.e("LiquidationsActivity", "Error al obtener grupos", task.getException());
                    }
                });
    }

    private Task<Void> getGroupInfo(String groupId, String groupName, List<String> groupUsers, String currency){
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        List<User> users = new ArrayList<>();
        List<Task<Void>> userTasks = new ArrayList<>();

        // Verificar si no hay usuarios en el grupo
        if (groupUsers.isEmpty()) {
            taskCompletionSource.trySetResult(null);
            return taskCompletionSource.getTask();
        }

        //Recorrer todos los usuarios del grupo
        for (String userId : groupUsers) {
            if (!userId.equals(uid)) {
                Task<Void> userTask = getGroupUsers(groupId, userId, currency, (user, totalBalance) -> {
                    users.add(user);
                });
                //Comprobar que el userTask no es nulo antes de añadirlo a la lista de tasks
                if (userTask != null) {
                    userTasks.add(userTask);
                }else{
                    Log.e("LiquidationsActivity", "La tarea es null para el usuario: " + userId);
                }
            }
        }
        Tasks.whenAllComplete(userTasks)
                .addOnSuccessListener(result ->{
                    // Construir un objeto Group con la información obtenida
                    Group group = new Group(groupId, groupName, users);

                    // Añadir el grupo a la lista de grupos
                    groupList.add(group);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Notificar al adaptador de los cambios
                            groupRecyclerViewAdapter.notifyDataSetChanged();
                            onDataLoaded();
                        }
                    });

                    //Indicar que la tarea se ha completado
                    taskCompletionSource.trySetResult(null);
                })
                .addOnFailureListener(e->{
                    //Al menos una tarea falló
                    taskCompletionSource.setException(e);
                });
        return taskCompletionSource.getTask();
    }

    private Task<Void> getGroupUsers(String groupId, String userId, String currency, UsersCallBack callBack) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
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

                                    //Verificar si el usuario ya está en la lista del spinner
                                    boolean userExists = false;
                                    for (User existingUser : usersListSp){
                                        if (existingUser.getUserId().equals(user.getUserId())){
                                            userExists = true;
                                            break;
                                        }
                                    }

                                    //Añadir a la lista del spinner los usuarios si no estan en la lista
                                    if (!userExists) {
                                        usersListSp.add(user);

                                        if (!usersListSp.isEmpty()) {
                                            // Configurar el adaptador para el Spinner de usuarios
                                            ArrayAdapter<User> adapter = new ArrayAdapter<>(LiquidationsActivity.this,
                                                    android.R.layout.simple_spinner_item, usersListSp);
                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                            // Establecer el adaptador para el Spinner de usuarios
                                            spUsers.setAdapter(adapter);
                                        }
                                    }
                                    //Añadir el usuario a la lista de usuarios
                                    usersList.add(user);

                                    // Verificar si se han cargado todos los usuarios
                                    if (usersList.size() == 1) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //Notificar al adaptador de los cambios realizados
                                                groupRecyclerViewAdapter.notifyDataSetChanged();

                                                // Llamar al método de devolución de llamada con el usuario construido
                                                callBack.onUsuarioRecibido(user, liquidations);
                                            }
                                        });
                                    }
                                    // Intentar establecer el resultado de la tarea después de que todo este completo
                                    taskCompletionSource.trySetResult(null);
                                });
                        }
                    } else {
                        // Manejar errores al obtener información del usuario
                        Log.e("LiquidationsActivity", "Error al obtener información del usuario", userDocument.getException());
                    }
                });

        return taskCompletionSource.getTask();
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Notificar al adaptador de los cambios realizados
                                groupRecyclerViewAdapter.notifyDataSetChanged();
                                // Llamar al método de devolución de llamada con la lista de balances
                                callback.onBalancesRecibidos(spends, liquidation);
                            }
                        });

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

    // Método que se ejecuta al pulsar el botón de inicio en el menu
    public void goToListUserGroups(View view){
        // Crear un intent para iniciar la actividad ListUserGroupsActivity
        Intent intent = new Intent(this, ListUserGroupsActivity.class);

        startActivity(intent);
    }
}

