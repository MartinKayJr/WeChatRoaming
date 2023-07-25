
package cn.martinkay.wechatroaming.settings.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.system.Os
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import cn.martinkay.wechatroaming.databinding.FragmentAbiVariantInfoBinding
import cn.martinkay.wechatroaming.databinding.ItemHostStatusBinding
import cn.martinkay.wechatroaming.utils.hookstatus.AbiUtils
import cn.martinkay.wechatroaming.utils.hookstatus.HookStatus
import cn.martinkay.wechatroaming.utils.host.hostInfo
import cn.martinkay.wechatroaming.utils.host.isInModuleProcess
import cn.martinkay.wechatroaming.settings.fragment.CheckAbiVariantModel.AbiInfo

class CheckAbiVariantFragment : BaseRootLayoutFragment() {

    private var mBinding: FragmentAbiVariantInfoBinding? = null

    override fun doOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        title = "原生库 ABI"
        mBinding = FragmentAbiVariantInfoBinding.inflate(inflater, container, false).apply {
            btnDownloadFromGitHub.setOnClickListener {
                openUri("https://github.com/cinit/QAuxiliary/releases/latest")
            }
            btnDownloadFromTelegram.setOnClickListener {
                openUri("https://t.me/QAuxiliary")
            }
            itemHostWechat.itemHostIgnoreButton.setOnClickListener(onIgnoreClickListener)
        }
        updateView()
        rootLayoutView = mBinding!!.root
        return mBinding!!.root
    }

    @UiThread
    private fun updateView() {
        mBinding?.apply {
            val abiStatus = CheckAbiVariantModel.collectAbiInfo(requireContext())
            val isTaiChiYin = isInModuleProcess && HookStatus.isTaiChiInstalled(requireContext()) && !HookStatus.isZygoteHookMode()
            if (isTaiChiYin && abiStatus.suggestedApkAbiVariant != "universal") {
                abiStatus.suggestedApkAbiVariant = "armAll"
            }
            if (!abiStatus.isAbiMatch || (isTaiChiYin && abiStatus.suggestedApkAbiVariant != "universal" && "armAll" != AbiUtils.getModuleFlavorName())) {
                warnAbiMismatchBar.visibility = View.VISIBLE
                recommendedModuleAbiVariant.text = "推荐您将模块更换为使用 ${abiStatus.suggestedApkAbiVariant} 原生库的版本" +
                    (if (isTaiChiYin) "\n太极用户请使用 armAll 原生库，其他版本将不会生效" else "")
            } else {
                warnAbiMismatchBar.visibility = View.GONE
            }
            val message = StringBuilder("当前模块使用的原生库为 " + AbiUtils.getModuleFlavorName())
            if (AbiUtils.archStringToArchInt(Os.uname().machine) and (AbiUtils.ABI_X86 or AbiUtils.ABI_X86_64) != 0) {
                message.append("\n").append("当前系统 uname machine 为 ").append(Os.uname().machine)
            }
            currentModuleAbiVariant.text = message.toString()
            itemHostNotFound.visibility = if (abiStatus.packages.isEmpty()) View.VISIBLE else View.GONE
            updateHostItem(itemHostWechat, abiStatus.packages[CheckAbiVariantModel.HOST_PACKAGES[0]])
        }
    }

    @UiThread
    private fun updateHostItem(binding: ItemHostStatusBinding, pkg: AbiInfo.Package?) {
        if (pkg == null) {
            binding.root.visibility = View.GONE
            return
        }
        binding.root.visibility = View.VISIBLE
        binding.hostDescription.text = AbiUtils.archIntToNames(pkg.abi)
        binding.hostTitle.text = pkg.packageName
        binding.itemHostIgnoreButton.tag = pkg.packageName
        if (hostInfo.packageName == pkg.packageName) {
            binding.itemHostIgnoreButton.apply {
                text = "当前应用"
                isClickable = false
            }
        } else {
            binding.itemHostIgnoreButton.apply {
                text = if (pkg.ignored) "已忽略" else "忽略"
                isClickable = true
            }
        }
    }

    private val onIgnoreClickListener = View.OnClickListener { v ->
        val pkg = v.tag as String
        CheckAbiVariantModel.setPackageIgnored(pkg, !CheckAbiVariantModel.isPackageIgnored(pkg))
        updateView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onResume() {
        super.onResume()
        updateView()
    }

    private fun openUri(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }
}
