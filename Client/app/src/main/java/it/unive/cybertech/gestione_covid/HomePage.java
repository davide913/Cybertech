package it.unive.cybertech.gestione_covid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import it.unive.cybertech.R;
/**
 * HomePage is the Fragment that initializes the
 * ViewPager and creates the various Fragments displayed.
 *
 * @author Enrico De Zorzi
 * @since 1.0
 */
public class HomePage extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_page_covid,container, false);
        initViews(view);
        return view;
    }

    /**
     * setupViewPager initializes (with the adapter) the ViewPager and creates the Fragments.
     *
     * @author Enrico De Zorzi
     * @since 1.0
     */
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new ManifestPositivityFragment(), "Segnala Positività"); //Creation of the first fragments
        adapter.addFragment(new PosReportedFragment(), "Segnalazioni Ricevute"); //Creation of the second fragments
        viewPager.setAdapter(adapter);
    }

    private void initViews(View view){
        TabLayout tabLayout = view.findViewById(R.id.group_tabs);
        ViewPager viewPager = view.findViewById(R.id.groups_viewpager);
        Toolbar toolbar_covid_homepage = view.findViewById(R.id.main_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar_covid_homepage);
        tabLayout.setupWithViewPager(viewPager);

        setupViewPager(viewPager);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            //finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }



    //Adapter for ViewPager
    static class Adapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(Object object) {
            // POSITION_NONE makes it possible to reload the PagerAdapter
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }



}
