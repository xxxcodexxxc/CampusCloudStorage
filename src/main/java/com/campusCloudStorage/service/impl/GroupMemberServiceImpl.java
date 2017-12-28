package com.campusCloudStorage.service.impl;

import com.campusCloudStorage.dao.GroupFileShareDao;
import com.campusCloudStorage.dao.GroupMemberDao;
import com.campusCloudStorage.dao.UserDao;
import com.campusCloudStorage.dao.UserGroupDao;
import com.campusCloudStorage.entity.GroupMember;
import com.campusCloudStorage.entity.GroupMemberKey;
import com.campusCloudStorage.entity.User;
import com.campusCloudStorage.entity.UserGroup;
import com.campusCloudStorage.enums.DeleteStateEnum;
import com.campusCloudStorage.enums.GroupRequestStateEnum;
import com.campusCloudStorage.service.GroupMemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("groupMemberService")
public class GroupMemberServiceImpl implements GroupMemberService{
    @Resource
    private UserDao userDao;

    @Resource
    private GroupMemberDao groupMemberDao;

    @Resource
    private UserGroupDao userGroupDao;

    @Resource
    private GroupFileShareDao groupFileShareDao;

    @Override
    public GroupMember selectByPrimaryKey(int gId, int uId) {
        GroupMemberKey groupMemberKey=new GroupMemberKey();
        groupMemberKey.setgId(gId);
        groupMemberKey.setuId(uId);
        return groupMemberDao.selectByPrimaryKey(groupMemberKey);
    }

    @Override
    public List<User> selectUnpermittedMembers(int gId) {
        //GroupMember里含有uId，需要用此uId得到User对象
        List<GroupMember> groupMemberList=groupMemberDao.selectUnpermittedMembers(gId);
        List<User>detailMemberList=new ArrayList<User>();
        for (GroupMember groupMember:groupMemberList){
            User member = userDao.selectByPrimaryKey(groupMember.getuId());
            if(member!=null){
                detailMemberList.add(member);
            }
        }
        return detailMemberList;
    }

    @Override
    public GroupRequestStateEnum permitMemberRequest(int gId, int memberId) {
        GroupMember groupMember=new GroupMember();
        groupMember.setgId(gId);
        groupMember.setuId(memberId);
        groupMember.setPermitted(new Byte("1"));
        int updateCount = groupMemberDao.updateByPrimaryKey(groupMember);
        if(updateCount==1){
            return GroupRequestStateEnum.PERMIT_SUCCESS;
        }
        return GroupRequestStateEnum.PERMIT_FAIL;
    }

    @Override
    public GroupRequestStateEnum refuseGroupRequest(int gId, int uId) {
        GroupMemberKey groupMemberKey = new GroupMemberKey();
        groupMemberKey.setgId(gId);
        groupMemberKey.setuId(uId);
        int deleteCount = groupMemberDao.deleteByPrimaryKey(groupMemberKey);
        if(deleteCount==1){
            return GroupRequestStateEnum.REFUSE_SUCCESS;
        }
        return GroupRequestStateEnum.REFUSE_FAIL;
    }

    @Override
    public DeleteStateEnum deleteMember(int gId, int memberId) {
        GroupMemberKey groupMemberKey = new GroupMemberKey();
        groupMemberKey.setgId(gId);
        groupMemberKey.setuId(memberId);
        int deleteCount = groupMemberDao.deleteByPrimaryKey(groupMemberKey);
        if(deleteCount==1){
            groupFileShareDao.deleteByGIdAndProviderId(gId,memberId);
            return DeleteStateEnum.SUCCESS;
        }
        return DeleteStateEnum.FAILED;
    }

    @Override
    public GroupRequestStateEnum sendJoinRequests(int gId, int uId) {
        GroupMember groupMember=new GroupMember();
        groupMember.setuId(uId);
        groupMember.setgId(gId);

        //如果该群为空，返回
        UserGroup userGroup=userGroupDao.selectByPrimaryKey(gId);
        if(userGroup==null){
            return GroupRequestStateEnum.NO_GROUP;
        }

        //如果该群组创建者是自己，返回
        if(userGroup.getBuilderId()==uId){
            return GroupRequestStateEnum.SELF_REQUEST;
        }

        //如果已经加入了该群组，返回
        GroupMember groupMemberFromDB=groupMemberDao.selectByPrimaryKey(groupMember);
        if(groupMemberFromDB!=null){
            return GroupRequestStateEnum.REPEAT;
        }

        //如果群组是公开的，可以直接加入，如果不是，暂时将permitted字段设为0
        if(userGroup.getType().equals("PRIVATE")){
            groupMember.setPermitted(new Byte("0"));
        }else {
            groupMember.setPermitted(new Byte("1"));
        }
        int insertCount = groupMemberDao.insert(groupMember);
        if(insertCount==1){
            return GroupRequestStateEnum.SEND_SUCCESS;
        }
        return GroupRequestStateEnum.NO_GROUP;
    }
}
