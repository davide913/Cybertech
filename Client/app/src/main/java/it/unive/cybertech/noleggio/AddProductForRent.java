package it.unive.cybertech.noleggio;

import static it.unive.cybertech.utils.CachedUser.user;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import it.unive.cybertech.R;
import it.unive.cybertech.database.Material.Material;
import it.unive.cybertech.database.Material.Type;
import it.unive.cybertech.utils.Utils;

public class AddProductForRent extends AppCompatActivity {

    private ImageView image;
    private Uri output;
    private Spinner type;
    //TODO gestire immagine e datepicker

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product_for_rent);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.add_to_showcase);
        FloatingActionButton done = findViewById(R.id.showcase_details_done);
        EditText title = findViewById(R.id.showcase_details_title),
                description = findViewById(R.id.showcase_details_description),
                date = findViewById(R.id.showcase_details_date);
        type = findViewById(R.id.showcase_details_type);
        image = findViewById(R.id.showcase_details_image);
        Button loadImage = findViewById(R.id.showcase_details_load_image);
        loadImage.setOnClickListener(v -> pickImage());
        done.setOnClickListener(v -> {
            boolean formOk = true;
            if (title.length() == 0) {
                formOk = false;
                title.setError("Campo obbligatorio");
            }
            if (description.length() == 0) {
                formOk = false;
                description.setError("Campo obbligatorio");
            }
            if (date.length() == 0) {
                formOk = false;
                date.setError("Campo obbligatorio");
            }
            if (type.getSelectedItem() != null && type.getSelectedItem().toString().length() == 0)
                formOk = false;

            if (formOk) {
                Toast.makeText(this, "FORM OK", Toast.LENGTH_LONG).show();
                AtomicReference<Material> m = new AtomicReference<>();
                Thread t = new Thread(() -> {
                    try {
                        String baseString = null;
                        if (image.getDrawable() != null) {
                            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                            byte[] byteArray = byteStream.toByteArray();
                            baseString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        }
                        m.set(Material.createMaterial(user, title.getText().toString(), description.getText().toString(), baseString, (Type) type.getSelectedItem(), 45, 12, new SimpleDateFormat("dd/MM/yyyy").parse(date.getText().toString())));
                    } catch (ExecutionException | InterruptedException | ParseException e) {
                        e.printStackTrace();
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (m.get() != null) {
                    t = new Thread(() -> {
                        user.addMaterial(m.get());
                    });
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    new Utils.Dialog(this)
                            .hideCancelButton()
                            .setCallback(new Utils.DialogResult() {
                                @Override
                                public void onSuccess() {
                                    finish();
                                }

                                @Override
                                public void onCancel() {

                                }
                            })
                            .show("Fatto!", "Il tuo annuncio è stato pubblicato");
                } else
                    new Utils.Dialog(this).show("Errore", "Non è stato possibile aggiungere il tuo materiale in prestito. Riprova tra qualche istante");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            AtomicReference<ArrayAdapter<Type>> userAdapter = new AtomicReference<>();
            Thread t = new Thread(() -> {
                try {
                    ArrayList<Type> types = Type.getMaterialTypes();
                    userAdapter.set(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
            t.join();
            type.setAdapter(userAdapter.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pickImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_PICK);
        final List<Intent> intents = new ArrayList<Intent>();
        intents.add(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        File file = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        output = Uri.fromFile(file);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            intents.add(intent);
        }

        Intent result = Intent.createChooser(intents.remove(0), null);
        if (!intents.isEmpty()) {
            result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
        }
        startActivityForResult(result, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AddProductForRent", resultCode + "");
        if (requestCode == 0) {
            if (resultCode == -1)
                if (data.getData() != null)
                    image.setImageURI(data.getData());
                else
                    image.setImageURI(output);
        }
    }
}