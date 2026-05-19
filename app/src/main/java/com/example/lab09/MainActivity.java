package com.example.lab09;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lab09.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String LOAD_URL = "http://10.0.2.2/projet/ws/loadEtudiant.php";
    private static final String CREATE_URL = "http://10.0.2.2/projet/ws/createEtudiant.php";

    private EditText nom;
    private EditText prenom;
    private Spinner ville;
    private RadioButton homme;
    private LinearLayout list;
    private TextView status;
    private RequestQueue requestQueue;
    private final List<Etudiant> students = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        ville = findViewById(R.id.ville);
        homme = findViewById(R.id.homme);
        list = findViewById(R.id.studentList);
        status = findViewById(R.id.status);

        ville.setAdapter(ArrayAdapter.createFromResource(
                this,
                R.array.villes,
                android.R.layout.simple_spinner_dropdown_item
        ));

        requestQueue = Volley.newRequestQueue(this);
        findViewById(R.id.add).setOnClickListener(v -> sendStudent());
        findViewById(R.id.reload).setOnClickListener(v -> loadStudents());

        useFallbackData("Ready with sample data");
        loadStudents();
    }

    private void sendStudent() {
        String lastName = nom.getText().toString().trim();
        String firstName = prenom.getText().toString().trim();
        if (TextUtils.isEmpty(lastName) || TextUtils.isEmpty(firstName)) {
            Toast.makeText(this, "Name and first name are required", Toast.LENGTH_SHORT).show();
            return;
        }

        status.setText("Status: sending POST request...");
        StringRequest request = new StringRequest(Request.Method.POST, CREATE_URL,
                response -> updateFromJson(response, "Student saved from PHP response"),
                error -> {
                    students.add(new Etudiant(
                            students.size() + 1,
                            lastName,
                            firstName,
                            ville.getSelectedItem().toString(),
                            homme.isChecked() ? "homme" : "femme"
                    ));
                    drawStudents();
                    status.setText("Status: server unavailable, added locally for demo");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nom", lastName);
                params.put("prenom", firstName);
                params.put("ville", ville.getSelectedItem().toString());
                params.put("sexe", homme.isChecked() ? "homme" : "femme");
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void loadStudents() {
        status.setText("Status: loading from PHP service...");
        StringRequest request = new StringRequest(Request.Method.GET, LOAD_URL,
                response -> updateFromJson(response, "Loaded from PHP service"),
                error -> useFallbackData("Status: server unavailable, showing fallback data")
        );
        requestQueue.add(request);
    }

    private void updateFromJson(String response, String message) {
        Type type = new TypeToken<Collection<Etudiant>>() {}.getType();
        Collection<Etudiant> parsed = new Gson().fromJson(response, type);
        students.clear();
        students.addAll(parsed);
        drawStudents();
        status.setText("Status: " + message);
    }

    private void useFallbackData(String message) {
        students.clear();
        students.add(new Etudiant(1, "Lachgar", "Mohamed", "Rabat", "homme"));
        students.add(new Etudiant(2, "Safi", "Amine", "Marrakech", "homme"));
        students.add(new Etudiant(3, "Dupont", "Sara", "Casablanca", "femme"));
        drawStudents();
        status.setText(message);
    }

    private void drawStudents() {
        list.removeAllViews();
        for (Etudiant student : students) {
            TextView row = new TextView(this);
            row.setText(student.displayName());
            row.setTextColor(0xFF184B4F);
            row.setTextSize(16);
            row.setPadding(18, 18, 18, 18);
            row.setBackgroundResource(R.drawable.panel);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 12);
            list.addView(row, params);
        }
    }
}
