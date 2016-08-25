package com.anly.githubapp.presenter.account;

import android.app.Application;
import android.content.Context;

import com.anly.githubapp.common.util.AppLog;
import com.anly.githubapp.data.api.AccountApi;
import com.anly.githubapp.data.model.User;
import com.anly.githubapp.data.pref.AccountPref;
import com.anly.githubapp.data.rx.ResponseObserver;
import com.anly.githubapp.di.ApplicationContext;
import com.anly.githubapp.presenter.base.RxMvpPresenter;
import com.anly.mvp.lce.LceView;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by mingjun on 16/7/27.
 */
public class LoginPresenter extends RxMvpPresenter<LceView<User>> {

    private final AccountApi mAccountApi;

    @Inject
    public LoginPresenter(AccountApi api) {
        this.mAccountApi = api;
    }

    @Inject
    Application mContext;

    public void login(String username, String password) {
        mCompositeSubscription.add(mAccountApi.login(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        getMvpView().showLoading();
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        getMvpView().dismissLoading();
                    }
                })
                .subscribe(new ResponseObserver<User>() {
                    @Override
                    public void onSuccess(User user) {
                        // save user
                        AccountPref.saveLogonUser(mContext, user);

                        AppLog.d("user:" + user.getLogin());
                        getMvpView().showContent(user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        AppLog.e(e);
                        getMvpView().showError(e);
                    }
                }));
    }
}
