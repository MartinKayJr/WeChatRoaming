package cn.martinkay.wechatroaming.settings.base.annotation;

import cn.martinkay.wechatroaming.settings.base.IUiItemAgentProvider;

/**
 * 这是一个UI item agent入口（与hook无关）。
 * <p>
 * 它应该是一个 Kotlin 对象（或一个带有 public static final INSTANCE 字段的 Java 类）。
 * <p>
 * 目标应该是 {@link IUiItemAgentProvider} 的一个实例
 */
public @interface UiItemAgentEntry {

}
