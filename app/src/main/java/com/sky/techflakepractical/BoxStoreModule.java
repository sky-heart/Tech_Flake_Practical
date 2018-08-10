package com.sky.techflakepractical;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.objectbox.BoxStore;

@Module
public class BoxStoreModule {

    private BoxStore boxStore;

    public BoxStoreModule(BoxStore boxStore) {
        this.boxStore = boxStore;
    }

    @Singleton
    @Provides
    public BoxStore provideBox() {
        return boxStore;
    }
}
