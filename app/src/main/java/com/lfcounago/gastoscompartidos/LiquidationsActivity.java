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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class LiquidationsActivity extends AppCompatActivity{
    //Declarar los atributos de la clase
    private RecyclerView rvGroups;
    private GroupRecyclerViewAdapter groupRecyclerViewAdapter;
    private List<Group> groupList;
    private List<User> usersListSp;
    private List<ExpenseItem> currencyListSp;
    private String uid;
    private Spinner spCurrency;
    private Spinner spUsers;
    private TextView tvUsers;
    private TextView tvCurrencies;
    private FirebaseFirestore fStore;

    private String lastSelectedUserId;
    private String lastSelectedCurrency;

    //Array para spinner
    private String[] currencies;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liquidations); //Establecer el layout correspondiente

        //Inicializar los atributos de la clase
        rvGroups = findViewById(R.id.rvGroupUsers);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        tvUsers = findViewById(R.id.tvUsers);
        tvCurrencies = findViewById(R.id.tvCurrencies);

        //Inicializar las listas
        groupList = new ArrayList<>();
        usersListSp = new ArrayList<>();
        currencyListSp = new ArrayList<>();

        //Inicializar las divisas
        currencies = getResources().getStringArray(R.array.currencies);

        //Inicializar el adaptador
        groupRecyclerViewAdapter = new GroupRecyclerViewAdapter(groupList,false);

        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(groupRecyclerViewAdapter);

        //Añadir texto a los TextView de los spinners
        tvUsers.setText("Filtrado por usuario:");
        tvCurrencies.setText("Filtrado por divisa:");

        //Obtener la referencia al LinearLayout que contiene el titulo "Liquidaciones"
        LinearLayout llLiquidaciones = findViewById(R.id.tvTituloLiquidaciones);
        //Obtener la referencia al LinearLayout que contiene el spinner de usuarios
        LinearLayout llSpUsers = findViewById(R.id.llspUsers);
        //Obtener la referencia al LinearLayout que contiene el spinner de divisas
        LinearLayout llSpCurrency = findViewById(R.id.llspCurrency);

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

        //Añadir el spinner al LinearLayout
        spUsers = new Spinner(this);
        spUsers.setLayoutParams(layoutParams);
        spUsers.setAccessibilityPaneTitle("Filtrar por usuario");

        //Añadir el spinner al LinearLayout
        spCurrency = new Spinner(this);
        spCurrency.setLayoutParams(layoutParams);
        spCurrency.setAccessibilityPaneTitle("Filtrar por divisa");

        // Añadir al LinearLayout
        llLiquidaciones.addView(tvTituloLiquidaciones);
        llSpUsers.addView(spUsers);
        llSpCurrency.addView(spCurrency);

        //Indicar que se trata de las liquidaciones
        groupRecyclerViewAdapter.setShowBalancesMode(false);

        //Inicializar el adaptador de currencies
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(currencyAdapter);

        //Verificar si el adaptaddor del spinner de divisas tiene elementos antes de intentar seleccionar uno
        if (currencyAdapter.getCount() > 0) {
            // Seleccionar el elemento en la posición 0 (o la posición que desees)
            spCurrency.setSelection(0);
        } else {
            // El adaptador no tiene elementos, manejar esto según tus necesidades
            Log.e("LiquidationsActivity", "El adaptador del Spinner de divisas no tiene elementos.");
        }

/*
        spCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener la divisa seleccionada
                lastSelectedCurrency = currencies[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No hacer nada en este caso
            }
        });
*/

        //Llamar al método que obtiene los grupos
        getGroups();
    }

    /*
    // Método para aplicar filtros
    private void applyFilters() {
        // Limpiar la lista de grupos
        groupList.clear();

        // Volver a obtener los grupos con los nuevos filtros
        getGroups();
    }
    */



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

    //Método para listar los grupos una vez seleccionado un usuario del filtro
    private void getGroupsForUserSpinner(String userId) {
        Log.e("LIquidationsActivity", "entro en getgroupsforuserspinner");
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

                            Log.e("LiquidationsActivity", "getgroupfor users Groupname: " + groupName);
                            Log.e("LiquidationsActivity", "Groupusers for users: " + groupUsers);

                            //Comprobar si hay usuarios y si el usuario actual pertenece a ese grupo
                            if (groupUsers != null && groupUsers.contains(uid) && groupUsers.contains(userId)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Task para llevar cuenta de los grupos comprobados
                                        Task<Void> groupTask = getGroupInfo(groupId, groupName, groupUsers, currency);
                                        tasks.add(groupTask);
                                    }
                                });


                            } else {
                                Log.e("LiquidationsActivity", "El usuario no pertenece a este grupo, funcion de spinner");
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

                                    Log.d("LiquidationsActivity", "El usuario pertenece a este grupo. Iniciando tarea para obtener información del grupo...");

                                    //Task para llevar cuenta de los grupos comprobados
                                    Task<Void> groupTask = getGroupInfo(groupId, groupName, groupUsers, currency);

                                    // Verificar si la tarea no es null antes de agregarla a la lista
                                    if (groupTask != null) {
                                        tasks.add(groupTask);
                                        Log.e("LiquidationsActivity", "La tarea no es null para el grupo: " + groupId);
                                    } else {
                                        Log.e("LiquidationsActivity", "La tarea es null para el grupo: " + groupId);
                                    }

                                } else {
                                    Log.e("LiquidationsActivity", "El usuario no pertenece a este grupo");
                                }
                                Log.e("LiquidationsActivity", "groupId: " + groupId + ", groupName: " + groupName + ", groupUsers: " + groupUsers);
                            }else{
                                Log.e("LiquidationsActivity", "El document es null");
                            }
                        }

                        for (Task<Void> task1 : tasks) {
                            if (task1 != null) {
                                if (!task1.isSuccessful()) {
                                    Exception exception = task1.getException();
                                    if (exception != null) {
                                        exception.printStackTrace();
                                        Log.e("LiquidationsActivity", "Estado de la tarea: false. Mensaje de error: " + exception.getMessage());
                                    } else {
                                        Log.e("LiquidationsActivity", "Estado de la tarea: false. La excepción es null.");
                                    }
                                } else {
                                    Log.d("LiquidationsActivity", "Estado de la tarea: true");
                                }
                            }else{
                                Log.e("LiquidationsActivity", "La tarea es null en el bucle for.");
                            }
                        }

                        Log.d("LiquidationsActivity", "Contenido de tasks: " + tasks);
                        // Esperar a que todas las tareas se completen
                        Tasks.whenAllSuccess(tasks)
                                .continueWith(taskResult ->{
                                    Log.d("LiquidationsActivity", "Todas las tareas completadas con éxito.");
                                    if (taskResult.isSuccessful()) {
                                        // Verificar si el resultado de la tarea no es nulo
                                        if (taskResult.getResult() != null) {
                                            // Notificar al adaptador de los cambios realizados
                                            groupRecyclerViewAdapter.notifyDataSetChanged();
                                            onDataLoaded();

                                            Log.d("GetGroups", "Tamaño de usersListSp antes de configurar el adaptador: " + usersListSp.size());

                                            //Configurar el adaptador de usuarios
                                            ArrayAdapter<User> userAdapter = new ArrayAdapter<>(LiquidationsActivity.this, android.R.layout.simple_spinner_item, usersListSp);
                                            userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            spUsers.setAdapter(userAdapter);

                                            Log.e("Getgrousp", "la lista es: " + usersListSp);

                                            if (spUsers.getCount() > 0) {
                                                //Configurar el listener para el spinner de usuarios
                                                spUsers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        if (position >= 0 && position < parent.getCount()) {
                                                            User selectedUser = (User) parent.getItemAtPosition(position);
                                                            lastSelectedUserId = selectedUser.getUserId();
                                                            getGroupsForUserSpinner(lastSelectedUserId);

                                                            String selection = parent.getItemAtPosition(position).toString();
                                                            Toast.makeText(LiquidationsActivity.this, "Seleccion actual: " + selection, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                        //No hacer nada en este caso
                                                    }
                                                });
                                            } else {
                                                Log.e("LiquidationsActivivty", "El spinner de usuarios está vacío");
                                            }
                                        }else {
                                            // Manejar el caso en que taskResult.getResult() es nulo
                                            Log.e("LiquidationsActivity", "El resultado de la tarea es nulo");
                                        }
                                    }else {
                                        Exception exception = taskResult.getException();
                                        if (exception != null) {
                                            Log.e("LiquidationsActivity", "Error al esperar a que todas las tareas se completen", exception);
                                            Log.e("LiquidationsActivity", "Error al obtener información de grupos", exception);
                                        } else {
                                            Log.e("LiquidationsActivity", "Error al esperar a que todas las tareas se completen. La excepción es null.");
                                        }
                                    }
                                    return null;
                                })
                                .addOnFailureListener(e->{
                                    Log.e("LiquidationsActivity", "Error al esperar a que todas las tareas se completen", e);
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

        for (String userId : groupUsers) {
            if (!userId.equals(uid)) {
                Log.d("LiquidationsActivity", "Llamando a getGroupUsers para usuario: " + userId);
                Task<Void> userTask = getGroupUsers(groupId, userId, currency, (user, totalBalance) -> {
                    users.add(user);
                });
                if (userTask != null) {
                    userTasks.add(userTask);
                    Log.e("LiquidationsActivity", "La tarea no es null para el usuario: " + userId);
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

                    // Indicar que la tarea se ha completado
                    taskCompletionSource.trySetResult(null);
                })
                .addOnFailureListener(e->{
                    // Al menos una tarea falló
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
                        //usersListSp.clear();
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
                                    // Intentar establecer el resultado de la tarea después de que todo esté completo
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

    interface UsersListCallback{
        void onUsersListReceived(List<User> usersList);
    }
}

/*
    private Task<Void> getGroupInfoAntiguo(String groupId, String groupName, List<String> groupUsers, String currency){
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        List<User> users = new ArrayList<>();

        AtomicInteger userCount = new AtomicInteger(0);

        for (String uId : groupUsers) {
            //Filtrar el usuario actual
            if (!uId.equals(uid)){
                getGroupUsers(groupId, uId, currency, (user, totalBalance) -> {
                    //Añadir los usuarios a la lista de usuarios, excepto el currentUser
                    users.add(user);

                    // Verificar si se han cargado todos los usuarios
                    if (!groupUsers.isEmpty() && userCount.incrementAndGet() == groupUsers.size() - 1) {//Se resta uno porque el usuario actual no se procesa aqui
                                // Construir un objeto Group con la información obtenida
                                Group group = new Group(groupId, groupName, users);

                                //Añadir el grupo a la lista de grupos
                                groupList.add(group);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Notificar al adaptador de los cambios
                                        groupRecyclerViewAdapter.notifyDataSetChanged();

                                        taskCompletionSource.trySetResult(null);
                                    }
                                });

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
    */

