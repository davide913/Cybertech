package it.unive.cybertech.noleggio;

import static it.unive.cybertech.noleggio.HomePage.RENT_CODE;
import static it.unive.cybertech.utils.CachedUser.user;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import it.unive.cybertech.R;
import it.unive.cybertech.database.Material.Material;
import it.unive.cybertech.database.Profile.LendingInProgress;
import it.unive.cybertech.utils.Utils;

public class ShowcaseFragment extends Fragment implements Utils.ItemClickListener {

    public static final String ID = "ShowcaseFragment";
    private List<Material> items;
    private ShowcaseAdapter adapter;
    private ProgressBar loader;
    private RecyclerView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_showcase, container, false);
        list = view.findViewById(R.id.showcase_list);
        FloatingActionButton add = view.findViewById(R.id.showcase_add);
        list.setLayoutManager(new GridLayoutManager(getContext(), 2));
        items = new ArrayList<>();
        adapter = new ShowcaseAdapter(items);
        adapter.setClickListener(this);
        loader = view.findViewById(R.id.showcase_loader);
        list.setAdapter(adapter);
        add.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddProductForRent.class));
        });

        view.findViewById(R.id.test_showcase).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), RentFeedback.class));
        });
        initList();
        return view;
    }

    private void initList() {
        super.onStart();
        //TODO get posizione
        Utils.executeAsync(() -> Material.getRentableMaterials(45, 12, 10000, user.getId()), new Utils.TaskResult<List<Material>>() {
            @Override
            public void onComplete(List<Material> result) {
                Log.d(ID, "Size: " + result.size());
                items = result;
                adapter.setItems(items);
                adapter.notifyDataSetChanged();
                loader.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RENT_CODE && resultCode == ProductDetails.RENT_SUCCESS) {
            int pos = data.getIntExtra("Position", -1);
            if (pos >= 0) {
                adapter.removeAt(pos);
                String idLending = data.getStringExtra("LendingID");
                if (idLending != null) {
                    HomePage h = (HomePage) getParentFragment();
                    if (h != null) {
                        MyRentedMaterialsFragment f = (MyRentedMaterialsFragment) h.getFragmentByID(MyRentedMaterialsFragment.ID);
                        if (f != null)
                            f.addLendingById(idLending);
                    }
                }
            }
        }
    }

    public void onItemClick(View view, int position) {
        Intent i = new Intent(getActivity(), ProductDetails.class);
        Material m = items.get(position);
        i.putExtra("ID", m.getId());
        i.putExtra("Position", position);
        i.putExtra("Type", m.getOwner().getId().equals(user.getId()) ? MyRentMaterialsFragment.ID : ID);
        startActivityForResult(i, RENT_CODE);
    }
}