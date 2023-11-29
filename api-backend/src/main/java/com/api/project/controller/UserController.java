package com.api.project.controller;

import com.api.common.model.entity.User;
import com.api.project.annotation.AuthCheck;
import com.api.project.common.BaseResponse;
import com.api.project.common.DeleteRequest;
import com.api.project.common.ErrorCode;
import com.api.project.common.ResultUtils;
import com.api.project.constant.UserConstant;
import com.api.project.exception.BusinessException;
import com.api.project.exception.ThrowUtils;
import com.api.project.model.dto.user.*;
import com.api.project.model.entity.IdRequest;
import com.api.project.model.vo.UserVO;
import com.api.project.service.UserService;
import com.api.project.service.commonservice.MailService;
import com.api.project.service.impl.UserServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.api.project.common.ErrorCode.*;

/**
 * 用户接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/user")
public class UserController {
    // 创建一个HashSet， 判断传入的文件类型
    private static HashSet<String> set = new HashSet<>();
    private static final int MAX_SIZE = 5 * 1024;
    private static final int MIN_SIZE =  10;
    static {
        set.add("image/jpeg");
        set.add("image/png");
        set.add("image/gif");
        set.add("image/bmp");
        set.add("image/webp");
    }
    @Resource
    private UserService userService;

    @Resource
    private MailService mailService;

    @Resource
    private RedisTemplate redisTemplate;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping({"/register"})
    public BaseResponse userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();

        // 从数据库中查询该账户的信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        if (userService.getOne(queryWrapper) != null) {
            return ResultUtils.error(1,"账户已存在");
        }

        //  todo 和redis当中的验证码进行对比
        String code = userRegisterRequest.getCode();
        String codeOld = (String) redisTemplate.opsForValue().get(userRegisterRequest.getUserAccount());
        if(codeOld == null || code == null || !code.equals(codeOld)) {
            return ResultUtils.error(1,"请检查验证码信息/或者再次请求验证码");
        }

        User user = new User();
        BeanUtils.copyProperties(userRegisterRequest, user);

        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, code, userPassword, checkPassword);
        redisTemplate.delete(userRegisterRequest.getUserAccount());
        return ResultUtils.success(result);
    }

    @PostMapping("/updatePassword")
    public BaseResponse updatePassword(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 从redis中取出验证码进行比较数据
        String oldCode = (String) redisTemplate.opsForValue().get(userRegisterRequest.getUserAccount());
        if(oldCode== null || !oldCode.equals(userRegisterRequest.getCode())) {
            return ResultUtils.error(PARAMS_ERROR, "验证码错误");
        }
        // 对密码进行加密
        String password = DigestUtils.md5DigestAsHex((UserServiceImpl.SALT + userRegisterRequest.getUserPassword()).getBytes());

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userRegisterRequest.getUserAccount());
        User user = new User();
        BeanUtils.copyProperties(userRegisterRequest, user);
        user.setUserPassword(password);
        boolean update = userService.update(user, queryWrapper);
        if(!update) {
            throw new BusinessException(SYSTEM_ERROR);
        }
        // 将验证码删除
        Boolean delete = redisTemplate.delete(userRegisterRequest.getUserAccount());
        if(!delete) {
            throw new BusinessException(REDIS_DELET_EERROR);
        }

        return ResultUtils.success("修改成功");
    }

    /**
     * 用户注册时获取验证码
     */
    @PostMapping("/getCode")
    public BaseResponse getCode(@RequestBody UserRegisterRequest userRegisterRequest) {
        // todo 进行判断， 如果update为1，则表示注册， 如果update为0，表示新增用户
        if(userRegisterRequest.getUpdate() == 1) {
            // 判断该用户是否存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userRegisterRequest.getUserAccount());
            if (userService.getOne(queryWrapper) == null) {
                return ResultUtils.error(ACCOUNT_NOTEXIST);
            }
        }
        // 账户存在，正常发送
        String code = mailService.sendMail(userRegisterRequest.getUserAccount());
        if(code == null || code.length() != 6) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        // todo 将验证码存入到redis，一个邮件只能存一个
        redisTemplate.opsForValue().set(userAccount, code);
        // todo 使用set，五分钟有效
        // 设置过期时间
        redisTemplate.expire(userAccount, 300, TimeUnit.SECONDS);

        // todo 验证存入成功
        if (redisTemplate.opsForValue().get(userAccount) == null) {
            // todo 未找到 返回异常
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(code);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //  todo 后端生成签名，保存到redis中，
        //       将签名发送给前端，前端每次发送请求都携带 token
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        String userId = String.valueOf(user.getId());
        // todo 向redis中保存用户的登录态
        // 设置过期时间为5分钟
        Map<String, Object> map = new HashMap<>();
        map.put("leaveTimes", 200);
        redisTemplate.opsForHash().putAll(String.valueOf(user.getId()), map);
        redisTemplate.expire(userId, 15 * 60, TimeUnit.SECONDS);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        if(user.getUserName() == null) {
            user.setUserName("");
        }
        if(user.getUserProfile() == null) {
            user.setUserProfile("");
        }
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取用户只是获取到包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        userVO.setCreateTime(sdf.format(user.getCreateTime()));
        userVO.setUpdateTime(sdf.format(user.getUpdateTime()));
        return ResultUtils.success(userVO);
    }

    /**
     * 根据 id 查询用户的所有信息
     *
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id){
        if(id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        // 封装的抛出异常类
        ThrowUtils.throwIf(user == null, NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }


    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        long current = 1;
        long size = 5;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(userPage);
    }

    /**
     * 更新自己的appSecret
     */
    @PostMapping("/update/appSecret")
    public BaseResponse<Boolean> updateAppSecret(@RequestBody IdRequest idRequest) {
        if(idRequest == null || idRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(!userService.updateAppSecret(idRequest), OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更换头像
     */
    @PostMapping("/update/avatar/{userId}")
    public BaseResponse updateUserAvatar(@RequestParam("file") MultipartFile file,
                                         @PathVariable(name = "userId") Long userId) throws IOException {
        // todo 使用redis或者token令牌桶或者对userId进行就加密，
        //  使得即使id泄露，也不会发生流量爆炸的问题

        //  todo 判断文件是否为空
        //  todo 判断文件类型 不是图片返回null
        if(file.isEmpty() || !set.contains(file.getContentType())) {
            return ResultUtils.error(PARAMS_ERROR, "文件类型不能为null或者文件类型不匹配");
        }
        if(userId == null) {
            return ResultUtils.error(PARAMS_ERROR);
        }
        double size = file.getSize() / 1024;
        if(size > MAX_SIZE || size < MIN_SIZE) {
            return ResultUtils.error(PARAMS_ERROR, "图片过大过着过小");
        }
        // todo 进入service文件，对图片进一步判断和校验，

        if (!userService.storeImg(file, userId)) {
            throw new IOException("保存失败");
        }

        return ResultUtils.success(true);
    }



    /**
     *  解封账号
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onLineUser(@RequestBody IdRequest idRequest) {
        if(idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在数据库中查询该id是否存在
        User user = userService.getById(idRequest.getId());
        if(user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 修改此用户的状态
        user.setId(idRequest.getId());
        user.setIsDelete(0);
        boolean result = userService.updateById(user);
        if(!result) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        } else {
            return ResultUtils.success(result);
        }

    }

    /**
     * 封禁账号
     * @param idRequest
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineUser(@RequestBody IdRequest idRequest) {
        // 查询该用户是否存在，
        User user = userService.getById(idRequest.getId());

        if(user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 对用户进行下线处理
        user.setIsDelete(1);
        boolean result = userService.updateById(user);
        if(!result) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        } else {
            return ResultUtils.success(result);
        }

    }
}
