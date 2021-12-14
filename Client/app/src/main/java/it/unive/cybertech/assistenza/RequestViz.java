package it.unive.cybertech.assistenza;

import static it.unive.cybertech.database.Profile.QuarantineAssistance.getQuarantineAssistanceById;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

import it.unive.cybertech.R;
import it.unive.cybertech.database.Profile.QuarantineAssistance;
import it.unive.cybertech.utils.CachedUser;
import it.unive.cybertech.utils.Utils;

public class RequestViz extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_visualisation);

        Toolbar toolbar = findViewById(R.id.toolbar_RequestViz);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Dettagli richiesta");

        QuarantineAssistance request = null;
        String id = getIntent().getStringExtra("id");//prendo l'id della richiesta dal chiamante
        if(id != null) {
            try {
                request = getQuarantineAssistanceById(getIntent().getStringExtra("id"));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        else { //qui solo se accedo dalla presa in carico
            new Utils.Dialog(this).show("Nessuna Incarico preso", "Nessuna richiesta è stata presa in carico!");


            this.findViewById(R.id.btn_changeFields).setVisibility(View.GONE);
            this.findViewById(R.id.btn_deleteRequest).setVisibility(View.GONE);
            this.findViewById(R.id.accept_request).setVisibility(View.GONE);
            this.findViewById(R.id.btn_chat).setVisibility(View.GONE);
            this.findViewById(R.id.stop_helping).setVisibility(View.GONE);

            this.findViewById(R.id.textTitle).setVisibility(View.GONE);
            this.findViewById(R.id.textFull).setVisibility(View.GONE);
            this.findViewById(R.id.textCountry).setVisibility(View.GONE);
            this.findViewById(R.id.textCity).setVisibility(View.GONE);
            this.findViewById(R.id.textAddress).setVisibility(View.GONE);
            this.findViewById(R.id.textDate).setVisibility(View.GONE);
        }

        if (request != null) {
            TextView textTitle = findViewById(R.id.textTitle);
            TextView text = findViewById(R.id.textFull);
            TextView textCountry = findViewById(R.id.textCountry);
            TextView textCity = findViewById(R.id.textCity);
            TextView textAddress = findViewById(R.id.textAddress);
            TextView textDate = findViewById(R.id.textDate);

            String title = getIntent().getStringExtra("title");
            textTitle.setText(title);

            String country = getIntent().getStringExtra("country");
            textCountry.setText(country);

            String city = getIntent().getStringExtra("city");
            textCity.setText(city);

            String address = getIntent().getStringExtra("address");
            textAddress.setText(address);

            text.setText(request.getDescription());

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String strDate = dateFormat.format(request.getDeliveryDate());
            textDate.setText(strDate);

            String callerClass = getIntent().getStringExtra("class");
            //QuarantineAssistance thisAssistance = null;

            if (callerClass.equals("taken")) {
                this.findViewById(R.id.btn_chat).setOnClickListener(v -> { //per spostarsi alla chat locale con chi ha accettato la richiesta
                    finish();
                });

                QuarantineAssistance thisRequest = request;
                this.findViewById(R.id.stop_helping).setOnClickListener(v -> {
                    //QuarantineAssistance

                    Utils.Dialog dialog = new Utils.Dialog(this);
                    dialog.show("Attenzione!", "Stai per abbandonare la richiesta");
                    dialog.setCallback(new Utils.DialogResult() {
                        @Override
                        public void onSuccess() {
                            thisRequest.updateInCharge_QuarantineAssistance(null);
                            finish();
                        }

                        @Override
                        public void onCancel() {
                            finish();
                        }
                    });

                });

                this.findViewById(R.id.btn_changeFields).setVisibility(View.GONE);
                this.findViewById(R.id.btn_deleteRequest).setVisibility(View.GONE);
                this.findViewById(R.id.accept_request).setVisibility(View.GONE);
            }

            if (callerClass.equals("Homenegative")) {
                QuarantineAssistance finalRequest1 = request;
                this.findViewById(R.id.accept_request).setOnClickListener(v -> {
                    //La richiesta viene affidata a me, la posso visualizzare nella mia sezione Taken Requests e tolta dalla lista nella Home

                    Utils.Dialog dialog = new Utils.Dialog(this);
                    dialog.show("Operazione confermata!", "Hai preso in carico una richiesta");
                    dialog.setCallback(new Utils.DialogResult() {
                        @Override
                        public void onSuccess() {
                            finalRequest1.updateInCharge_QuarantineAssistance(CachedUser.user);
                            finish();
                        }

                        @Override
                        public void onCancel() {
                            finish();
                        }
                    });
                });

                QuarantineAssistance finalrequest = request;
                this.findViewById(R.id.stop_helping).setOnClickListener(v -> {
                    //QuarantineAssistance

                    Utils.Dialog dialog = new Utils.Dialog(this);
                    dialog.show("Attenzione!", "Stai per abbandonare la richiesta");
                    dialog.setCallback(new Utils.DialogResult() {
                        @Override
                        public void onSuccess() {
                            finalrequest.updateInCharge_QuarantineAssistance(null);
                            finish();
                        }

                        @Override
                        public void onCancel() {
                            finish();
                        }
                    });
                });

                this.findViewById(R.id.btn_changeFields).setVisibility(View.GONE);
                this.findViewById(R.id.btn_deleteRequest).setVisibility(View.GONE);
                this.findViewById(R.id.btn_chat).setVisibility(View.GONE);
            } else {
                //Pulsanti visibili solo dall'utente positivo che richiede soccorso
                QuarantineAssistance finalRequest = request;
                this.findViewById(R.id.btn_changeFields).setOnClickListener(v -> { //per modificare i campi, nel DB verificare e sostituire i campi nuovi con quelli vecchi della stessa richiesta
                    Intent newIntent = new Intent(this, RequestDetails.class);

                    newIntent.putExtra("id", finalRequest.getId());
                    newIntent.putExtra("changeFields", "true");
                    startActivity(newIntent);
                    finish();
                });

                QuarantineAssistance finalRequest2 = request;
                this.findViewById(R.id.btn_deleteRequest).setOnClickListener(v -> { //per eliminare dal Db la richiesta
                    try {
                        finalRequest2.removeQuarantineAssistance();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                });

                this.findViewById(R.id.btn_chat).setOnClickListener(v -> { //per spostarsi alla chat locale con chi ha accettato la richiesta
                    finish();
                });

                this.findViewById(R.id.accept_request).setVisibility(View.GONE);
                this.findViewById(R.id.stop_helping).setVisibility(View.GONE);
            }
        }
    }
}
