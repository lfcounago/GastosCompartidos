package com.lfcounago.gastoscompartidos;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CreateGroupActivity extends AppCompatActivity {
    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private CollectionReference groupsRef;

    // UI elements
    private EditText groupNameEditText;
    private Spinner currencySpinner;
    private Spinner categorySpinner;
    private EditText usersEditText;
    private Button createGroupButton;

    // Arrays for spinners
    private String[] currencies;
    private String[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        groupsRef = db.collection("groups");

        // Initialize UI elements
        groupNameEditText = findViewById(R.id.group_name_edit_text);
        currencySpinner = findViewById(R.id.currency_spinner);
        categorySpinner = findViewById(R.id.category_spinner);
        usersEditText = findViewById(R.id.users_edit_text);
        createGroupButton = findViewById(R.id.create_group_button);

        // Initialize arrays for spinners
        currencies = getResources().getStringArray(R.array.currencies);
        categories = getResources().getStringArray(R.array.categories);

        // Set up adapters for spinners
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Set up listener for create group button
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
    }

    // Method to create a group and save it to Firebase
    private void createGroup() {
        // Get the input values
        String groupName = groupNameEditText.getText().toString().trim();
        String currency = currencySpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String users = usersEditText.getText().toString().trim();

        // Check if the input values are valid
        if (groupName.isEmpty()) {
            groupNameEditText.setError("El nombre del grupo es obligatorio");
            groupNameEditText.requestFocus();
            return;
        }

        if (users.isEmpty()) {
            usersEditText.setError("Los usuarios del grupo son obligatorios");
            usersEditText.requestFocus();
            return;
        }

        // Split the users input by commas
        String[] emails = users.split(",");

        // Create a list to store the user IDs
        ArrayList<String> userIds = new ArrayList<>();

        // Loop through the emails and find the corresponding user IDs
        for (String email : emails) {
            // Trim the email and make it lowercase
            email = email.trim().toLowerCase();

            // Query the users collection by email
            usersRef.whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Email found, get the user ID
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String userId = document.getId();
                                    // Add the user ID to the list
                                    userIds.add(userId);
                                }
                                // Show a toast message
                                //Toast.makeText(CreateGroupActivity.this, "Email encontrado: " + email, Toast.LENGTH_SHORT).show();
                            } else {
                                // Email not found, show a toast message
                                //Toast.makeText(CreateGroupActivity.this, "Email no encontrado: " + email, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Query failed, show a toast message
                            //Toast.makeText(CreateGroupActivity.this, "Error al buscar el email: " + email, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Get the current user ID
        String ownerId = mAuth.getCurrentUser().getUid();

        // Add the owner ID to the list
        userIds.add(ownerId);

        // Create a map to store the group data
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);
        groupData.put("currency", currency);
        groupData.put("category", category);
        groupData.put("users", userIds);
        groupData.put("owner", ownerId);

        // Add the group data to the groups collection
        groupsRef.add(groupData)
                .addOnSuccessListener(documentReference -> {
                    // Group created successfully, show a toast message
                    Toast.makeText(CreateGroupActivity.this, "Grupo creado con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Group creation failed, show a toast message
                    Toast.makeText(CreateGroupActivity.this, "Error al crear el grupo", Toast.LENGTH_SHORT).show();
                });
    }
}
