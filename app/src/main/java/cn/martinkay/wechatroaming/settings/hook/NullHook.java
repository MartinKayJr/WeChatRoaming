
package cn.martinkay.wechatroaming.settings.hook;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.martinkay.wechatroaming.settings.base.ISwitchCellAgent;
import cn.martinkay.wechatroaming.settings.base.IUiItemAgent;
import cn.martinkay.wechatroaming.settings.base.IUiItemAgentProvider;
import cn.martinkay.wechatroaming.settings.base.annotation.UiItemAgentEntry;
import cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlinx.coroutines.flow.MutableStateFlow;

@UiItemAgentEntry
public class NullHook implements IUiItemAgent, IUiItemAgentProvider {

    public static final NullHook INSTANCE = new NullHook();

    private NullHook() {
    }

    public static void onAddAccountClick(@NonNull Context baseContext) {

    }

    @NonNull
    @Override
    public Function1<IUiItemAgent, String> getTitleProvider() {
        return (agent) -> "空HOOK";
    }

    @Nullable
    @Override
    public Function2<IUiItemAgent, Context, CharSequence> getSummaryProvider() {
        return null;
    }

    @Nullable
    @Override
    public MutableStateFlow<String> getValueState() {
        return null;
    }

    @Nullable
    @Override
    public Function1<IUiItemAgent, Boolean> getValidator() {
        return null;
    }

    @Nullable
    @Override
    public ISwitchCellAgent getSwitchProvider() {
        return null;
    }

    @Nullable
    @Override
    public Function3<IUiItemAgent, Activity, View, Unit> getOnClickListener() {
        return (agent, activity, view) -> {
            onAddAccountClick(activity);
            return Unit.INSTANCE;
        };
    }

    @Nullable
    @Override
    public Function2<IUiItemAgent, Context, String[]> getExtraSearchKeywordProvider() {
        return null;
    }

    @NonNull
    @Override
    public IUiItemAgent getUiItemAgent() {
        return this;
    }

    @NonNull
    @Override
    public String[] getUiItemLocation() {
        return FunctionEntryRouter.Locations.DebugCategory.DEBUG_CATEGORY;
    }

    @NonNull
    @Override
    public String getItemAgentProviderUniqueIdentifier() {
        return getClass().getName();
    }
}
