package com.lfcounago.gastoscompartidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupDetailsActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private TextView tvName, tvCurrency, tvCategory, tvDescription;
    private String groupId;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details); // Establecer el layout correspondiente

        // Inicializar los atributos de la clase
        tvName = findViewById(R.id.textViewName); // Obtener la referencia al componente textViewName del layout
        tvCurrency = findViewById(R.id.textViewCurrency); // Obtener la referencia al componente textViewCurrency del layout
        tvCategory = findViewById(R.id.textViewCategory); // Obtener la referencia al componente textViewCategory del layout
        tvDescription = findViewById(R.id.textViewDescription); // Obtener la referencia al componente textViewDescription del layout
        groupId = getIntent().getStringExtra("groupId"); // Obtener el id del grupo como un extra del intent que inició la actividad
        fStore = FirebaseFirestore.getInstance(); // Obtener la instancia de la base de datos de Firestore

        // Llamar al método que obtiene los datos del grupo
        getGroupData();
    }

    // Definir el método que obtiene los datos del grupo
    private void getGroupData() {
        // Realizar una consulta al documento del grupo en la colección "groups" de la base de datos de Firestore
        fStore.collection("groups").document(groupId)
                .get() // Obtener el documento
                .addOnCompleteListener(task -> { // Añadir un listener que se ejecuta cuando la tarea se completa
                    if (task.isSuccessful()) { // Si la tarea se ha completado con éxito
                        // Obtener el resultado de la tarea como un documento
                        DocumentSnapshot document = task.getResult();
                        if (document != null) { // Si el documento no es nulo
                            if (document.exists()) { // Si el documento existe
                                // Obtener el nombre del grupo como el valor del campo "name" del documento
                                String name = document.getString("name");
                                // Obtener la divisa del grupo como el valor del campo "currency" del documento
                                String currency = document.getString("currency");
                                // Obtener la categoría del grupo como el valor del campo "category" del documento
                                String category = document.getString("category");
                                // Obtener la descripción del grupo como el valor del campo "description" del documento
                                String description = document.getString("description");
                                // Establecer el texto de los componentes con los datos obtenidos
                                tvName.setText(name);
                                tvCurrency.setText(currency);
                                tvCategory.setText(category);
                                tvDescription.setText(description);
                            }
                        }
                    }
                });
    }

    // Definir el método que se ejecuta al pulsar el botón de ingresar saldo
    public void goToIngresarSaldo(View view) {
        // Crear un intent para iniciar la actividad CreateGroupActivity
        Intent intent = new Intent(this, IngresarSaldoActivity.class);

        // Iniciar la actividad
        startActivity(intent);
    }
}