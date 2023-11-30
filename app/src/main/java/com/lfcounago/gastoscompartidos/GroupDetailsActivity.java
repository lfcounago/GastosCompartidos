package com.lfcounago.gastoscompartidos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GroupDetailsActivity extends AppCompatActivity {

    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        groupId = getIntent().getStringExtra("groupId");

        //Vista de gastos copiar de ListUserGroupsActivity
        //Coger groupId de la actividad anterior
        //
    }

    // Método que se ejecuta al pulsar el texto del nombre del grupo
    public void goToDeatilsGroup(View view) {
        // Crear un intent para iniciar la actividad GroupProfileActivity
        Intent intent = new Intent(GroupDetailsActivity.this, GroupProfileActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    // Definir el método que se ejecuta al pulsar el botón de ingresar saldo
    public void goToIngresarSaldo(View view) {
        // Crear un intent para iniciar la actividad AddSpendActivity a la que se le pasa el groupId
        Intent intent = new Intent(view.getContext(), AddSpendActivity.class);

        intent.putExtra("groupId", groupId);

        // Iniciar la actividad
        startActivity(intent);
    }
}