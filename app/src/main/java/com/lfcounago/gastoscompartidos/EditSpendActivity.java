package com.lfcounago.gastoscompartidos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSpendActivity extends AppCompatActivity {

    private EditText etTitle, etGasto;
    private TextView tvDivisa;
    private Button btnEditar;
    private String spendId, groupIda;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_spend);

        etTitle = findViewById(R.id.etTitle);
        etGasto = findViewById(R.id.etGasto);
        tvDivisa = findViewById(R.id.tvDivisa);
        spendId = getIntent().getStringExtra("spendId");
        btnEditar = findViewById(R.id.btnEditar);
        fStore = FirebaseFirestore.getInstance();

        // Llamar al método que obtiene los gastos pertenecientes al grupo
        getSpends();
    }

    // Método que obtiene los gastos del grupo pulsado
    private void getSpends() {
        fStore.collection("spends").document(spendId)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document != null){
                            if(document.exists()){
                                String spendID = document.getId();
                                String title = document.getString("title");
                                Number amount = document.getDouble("amount");
                                String groupId = document.getString("groupID");
                                groupIda = document.getString("groupID");
                                if(spendId != null && spendId.equalsIgnoreCase(spendID)){
                                    fStore.collection("groups").document(groupId)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if(task1.isSuccessful()){
                                                    DocumentSnapshot documento = task1.getResult();
                                                    if(documento != null && documento.exists()){
                                                        if(documento.exists()){
                                                            String currency = documento.getString("currency");
                                                            String groupID = documento.getId();
                                                            if(groupId != null && groupId.equalsIgnoreCase(groupID)){
                                                                //Establecer el texto de los componentes
                                                                tvDivisa.setText(currency);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                    //Establecer el texto de los componentes
                                    etTitle.setText(title);
                                    etGasto.setText(String.valueOf(amount));
                                }
                            }
                        }
                    }
                });

    }

    public void guardarCambiosGasto(View view) {
        // Obtener los nuevos valores de los campos
        String nuevoTitulo = etTitle.getText().toString();
        double nuevoGasto = Double.parseDouble(etGasto.getText().toString());

        // Obtener la referencia al documento del gasto en Firestore
        DocumentReference spendRef = fStore.collection("spends").document(spendId);

        // Actualizar los valores del gasto
        spendRef.update("title", nuevoTitulo, "amount", nuevoGasto)
                .addOnSuccessListener(aVoid -> {
                    // Éxito al actualizar el gasto
                    Toast.makeText(EditSpendActivity.this, "Gasto actualizado con éxito", Toast.LENGTH_SHORT).show();
                    // Puedes cerrar la actividad después de la actualización si es necesario
                    //finish();
                })
                .addOnFailureListener(e -> {
                    // Error al intentar actualizar el gasto
                    Toast.makeText(EditSpendActivity.this, "No se pudo actualizar el gasto", Toast.LENGTH_SHORT).show();
                });
        toGroupDetails();
    }

    // Método para cuando se edita un grupo volver a la pantalla principal de los gastos del grupo GroupDetailsActivity
    private void toGroupDetails() {
        // Crear un Intent para iniciar la actividad de detalles del grupo
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.putExtra("groupId", groupIda);
        startActivity(intent);
        // Cerrar la actividad actual si es necesario
        finish();
    }
}
