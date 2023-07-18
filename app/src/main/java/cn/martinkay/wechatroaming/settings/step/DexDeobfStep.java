package cn.martinkay.wechatroaming.settings.step;

import cn.martinkay.wechatroaming.utils.dexkit.DexKit;
import cn.martinkay.wechatroaming.utils.dexkit.DexKitTarget;
import cn.martinkay.wechatroaming.utils.dexkit.DexKitTargetSealedEnum;

public class DexDeobfStep implements Step {

    private final DexKitTarget target;

    public DexDeobfStep(String id) {
        this.target = DexKitTargetSealedEnum.INSTANCE.valueOf(id);
    }

    public DexDeobfStep(DexKitTarget target) {
        this.target = target;
    }

    public String getId() {
        return DexKitTargetSealedEnum.INSTANCE.nameOf(target);
    }

    @Override
    public boolean step() {
        try {
            if (target instanceof DexKitTarget.UsingStr) {
                var t = (DexKitTarget.UsingStr) target;
                if (t.getFindMethod()) {
                    DexKit.doFindMethod(t);
                } else {
                    DexKit.doFindClass(t);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isDone() {
        return !DexKit.isRunDexDeobfuscationRequired(target);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getDescription() {
        if (target instanceof DexKitTarget.UsingStr) {
            var t = (DexKitTarget.UsingStr) target;
            if (t.getFindMethod()) {
                return "定位被混淆方法: " + getId();
            } else {
                return "定位被混淆类: " + getId();
            }
        }
        return "Unsupported Type";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DexDeobfStep that = (DexDeobfStep) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
