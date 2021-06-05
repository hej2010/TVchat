package se.arctosoft.tvchat.flavour;

import com.google.android.gms.ads.MobileAds;

import se.arctosoft.tvchat.ChannelActivity;

public class ChannelFlavour {

    public ChannelFlavour(ChannelActivity channelActivity) {
        MobileAds.initialize(channelActivity);
    }
}
