package com.sky.techflakepractical;

import android.app.Application;
import android.util.Log;

import com.sky.techflakepractical.models.MyObjectBox;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class App extends Application {

    public static final String TAG = "ObjectBoxExample";
    public static final boolean EXTERNAL_DIR = false;

    private MyComponent myComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        BoxStore boxStore = MyObjectBox.builder().androidContext(App.this).build();
        if (BuildConfig.DEBUG) {
            new AndroidObjectBrowser(boxStore).start(this);
        }

        myComponent = DaggerMyComponent.builder()
                .boxStoreModule(new BoxStoreModule(boxStore))
                .build();

        Log.d("App", "Using ObjectBox " + BoxStore.getVersion() + " (" + BoxStore.getVersionNative() + ")");
    }

    public MyComponent getNetComponent() {
        return myComponent;
    }

}
