package com.lfcounago.gastoscompartidos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtLiquidationActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private ListView lvUsuarios;
    private List<String> usuarios, uids, idsUsuario, deudas;
    private Map<String, String> uidToName, nameToUid;
    private ArrayAdapter<String> adapter;
    private String gid, titulo;
    private String uidU, rojo;
    private Double gastoUsuario;
    private FirebaseFirestore fStore;
    private Toolbar toolbar;
    private Window window;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_liquidation);

        // Inicializar los atributos de la clase
        lvUsuarios = (ListView) findViewById(R.id.listView);
        usuarios = new ArrayList<>();
        uids = new ArrayList<>();
        idsUsuario = new ArrayList<>();
        deudas = new ArrayList<>();
        uidToName = new HashMap<>();
        nameToUid = new HashMap<>();
        titulo = "Liquidación";
        gid = getIntent().getStringExtra("groupId");//"bmMYudVqJVuPzEaWwVNj";
        uidU = FirebaseAuth.getInstance().getCurrentUser().getUid();//"uivt6Bi7ZjapLuBKUXF8e052Oku2";
        fStore = FirebaseFirestore.getInstance();
        toolbar = findViewById(R.id.toolbar);
        rojo = "#ff4561";

        // Crear un adaptador que vincula los nombres de los usuarios con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuarios);
        lvUsuarios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userId = idsUsuario.get(position);
                String groupId = gid;
                Double spendUsu = gastoUsuario;
                Intent intent = new Intent(DebtLiquidationActivity.this, SettleDebtActivity.class);
                intent.putExtra("IDUser", userId);
                intent.putExtra("groupId", groupId);
                intent.putExtra("spendUsu", spendUsu);
                startActivity(intent);
            }
        });

        //Parámetros para cambiar el color de la barra de estado
        this.window = getWindow();
        window.setStatusBarColor(Color.parseColor(rojo));

        // Configurar la barra de acción
        setSupportActionBar(toolbar);

        // Llamar al método que obtiene los gastos
        getSpends(gid);
    }

    //Método para crear las opciones del menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        this.getMenuInflater().inflate(R.menu.group_liquidations_menu, menu);

        return true;
    }

    //Método para saber que opción ha sido seleccionada y actuar en consecuencia
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean toret = false;

        if (item.getItemId() == R.id.itHome){
            goToListUserGroups();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupDetails){
            goToGroupDetails();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupProfile) {
            goToGroupProfile();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupSpends) {
            goToGroupSpends();
            toret = true;
        }

        return toret;
    }

    //Recorre la colección "spends" para obtener todos los datos necesarios
    private void getSpends(String groupId) {
        List<Number> gastos = new ArrayList<>(); //Lista con los gastos del grupo

        // Realizar una consulta a la colección "spends" de la base de datos de Firestore
        fStore.collection("spends")
                .get() // Obtener todos los documentos de la colección
                .addOnCompleteListener(task -> { // Añadir un listener que se ejecuta cuando la tarea se completa
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            // Recorrer cada documento del resultado
                            deudas.clear();
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
                                String title = document.getString("title");
                                Number amount = document.getDouble("amount");
                                String groupID = document.getString("groupID");
                                String payer = document.getString("payer");
                                // Limpiar la lista de nombres de los usuarios
                                usuarios.clear();
                                // Limpiar los mapas de correspondencia entre UID y nombre de los usuarios
                                uidToName.clear();
                                nameToUid.clear();
                                if(groupID != null && groupID.equals(groupId) && title.equals(titulo) && uidU.equals(payer)){
                                    Object sharedWithObj = document.get("sharedWith");
                                    if (sharedWithObj instanceof String) {
                                        deudas.add((String) sharedWithObj);
                                    }
                                }
                                if (groupID != null && groupID.equals(groupId) && amount != null && !title.equals(titulo)) { // Si la lista de gastos contiene el id del grupo actual
                                    // Añadir el gasto del grupo a la lista de gastos del grupo
                                    gastos.add(amount);
                                    //Cogemos los usuarios correspondientes al gasto
                                    uids = (List<String>) document.get("sharedWith");
                                }
                            }
                            totalExpenses(gastos, uids, deudas); //Llama a la función que obtiene los gastos del grupo y del "payer"
                        }
                    }
                });
    }

    private void totalExpenses(List<Number> gastos, List<String> uidsUsu, List<String> deudas) {
        double gastosTotales = 0;
        int numUsu = uidsUsu.size();

        for (Number amount : gastos) { //Calcula el gasto total del grupo
            gastosTotales += amount.doubleValue();
        }

        double gastoUsu = round2(gastosTotales/numUsu);
        gastoUsuario = gastoUsu;
        for(String uid : uidsUsu){
            if(!uid.equals(uidU) && !deudas.contains(uid)) {
                // Obtener el documento correspondiente al UID del usuario de la colección users de Firebase
                fStore.collection("users").document(uid)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Obtener el documento del usuario
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Obtener el nombre del usuario
                                        String name = document.getString("fName");
                                        // Añadir el nombre del usuario a la lista de nombres de los usuarios
                                        usuarios.add(name + "   -   " + gastoUsu);
                                        // Añadir la correspondencia entre el UID y el nombre del usuario a los mapas
                                        uidToName.put(uid, name);
                                        nameToUid.put(name, uid);
                                        idsUsuario.add(uid);
                                        //Actualizar el adaptador
                                        lvUsuarios.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(DebtLiquidationActivity.this, "El documento del usuario no existe", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(DebtLiquidationActivity.this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    //Método para hacer el redonde a 2 cifras decimales
    public static double round2(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

    //Método que se ejecuta al pulsar la opción de inicio en el menu
    public void goToListUserGroups() {
        Intent intent = new Intent(this, ListUserGroupsActivity.class);
        // Iniciar la actividad
        startActivity(intent);
    }

    //Método que se ejecuta al pulsar la opción de detalles del grupo
    public void goToGroupDetails() {
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.putExtra("groupId", gid);
        // Iniciar la actividad
        startActivity(intent);
    }

    //Método que se ejecuta al pulsar la opción de perfil del grupo
    public void goToGroupProfile() {
        Intent intent = new Intent(this, GroupProfileActivity.class);
        intent.putExtra("groupId", gid);
        // Iniciar la actividad
        startActivity(intent);
    }

    //Método que se ejecuta al pulsar la opción de gastos del grupo
    public void goToGroupSpends() {
        Intent intent = new Intent(this, TotalExpensesActivity.class);
        intent.putExtra("groupId", gid);
        // Iniciar la actividad
        startActivity(intent);
    }
}