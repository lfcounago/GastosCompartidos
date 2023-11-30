package com.lfcounago.gastoscompartidos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupProfileActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private TextView tvName, tvCurrency, tvCategory;
    private ListView lvUsuarios;
    private List<String> usuarios, uids;
    private Map<String, String> uidToName, nameToUid;
    private ArrayAdapter<String> adapter;
    private FloatingActionButton btnEliminarGrupo, btnAnadirUsuario;
    private String groupId;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile); // Establecer el layout correspondiente

        // Inicializar los atributos de la clase
        tvName = findViewById(R.id.textViewName);
        tvCurrency = findViewById(R.id.textViewCurrency);
        tvCategory = findViewById(R.id.textViewCategory);
        lvUsuarios = (ListView) findViewById(R.id.lvUsuarios);
        usuarios = new ArrayList<>();
        uids = new ArrayList<>();
        uidToName = new HashMap<>();
        nameToUid = new HashMap<>();
        groupId = getIntent().getStringExtra("groupId");
        fStore = FirebaseFirestore.getInstance();
        btnEliminarGrupo = findViewById(R.id.btnEliminarGrupo);

        lvUsuarios.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el nombre del usuario seleccionado
                String selectedUserName = usuarios.get(position);

                // Mostrar un diálogo de confirmación para eliminar el usuario
                mostrarDialogoEliminarUsuario(selectedUserName);

                return true; // Indicar que se ha gestionado el evento
            }
        });
        btnEliminarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogoConfirmacion();
            }
        });

        // Crear un adaptador que vincula los nombres de los usuarios con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuarios);

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
                                uids = (List<String>) document.get("users");
                                // Limpiar la lista de nombres de los usuarios
                                usuarios.clear();
                                // Limpiar los mapas de correspondencia entre UID y nombre de los usuarios
                                uidToName.clear();
                                nameToUid.clear();
                                // Recorrer la lista de UID de los usuarios
                                for (String uid : uids) {
                                    // Obtener el documento correspondiente al UID del usuario de la colección users de Firebase
                                    fStore.collection("users").document(uid)
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
                                                            lvUsuarios.setAdapter(adapter);
                                                            adapter.notifyDataSetChanged();
                                                        } else {
                                                            Toast.makeText(GroupProfileActivity.this, "El documento del usuario no existe", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(GroupProfileActivity.this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                                // Establecer el texto de los componentes con los datos obtenidos
                                tvName.setText(name);
                                tvCurrency.setText(currency);
                                tvCategory.setText(category);
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

    // Método para mostrar el diálogo de confirmación para eliminar un usuario
    private void mostrarDialogoEliminarUsuario(String userName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Usuario");
        builder.setMessage("¿Estás seguro de que deseas eliminar el usuario " + userName + "?");
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Lógica para eliminar al usuario
                eliminarUsuario(userName);
            }
        });
        builder.setNegativeButton("Cancelar", null); // No es necesario gestionar la acción de Cancelar aquí

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para eliminar un usuario
    private void eliminarUsuario(String userName) {
        // Obtener el UID del usuario a partir del nombre
        String uidToDelete = nameToUid.get(userName);

        // Eliminar el UID de la lista de usuarios y actualizar la base de datos
        uids.remove(uidToDelete);

        // Actualizar la base de datos con la nueva lista de usuarios
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("users", uids);

        fStore.collection("groups").document(groupId).update(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // El usuario se elimina con éxito
                        Toast.makeText(getApplicationContext(), "Usuario eliminado con éxito", Toast.LENGTH_SHORT).show();
                        // Actualizar la interfaz de usuario
                        getGroupData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Se produjo un error al intentar eliminar al usuario
                        Toast.makeText(getApplicationContext(), "No se pudo eliminar al usuario", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método que se ejecuta al pulsar el boton mas
    public void goToAddUser(View view) {
        // Crear un intent para iniciar la actividad GroupProfileActivity
        Intent intent = new Intent(GroupProfileActivity.this, AddUserActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }




}