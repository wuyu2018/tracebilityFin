package com.foodtraceability.aop;

import com.foodtraceability.anchor.repository.OperationLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogRepository repository;

    public OperationLogAspect(OperationLogRepository repository) {
        this.repository = repository;
    }

    @Around("@annotation(operationLog)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        String operator = getCurrentOperator();
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - startTime;
        Long entityId = extractEntityId(joinPoint, result);
        String summary = buildSummary(operationLog.entityType(), operationLog.action(), entityId);

        com.foodtraceability.anchor.entity.OperationLog entity =
                new com.foodtraceability.anchor.entity.OperationLog();
        entity.setOperator(operator);
        entity.setEntityType(operationLog.entityType());
        entity.setEntityId(entityId);
        entity.setAction(operationLog.action());
        entity.setSummary(summary);
        try {
            repository.save(entity);
        } catch (Exception e) {
            log.error("[操作日志] 写入失败: {} {} {} - {}", operator, operationLog.action(), summary, e.getMessage());
        }

        log.debug("[操作日志] {} {} {} - 耗时: {}ms", operator, operationLog.action(), summary, duration);
        return result;
    }

    private String getCurrentOperator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    private Long extractEntityId(ProceedingJoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Annotation[][] paramAnns = method.getParameterAnnotations();

        for (int i = 0; i < paramAnns.length; i++) {
            String paramName = null;
            for (Annotation ann : paramAnns[i]) {
                if (ann instanceof PathVariable pv) {
                    paramName = pv.value().isEmpty() ? pv.name() : pv.value();
                } else if (ann instanceof RequestParam rp) {
                    paramName = rp.value().isEmpty() ? rp.name() : rp.value();
                }
            }
            if (paramName != null && args[i] instanceof Number num) {
                if (paramName.equals("id") || paramName.endsWith("Id")) {
                    return num.longValue();
                }
            }
        }

        if (result instanceof com.foodtraceability.dto.SecurityCodeGenerateResponse r) {
            return null;
        }

        if (result instanceof org.springframework.http.ResponseEntity<?> re
                && re.getBody() instanceof Map<?, ?> map) {
            Object idVal = map.get("id");
            if (idVal instanceof Number num) {
                return num.longValue();
            }
        }

        return null;
    }

    private String buildSummary(String entityType, String action, Long entityId) {
        String typeName = switch (entityType) {
            case "PRODUCT" -> "产品";
            case "MATERIAL" -> "原料品种";
            case "MATERIAL_PURCHASE" -> "原料采购";
            case "BATCH" -> "生产批次";
            case "STORAGE" -> "仓储";
            case "INSPECTION" -> "检测";
            case "TRANSPORT_SALE" -> "运输销售";
            case "SECURITY_CODE" -> "防伪码";
            case "COMPLAINT" -> "投诉";
            case "ADMIN" -> "管理员";
            default -> entityType;
        };
        String actionName = switch (action) {
            case "CREATE" -> "创建";
            case "UPDATE" -> "修改";
            case "DELETE" -> "删除";
            case "HARD_DELETE" -> "物理删除";
            case "ACTIVATE" -> "启用";
            case "DEACTIVATE" -> "停用";
            case "LOGIN" -> "登录";
            case "REGISTER" -> "注册";
            case "BATCH_DELETE" -> "批量删除";
            case "FREEZE" -> "冻结";
            default -> action;
        };
        if (entityId != null) {
            return actionName + typeName + " ID=" + entityId;
        }
        return actionName + typeName;
    }
}
