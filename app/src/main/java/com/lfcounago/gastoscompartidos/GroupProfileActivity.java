package com.lfcounago.gastoscompartidos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupProfileActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private EditText etName;
    private TextView tvCurrency, tvCategory;
    private ListView lvUsuarios;
    private List<String> usuarios, uids;
    private Map<String, String> uidToName, nameToUid;
    private ArrayAdapter<String> adapter;
    private FloatingActionButton btnEliminarGrupo, btnAnadirUsuario;
    private Button btnGuardar;
    private String groupId;
    FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile); // Establecer el layout correspondiente

        // Inicializar los atributos de la clase
        etName = findViewById(R.id.textViewName);
        tvCurrency = findViewById(R.id.textViewCurrency);
        tvCategory = findViewById(R.id.textViewCategory);
        lvUsuarios = (ListView) findViewById(R.id.lvUsuarios);
        usuarios = new ArrayList<>();
        uids = new ArrayList<>();
        uidToName = new HashMap<>();
        nameToUid = new HashMap<>();
        groupId = getIntent().getStringExtra("groupId");
        toolbar = findViewById(R.id.toolbar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        btnEliminarGrupo = findViewById(R.id.btnEliminarGrupo);
        btnGuardar = findViewById(R.id.btnGuardar);

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

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambiosGrupo();
            }
        });

        // Crear un adaptador que vincula los nombres de los usuarios con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usuarios);

        setSupportActionBar(toolbar);

        // Llamar al método que obtiene los datos del grupo
        getGroupData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        this.getMenuInflater().inflate(R.menu.group_profile_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean toret = false;

        if (item.getItemId() == R.id.itGroupDetails){
            toGroupDetails();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupSpends) {
            goToGroupSpends();
            toret = true;
        } else if (item.getItemId() == R.id.itGroupLiquidations) {
            goToGroupSpendLiquidations();
            toret = true;
        }

        return toret;
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
                                                            //Actualizar el adaptador
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
                                etName.setText(name);
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
        fStore.collection("groups").document(groupId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if(documentSnapshot.exists()){
                                //Obtener UID del propietario
                                String ownerUid = documentSnapshot.getString("owner");
                                //Obtener el UID del usuario actual
                                String currentUid = fAuth.getCurrentUser().getUid();

                                //Verificar si el usuario logueado es el propietario del grupo
                                if(currentUid.equals(ownerUid)){
                                    fStore.collection("groups").document(groupId).delete() //Accede al documento del grupo para eliminarlo
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    fStore.collection("spends")
                                                            .get() // Obtener todos los documentos de la colección
                                                            .addOnCompleteListener(task -> {
                                                                if(task.isSuccessful()){
                                                                    QuerySnapshot result = task.getResult();
                                                                    if(result != null){
                                                                        for(QueryDocumentSnapshot document : result){
                                                                            String spendId = document.getId();
                                                                            String groupIdspend = document.getString("groupID");
                                                                            if(groupIdspend.equals(groupId)){
                                                                                fStore.collection("spends").document(spendId).delete()
                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void unused) {
                                                                                                //El grupo se elimina con éxito
                                                                                                Toast.makeText(getApplicationContext(), "Gasto perteneciente al grupo eliminado", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Toast.makeText(getApplicationContext(), "El gasto no se pudo eliminar", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                    //El grupo se elimina con éxito
                                                    Toast.makeText(getApplicationContext(), "Grupo eliminado con éxito", Toast.LENGTH_SHORT).show();
                                                    toListUserGroups();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Se produjo un error al intentar eliminar el grupo
                                                    Toast.makeText(getApplicationContext(), "El grupo no se pudo eliminar", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else{
                                    Toast.makeText(getApplicationContext(), "Solo el propietario puede eliminar el grupo", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                    .addOnFailureListener(e -> {
                        // Manejar el error al obtener el documento del grupo
                        Toast.makeText(getApplicationContext(), "Error al obtener información del grupo", Toast.LENGTH_SHORT).show();
                    });

    }

    // Método para cuando se elimina grupo volver a la pantalla principal de la aplicación ListUserGroupsActivity
    private void toListUserGroups() {
        // Crear un Intent para iniciar la actividad de detalles del grupo
        Intent intent = new Intent(this, ListUserGroupsActivity.class);
        startActivity(intent);
        // Cerrar la actividad actual si es necesario
        finish();
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

    private void guardarCambiosGrupo() {

        String nuevoNombre = etName.getText().toString();

        if (!nuevoNombre.isEmpty()) {
            // Actualizar el nombre en la base de datos
            actualizarGrupo(nuevoNombre);
        } else {
            Toast.makeText(getApplicationContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarGrupo(String nuevoNombre) {
        // Crear un mapa con el nuevo nombre
        Map<String, Object> newName = new HashMap<>();
        newName.put("name", nuevoNombre);

        // Actualizar el documento del grupo en la colección "groups" en la base de datos
        fStore.collection("groups").document(groupId).update(newName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Ahora, actualiza el nombre del grupo solo en los documentos de la colección "spends" que pertenecen a ese grupo
                        fStore.collection("spends")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        QuerySnapshot result = task.getResult();
                                        if(result != null){
                                            for(QueryDocumentSnapshot document : result){
                                                String spendId = document.getId();
                                                String groupID = document.getString("groupID");
                                                if(groupID.equals(groupId)){
                                                    fStore.collection("spends").document(spendId)
                                                            .update("group", nuevoNombre)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    // Éxito al actualizar el nombre del grupo en la colección "spends"
                                                                    Toast.makeText(getApplicationContext(), "Nombre del grupo actualizado en la colección 'spends'", Toast.LENGTH_SHORT).show();
                                                                    // Actualizar la interfaz de usuario
                                                                    etName.setText(nuevoNombre);
                                                                    getGroupData();
                                                                    toGroupDetails();
                                                                    // Refrescar el listview también por si acaso
                                                                    adapter.notifyDataSetChanged();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Error al intentar actualizar el nombre del grupo en la colección "spends"
                                                                    Toast.makeText(getApplicationContext(), "No se pudo actualizar el nombre del grupo en la colección 'spends'", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error al intentar actualizar el nombre del grupo en la colección "groups"
                        Toast.makeText(getApplicationContext(), "No se pudo actualizar el nombre del grupo", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Método para cuando se edita un grupo volver a la pantalla principal de los gastos del grupo GroupDetailsActivity
    private void toGroupDetails() {
        // Crear un Intent para iniciar la actividad de detalles del grupo
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
        // Cerrar la actividad actual si es necesario
        finish();
    }

    public void goToGroupSpends() {
        //CAMBIAR activity
        Intent intent = new Intent(this, GroupProfileActivity.class);
        intent.putExtra("groupId", groupId);
        // Iniciar la actividad
        startActivity(intent);
    }

    public void goToGroupSpendLiquidations() {
        //CAMBIAR activity
        Intent intent = new Intent(this, GroupProfileActivity.class);
        intent.putExtra("groupId", groupId);
        // Iniciar la actividad
        startActivity(intent);
    }


}