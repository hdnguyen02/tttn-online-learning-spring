package com.hdnguyen.service;


import com.hdnguyen.dao.UserGroupDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupDao userGroupDao;


    public boolean deleteUserGroup(Long idUserGroup) {
        try {
            userGroupDao.deleteById(idUserGroup);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}