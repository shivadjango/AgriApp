package com.shivaconsulting.agriapp.History;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shivaconsulting.agriapp.Adapter.BookingAdapter;
import com.shivaconsulting.agriapp.Home.MapsActivity;
import com.shivaconsulting.agriapp.Models.Booking;
import com.shivaconsulting.agriapp.Profile.ProfileActivity;
import com.shivaconsulting.agriapp.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BookingHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    //Const
    private static final String TAG = "BookingHistoryActivity";
    private Context mContext = BookingHistoryActivity.this;

    //Vars
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;

    //Id's
    private ImageView home,booking_history,profile;
    private RecyclerView booking_recycler_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        home = findViewById(R.id.home);
        booking_history = findViewById(R.id.booking_history);
        profile = findViewById(R.id.profile);
        booking_recycler_view = findViewById(R.id.booking_recycler_view);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(mContext,
                LinearLayoutManager.VERTICAL, false);

        booking_recycler_view.setLayoutManager(linearLayoutManager1);
        bookingList = new ArrayList<>();

        bookingAdapter = new BookingAdapter(bookingList,mContext);

        booking_recycler_view.setAdapter(bookingAdapter);


        home.setOnClickListener(this);
        profile.setOnClickListener(this);

        booking_history.setImageResource(R.drawable.ic_baseline_file_copy);

        init();
    }

    private void init() {
        String UUID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Services").document(UUID).collection("List")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Booking booking = document.toObject(Booking.class);
                        bookingList.add(booking);
                    }

                    bookingAdapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


          /*
    ---------------------------------------BottomNavBar-------------------------------------------------
     */

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.home:
                Intent intent = new Intent(mContext, MapsActivity.class);
                startActivity(intent);
                break;

            case R.id.profile:
                Intent intent1 = new Intent(mContext, ProfileActivity.class);
                startActivity(intent1);
                break;
        }

    }
}