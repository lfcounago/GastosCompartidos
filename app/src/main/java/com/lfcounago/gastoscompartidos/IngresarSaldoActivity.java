package com.lfcounago.gastoscompartidos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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

public class IngresarSaldoActivity extends AppCompatActivity {

    // Declaración de variables para los elementos de la interfaz
    private EditText etTitulo, etCantidad;
    private TextView tvFecha, tvDivisa;
    private Spinner spPagador, spGrupo;
    private Button btFecha, btGuardar;
    private CheckBox cbTodos;

    // Declaración de variables para almacenar los datos introducidos por el usuario
    private String titulo, fecha, pagador, grupo, nombreGrupo;
    private double cantidad;
    private List<String> compartidos;

    // Declaración de variables para acceder a la base de datos de Firebase
    private FirebaseFirestore db;
    private DocumentReference docRef;

    // Declaración de variables para almacenar los datos obtenidos de Firebase
    private List<String> grupos, usuarios, uids;
    private Map<String, String> uidToName, nameToUid;
    private String uidGrupo, divisa;

    // Declaración de variables para el calendario y el formato de fecha
    private Calendar calendario;
    private DatePickerDialog.OnDateSetListener date;
    private SimpleDateFormat formatoFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresar_saldo);

        // Inicialización de las variables para los elementos de la interfaz
        etTitulo = findViewById(R.id.etTitulo);
        etCantidad = findViewById(R.id.etCantidad);
        tvFecha = findViewById(R.id.tvFecha);
        tvDivisa = findViewById(R.id.tvDivisa);
        spPagador = findViewById(R.id.spPagador);
        spGrupo = findViewById(R.id.spGrupo);
        btFecha = findViewById(R.id.btFecha);
        btGuardar = findViewById(R.id.btGuardar);
        cbTodos = findViewById(R.id.cbTodos);

        // Inicialización de las variables para almacenar los datos introducidos por el usuario
        titulo = "";
        fecha = "";
        pagador = "";
        grupo = "";
        nombreGrupo = "";
        cantidad = 0.0;
        compartidos = new ArrayList<>();

        // Inicialización de las variables para acceder a la base de datos de Firebase
        db = FirebaseFirestore.getInstance();
        docRef = null;

        // Inicialización de las variables para almacenar los datos obtenidos de Firebase
        grupos = new ArrayList<>();
        usuarios = new ArrayList<>();
        uids = new ArrayList<>();
        uidToName = new HashMap<>();
        nameToUid = new HashMap<>();
        uidGrupo = "";
        divisa = "";

        // Inicialización de las variables para el calendario y el formato de fecha
        calendario = Calendar.getInstance();
        formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Establecer la fecha actual como valor predeterminado del TextView de la fecha
        actualizarFecha();

        // Establecer el listener para el botón de seleccionar fecha
        btFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar un diálogo para elegir la fecha
                new DatePickerDialog(IngresarSaldoActivity.this, date, calendario
                        .get(Calendar.YEAR), calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Establecer el listener para el cambio de fecha
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // Actualizar el calendario con la fecha elegida
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, monthOfYear);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // Actualizar el TextView de la fecha con la fecha elegida
                actualizarFecha();
            }

        };

        // Obtener la lista de grupos de la colección groups de Firebase
        db.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Recorrer los documentos de la colección
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Añadir el nombre del grupo a la lista de grupos
                                grupos.add(document.getString("name"));
                            }
                            // Crear un adaptador para el Spinner de grupos con la lista de grupos
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(IngresarSaldoActivity.this,
                                    android.R.layout.simple_spinner_item, grupos);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            // Establecer el adaptador para el Spinner de grupos
                            spGrupo.setAdapter(adapter);
                        } else {
                            // Mostrar un mensaje de error si la consulta falla
                            Toast.makeText(IngresarSaldoActivity.this, "Error al obtener los grupos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Establecer el listener para el cambio de grupo
        spGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el nombre del grupo seleccionado
                nombreGrupo = parent.getItemAtPosition(position).toString();
                // Obtener el documento correspondiente al nombre del grupo seleccionado de la colección groups de Firebase
                db.collection("groups").whereEqualTo("name", nombreGrupo)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Comprobar si hay algún documento que coincida con el nombre del grupo
                                    if (!task.getResult().isEmpty()) {
                                        // Obtener el primer documento que coincida con el nombre del grupo
                                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                        // Obtener el UID del grupo
                                        uidGrupo = document.getId();
                                        // Obtener la divisa del grupo y mostrarla en el TextView de la divisa
                                        divisa = document.getString("currency");
                                        tvDivisa.setText(divisa);
                                        // Obtener la lista de UID de los usuarios del grupo
                                        uids = (List<String>) document.get("users");
                                        // Limpiar la lista de nombres de los usuarios
                                        usuarios.clear();
                                        // Limpiar los mapas de correspondencia entre UID y nombre de los usuarios
                                        uidToName.clear();
                                        nameToUid.clear();
                                        // Recorrer la lista de UID de los usuarios
                                        for (String uid : uids) {
                                            // Obtener el documento correspondiente al UID del usuario de la colección users de Firebase
                                            db.collection("users").document(uid)
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                // Obtener el documento del usuario
                                                                DocumentSnapshot document = task.getResult();
                                                                if (document.exists()) {
                                                                    // Obtener el nombre del usuario
                                                                    String name = document.getString("fName");
                                                                    // Añadir el nombre del usuario a la lista de nombres de los usuarios
                                                                    usuarios.add(name);
                                                                    // Añadir la correspondencia entre el UID y el nombre del usuario a los mapas
                                                                    uidToName.put(uid, name);
                                                                    nameToUid.put(name, uid);
                                                                    // Crear un adaptador para el Spinner de pagador con la lista de nombres de los usuarios
                                                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(IngresarSaldoActivity.this,
                                                                            android.R.layout.simple_spinner_item, usuarios);
                                                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                                    // Establecer el adaptador para el Spinner de pagador
                                                                    spPagador.setAdapter(adapter);
                                                                    // Crear los CheckBox para los usuarios con los que se comparte el gasto
                                                                    crearCheckBox();
                                                                } else {
                                                                    // Mostrar un mensaje de error si el documento del usuario no existe
                                                                    Toast.makeText(IngresarSaldoActivity.this, "El documento del usuario no existe", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                // Mostrar un mensaje de error si la consulta del usuario falla
                                                                Toast.makeText(IngresarSaldoActivity.this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    } else {
                                        // Mostrar un mensaje de error si no hay ningún documento que coincida con el nombre del grupo
                                        Toast.makeText(IngresarSaldoActivity.this, "Error: no hay ningún documento que coincida con el nombre del grupo", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Mostrar un mensaje de error si la consulta del grupo falla
                                    Toast.makeText(IngresarSaldoActivity.this, "Error al obtener el grupo", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no se selecciona ningún grupo
            }
        });

        // Establecer el listener para el cambio de pagador
        spPagador.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el nombre del pagador seleccionado
                pagador = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no se selecciona ningún pagador
            }
        });

        // Establecer el listener para el CheckBox de todos los usuarios
        cbTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Comprobar si el CheckBox está marcado o no
                if (cbTodos.isChecked()) {
                    // Marcar todos los CheckBox de los usuarios
                    marcarCheckBox(true);
                } else {
                    // Desmarcar todos los CheckBox de los usuarios
                    marcarCheckBox(false);
                }
            }
        });

        // Establecer el listener para el botón de guardar
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los datos introducidos por el usuario
                titulo = etTitulo.getText().toString();
                fecha = tvFecha.getText().toString();
                cantidad = Double.parseDouble(etCantidad.getText().toString());
                // Comprobar si los datos son válidos
                if (validarDatos()) {
                    // Crear un mapa para almacenar los datos del gasto
                    Map<String, Object> gasto = new HashMap<>();
                    gasto.put("title", titulo);
                    gasto.put("date", fecha);
                    gasto.put("amount", cantidad);
                    gasto.put("payer", nameToUid.get(pagador));
                    gasto.put("sharedWith", compartidos);
                    gasto.put("group", nombreGrupo);
                    // Añadir el documento del gasto a la colección spends de Firebase
                    db.collection("spends")
                            .add(gasto)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        // Mostrar un mensaje de éxito si el gasto se guarda correctamente
                                        Toast.makeText(IngresarSaldoActivity.this, "Gasto guardado con éxito", Toast.LENGTH_SHORT).show();
                                        // Limpiar los campos de entrada
                                        limpiarCampos();
                                    } else {
                                        // Mostrar un mensaje de error si el gasto no se guarda correctamente
                                        Toast.makeText(IngresarSaldoActivity.this, "Error al guardar el gasto", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Mostrar un mensaje de error si los datos no son válidos
                    Toast.makeText(IngresarSaldoActivity.this, "Por favor, introduce datos válidos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para actualizar el TextView de la fecha con el formato deseado
    private void actualizarFecha() {
        tvFecha.setText(formatoFecha.format(calendario.getTime()));
    }

    // Método para crear los CheckBox para los usuarios con los que se comparte el gasto
    private void crearCheckBox() {
        // Obtener el layout donde se van a añadir los CheckBox
        LinearLayout layout = findViewById(R.id.layoutCheckBox);
        // Limpiar el layout de los CheckBox anteriores
        layout.removeAllViews();
        // Recorrer la lista de nombres de los usuarios
        for (String usuario : usuarios) {
            // Crear un nuevo CheckBox con el nombre del usuario
            CheckBox cb = new CheckBox(this);
            cb.setText(usuario);
            // Establecer el listener para el cambio de estado del CheckBox
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Obtener el nombre del usuario asociado al CheckBox
                    String usuario = buttonView.getText().toString();
                    // Comprobar si el CheckBox está marcado o no
                    if (isChecked) {
                        // Añadir el UID del usuario a la lista de compartidos
                        compartidos.add(nameToUid.get(usuario));
                    } else {
                        // Eliminar el UID del usuario de la lista de compartidos
                        compartidos.remove(nameToUid.get(usuario));
                    }
                }
            });
            // Añadir el CheckBox al layout
            layout.addView(cb);
        }
    }

    // Método para marcar o desmarcar todos los CheckBox de los usuarios
    private void marcarCheckBox(boolean estado) {
        // Obtener el layout donde se encuentran los CheckBox
        LinearLayout layout = findViewById(R.id.layoutCheckBox);
        // Recorrer los hijos del layout
        for (int i = 0; i < layout.getChildCount(); i++) {
            // Obtener el hijo en la posición i
            View v = layout.getChildAt(i);
            // Comprobar si el hijo es un CheckBox
            if (v instanceof CheckBox) {
                // Establecer el estado del CheckBox
                ((CheckBox) v).setChecked(estado);
            }
        }
    }

    // Método para validar los datos introducidos por el usuario
    private boolean validarDatos() {
        // Comprobar si el título está vacío
        if (titulo.isEmpty()) {
            return false;
        }
        // Comprobar si la cantidad es cero o negativa
        if (cantidad <= 0) {
            return false;
        }
        // Comprobar si el pagador está vacío
        if (pagador.isEmpty()) {
            return false;
        }
        // Comprobar si la lista de compartidos está vacía
        if (compartidos.isEmpty()) {
            return false;
        }
        // Si todas las condiciones se cumplen, devolver true
        return true;
    }

    // Método para limpiar los campos de entrada
    private void limpiarCampos() {
        etTitulo.setText("");
        etCantidad.setText("");
        actualizarFecha();
        spPagador.setSelection(0);
        cbTodos.setChecked(false);
        marcarCheckBox(false);
    }
}
