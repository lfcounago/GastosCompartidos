package com.lfcounago.gastoscompartidos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettleDebtActivity extends AppCompatActivity {

    // Declaración de variables para los elementos de la interfaz
    private TextView tvTitulo, tvCantidad, tvPagador, tvUsuDeuda;
    private Button btGuardar;

    // Declaración de variables para almacenar los datos
    private String usuConectado, titulo, grupo, groupId, userId;
    private double amount;

    // Declaración de variables para acceder a la base de datos de Firebase
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_debt);

        // Inicialización de las variables para los elementos de la interfaz
        tvTitulo = findViewById(R.id.tvTitulo);
        tvCantidad = findViewById(R.id.tvCantidad);
        tvPagador = findViewById(R.id.tvPagador);
        tvUsuDeuda = findViewById(R.id.tvUsuDeuda);
        btGuardar = findViewById(R.id.btGuardar);

        fStore = FirebaseFirestore.getInstance();

        //Obtener el groupId que se pasa con el intent
        groupId = getIntent().getStringExtra("groupId");
        userId = getIntent().getStringExtra("IDUser");
        amount = getIntent().getDoubleExtra("spendUsu", 0.0);
        usuConectado = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (groupId == null && userId == null && usuConectado == null) {
            Toast.makeText(this, "Datos nulos", Toast.LENGTH_SHORT).show();
        }

        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        // Crear un formato de fecha
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // Obtener la fecha en formato de cadena
        String fechaActual = dateFormat.format(calendar.getTime());

        //Obtener referencia al documento del usuario conectado que paga la deuda por el ID
        fStore.collection("users").document(usuConectado)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Obtener el nombre del primer usuario
                            String nameUsuPag = document.getString("fName");
                            tvPagador.setText(nameUsuPag);
                        }
                    }
                });
        //Obtener referencia al documento del usuario conectado que se le paga la deuda por su ID
        DocumentReference userRef2 = fStore.collection("users").document(userId);

        userRef2.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Obtener el nombre del primer usuario
                            String nameUsu = document.getString("fName");
                            tvUsuDeuda.setText(nameUsu);
                        }
                    }
                });

        tvTitulo.setText("Liquidación");
        tvCantidad.setText(String.valueOf(amount));

        //Obtener el nombre del grupo mediante su ID
        fStore.collection("groups").document(groupId)
                .get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){
                                    String nameGroup = document.getString("name");
                                    grupo = nameGroup;
                                }
                            }
                        });

        // Listener para el botón de guardar
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titulo = "Liquidación";
                // Comprobar si los datos son válidos
                if (validarDatos()) {
                    // Crear un mapa para almacenar los datos del gasto
                    Map<String, Object> gasto = new HashMap<>();
                    gasto.put("title", titulo);
                    gasto.put("date", fechaActual);
                    gasto.put("amount", amount);
                    gasto.put("payer", usuConectado);
                    gasto.put("sharedWith", userId);
                    gasto.put("groupID", groupId);
                    gasto.put("group", grupo);
                    // Añadir el documento del gasto a la colección spends de Firebase
                    fStore.collection("spends")
                            .add(gasto)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettleDebtActivity.this, "Liquidación guardada con éxito", Toast.LENGTH_SHORT).show();
                                        toDebtLiquidation();
                                    } else {
                                        Toast.makeText(SettleDebtActivity.this, "Error al guardar el gasto", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(SettleDebtActivity.this, "Los datos no son válidos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para validar los datos introducidos por el usuario
    private boolean validarDatos() {
        if (titulo.isEmpty()) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }
        return true;
    }

    // Método para cuando se elimina grupo volver a la pantalla principal de la aplicación DebtLiquidationActivity
    private void toDebtLiquidation() {
        // Crear un Intent para iniciar la actividad de detalles del grupo
        Intent intent = new Intent(this, DebtLiquidationActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
        // Cerrar la actividad actual si es necesario
        finish();
    }

    public void goToActividadAnterior(View view) {
        // Crear un intent para iniciar la actividad DebtLiquidationActivity
        Intent intent = new Intent(this, DebtLiquidationActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private void actualizarInterfazUsuario() {
        tvTitulo.setText("Liquidación");
        tvCantidad.setText(String.valueOf(amount));
    }

}