package com.acgist.taoyao.signal.controller;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acgist.taoyao.boot.annotation.Description;
import com.acgist.taoyao.boot.config.Constant;
import com.acgist.taoyao.signal.protocol.Protocol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 信令
 * 
 * @author acgist
 */
@Tag(name = "信令", description = "信令管理")
@Slf4j
@RestController
@RequestMapping("/protocol")
public class ProtocolController {

    private final ApplicationContext applicationContext;
    
    public ProtocolController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Operation(summary = "信令列表", description = "信令列表Markdown")
    @GetMapping("/list")
    public String list() {
        final StringBuilder builder = new StringBuilder("""
            ## 信令格式
            
            ```
            {
                "header": {
                    "v": "消息版本",
                    "id": "消息标识",
                    "signal": "信令标识"
                },
                "code": "状态编码",
                "message": "状态描述",
                "body": {
                    ...
                }
            }
            ```
            
            ### 符号解释
            
            ```
            -[消息类型]> 异步请求 | 单播
            =[消息类型]> 同步请求
            -[消息类型]) 全员广播：对所有的终端广播信令（排除自己）
            +[消息类型]) 全员广播：对所有的终端广播信令（包含自己）
            ...：其他自定义的透传内容
            ```
            
            > 没有指定消息类型时表示和信令消息类型相同
            
            """);
        this.applicationContext.getBeansOfType(Protocol.class).entrySet().stream()
        .sorted((a, z) -> a.getValue().signal().compareTo(z.getValue().signal()))
        .forEach(e -> {
            final String key = e.getKey();
            final Protocol protocol = e.getValue();
            final String name = protocol.name();
            final String signal = protocol.signal();
            final Description annotation = protocol.getClass().getDeclaredAnnotation(Description.class);
            if(annotation == null) {
                log.info("信令没有注解：{}-{}", key, name);
                return;
            }
            // 信令名称
            builder.append("### ").append(name).append("（").append(signal).append("）")
            .append(Constant.LINE).append(Constant.LINE)
            .append("```").append(Constant.LINE);
            // 消息主体
            builder.append("# 消息主体").append(Constant.LINE);
            Stream.of(annotation.body()).forEach(line -> builder.append(line.strip()).append(Constant.LINE));
            // 数据流向
            builder.append("# 数据流向").append(Constant.LINE);
            Stream.of(annotation.flow()).forEach(line -> builder.append(line.strip()).append(Constant.LINE));
            builder.append("```").append(Constant.LINE).append(Constant.LINE);
            // 描述信息
            final String memo = annotation.memo().strip();
            if(StringUtils.isNotEmpty(memo)) {
                builder.append(memo).append(Constant.LINE).append(Constant.LINE);
            }
        });
        return builder.toString();
    }
    
}
