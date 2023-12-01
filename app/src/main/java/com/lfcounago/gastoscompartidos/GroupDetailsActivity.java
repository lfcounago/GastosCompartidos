package com.lfcounago.gastoscompartidos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {

    private TextView tvGroupName;
    private ListView lvSpends;
    private ArrayAdapter<String> adapter;
    private List<String> spendNames;
    private String groupId;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        tvGroupName = findViewById(R.id.tvGroupName);
        lvSpends = findViewById(R.id.listView);
        spendNames = new ArrayList<>();
        groupId = getIntent().getStringExtra("groupId");
        fStore = FirebaseFirestore.getInstance();

        // Crear un adaptador que vincula los gastos del grupo con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spendNames);
        lvSpends.setAdapter(adapter); // Establecer el adaptador al listView

        // Llamar al método que obtiene los gastos pertenecientes al grupo
        getSpends();

        //Vista de gastos copiar de ListUserGroupsActivity
        //Coger groupId de la actividad anterior
        //
    }

    // Método que obtiene los gastos del grupo pulsado
    private void getSpends() {
        // Realizar una consulta a la colección "spends" de la base de datos de Firestore
        fStore.collection("spends")
                .orderBy("date", Query.Direction.DESCENDING)
                .get() // Obtener todos los documentos de la colección
                .addOnCompleteListener(task -> { // Añadir un listener que se ejecuta cuando la tarea se completa
                    if (task.isSuccessful()) {
                        spendNames.clear();
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
                                String groupID = document.getString("groupID");
                                String groupName = document.getString("group");
                                Number amount = document.getDouble("amount");
                                String spendName = document.getString("title");
                                if (groupID.equals(groupId)) {
                                    tvGroupName.setText(groupName);
                                    // Añadir el nombre del gasto y el gasto del grupo
                                    spendNames.add(spendName + "\n" + amount);
                                }
                            }
                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    // Método que se ejecuta al pulsar el texto del nombre del grupo
    public void goToDeatilsGroup(View view) {
        // Crear un intent para iniciar la actividad GroupProfileActivity
        Intent intent = new Intent(GroupDetailsActivity.this, GroupProfileActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    // Definir el método que se ejecuta al pulsar el botón de ingresar saldo
    public void goToIngresarSaldo(View view) {
        // Crear un intent para iniciar la actividad IngresarSaldoActivity
        Intent intent = new Intent(this, IngresarSaldoActivity.class);

        // Iniciar la actividad
        startActivity(intent);
    }
}