package cn.iocoder.yudao.module.crm.service.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 客户 Service 接口
 *
 * @author Wanwan
 */
public interface CrmCustomerService {

    /**
     * 创建客户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomer(@Valid CrmCustomerCreateReqVO createReqVO);

    /**
     * 更新客户
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomer(@Valid CrmCustomerUpdateReqVO updateReqVO);

    /**
     * 删除客户
     *
     * @param id 编号
     */
    void deleteCustomer(Long id);

    /**
     * 获得客户
     *
     * @param id 编号
     * @return 客户
     */
    CrmCustomerDO getCustomer(Long id);

    /**
     * 获得客户列表
     *
     * @param ids 编号
     * @return 客户列表
     */
    List<CrmCustomerDO> getCustomerList(Collection<Long> ids);

    /**
     * 获得客户分页
     *
     * @param pageReqVO 分页查询
     * @return 客户分页
     */
    PageResult<CrmCustomerDO> getCustomerPage(CrmCustomerPageReqVO pageReqVO);

    /**
     * 获得客户列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 客户列表
     */
    List<CrmCustomerDO> getCustomerList(CrmCustomerExportReqVO exportReqVO);

    /**
     * 校验客户是否存在
     *
     * @param customerId 客户id
     * @return
     */
    CrmCustomerDO validateCustomer(Long customerId);

}