package it.unive.cybertech.noleggio;

import static it.unive.cybertech.utils.CachedUser.user;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.unive.cybertech.R;
import it.unive.cybertech.database.Material.Material;
import it.unive.cybertech.database.Profile.LendingInProgress;
import it.unive.cybertech.utils.Utils;

public class ExpiredRents extends AppCompatActivity implements Utils.ItemClickListener {

    public static final String ID = "ExpiredRents";
    static final int CONFIRM_END_RENT = 0;
    static final int CONFIRM_END_LENDING = 1;
    private RentedMaterialsAdapter rentMaterialsAdapter, rentedMaterialsAdapter;
    private List<LendingInProgress> rentMaterials, rentedMaterials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expired_rents);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Scadenze");
        RecyclerView rent = findViewById(R.id.expired_rent_list),
                rented = findViewById(R.id.expired_rented_list);
        rentMaterials = new ArrayList<>();
        rentedMaterials = new ArrayList<>();
        rentMaterialsAdapter = new RentedMaterialsAdapter(rentMaterials, "rentMaterialsAdapter");
        rentedMaterialsAdapter = new RentedMaterialsAdapter(rentedMaterials, "rentedMaterialsAdapter");
        rentedMaterialsAdapter.setClickListener(this);
        rentMaterialsAdapter.setClickListener(this);
        rent.setLayoutManager(new GridLayoutManager(this, 2));
        rented.setLayoutManager(new GridLayoutManager(this, 2));
        rent.setAdapter(rentMaterialsAdapter);
        rented.setAdapter(rentedMaterialsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO non va
        Utils.executeAsync(() -> user.getExpiredLending(), new Utils.TaskResult<List<LendingInProgress>>() {
            @Override
            public void onComplete(List<LendingInProgress> result) {
                rentedMaterials = result;
                rentedMaterialsAdapter.setItems(rentedMaterials);
                rentedMaterialsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        Utils.executeAsync(() -> user.getMyMaterialsExpiredLending(), new Utils.TaskResult<List<LendingInProgress>>() {
            @Override
            public void onComplete(List<LendingInProgress> result) {
                rentMaterials = result;
                rentMaterialsAdapter.setItems(rentMaterials);
                rentMaterialsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        String tag = view.getTag().toString();
        Intent i = new Intent(this, ProductDetails.class);
        i.putExtra("Type", tag);
        i.putExtra("Position", position);
        if (tag.equals("rentedMaterialsAdapter")) {
            i.putExtra("ID", rentedMaterials.get(position).getId());
            startActivityForResult(i, CONFIRM_END_LENDING);
        }else {
            i.putExtra("ID", rentMaterials.get(position).getId());
            startActivityForResult(i, CONFIRM_END_RENT);
        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIRM_END_RENT) {
            if (resultCode == ProductDetails.RENT_TERMINATED) {
                int pos = data.getIntExtra("Position", -1);
                if (pos >= 0)
                        rentMaterialsAdapter.removeAt(pos);
            }
        }else if (requestCode == CONFIRM_END_LENDING) {
            if (resultCode == ProductDetails.RENT_TERMINATED) {
                int pos = data.getIntExtra("Position", -1);
                if (pos >= 0)
                    rentedMaterialsAdapter.removeAt(pos);
            }
        }
    }
}