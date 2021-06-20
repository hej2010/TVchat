package se.arctosoft.tvchat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import se.arctosoft.tvchat.data.Channel;
import se.arctosoft.tvchat.data.Message;
import se.arctosoft.tvchat.data.Report;

public class ChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Channel.class);
        ParseObject.registerSubclass(Report.class);

        // Use for monitoring Parse network traffic
        /*OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // any network interceptors must be added with the Configuration Builder given this syntax
        builder.networkInterceptors().add(httpLoggingInterceptor);*/

        // set applicationId and server based on the values in the Back4App settings.
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("GNR7NSEFG7GFGF") // Application ID from Back4App Dashboard
                .clientKey("FMN4398FRGNST2N3FNSD") // Client Key from Back4App Dashboard
                //.clientBuilder(builder)
                .server("https://api.arctosoft.com/chat").build());
    }
}
