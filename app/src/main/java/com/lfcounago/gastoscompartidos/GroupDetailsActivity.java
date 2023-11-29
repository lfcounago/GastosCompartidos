package com.lfcounago.gastoscompartidos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupDetailsActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private TextView tvName, tvCurrency, tvCategory, tvDescription;
    private FloatingActionButton btnEliminarGrupo;
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
        btnEliminarGrupo = findViewById(R.id.btnEliminarGrupo);
        btnEliminarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogoConfirmacion();
            }
        });

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

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Grupo");
        builder.setMessage("¿Estás seguro de que deseas eliminar el grupo?");
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Lógica para eliminar el grupo
                eliminarGrupo();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Acción al hacer clic en Cancelar (puede no hacer nada o mostrar un mensaje, por ejemplo)
                Toast.makeText(getApplicationContext(), "Eliminación de grupo cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Definir el método que se ejecuta al pulsar el botón de eliminar grupo
    private void eliminarGrupo() {
        fStore.collection("groups").document(groupId).delete() //Accede al documento del grupo para eliminarlo
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //El grupo se elimina con éxito
                        Toast.makeText(getApplicationContext(), "Grupo eliminado con éxito", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Se produjo un error al intentar eliminar el grupo
                        Toast.makeText(getApplicationContext(), "El grupo no se pudo eliminar", Toast.LENGTH_SHORT).show();
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