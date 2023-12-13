package com.lfcounago.gastoscompartidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListUserGroupsActivity extends AppCompatActivity{

    // Declarar los atributos de la clase
    private ListView lvGroups;
    private ArrayAdapter<String> adapter;
    private List<String> groupNames;
    private List<String> groupIds;
    private String uid;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_groups);

        // Inicializar los atributos de la clase
        lvGroups = findViewById(R.id.listView);
        groupNames = new ArrayList<>();
        groupIds = new ArrayList<>();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        navigationView = findViewById(R.id.navView);
        drawerLayout = findViewById(R.id.dlMenuLateral);

        // Crear un adaptador que vincula los nombres de los grupos con la vista del listView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
        lvGroups.setAdapter(adapter); // Establecer el adaptador al listView

        // Añadir un listener al listView que se activa cuando se hace clic en un elemento de la lista
        lvGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String groupId = groupIds.get(position);
                Intent intent = new Intent(ListUserGroupsActivity.this, GroupDetailsActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        // Configurar el ActionBarDrawerToggle para el menú lateral
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Configurar la barra de acción
        setSupportActionBar(findViewById(R.id.toolbar));

        // Configurar el botón de "Atrás"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Manejar eventos del menú lateral
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.mnProfile){
                    goToProfile();
                } else if (itemId == R.id.mnBalances) {
                    goToBalances();
                } else if (itemId == R.id.mnLiquidations) {
                    goToLiquidations();
                } else if (itemId == R.id.mnLogOut) {
                    goToLogin();
                }

                // Cerrar el menú lateral después de la selección
                drawerLayout.closeDrawers();
                return true;
            }
        });

        //Llamar al método que configura la barra de herramientas
        setToolBar();

        // Llamar al método que obtiene los grupos a los que pertenece el usuario
        getGroups();
    }

    // Método para configurar la barra de herramientas
    private void setToolBar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        this.getMenuInflater().inflate( R.menu.nav_options, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
                /*
            case R.id.mnProfile:
                goToProfile();
                return true;
            case R.id.mnLiquidations:
                goToLiquidations();
                return true;
            case R.id.mnCerrarSesion:
                fAuth.signOut();
                return true;

                 */
        }
        return super.onOptionsItemSelected(item);
    }

    // Método que obtiene los grupos a los que pertenece el usuario
    private void getGroups() {
        // Realizar una consulta a la colección "groups" de la base de datos de Firestore
        fStore.collection("groups")
                .get() // Obtener todos los documentos de la colección
                .addOnCompleteListener(task -> { // Añadir un listener que se ejecuta cuando la tarea se completa
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
                                String groupId = document.getId();
                                String groupName = document.getString("name");
                                String groupCurrency = document.getString("currency");
                                String groupCategory = document.getString("category");
                                List<String> groupUsers = (List<String>) document.get("users");
                                if (groupUsers != null) {
                                    if (groupUsers.contains(uid)) { // Si la lista de usuarios contiene el id del usuario actual
                                        // Añadir el id del grupo a la lista de ids de los grupos
                                        groupIds.add(groupId);
                                        // Añadir el nombre del grupo, la divisa y la categoría a la lista de nombres de los grupos
                                        groupNames.add(groupName + "\n" + groupCurrency + " - " + groupCategory);
                                    }
                                }
                            }
                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    // Método que se ejecuta al pulsar el botón de ingresar saldo
    public void goToCrearGrupo(View view) {
        // Crear un intent para iniciar la actividad CreateGroupActivity
        Intent intent = new Intent(this, CreateGroupActivity.class);

        startActivity(intent);
    }

    // Método que se ejecuta al pulsar el botón de perfil en el menu
    public void goToProfile(){
        // Crear un intent para iniciar la actividad CreateGroupActivity
        Intent intent = new Intent(this, ProfileActivity.class);

        startActivity(intent);
    }

    // Método que se ejecuta al pulsar el botón de liquidaciones en el menu
    public void goToLiquidations(){
        // Crear un intent para iniciar la actividad CreateGroupActivity
        Intent intent = new Intent(this, LiquidationsActivity.class);

        startActivity(intent);
    }

    // Método que se ejecuta al pulsar el botón de saldos en el menu
    public void goToBalances(){
        // Crear un intent para iniciar la actividad BalanceActivity
        Intent intent = new Intent(this, BalanceActivity.class);

        startActivity(intent);
    }

    // Método que se ejecuta al pulsar el botón de cerrar sesion en el menu
    public void goToLogin(){
        FirebaseAuth.getInstance().signOut();
        // Crear un intent para iniciar la actividad LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
    }
}