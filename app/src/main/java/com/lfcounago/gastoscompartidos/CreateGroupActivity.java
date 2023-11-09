package com.lfcounago.gastoscompartidos;

// Importar las librerías necesarias
import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateGroupActivity extends AppCompatActivity {
    // Declarar las variables de los elementos de la interfaz
    private EditText etGroupName;
    private Spinner spCurrency;
    private Spinner spCategory;
    private EditText etEmails;
    private Button btCreateGroup;

    // Declarar las variables de Firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    // Declarar una lista para almacenar los UID de los usuarios
    private List<String> userUIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Inicializar las variables de los elementos de la interfaz
        etGroupName = findViewById(R.id.groupName);
        spCurrency = findViewById(R.id.groupCurrency);
        spCategory = findViewById(R.id.groupCategory);
        etEmails = findViewById(R.id.groupMembers);
        btCreateGroup = findViewById(R.id.createGroupBtn);

        // Inicializar las variables de Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Inicializar la lista de los UID de los usuarios
        userUIDs = new ArrayList<>();

        // Crear un adaptador para el spinner de la divisa
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(currencyAdapter);

        // Crear un adaptador para el spinner de la categoría
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Establecer un listener para el botón de crear grupo
        btCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userUIDs.clear();

                // Obtener los valores introducidos por el usuario
                String groupName = etGroupName.getText().toString().trim();
                String emails = etEmails.getText().toString().trim();

                // Comprobar si los campos están vacíos
                if (groupName.isEmpty() || emails.isEmpty()) {
                    Toast.makeText(CreateGroupActivity.this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Dividir los emails por comas y espacios
                    String[] emailArray = emails.split("[,\\s]+");
                    getUserUID(emailArray);
                }
            }
        });
    }

    // Método para obtener el UID de un usuario a partir de su email
    private void getUserUID(String[]  emailArray) {

        // Crear la referencia a la colección de usuarios
        CollectionReference usersRef = fStore.collection("users");

        usersRef.whereArrayContainsAny("email", Arrays.asList(emailArray))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                userUIDs.add(document.getId());
                            }
                        } else {
                            // Mostrar un mensaje de error
                            //Toast.makeText(CreateGroupActivity.this, "No se ha podido obtener el UID del usuario " + email, Toast.LENGTH_SHORT).show();
                        }
                        processSaveGroup();
                    }
                });
    }

    private void processSaveGroup() {
        // Obtener los valores introducidos por el usuario
        String groupName = etGroupName.getText().toString().trim();
        String currency = spCurrency.getSelectedItem().toString();
        String category = spCategory.getSelectedItem().toString();
        String owner = fAuth.getCurrentUser().getUid();
        userUIDs.add(owner);

        // Crear un mapa con los datos del grupo
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);
        groupData.put("currency", currency);
        groupData.put("category", category);
        groupData.put("owner", owner);
        groupData.put("users", userUIDs);

        // Guardar el mapa en la colección "groups" de Firestore
        saveGroupData(groupData);
    }

    // Método para guardar los datos del grupo en Firestore
    private void saveGroupData(Map<String, Object> groupData) {
        // Obtener la referencia de la colección "groups"
        CollectionReference groupsRef = fStore.collection("groups");

        // Añadir un nuevo documento con los datos del grupo
        groupsRef.add(groupData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            // Mostrar un mensaje de éxito
                            Toast.makeText(CreateGroupActivity.this, "Grupo creado con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            // Mostrar un mensaje de error
                            Toast.makeText(CreateGroupActivity.this, "No se ha podido crear el grupo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
