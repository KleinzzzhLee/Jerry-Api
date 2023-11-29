package com.api.project.controller;

import com.api.project.annotation.AuthCheck;
import com.api.project.common.BaseResponse;
import com.api.project.common.DeleteRequest;
import com.api.project.common.ErrorCode;
import com.api.project.common.ResultUtils;
import com.api.project.constant.UserConstant;
import com.api.project.exception.BusinessException;
import com.api.project.exception.ThrowUtils;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.api.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.api.project.model.entity.IdRequest;
import com.api.project.service.InterfaceInfoService;
import com.api.project.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.apiclientsdk.clients.ApiClient;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.api.common.model.entity.InterfaceInfo;
import com.api.common.model.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private ApiClient apiClient;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);

        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
//        boolean result = interfaceInfoService.save(interfaceInfo);
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if(oldInterfaceInfo == null) {
            ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);

        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }
    /**
     * 根据接口的id获取到接口信息
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo>  getInterfaceInfoById(long id) {
        if(id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return  ResultUtils.success(interfaceInfo);
    }

    /**
     * 获取到接口列表 仅管理员可见
     */

    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        if(interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfo);
        }
        QueryWrapper<InterfaceInfo> wrapper = new QueryWrapper<>(interfaceInfo);
        List<InterfaceInfo> list = interfaceInfoService.list(wrapper);
        return ResultUtils.success(list);
    }

    /**
     *  分页获取列表
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 提取封装在dto对象中的信息
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);

        // 分页查询， 查询页数， 和页面大小
        long current = interfaceInfoQueryRequest.getCurrent();
        long pageSize = interfaceInfoQueryRequest.getPageSize();
        // 支持关于description的模糊搜索
        String description = interfaceInfoQuery.getDescription();
        interfaceInfoQuery.setDescription(description);

        // 限制爬虫
        if(pageSize > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 进行分页查询
        QueryWrapper<InterfaceInfo> wrapper = new QueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(description), "description", description);

        Page<InterfaceInfo> page = new Page<>(current, pageSize);
        Page<InterfaceInfo> result = interfaceInfoService.page(page, wrapper);
        return ResultUtils.success(result);
    }

    /**
     * 将接口上线（仅管理员）
     *      将一个接口上线，考虑过程:
     *          1、在数据库中查询该接口是否存在
     *          2、验证该接口是否可用
     *          3、将数据库中的接口状态修改
     *      此时，就数据库中查询某个接口的信息，便只需要将该接口的id传入即可
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterface(@RequestBody IdRequest idRequest) {
        if(idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在数据库中查询该id是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(idRequest.getId());
        if(interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 验证此接口可以使用
        com.example.apiclientsdk.model.User user = new com.example.apiclientsdk.model.User();
        user.setName("test");
        String username = apiClient.getUsernameByPost(user);
        if(username == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        }

        // 修改此接口的状态
        interfaceInfo.setId(idRequest.getId());
        interfaceInfo.setStatus(1);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        if(!result) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        } else {
            return ResultUtils.success(result);
        }

    }

    /**
     * 下线接口（仅管理员）
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterface(@RequestBody IdRequest idRequest) {
        // 查询该端口是否存在，
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(idRequest.getId());

        if(interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 对接口进行下线处理
        interfaceInfo.setStatus(0);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        if(!result) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        } else {
            return ResultUtils.success(result);
        }

    }



    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest invokeRequest,
                                                     HttpServletRequest request) {
        if(invokeRequest == null || invokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在数据库中查询该id是否存在,以及 该id对应的接口的状态
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(invokeRequest.getId());
        if(interfaceInfo == null || interfaceInfo.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "当前接口存在问题...");
        }
        // 进行接口的调用，通过封装的SDK：ApiClient
        // 获得登录的用户， 得到其签名认证
        User loginUser = userService.getLoginUser(request);
        String appKey = loginUser.getAppKey();
        String appSecret = loginUser.getAppSecret();

        ApiClient apiClient = new ApiClient(appKey,appSecret);
        Gson gson = new Gson();
        com.example.apiclientsdk.model.User fromJson = gson.fromJson(invokeRequest.getUserRequestParams(), com.example.apiclientsdk.model.User.class);
        String nameByPost = apiClient.getUsernameByPost(fromJson);
        return ResultUtils.success(nameByPost);

    }
}
