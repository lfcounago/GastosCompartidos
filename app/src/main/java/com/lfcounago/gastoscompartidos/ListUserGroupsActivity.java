package com.lfcounago.gastoscompartidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListUserGroupsActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private ListView listView; // El componente que muestra la lista de grupos
    private ArrayAdapter<String> adapter; // El adaptador que vincula los datos con la vista
    private List<String> groupNames; // La lista de nombres de los grupos
    private List<String> groupIds; // La lista de ids de los grupos
    private String uid; // El id del usuario actual
    private FirebaseFirestore db; // La referencia a la base de datos de Firestore

    // Sobreescribir el método onCreate que se ejecuta al crear la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_groups); // Establecer el layout correspondiente

        // Inicializar los atributos de la clase
        listView = findViewById(R.id.listView); // Obtener la referencia al componente listView del layout
        groupNames = new ArrayList<>(); // Crear una lista vacía para los nombres de los grupos
        groupIds = new ArrayList<>(); // Crear una lista vacía para los ids de los grupos
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Obtener el id del usuario actual
        db = FirebaseFirestore.getInstance(); // Obtener la instancia de la base de datos de Firestore

        // Crear un adaptador que vincula los nombres de los grupos con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
        listView.setAdapter(adapter); // Establecer el adaptador al listView

        // Añadir un listener al listView que se activa cuando se hace clic en un elemento de la lista
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el id del grupo correspondiente al elemento pulsado
                String groupId = groupIds.get(position);
                // Crear un intent para iniciar una nueva actividad que muestre los datos del grupo
                Intent intent = new Intent(ListUserGroupsActivity.this, GroupDetailsActivity.class);
                // Añadir el id del grupo como un extra al intent
                intent.putExtra("groupId", groupId);
                // Iniciar la nueva actividad
                startActivity(intent);
            }
        });

        // Llamar al método que obtiene los grupos a los que pertenece el usuario
        getGroups();
    }

    // Definir el método que obtiene los grupos a los que pertenece el usuario
    private void getGroups() {
        // Realizar una consulta a la colección "groups" de la base de datos de Firestore
        db.collection("groups")
                .get() // Obtener todos los documentos de la colección
                .addOnCompleteListener(task -> { // Añadir un listener que se ejecuta cuando la tarea se completa
                    if (task.isSuccessful()) { // Si la tarea se ha completado con éxito
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null) { // Si el resultado no es nulo
                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener el id del documento como el id del grupo
                                String groupId = document.getId();
                                // Obtener el nombre del grupo como el valor del campo "name" del documento
                                String groupName = document.getString("name");
                                // Obtener la divisa del grupo como el valor del campo "currency" del documento
                                String groupCurrency = document.getString("currency");
                                // Obtener la categoría del grupo como el valor del campo "category" del documento
                                String groupCategory = document.getString("category");
                                // Obtener la lista de usuarios del grupo como el valor del campo "users" del documento
                                List<String> groupUsers = (List<String>) document.get("users");
                                if (groupUsers != null) { // Si la lista de usuarios no es nula
                                    if (groupUsers.contains(uid)) { // Si la lista de usuarios contiene el id del usuario actual
                                        // Añadir el id del grupo a la lista de ids de los grupos
                                        groupIds.add(groupId);
                                        // Añadir el nombre del grupo, la divisa y la categoría a la lista de nombres de los grupos
                                        groupNames.add(groupName + "\n" + groupCurrency + " - " + groupCategory);
                                    }
                                }
                            }
                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    // Definir el método que se ejecuta al pulsar el botón de ingresar saldo
    public void goToIngresarSaldo(View view) {
        // Obtener la posición del elemento seleccionado en el listView
        int position = listView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) { // Si la posición es válida
            // Obtener el id del grupo correspondiente al elemento seleccionado
            String groupId = groupIds.get(position);
            // Crear un intent para iniciar la actividad IngresarSaldoActivity
            Intent intent = new Intent(this, IngresarSaldoActivity.class);
            // Añadir el id del grupo como un extra al intent
            intent.putExtra("groupId", groupId);
            // Iniciar la actividad
            startActivity(intent);
        }
    }
}