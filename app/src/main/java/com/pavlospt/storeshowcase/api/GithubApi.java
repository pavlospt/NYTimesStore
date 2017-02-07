package com.pavlospt.storeshowcase.api;

import com.pavlospt.storeshowcase.models.GithubUser;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface GithubApi {

    @GET("users/{username}")
    Observable<GithubUser> getUser(@Path("username") String username);

}
