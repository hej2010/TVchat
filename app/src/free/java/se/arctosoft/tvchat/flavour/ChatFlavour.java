package se.arctosoft.tvchat.flavour;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import se.arctosoft.tvchat.ChatActivity;
import se.arctosoft.tvchat.R;

public class ChatFlavour {
    private final AdView mAdView;

    public ChatFlavour(ChatActivity activity) {
        mAdView = activity.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}
