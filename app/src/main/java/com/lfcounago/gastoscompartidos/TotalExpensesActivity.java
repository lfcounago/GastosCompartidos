package com.lfcounago.gastoscompartidos;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

public class TotalExpensesActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private ListView lvUsuarios, lvGasto;
    private  List<String> usuarios, uids, detalleGasto;
    private Map<String, String> uidToName, nameToUid;
    private ArrayAdapter<String> adapter, adapterG;
    private TextView tvGastoTotal, tvTotalPagoUsu, tvPagoRealizadoUsu, tvPagosRecibidos;
    private String gid;
    private String uid;
    private FirebaseFirestore fStore;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_expenses);

        // Inicializar los atributos de la clase
        lvUsuarios = (ListView) findViewById(R.id.listView);
        lvGasto = (ListView) findViewById(R.id.listViewGastos);
        usuarios = new ArrayList<>();
        uids = new ArrayList<>();
        uidToName = new HashMap<>();
        nameToUid = new HashMap<>();
        detalleGasto = new ArrayList<>();
        tvGastoTotal = findViewById(R.id.tvGastoTotal);
        tvTotalPagoUsu = findViewById(R.id.tvTotalPagoUsu);
        tvPagosRecibidos = findViewById(R.id.tvPagosRecibidos);
        gid = getIntent().getStringExtra("groupId");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        toolbar = findViewById(R.id.toolbar);

        // Crear un adaptador que vincula los nombres de los usuarios con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuarios);

        // Crear un adaptador que vincula los nombres del gasto con el vista del listView
        adapterG = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, detalleGasto);

        // Configurar la barra de acción
        setSupportActionBar(toolbar);

        // Llamar al método que obtiene los gastos
        getSpends(gid);
    }

    //Método para crear las opciones del menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        this.getMenuInflater().inflate(R.menu.group_expenses, menu);

        return true;
    }

    //Método para saber que opción ha sido seleccionada y actuar en consecuencia
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        boolean toret = false;

        if (item.getItemId() == R.id.itGroupProfile){
            goToGroupProfile();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupDetails) {
            goToGroupDetails();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupLiquidations) {
            goToGroupSpendLiquidations();
            toret = true;
        }

        return toret;
    }

    //Recorre la colección "spends" para obtener todos los datos necesarios
    private void getSpends(String groupId) {
        List<Number> gastos = new ArrayList<>(); //Lista con los gastos del grupo
        List<Number> gastosPag = new ArrayList<>(); //Lista con el gasto del usuario identificado si es el "payer" del gasto
        List<String> gastosTitulo = new ArrayList<>(); //Lista con el gasto del usuario identificado si es el "payer" del gasto con el nombre del gasto y el gasto

        // Realizar una consulta a la colección "spends" de la base de datos de Firestore
        fStore.collection("spends")
                .get() // Obtener todos los documentos de la colección
                .addOnCompleteListener(task -> { // Añadir un listener que se ejecuta cuando la tarea se completa
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
                                Number amount = document.getDouble("amount");
                                String groupID = document.getString("groupID");
                                String payer = document.getString("payer");
                                String gasto = document.getString("title");
                                // Limpiar la lista de nombres de los usuarios
                                usuarios.clear();
                                // Limpiar los mapas de correspondencia entre UID y nombre de los usuarios
                                uidToName.clear();
                                nameToUid.clear();
                                if (groupID != null && groupID.equals(groupId) && amount != null) { // Si la lista de gastos contiene el id del grupo actual
                                    // Añadir el gasto del grupo a la lista de gastos del grupo
                                    gastos.add(amount);
                                    //Cogemos los usuarios correspondientes al gasto
                                    uids = (List<String>) document.get("sharedWith");
                                    if(payer != null && payer.equals(uid)){
                                        gastosPag.add(amount);
                                        gastosTitulo.add(gasto);
                                    }
                                }
                            }
                            totalExpenses(gastos, gastosPag, uids, gastosTitulo); //Llama a la función que obtiene los gastos del grupo y del "payer"
                        }
                    }
                });
    }

    //Método para obtener los gastos totales
    private void totalExpenses(List<Number> gastos, List<Number> gastosPag, List<String> uidsUsu, List<String> gastosTitulo) {
        double gastosTotales = 0;
        double gastosPagador = 0;
        int numUsu = uidsUsu.size();

        for (Number amount : gastos) { //Calcula el gasto total del grupo
            gastosTotales += amount.doubleValue();
        }

        // Limpiar la lista de detalles de gastos
        detalleGasto.clear();

        for (int i = 0; i < gastosPag.size(); i++) {
            Number amountP = gastosPag.get(i);
            String gastoTitulo = gastosTitulo.get(i);

            // Añadir el título del gasto y el gasto correspondiente
            detalleGasto.add(gastoTitulo + " - " + round2(amountP.doubleValue()));

            // Sumar los gastos del pagador
            gastosPagador += amountP.doubleValue();
        }

        // Actualizar el adaptador de lvGasto y notificar cambios
        lvGasto.setAdapter(adapterG);
        adapterG.notifyDataSetChanged();
        double gastoUsu = round2(gastosTotales/numUsu);
        for(String uid : uidsUsu){
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
                                    //Actualizar el adaptador
                                    lvUsuarios.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(TotalExpensesActivity.this, "El documento del usuario no existe", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(TotalExpensesActivity.this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        tvGastoTotal.setText(String.valueOf(gastosTotales));
        tvTotalPagoUsu.setText(String.valueOf(gastosPagador));
    }

    //Método para hacer el redonde a 2 cifras decimales
    public static double round2(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

    //Método que se ejecuta al pulsar la opción del perfil del grupo
    public void goToGroupProfile() {
        Intent intent = new Intent(this, GroupProfileActivity.class);
        intent.putExtra("groupId", gid);
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

    //Método que se ejecuta al pulsar la opción de deudas del grupo
    public void goToGroupSpendLiquidations() {
        //Intent intent = new Intent(this, GroupProfileActivity.class);
        //intent.putExtra("groupId", groupId);
        // Iniciar la actividad
        //startActivity(intent);
    }

}
