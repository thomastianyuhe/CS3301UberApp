package com.cs3301.a150004675.cs3301uberapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> historyRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getHistoryData();
        mAdapter = new HistoryActivityAdapter(historyRecords);
        mRecyclerView.setAdapter(mAdapter);

    }

    private void getHistoryData(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("history");
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> post = postSnapshot.getValue(genericTypeIndicator);
                    Log.e("distanceOfJourney", post.get("distanceOfJourney").toString());
                    Log.e("totalCost", post.get("totalCost").toString());
                    Log.e("driverId", post.get("driverId").toString());
                    StringBuilder historyText = new StringBuilder("Distance of Journey (km): ");
                    historyText.append(post.get("distanceOfJourney").toString());
                    historyText.append("\n");
                    historyText.append("Total Cost ($): ");
                    historyText.append(post.get("totalCost").toString());
                    historyText.append("\n");
                    historyText.append("Driver Id: ");
                    historyText.append(post.get("driverId").toString());
                    historyRecords.add(historyText.toString());
                }
                mAdapter.notifyDataSetChanged();
                Log.e("Number: ", Integer.toString(historyRecords.size()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
