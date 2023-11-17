package com.lfcounago.gastoscompartidos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateGroupActivity extends AppCompatActivity {
    // Firebase references
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private CollectionReference usersRef;
    private CollectionReference groupsRef;

    // UI elements
    private EditText etGroupName;
    private Spinner spCurrency;
    private Spinner spCategory;
    private EditText etUsers;
    private Button btCreateGroup;

    // Arrays for spinners
    private String[] currencies;
    private String[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Inicializar las referencias de Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        usersRef = fStore.collection("users");
        groupsRef = fStore.collection("groups");

        // Inicializar los elementos de la interfaz
        etGroupName = findViewById(R.id.group_name_edit_text);
        spCurrency = findViewById(R.id.currency_spinner);
        spCategory = findViewById(R.id.category_spinner);
        etUsers = findViewById(R.id.users_edit_text);
        btCreateGroup = findViewById(R.id.create_group_button);

        // Inicializar los arrays de los spinners
        currencies = getResources().getStringArray(R.array.currencies);
        categories = getResources().getStringArray(R.array.categories);

        // Configurar los adapters para los spinners
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(currencyAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Listener para crear un grupo
        btCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
    }

    // Method to create a group and save it to Firebase
    private void createGroup() {
        // Obtener los valores de la interfaz
        String groupName = etGroupName.getText().toString().trim();
        String currency = spCurrency.getSelectedItem().toString();
        String category = spCategory.getSelectedItem().toString();
        String users = etUsers.getText().toString().trim();

        // Comprobar si los valores son válidos
        if (groupName.isEmpty()) {
            etGroupName.setError("El nombre del grupo es obligatorio");
            etGroupName.requestFocus();
            return;
        }

        if (users.isEmpty()) {
            etUsers.setError("Los usuarios del grupo son obligatorios");
            etUsers.requestFocus();
            return;
        }

        // Separar los usuarios por comas
        String[] emailsArray = users.split(",");
        for (int i = 0; i < emailsArray.length; i++) {
            emailsArray[i] = emailsArray[i].trim().toLowerCase();
        }
        List<String> emails = Arrays.asList(emailsArray);

        // Query para la colección de users por email
        usersRef.whereIn("email", emails)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<String> userIds = new ArrayList<>();
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String userId = document.getId();
                                // Añadir el userID a la lista
                                userIds.add(userId);
                            }
                            // Obtener el ID del usuario actual
                            String ownerId = fAuth.getCurrentUser().getUid();

                            // Añadir el owner ID a la lista
                            userIds.add(ownerId);

                            // Crear el mapa para guardar la información del grupo
                            Map<String, Object> groupData = new HashMap<>();
                            groupData.put("name", groupName);
                            groupData.put("currency", currency);
                            groupData.put("category", category);
                            groupData.put("users", userIds);
                            groupData.put("owner", ownerId);

                            // Añadir los datos del grupo a la colección de gruposAdd the group data to the groups collection
                            groupsRef.add(groupData)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(CreateGroupActivity.this, "Grupo creado con éxito", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CreateGroupActivity.this, "Error al crear el grupo", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(CreateGroupActivity.this, "Error al buscar los emails", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void goToActividadAnterior(View view) {
        // Crear un intent para iniciar la actividad ListUserGroupsActivity
        Intent intent = new Intent(this, ListUserGroupsActivity.class);

        startActivity(intent);
    }
}
