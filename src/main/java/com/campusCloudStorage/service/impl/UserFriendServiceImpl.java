package com.campusCloudStorage.service.impl;

import com.campusCloudStorage.dao.UserDao;
import com.campusCloudStorage.dao.UserFriendDao;
import com.campusCloudStorage.entity.User;
import com.campusCloudStorage.entity.UserFriend;
import com.campusCloudStorage.entity.UserFriendKey;
import com.campusCloudStorage.enums.FriendRequestStateEnum;
import com.campusCloudStorage.service.UserFriendService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("userFriendService")
public class UserFriendServiceImpl implements UserFriendService{
    @Resource
    private UserFriendDao userFriendDao;

    @Resource
    private UserDao userDao;

    @Override
    public List<User> selectPermittedFriendsByUId(int uId) {
        List<UserFriend> permittedFriendsList=userFriendDao.selectFriends(uId);
        List<User> permittedFriendsDetailList=new ArrayList<User>();
        for (UserFriend userFriend: permittedFriendsList){
            int friendId;
            //在UserFriend对象中，属性toId和fromId之一为本用户Id，另一个就是好友Id
            if(userFriend.getToId()!=uId){
                friendId=userFriend.getToId();
            }else {
                friendId=userFriend.getFromId();
            }
            User friendDetail = userDao.selectByPrimaryKey(friendId);
            permittedFriendsDetailList.add(friendDetail);
        }
        return permittedFriendsDetailList;
    }

    @Override
    public List<User> selectFriendRequestByUId(int uId) {
        List<UserFriend> unpermittedFriendsList=userFriendDao.selectFriendRequests(uId);
        List<User> unpermittedFriendsDetailList=new ArrayList<User>();
        for (UserFriend userFriend: unpermittedFriendsList){
            int friendId;
            //在UserFriend对象中，属性toId和fromId之一为本用户Id，另一个就是好友Id
            if(userFriend.getFromId()!=uId){
                friendId=userFriend.getFromId();
            }else {
                friendId=userFriend.getToId();
            }
            User friendDetail = userDao.selectByPrimaryKey(friendId);
            unpermittedFriendsDetailList.add(friendDetail);
        }
        return unpermittedFriendsDetailList;
    }

    @Override
    public FriendRequestStateEnum permitFriendRequest(int uId, int friendId) {
        //将permitted字段设为1，即为同意好友申请，两个用户成为好友
        UserFriend userFriend=new UserFriend(friendId,uId,new Byte("1"));
        int updateCount = userFriendDao.updateByPrimaryKey(userFriend);
        if (updateCount == 1){
            return FriendRequestStateEnum.PERMIT_SUCCESS;
        }
        return FriendRequestStateEnum.PERMIT_FAIL;
    }


    @Override
    public FriendRequestStateEnum deleteFriend(int uId, int friendId) {
        UserFriendKey userFriendKey=new UserFriendKey(friendId,uId);
        int deleteCount=userFriendDao.deleteByPrimaryKey(userFriendKey);
        if(deleteCount!=1){
            //可能是因为userFriendKey中的toId和fromId的次序问题，导致没删除。将两个参数颠倒，再尝试一次删除
            userFriendKey=new UserFriendKey(uId,friendId);
            deleteCount=userFriendDao.deleteByPrimaryKey(userFriendKey);
        }
        if(deleteCount==1){
            return FriendRequestStateEnum.DELETE_SUCCESS;
        }
        return FriendRequestStateEnum.DELETE_FAIL;
    }

    @Override
    public FriendRequestStateEnum sendFriendRequest(int fromId, int toId) {
        UserFriend userFriend = new UserFriend();
        userFriend.setFromId(fromId);
        userFriend.setToId(toId);

        //如果是公共账号，直接同意好友申请，直接成为好友
        User friend=userDao.selectByPrimaryKey(toId);
        if(friend.getType().equals("PRIVATE")){
            userFriend.setPermitted(new Byte("0"));
        }else {
            userFriend.setPermitted(new Byte("1"));
        }

        int insertCount=userFriendDao.insert(userFriend);
        if(insertCount==0){
            return FriendRequestStateEnum.REPEAT;
        }
        return FriendRequestStateEnum.SEND_SUCCESS;
    }
}
