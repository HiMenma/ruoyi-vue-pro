package cn.iocoder.dashboard.modules.system.service.logger;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum;
import cn.iocoder.dashboard.framework.tracer.core.util.TracerUtils;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysOperateLogDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.logger.SysOperateLogMapper;
import cn.iocoder.dashboard.modules.system.enums.common.SysSexEnum;
import cn.iocoder.dashboard.modules.system.service.logger.impl.SysOperateLogServiceImpl;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.RandomUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.RandomUtils.randomLongId;
import static cn.iocoder.dashboard.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Import({SysOperateLogServiceImpl.class})
public class SysOperateLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SysOperateLogService operateLogServiceImpl;

    @Resource
    private SysOperateLogMapper operateLogMapper;

    @MockBean
    private SysUserService userService;

    @Test
    public void testCreateOperateLogAsync() throws InterruptedException, ExecutionException {
        String traceId = TracerUtils.getTraceId();
        SysOperateLogCreateReqVO reqVO = RandomUtils.randomPojo(SysOperateLogCreateReqVO.class, o -> {
            o.setTraceId(traceId);
            o.setUserId(randomLongId());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });

        // 执行service方法
        Future<Boolean> future = operateLogServiceImpl.createOperateLogAsync(reqVO);
        future.get();
        // 断言插入是否正确
        SysOperateLogDO sysOperateLogDO = operateLogMapper.selectOne("trace_id", traceId);
        assertPojoEquals(reqVO, sysOperateLogDO);
    }

    @Test
    public void testGetOperateLogPage() {
        // 构造测试数据
        // 先构造用户
        SysUserDO user = RandomUtils.randomPojo(SysUserDO.class, o -> {
            o.setNickname("wangkai");
            o.setSex(SysSexEnum.MALE.getSEX());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByNickname("wangkai")).thenReturn(Collections.singletonList(user));
        Long userId = user.getId();
        // 构造操作日志
        SysOperateLogDO sysOperateLogDO = RandomUtils.randomPojo(SysOperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2021, 3, 6));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });
        operateLogMapper.insert(sysOperateLogDO);

        // 下面几个是不匹配的数据
        // 随机 userId
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setResultCode(GlobalErrorCodeConstants.BAD_REQUEST.getCode())));

        // 构造调用参数
        SysOperateLogPageReqVO reqVO = new SysOperateLogPageReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setBeginTime(buildTime(2021, 3, 5));
        reqVO.setEndTime(buildTime(2021, 3, 7));
        reqVO.setSuccess(true);

        // 调用service方法
        PageResult<SysOperateLogDO> pageResult = operateLogServiceImpl.getOperateLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(sysOperateLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetOperateLogs() {
        // 构造测试数据
        // 先构造用户
        SysUserDO user = RandomUtils.randomPojo(SysUserDO.class, o -> {
            o.setNickname("wangkai");
            o.setSex(SysSexEnum.MALE.getSEX());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByNickname("wangkai")).thenReturn(Collections.singletonList(user));
        Long userId = user.getId();
        // 构造操作日志
        SysOperateLogDO sysOperateLogDO = RandomUtils.randomPojo(SysOperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2021, 3, 6));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });
        operateLogMapper.insert(sysOperateLogDO);

        // 下面几个是不匹配的数据
        // 随机 userId
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode 不同
        operateLogMapper.insert(ObjectUtils.clone(sysOperateLogDO, logDO -> logDO.setResultCode(GlobalErrorCodeConstants.BAD_REQUEST.getCode())));

        // 构造调用参数
        SysOperateLogExportReqVO reqVO = new SysOperateLogExportReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setBeginTime(buildTime(2021, 3, 5));
        reqVO.setEndTime(buildTime(2021, 3, 7));
        reqVO.setSuccess(true);

        // 调用 service 方法
        List<SysOperateLogDO> list = operateLogServiceImpl.getOperateLogs(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(sysOperateLogDO, list.get(0));
    }

}
