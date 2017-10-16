package com.jeanboy.data.repository;

import android.arch.lifecycle.LiveData;

import com.jeanboy.data.cache.database.model.TokenModel;
import com.jeanboy.data.cache.database.model.UserModel;
import com.jeanboy.data.cache.manager.AppDatabase;
import com.jeanboy.data.cache.manager.DBManager;
import com.jeanboy.data.mapper.TokenDataMapper;
import com.jeanboy.data.mapper.UserDataMapper;
import com.jeanboy.data.net.api.UserService;
import com.jeanboy.data.net.entity.TokenEntity;
import com.jeanboy.data.net.entity.UserEntity;
import com.jeanboy.data.net.manager.OkHttpManager;
import com.jeanboy.data.repository.handler.RepositoryHandler;

import java.util.List;

import javax.inject.Singleton;

import io.reactivex.Flowable;

/**
 * Created by jeanboy on 2017/9/29.
 */
@Singleton
public class UserRepository {

    private AppDatabase database = DBManager.getInstance().getDataBase();
    private UserService userDao = OkHttpManager.getInstance().create(UserService.BASE_URL, UserService.class);

    public LiveData<TokenModel> login(final String username, final String password) {
        return new RepositoryHandler<TokenEntity, TokenModel>() {

            @Override
            protected boolean shouldFetch(TokenModel result) {
                return true;
            }

            @Override
            protected Flowable<TokenEntity> fetchFromNetWork() {
                return userDao.login(username, password);
            }

            @Override
            protected TokenModel onMapper(TokenEntity tokenEntity) {
                return new TokenDataMapper().transform(tokenEntity);
            }
        }.asLiveData();
    }

    public LiveData<UserModel> getInfo(final String accessToken, final String userId) {
        return new RepositoryHandler<UserEntity, UserModel>() {

            @Override
            protected LiveData<UserModel> loadFromCache() {
                return database.userDao().getById(userId);
            }

            @Override
            protected void saveToCache(UserModel result) {
                database.beginTransaction();
                try {
                    database.userDao().insert(result);
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(UserModel result) {
                return result == null;
            }

            @Override
            protected Flowable<UserEntity> fetchFromNetWork() {
                return userDao.getInfo(accessToken, userId);
            }

            @Override
            protected UserModel onMapper(UserEntity userEntity) {
                return new UserDataMapper().transform(userEntity);
            }
        }.asLiveData();
    }

    public LiveData<List<UserModel>> getFriendList(final String accessToken, final String userId, final int skip, final int limit) {
        return new RepositoryHandler<List<UserEntity>, List<UserModel>>() {

            @Override
            protected boolean shouldFetch(List<UserModel> result) {
                return result == null || result.isEmpty();
            }

            @Override
            protected Flowable<List<UserEntity>> fetchFromNetWork() {
                return userDao.getFriendList(accessToken, userId, skip, limit);
            }

            @Override
            protected List<UserModel> onMapper(List<UserEntity> entityList) {
                return new UserDataMapper().transform(entityList);
            }
        }.asLiveData();
    }

}
