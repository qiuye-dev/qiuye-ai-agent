package com.qiuye.yeaiagent.agent;

import com.qiuye.yeaiagent.exception.BusinessException;
import com.qiuye.yeaiagent.exception.ErrorCode;

/**
 * ReAct模式的代理实现类
 * 实现了 思考-行动 的循环模式
 */

public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前行动和思考好下一步形动
     * @return 表示是否执行行动 true 表示行动 false表示不执行
     */
    public abstract boolean think();

    /**
     * 执行的行动
     * @return 执行结果
     */
    public abstract String act();


    @Override
    public String step() {
        try {
            boolean thinkResult = think();
            if (!thinkResult){
                return "思考完成-不需要执行";
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "步骤执行失败" + e.getMessage();
        }
    }
}
