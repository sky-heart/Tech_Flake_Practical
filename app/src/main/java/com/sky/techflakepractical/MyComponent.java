package com.sky.techflakepractical;

import com.sky.techflakepractical.ui.activity.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {BoxStoreModule.class})
public interface MyComponent {
    void inject(MainActivity activity);
}
