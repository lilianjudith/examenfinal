package proyectos.create.examen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText nombre, correo, clave;
    Button registro, ingresar;

    ProgressBar progressBar;

    String name ,email, password; // Estas variables van a contener lo del correo y clave

    FirebaseAuth autentificacion;
    DatabaseReference base_de_datos;

    /* VARIABLES DE LOS DATOS QUE VAMOS A REGISTRAR ( SERVICIO DE REALTIME DATABASE )
    private String nombre_bd = "";
    private String correo_bd = "";
    private String clave_bd = "";*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializamos Firebase (Autentificacion, Realtime Database)
        autentificacion = FirebaseAuth.getInstance();
        base_de_datos = FirebaseDatabase.getInstance().getReference();

        //Casteo de los Objetos (Widgets)
        correo = findViewById(R.id.txt_email);
        clave = findViewById(R.id.txt_clave);
        progressBar = findViewById(R.id.loading);
        nombre = findViewById(R.id.txt_nombre);
        registro = findViewById(R.id.btn_registro);
        ingresar = findViewById(R.id.btn_iniciar_sesion);

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name = nombre.getText().toString().trim();
                email = correo.getText().toString().trim();
                password  = clave.getText().toString().trim();

                /*if(email.length() > 0 && password.length() > 0){
                    registrarUsuario(email, password);
                }else{
                    Toast.makeText(MainActivity.this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show();
                }*/

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //Set error and focus to email Edittext
                    correo.setError("Correo Electronico invalido");
                    correo.setFocusable(true);
                }
                else if(password.length()<6){
                    //Set error and focus to password Edittext
                    clave.setError("Contraseña muy corta");
                    clave.setFocusable(true);
                }
                else{
                    registrarUsuario(email,password);
                }

            }
        });

        ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = correo.getText().toString().trim();
                password = clave.getText().toString().trim();

                if(email.length() > 0 && password.length() > 0){
                    IniciarSesion(email, password);
                }else{
                    Toast.makeText(MainActivity.this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void registrarUsuario(String email, String password){

        progressBar.setVisibility(View.VISIBLE);

        autentificacion.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            LimpiarCajasDeTextos();
                            progressBar.setVisibility(View.INVISIBLE);

                            //Traemos el UID del usuario que se esta registrando (id)
                            String id = autentificacion.getCurrentUser().getUid();

                            Map<String, Object> map = new HashMap<>();
                            map.put("nombre", name);
                            map.put("email", email);
                            map.put("clave", password);

                            base_de_datos.child("Usuarios").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task2) {

                                    if(task2.isSuccessful()){
                                        startActivity(new Intent(MainActivity.this, MainActivity2.class));
                                        finish();
                                    }else{
                                        Toast.makeText(MainActivity.this, "error vuelva interntarlo", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            Toast.makeText(MainActivity.this, "Usuario creado", Toast.LENGTH_SHORT).show();
                        }else {
                            LimpiarCajasDeTextos();
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LimpiarCajasDeTextos();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "ERROR: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void IniciarSesion(String email, String password){

        progressBar.setVisibility(View.VISIBLE);

        autentificacion.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            LimpiarCajasDeTextos();
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Usuario a sido creado", Toast.LENGTH_SHORT).show();

                            //Nos llevará al segundo activity
                            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                            startActivity(intent);
                        }else{
                            LimpiarCajasDeTextos();
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Email y/o contraseña son incorrectos", Toast.LENGTH_LONG).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LimpiarCajasDeTextos();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "ERROR: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void LimpiarCajasDeTextos(){
        clave.setText("");
        correo.setText("");
    }

}