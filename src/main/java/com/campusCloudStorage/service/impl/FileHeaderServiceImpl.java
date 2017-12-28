package com.campusCloudStorage.service.impl;


import com.campusCloudStorage.dao.FileHeaderDao;
import com.campusCloudStorage.dao.UserFileShareDao;
import com.campusCloudStorage.entity.FileHeader;
import com.campusCloudStorage.enums.CreateStateEnum;
import com.campusCloudStorage.enums.DeleteStateEnum;
import com.campusCloudStorage.enums.UpdateStateEnum;
import com.campusCloudStorage.service.FileHeaderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Service("fileHeaderService")
public class FileHeaderServiceImpl implements FileHeaderService{

    @Resource
    private FileHeaderDao fileHeaderDao;

    @Resource
    private UserFileShareDao userFileShareDao;


    @Override
    public CreateStateEnum createFileHeader(FileHeader fileHeader) {
        if(fileHeader==null){
            return CreateStateEnum.FAILED;
        }

        fileHeader.setSubmitTime(new Date());
        int insertCount = fileHeaderDao.insert(fileHeader);
        if(insertCount==1){
            return CreateStateEnum.SUCCESS;
        }
        return CreateStateEnum.FAILED;
    }

    @Override
    public DeleteStateEnum deleteFileHeader(int fId) {
        FileHeader fileHeader= getFileHeaderById(fId);

        //删除本地文件系统的文件
        File file = new File(fileHeader.getPath());
        if(file.exists()){
            file.delete();
        }

        userFileShareDao.deleteByFId(fId);
        int deleteCount=fileHeaderDao.deleteByPrimaryKey(fId);
        if(deleteCount==1){
            return DeleteStateEnum.SUCCESS;
        }
        return DeleteStateEnum.FAILED;
    }

    @Override
    public FileHeader getFileHeaderById(int fId) {
        return fileHeaderDao.selectByPrimaryKey(fId);
    }

    @Override
    public UpdateStateEnum updateFileHeader(FileHeader fileHeader) {
        if(fileHeader==null){
            return UpdateStateEnum.FAILED;
        }
        int updateCount = fileHeaderDao.updateByPrimaryKey(fileHeader);
        if(updateCount==1){
            return UpdateStateEnum.SUCCESS;
        }
        return UpdateStateEnum.FAILED;
    }
}
