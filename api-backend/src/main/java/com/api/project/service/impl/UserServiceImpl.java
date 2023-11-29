package com.api.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.api.common.model.entity.User;
import com.api.project.common.ErrorCode;
import com.api.project.constant.UserConstant;
import com.api.project.exception.BusinessException;
import com.api.project.mapper.UserMapper;
import com.api.project.model.entity.IdRequest;
import com.api.project.service.UserService;
import com.api.project.service.commonservice.FileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * 用户服务实现类
 *
 * @author yupi
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private FileService fileService;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "zzzhlee";

    @Override
    public boolean updateAppSecret(IdRequest idRequest) {
        User user = this.getById(idRequest.getId());

        user.setAppKey(DigestUtil.md5Hex((SALT + user.getUserAccount() + RandomUtil.randomString(4)).getBytes()));
        user.setAppSecret(DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomString(8)));

        return this.updateById(user);
    }

    @Override
    public long userRegister(String userAccount, String code, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 4 || checkPassword.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3、分配 appKey 和 appSecret
            String appKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomString(3));
            String appSecret = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomString(8));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAppKey(appKey);
            user.setAppSecret(appSecret);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
//        if (userPassword.length() < 8) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
//        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在

        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        log.info("Storing user information in session: {}", user);

        return user;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // todo 改为token
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public boolean storeImg(MultipartFile img, Long userId) throws IOException {

        /**
         * 1、从该MutipartFile对象中获取字节流，
         * 2、将该字节流在转为图片
         * 3、保存在 static/users/user的id/sculptureHeader
         * 4、验证存入成功，
         * 5、将该对象的url保存到数据库
         * 6、验证保存成功
         */
        // 获取到当前的登录对象


        String relativePath = fileService.createImgFile(userId, img);

        // 返回文件的访问路径
        // todo 拼接请求路径，存入到数据库
        String uriString = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(relativePath)
                .toUriString();

        // 更新数据库
        User user = new User();
        user.setId(userId);
        user.setUserAvatar(uriString);
        if (!this.updateById(user)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }

        return true;
    }

}




