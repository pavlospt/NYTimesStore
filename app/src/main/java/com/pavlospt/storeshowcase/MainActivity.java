package com.pavlospt.storeshowcase;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.nytimes.android.external.store.base.Fetcher;
import com.nytimes.android.external.store.base.Store;
import com.nytimes.android.external.store.base.impl.BarCode;
import com.nytimes.android.external.store.base.impl.ParsingStoreBuilder;
import com.pavlospt.storeshowcase.api.GithubApi;
import com.pavlospt.storeshowcase.models.GithubUser;
import com.pavlospt.storeshowcase.store.StoreUtils;

import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TEST_USER_LOGIN = "pavlospt";

    private GithubUser githubUser;
    private GithubApi githubApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUser();
        initRetrofit();

        final Store githubUserStore = provideGithubUserStore();
        final BarCode githubUserBarCode = StoreUtils.generateBarCodeForGithubUser(githubUser.getName());

        githubUserStore
            .get(githubUserBarCode)
            .concatWith(githubUserStore.fetch(githubUserBarCode))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<GithubUser>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(GithubUser githubUser) {
                    handleGithubUser(githubUser);
                }
            });
    }

    private void handleGithubUser(GithubUser githubUser) {
        Toast.makeText(this, "UserName: " + githubUser.getName(), Toast.LENGTH_LONG).show();
    }

    private Store<GithubUser> provideGithubUserStore() {
        return ParsingStoreBuilder.<BufferedSource, GithubUser>builder()
            .fetcher(new Fetcher<BufferedSource>() {
                @NonNull
                @Override
                public Observable<BufferedSource> fetch(BarCode barCode) {
                    return StoreUtils.buildGithubUserFetcher(buildSourceObservable());
                }
            })
            .persister(StoreUtils.newPersister(this))
            .parser(StoreUtils.provideGsonParserFactoryFor(GithubUser.class))
            .open();
    }

    private GithubApi provideGithubAPI() {
        return new Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(GithubApi.class);
    }

    private Observable<GithubUser> buildSourceObservable() {
        return githubApi.getUser(TEST_USER_LOGIN);
    }

    private void initRetrofit() {
        this.githubApi = provideGithubAPI();
    }

    private void initUser() {
        this.githubUser = new GithubUser();
        this.githubUser.setName("pavlospt");
        this.githubUser.setLogin(TEST_USER_LOGIN);
    }
}
