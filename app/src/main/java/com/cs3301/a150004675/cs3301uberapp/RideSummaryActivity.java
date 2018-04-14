package com.cs3301.a150004675.cs3301uberapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class RideSummaryActivity extends AppCompatActivity {

    private Button mWriteReviews, mConfirm;
    private TextView mTotalCost, mDistanceOfJourney;
    private EditText mReviews;
    private RatingBar mRatings;
    private String reviews;
    private DatabaseReference customerRef;
    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_summary);
        mWriteReviews = findViewById(R.id.writeReview);
        mReviews = findViewById(R.id.review);
        mReviews.setVisibility(View.GONE);
        mConfirm = findViewById(R.id.confirm);
        mTotalCost = findViewById(R.id.totalCost);
        mDistanceOfJourney = findViewById(R.id.distanceOfJourney);
        mRatings = findViewById(R.id.ratingBar);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("rideSummary");
        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> rideSummaryMap = (Map<String, Object>) dataSnapshot.getValue();
                mTotalCost.setText("Total Cost ($): " + rideSummaryMap.get("totalCost").toString());
                mDistanceOfJourney.setText("Distance of Journey (km): " + rideSummaryMap.get("distanceOfJourney").toString());
                driverId = rideSummaryMap.get("driverId").toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        mWriteReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReviews.setVisibility(View.VISIBLE);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send the rating and review (if any) into the database
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
                DatabaseReference newRatingRef = driverRef.child("rating").push();
                Log.e("rating ref", newRatingRef.toString());
                newRatingRef.setValue(mRatings.getRating());
                reviews = mReviews.getText().toString();
                Log.e("reviews", reviews);
                if(reviews != null){
                    DatabaseReference newReviewRef = driverRef.child("reviews").push();
                    Log.e("review ref", newRatingRef.toString());
                    newReviewRef.setValue(reviews);
                }
                finish();
                return;
            }
        });

    }

}
