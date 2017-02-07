package com.pavlospt.storeshowcase.store;


import android.content.Context;

import com.google.gson.Gson;
import com.nytimes.android.external.fs.SourcePersisterFactory;
import com.nytimes.android.external.store.base.Parser;
import com.nytimes.android.external.store.base.Persister;
import com.nytimes.android.external.store.base.impl.BarCode;
import com.nytimes.android.external.store.middleware.GsonParserFactory;
import com.pavlospt.storeshowcase.models.GithubUser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import okio.BufferedSource;
import okio.Okio;
import rx.Observable;
import rx.functions.Func1;

import static okhttp3.internal.Util.UTF_8;

public class StoreUtils {

    private static final String GITHUB_USER_CACHE = "/github_users/";

    private StoreUtils() {
        //NO INSTANCE
    }

    public static BarCode generateBarCodeForGithubUser(String userName) {
        return new BarCode(GithubUser.class.getSimpleName(), userName);
    }

    public static Parser provideGsonParserFactoryFor(Class clazz) {
        return GsonParserFactory.createSourceParser(new Gson(), clazz);
    }

    public static Persister newPersister(Context context) {
        Persister persister = null;
        try {
            File githubUserCacheSubfolder = new File(context.getCacheDir(), GITHUB_USER_CACHE);
            persister = SourcePersisterFactory.create(githubUserCacheSubfolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return persister;
    }

    public static Observable<BufferedSource> buildGithubUserFetcher(Observable<GithubUser> sourceObservable) {
        return sourceObservable
            .map(new Func1<GithubUser, BufferedSource>() {
                @Override
                public BufferedSource call(GithubUser githubUser) {
                    Gson gson = new Gson();
                    return Okio.buffer(Okio.source(new ByteArrayInputStream(gson.toJson(githubUser).getBytes(UTF_8))));
                }
            });
    }

}
