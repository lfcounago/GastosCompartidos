package com.lfcounago.gastoscompartidos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TotalExpensesActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private TextView tvGastoTotal, tvTotalPagoUsu, tvCuotaTotal, tvPagoRealizadoUsu, tvPagosRecibidos;
    private String gid;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_expenses);

        // Inicializar los atributos de la clase
        tvGastoTotal = findViewById(R.id.tvGastoTotal);
        tvTotalPagoUsu = findViewById(R.id.tvTotalPagoUsu);
        tvCuotaTotal = findViewById(R.id.tvCuotaTotal);
        tvPagoRealizadoUsu = findViewById(R.id.tvPagoRealizadoUsu);
        tvPagosRecibidos = findViewById(R.id.tvPagosRecibidos);
        gid = "bmMYudVqJVuPzEaWwVNj";
        fStore = FirebaseFirestore.getInstance();

        // Llamar al método que obtiene los gastos
        getSpends(gid);
    }

    //Recorre la colección "spends" para obtener todos los datos necesarios
    private void getSpends(String groupId) {
        List<Number> gastos = new ArrayList<>();

        // Realizar una consulta a la colección "spends" de la base de datos de Firestore
        fStore.collection("spends")
                .get() // Obtener todos los documentos de la colección
                .addOnCompleteListener(task -> { // Añadir un listener que se ejecuta cuando la tarea se completa
                    if (task.isSuccessful()) {
                        // Obtener el resultado de la tarea como una lista de documentos
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            // Recorrer cada documento del resultado
                            for (QueryDocumentSnapshot document : result) {
                                // Obtener los valores correspondientes
                                Number amount = document.getDouble("amount");
                                String groupID = document.getString("groupID");
                                String payer = document.getString("payer");
                                if (groupID != null && groupID.equals(groupId) && amount != null) { // Si la lista de gastos contiene el id del grupo actual
                                    // Añadir el gasto del grupo a la lista de gastos del grupo
                                    gastos.add(amount);
                                    /*// Añadir el nombre del grupo, la divisa y la categoría a la lista de nombres de los grupos
                                    groupNames.add(groupName + "\n" + groupCurrency + " - " + groupCategory);*/
                                }
                            }
                            totalExpenses(gastos); //Llama a la función que obtiene el total de los gastos del grupo
                        }
                    }
                });
    }

    private void totalExpenses(List<Number> gastos) {
        double gastosTotales = 0;
        for (Number amount : gastos) {
            gastosTotales += amount.doubleValue();
        }
        tvGastoTotal.setText(String.valueOf(gastosTotales));
    }

}
