package com.lfcounago.gastoscompartidos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    // Declarar los atributos de la clase
    public static final String TAG = "TAG";
    EditText etFullName, etEmail, etPhone;
    ImageView ivProfileImage;
    Button btSave;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser fUser;
    StorageReference fStorageReference;
    private Window window;
    private String primaryDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Obtener los datos del intento que inició esta actividad
        Intent data = getIntent();
        final String fullName = data.getStringExtra("fullName");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phone");

        // Inicialización de las variables para acceder a la base de datos de Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        fStorageReference = FirebaseStorage.getInstance().getReference();

        // Inicializar los atributos de la clase
        etFullName = findViewById(R.id.profileFullName);
        etEmail = findViewById(R.id.profileEmailAddress);
        etPhone = findViewById(R.id.profilePhoneNo);
        ivProfileImage = findViewById(R.id.profileImageView);
        btSave = findViewById(R.id.saveProfileInfo);
        primaryDark = "#063642";

        //Parámetros para cambiar el color de la barra de estado
        this.window = getWindow();
        window.setStatusBarColor(Color.parseColor(primaryDark));

        // Crear una referencia a la imagen de perfil del usuario en el almacenamiento de Firebase
        StorageReference profileRef = fStorageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");

        // Obtener la URL de descarga de la imagen de perfil
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Cargar la imagen de perfil en la vista de la imagen usando la biblioteca Picasso
                Picasso.get().load(uri).into(ivProfileImage);
            }
        });

        // Establecer un escuchador de clics en la vista de la imagen de perfil
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        // Listener en el botón de guardar
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etFullName.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty() || etPhone.getText().toString().isEmpty()){
                    Toast.makeText(EditProfileActivity.this, "One or Many fields are empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String email = etEmail.getText().toString();
                fUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference docRef = fStore.collection("users").document(fUser.getUid());
                        Map<String,Object> edited = new HashMap<>();
                        // Añadir los datos al mapa
                        edited.put("email",email);
                        edited.put("fName", etFullName.getText().toString());
                        edited.put("phone", etPhone.getText().toString());
                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                                finish();
                            }
                        });
                        Toast.makeText(EditProfileActivity.this, "Email is changed.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this,   e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

        // Establecer las variables
        etEmail.setText(email);
        etFullName.setText(fullName);
        etPhone.setText(phone);

        Log.d(TAG, "onCreate: " + fullName + " " + email + " " + phone);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){ // Si el código de solicitud es 1000, significa que se ha seleccionado una imagen de la galería
            if(resultCode == Activity.RESULT_OK){ // Si el resultado es OK, significa que se ha obtenido una imagen válida
                Uri imageUri = data.getData();

                uploadImageToFirebase(imageUri);


            }
        }

    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Crear una referencia al archivo de la imagen de perfil del usuario en el almacenamiento de Firebase
        final StorageReference fileRef = fStorageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Obtener la URL de descarga de la imagen subida
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Cargar la imagen en la vista de la imagen usando la biblioteca Picasso
                        Picasso.get().load(uri).into(ivProfileImage);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToActividadAnterior(View view) {
        // Crear un intent para iniciar la actividad ProfileActivity
        Intent intent = new Intent(this, ProfileActivity.class);

        startActivity(intent);
    }
}