package com.lfcounago.gastoscompartidos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddUserActivity extends AppCompatActivity {

    private String groupId;
    private FirebaseFirestore fStore;
    private CollectionReference usersRef;
    private CollectionReference groupsRef;
    private EditText etUsers;
    private Button btnAceptar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        // Inicializar las referencias de Firebase
        fStore = FirebaseFirestore.getInstance();
        groupId = getIntent().getStringExtra("groupId");
        usersRef = fStore.collection("users");
        groupsRef = fStore.collection("groups");
        btnAceptar = findViewById(R.id.btnAceptar);

        etUsers = findViewById(R.id.etUsers);

        // Listener para crear un grupo
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });
    }

    // Metodo para añadir usuarios al grupo
    private void addUser() {

        String users = etUsers.getText().toString().trim();

        // Comprobar si los valores son válidos
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

        // Obtener el documento del grupo
        DocumentReference groupDocRef = groupsRef.document(groupId);

        groupDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // El documento del grupo existe

                        // Obtiene la lista actual de usuarios del grupo
                        List<String> userIds = (List<String>) documentSnapshot.get("users");

                        usersRef.whereIn("email", emails)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot querySnapshot = task.getResult();
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            //List<String> userIds = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                String userId = document.getId();
                                                // Añadir el userID a la lista
                                                userIds.add(userId);
                                            }
                                            // Crear el mapa para guardar la información del grupo
                                            Map<String, Object> groupData = new HashMap<>();
                                            groupData.put("users", userIds);

                                            // Actualiza el documento del grupo con la nueva lista de usuarios
                                            groupDocRef.update(groupData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(AddUserActivity.this, "Usuarios añadidos con éxito", Toast.LENGTH_SHORT).show();
                                                        //Despues de añadir los usuarios con éxito volver a GroupProfileActivity
                                                        toGroupDetails();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(AddUserActivity.this, "Error al añadir usuarios al grupo", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(AddUserActivity.this, "El documento del grupo no existe", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(AddUserActivity.this, "Error al buscar los emails", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddUserActivity.this, "Hubo un error al recuperar documento del grupo", Toast.LENGTH_SHORT).show();
                                });

                    }
                });
    }

    // Método para cuando se añaden los usuarios vuelva a GroupProfileActivity
    private void toGroupDetails() {
        // Crear un Intent para iniciar la actividad de detalles del grupo
        Intent intent = new Intent(this, GroupProfileActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
        // Cerrar la actividad actual si es necesario
        finish();
    }
}