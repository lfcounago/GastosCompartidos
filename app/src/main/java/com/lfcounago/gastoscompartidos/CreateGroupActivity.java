package com.lfcounago.gastoscompartidos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {


    public static final String TAG = "TAG";
    EditText mGroupName;
    Spinner mGroupCategory, mGroupCurrency;
    Button mCreateGroupBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mGroupName = findViewById(R.id.groupName);
        mGroupCategory = findViewById(R.id.groupCategory);
        mGroupCurrency = findViewById(R.id.groupCurrency);
        mCreateGroupBtn = findViewById(R.id.createGroupBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        Spinner groupCurrency = findViewById(R.id.groupCurrency);
        ArrayAdapter<CharSequence> adapterCurrency = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapterCurrency.setDropDownViewResource(android.R.layout.simple_spinner_item);
        groupCurrency.setAdapter(adapterCurrency);

        Spinner groupCategory = findViewById(R.id.groupCategory);
        ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_item);
        groupCategory.setAdapter(adapterCategory);

        mCreateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String groupName = mGroupName.getText().toString().trim();
                final String groupCategory = mGroupCategory.getSelectedItem().toString();
                final String groupCurrency = mGroupCurrency.getSelectedItem().toString();

                if (TextUtils.isEmpty(groupName)) {
                    mGroupName.setError("Group name is Required.");
                    return;
                }

                if (TextUtils.isEmpty(groupCurrency)) {
                    Toast.makeText(getApplicationContext(), "Group currency is Required.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                Toast.makeText(CreateGroupActivity.this, "Group Created.", Toast.LENGTH_SHORT).show();
                userID = fAuth.getCurrentUser().getUid();
                DocumentReference documentReference = fStore.collection("groups").document();
                Map<String, Object> group = new HashMap<>();
                group.put("gName", groupName);
                group.put("gCurrency", groupCurrency);
                group.put("gCategory", groupCategory);
                group.put("owner", userID);
                documentReference.set(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: group Profile is created for " + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                    }
                });
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));


            }
        });
    }
}
