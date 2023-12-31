package com.lfcounago.gastoscompartidos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDetailsActivity extends AppCompatActivity {

    private TextView tvGroupName;
    private ListView lvSpends;
    private ArrayAdapter<String> adapter;
    private List<String> spendNames;
    private List<String> spendIds, spendNombre, spendDate;
    private Map<String, String> spendNameId;
    private String groupId, blue;
    private FirebaseFirestore fStore;
    private Toolbar toolbar;
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        tvGroupName = findViewById(R.id.tvGroupName);
        lvSpends = findViewById(R.id.listView);
        spendNames = new ArrayList<>();
        spendIds = new ArrayList<>();
        spendNombre = new ArrayList<>();
        spendNameId = new HashMap<>();
        spendDate = new  ArrayList<>();
        groupId = getIntent().getStringExtra("groupId");
        fStore = FirebaseFirestore.getInstance();
        toolbar = findViewById(R.id.toolbar);
        blue = "#1fdcff";

        // Crear un adaptador que vincula los gastos del grupo con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spendNames);
        lvSpends.setAdapter(adapter); // Establecer el adaptador al listView

        lvSpends.setOnItemLongClickListener((parent, view, position, id) -> {
            // Obtener el nombre del gasto seleccionado
            String selectedSpendName = spendNombre.get(position);
            //Seleccionar el id segun el nombre del grupo
            String selectedSpendId = spendNameId.get(selectedSpendName);

            // Mostrar un diálogo de confirmación para eliminar el gasto
            mostrarDialogoEliminarGasto(selectedSpendId);

            return true;
        });

        lvSpends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String spendId = spendIds.get(position);
                Intent intent = new Intent(GroupDetailsActivity.this, EditSpendActivity.class);
                intent.putExtra("spendId", spendId);
                startActivity(intent);
            }
        });

        //Parámetros para cambiar el color de la barra de estado
        this.window = getWindow();
        window.setStatusBarColor(Color.parseColor(blue));

        // Configurar la barra de acción
        setSupportActionBar(toolbar);

        // Llamar al método que obtiene los gastos pertenecientes al grupo
        getSpends();
    }

    //Método para crear las opciones del menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        this.getMenuInflater().inflate(R.menu.group_details_menu, menu);

        return true;
    }


    //Método para saber que opción ha sido seleccionada y actuar en consecuencia
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean toret = false;

        if (item.getItemId() == R.id.itHome){
            goToListUserGroupsMenu();
            toret = true;
        }else if (item.getItemId() == R.id.itGroupProfile){
            goToGroupProfile();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupSpends) {
            goToGroupSpends();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupLiquidations) {
            goToGroupSpendLiquidations();
            toret = true;
        }

        return toret;
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
                                String spendId = document.getId();
                                String groupID = document.getString("groupID");
                                String groupName = document.getString("group");
                                Number amount = document.getDouble("amount");
                                String spendName = document.getString("title");
                                String date = document.getString("date");
                                if (groupID.equals(groupId)) {
                                    tvGroupName.setText(groupName);
                                    fStore.collection("groups").document(groupId)
                                            .get()
                                            .addOnCompleteListener(groupTask -> {
                                                if (groupTask.isSuccessful()) {
                                                    DocumentSnapshot groupDocument = groupTask.getResult();
                                                    if (groupDocument != null) {
                                                        String currency = groupDocument.getString("currency");
                                                        String currencySymbol = CurrencySymbol.getCurrencySymbol(currency);
                                                        // Añadir a la lista
                                                        spendNames.add(spendName + "\n" + amount + " " + currencySymbol + "\n" + date);
                                                        spendNameId.put(spendName, spendId);
                                                        spendIds.add(spendId); //Guardo el ID correspondiente para despues pueda acceder a editar el gasto
                                                        spendNombre.add(spendName); //Guardo el nombre correspondiente para despues pueda acceder a eliminar el gasto
                                                        spendDate.add(date);

                                                        // Notificar al adaptador que los datos han cambiado
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    // Método para mostrar el diálogo de confirmación para eliminar un gasto
    private void mostrarDialogoEliminarGasto(String spendId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Gasto");
        builder.setMessage("¿Estás seguro de que deseas eliminar este gasto?");
        builder.setPositiveButton("Eliminar", (dialogInterface, i) -> {
            // Lógica para eliminar el gasto
            eliminarGasto(spendId);
        });
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para eliminar un gasto
    private void eliminarGasto(String spendId) {
        fStore.collection("spends").document(spendId).delete()
                .addOnSuccessListener(unused -> {
                    // El gasto se elimina con éxito
                    Toast.makeText(getApplicationContext(), "Gasto eliminado con éxito", Toast.LENGTH_SHORT).show();
                    // Actualizar la lista de gastos
                    getSpends();
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al intentar eliminar el gasto
                    Toast.makeText(getApplicationContext(), "No se pudo eliminar el gasto", Toast.LENGTH_SHORT).show();
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
        // Crear un intent para iniciar la actividad AddSpendActivity a la que se le pasa el groupId
        Intent intent = new Intent(view.getContext(), AddSpendActivity.class);
        intent.putExtra("groupId", groupId);
        // Iniciar la actividad
        startActivity(intent);
    }

    public void goToListUserGroups(View view) {
        // Crear un intent para iniciar la actividad AddSpendActivity a la que se le pasa el groupId
        Intent intent = new Intent(this, ListUserGroupsActivity.class);
        // Iniciar la actividad
        startActivity(intent);
    }

    //Método que se ejecuta al pulsar la opción de inicio en el menu
    public void goToListUserGroupsMenu() {
        // Crear un intent para iniciar la actividad AddSpendActivity a la que se le pasa el groupId
        Intent intent = new Intent(this, ListUserGroupsActivity.class);
        // Iniciar la actividad
        startActivity(intent);
    }

    //Método que se ejecuta al pulsar la opción del perfil del grupo
    public void goToGroupProfile() {
        Intent intent = new Intent(this, GroupProfileActivity.class);
        intent.putExtra("groupId", groupId);
        // Iniciar la actividad
        startActivity(intent);
    }

    //Método que se ejecuta al pulsar la opción de gastos del grupo
    public void goToGroupSpends() {
        Intent intent = new Intent(this, TotalExpensesActivity.class);
        intent.putExtra("groupId", groupId);
        // Iniciar la actividad
        startActivity(intent);
    }

    //Método que se ejecuta al pulsar la opción de deudas del grupo
    public void goToGroupSpendLiquidations() {
        Intent intent = new Intent(this, DebtLiquidationActivity.class);
        intent.putExtra("groupId", groupId);
        // Iniciar la actividad
        startActivity(intent);
    }
}