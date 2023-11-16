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
        tvName = findViewById(R.id.textViewName);
        tvCurrency = findViewById(R.id.textViewCurrency);
        tvCategory = findViewById(R.id.textViewCategory);
        tvDescription = findViewById(R.id.textViewDescription);
        groupId = getIntent().getStringExtra("groupId");
        fStore = FirebaseFirestore.getInstance();

        // Llamar al método que obtiene los datos del grupo
        getGroupData();
    }

    // Definir el método que obtiene los datos del grupo
    private void getGroupData() {
        // Realizar una consulta al documento del grupo en la colección "groups" de la base de datos de Firestore
        fStore.collection("groups").document(groupId)
                .get() // Obtener el documento
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como un documento
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                // Obtener los datos necesarios
                                String name = document.getString("name");
                                String currency = document.getString("currency");
                                String category = document.getString("category");
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
        // Crear un intent para iniciar la actividad IngresarSaldoActivity
        Intent intent = new Intent(this, IngresarSaldoActivity.class);

        // Iniciar la actividad
        startActivity(intent);
    }
}