package com.lfcounago.gastoscompartidos;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

public class ProfileActivity extends AppCompatActivity {

    // Declarar los atributos de la clase
    private static final int GALLERY_INTENT_CODE = 1023;
    TextView tvFullName, tvEmail, tvPhone, tvVerifyMsg;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;
    Button btResendCode;
    Button btResetPassLocal, btChangeProfileImage;
    FirebaseUser fUser;
    ImageView ivProfileImage;
    StorageReference fStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicialización de las variables para los elementos de la interfaz
        tvPhone = findViewById(R.id.profilePhone);
        tvFullName = findViewById(R.id.profileName);
        tvEmail = findViewById(R.id.profileEmail);
        btResetPassLocal = findViewById(R.id.resetPasswordLocal);
        ivProfileImage = findViewById(R.id.profileImage);
        btChangeProfileImage = findViewById(R.id.changeProfile);

        // Inicialización de las variables para acceder a la base de datos de Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fStorageReference = FirebaseStorage.getInstance().getReference();

        // Obtención de la referencia al archivo de la imagen de perfil del usuario en Firebase Storage
        StorageReference profileRef = fStorageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        // Descarga de la imagen de perfil y asignación al ImageView correspondiente
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(ivProfileImage);
            }
        });

        // Inicialización de los elementos para verificar el correo electrónico del usuario
        btResendCode = findViewById(R.id.resendCode);
        tvVerifyMsg = findViewById(R.id.verifyMsg);


        userId = fAuth.getCurrentUser().getUid();
        fUser = fAuth.getCurrentUser();

        // Si el usuario no se ha verificado, se muestran los elementos para enviar el código de verificación
        if (!fUser.isEmailVerified()) {
            tvVerifyMsg.setVisibility(View.VISIBLE);
            btResendCode.setVisibility(View.VISIBLE);

            // Al pulsar el botón, se envía el código de verificación al correo del usuario
            btResendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "El email de verificación ha sido enviado", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                        }
                    });
                }
            });
        }

        // Obtención de la referencia al documento del usuario en la colección "users" de Firebase Firestore
        DocumentReference documentReference = fStore.collection("users").document(userId);
        // Listener de los cambios en el documento y actualización de los elementos de la interfaz con los datos del usuario
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    tvPhone.setText(documentSnapshot.getString("phone"));
                    tvFullName.setText(documentSnapshot.getString("fName"));
                    tvEmail.setText(documentSnapshot.getString("email"));

                }else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });

        // Al pulsar el botón, se muestra un diálogo para cambiar la contraseña local del usuario
        btResetPassLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetPassword = new EditText(v.getContext());

                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Cambiar contraseña");
                passwordResetDialog.setMessage("Introduzca una contraseña mayor de 6 caracteres.");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Extraer el correo electrónico y enviar el enlace de restablecimiento
                        String newPassword = resetPassword.getText().toString();

                        // Actualizar la contraseña del usuario en Firebase Auth
                        fUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfileActivity.this, "Contraseña cambiada de forma exitosa", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close
                    }
                });

                passwordResetDialog.create().show();

            }
        });

        // Enviar los datos del usuario a la actividad de edición de perfil
        btChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery
                Intent i = new Intent(v.getContext(),EditProfileActivity.class);
                i.putExtra("fullName", tvFullName.getText().toString());
                i.putExtra("email", tvEmail.getText().toString());
                i.putExtra("phone", tvPhone.getText().toString());
                startActivity(i);
            }
        });
    }

    // Iniciar la actividad de inicio de sesión
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

}
