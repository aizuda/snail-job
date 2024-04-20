package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientQueryVO;
import com.aizuda.snailjob.server.web.model.request.NotifyRecipientRequestVO;
import com.aizuda.snailjob.server.web.model.response.CommonLabelValueResponseVO;
import com.aizuda.snailjob.server.web.model.response.NotifyRecipientResponseVO;

import java.util.List;

/**
 * @author opensnail
 * @date 2024-04-17 21:24:21
 * @since sj_1.0.0
 */
public interface NotifyRecipientService {

    PageResult<List<NotifyRecipientResponseVO>> getNotifyRecipientPageList(NotifyRecipientQueryVO queryVO);

    Boolean saveNotifyRecipient(NotifyRecipientRequestVO requestVO);

    Boolean updateNotifyRecipient(NotifyRecipientRequestVO requestVO);

    List<CommonLabelValueResponseVO> getNotifyRecipientList();

}
