package com.api.common.service;

import com.api.common.model.entity.InterfaceInfo;

public interface InnerInterfaceInfoService {

    InterfaceInfo getInterfaceInfo(String path, String method);

}
