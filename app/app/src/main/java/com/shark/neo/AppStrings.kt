package com.shark.neo

enum class AppLanguage(val code: String, val title: String, val nativeName: String) {
    EN("en", "English", "English"),
    ZH("zh", "Chinese", "中文"),
    RU("ru", "Russian", "Русский"),
    PT("pt", "Portuguese", "Português");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
    }
}

object AppStrings {
    // Language Selection Gate
    fun langGateTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "选择语言"
        AppLanguage.RU -> "Выберите язык"
        AppLanguage.PT -> "Escolha o idioma"
        else -> "Choose Language"
    }

    fun langGateSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "请选择 Shark Neo 应用显示语言\nPlease select your preferred language"
        AppLanguage.RU -> "Пожалуйста, выберите язык приложения\nPlease select your preferred language"
        AppLanguage.PT -> "Por favor, selecione o idioma do aplicativo\nPlease select your preferred language"
        else -> "Please select your preferred language\nPor favor, selecione seu idioma preferido"
    }

    fun langGateContinue(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "继续"
        AppLanguage.RU -> "Продолжить"
        AppLanguage.PT -> "Continuar"
        else -> "Continue"
    }

    // Legal Disclaimer phrases
    val legalPhraseZh = "我已确认能接受使用该工具可能造成的相关后果并承诺遵守开源协议"
    val legalPhraseEn = "I accept the risks and promise to comply with the open source license"
    val legalPhraseRu = "Я принимаю риски и обязуюсь соблюдать условия открытой лицензии"
    val legalPhrasePt = "Aceito os riscos e comprometo-me a cumprir a licença de código aberto"

    fun legalPhrase(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> legalPhraseZh
        AppLanguage.RU -> legalPhraseRu
        AppLanguage.PT -> legalPhrasePt
        else -> legalPhraseEn
    }

    fun legalTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "免责声明与开源协议"
        AppLanguage.RU -> "Отказ от ответственности и лицензия"
        AppLanguage.PT -> "Isenção de Responsabilidade e Licença"
        else -> "Disclaimer & Open Source License"
    }

    fun legalIntro(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "使用本工具前，请完整阅读以下内容；确认一次后不再弹出。不认同其中任何一条，请立即退出并卸载。"
        AppLanguage.RU -> "Перед использованием этого инструмента полностью ознакомьтесь со следующими условиями. После подтверждения окно больше не появится. Если вы не согласны, выйдите и удалите приложение."
        AppLanguage.PT -> "Antes de usar esta ferramenta, leia atentamente os seguintes termos. Uma vez confirmado, este aviso não será exibido novamente. Se não concordar, saia e desinstale o app imediatamente."
        else -> "Before using this tool, please read the following terms completely. Once confirmed, this prompt will not appear again. If you disagree with any terms, please exit and uninstall immediately."
    }

    fun legalDisclaimerSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "免责声明"
        AppLanguage.RU -> "Отказ от ответственности"
        AppLanguage.PT -> "Isenção de Responsabilidade"
        else -> "Disclaimer"
    }

    val legalDisclaimerPointsZh = listOf(
        "1. 本工具仅限用于您本人拥有或已获得明确授权的设备，用于系统研究、学习与日常维护。严禁用于任何未经授权的设备、他人财产或非法用途；由此产生的一切后果由使用者自行承担。",
        "2. 本工具通过利用系统内核漏洞实现临时越狱与提权，属于对操作系统运行时行为的深度修改，其行为方式与常规应用程序有本质区别，可能被杀毒软件、企业 MDM 或应用商店安全扫描标记为风险程序。",
        "3. 使用过程中可能出现设备重启、卡死、无法开机、数据丢失、系统分区损坏等情况；严重时可能需要刷机恢复出厂设置甚至送修。请在操作前完整备份重要数据（照片、聊天记录、密钥、银行与支付凭证等），并确认自己具备恢复设备的能力。",
        "4. 越狱会削弱或绕过系统的部分安全机制（包括 SELinux、签名校验与启动链完整性检查），设备在越狱期间及其后一段时间内更容易受到恶意软件攻击，银行、支付、政务、企业办公等敏感应用可能拒绝运行或检测到环境异常。",
        "5. 越狱后设备的保修与售后政策可能受影响；系统 OTA 更新可能失败、被阻止或因版本不匹配导致开机异常。是否保留 Root、何时撤销以及何时更新系统，完全由使用者自行权衡并承担责任。",
        "6. 本工具按「现状」（AS IS）提供，不包含任何明示或暗示的保证，包括但不限于适销性、特定用途适用性、稳定性与不侵权保证。作者不对本工具在任何特定设备、特定系统版本或特定场景下的可用性、成功率或安全性做任何承诺。",
        "7. 在适用法律允许的最大范围内，作者与贡献者不对因使用或无法使用本工具而导致的任何直接、间接、附带、特殊、惩罚性或后果性损失承担责任，包括但不限于数据丢失、利润损失、业务中断、设备损坏、保修失效或第三方索赔。",
        "8. 使用者须自行确保使用行为符合所在地区适用的法律法规以及设备所属机构的管理制度，不得将本工具用于规避他人设备的安全策略、逃避计费、破解受保护内容或攻击网络设施。",
        "9. 本工具不提供任何云端账户、增值服务或商业付费功能，不主动收集、上传任何用户个人隐私数据；所有提权操作均在本地运行。非官方渠道重新打包的版本可能包含未经验证的修改，请仅从官方仓库获取并自行核对校验和。",
        "10. 是否使用本工具、在哪些设备上使用、使用到何种程度，完全由您自行决定。若您对上述任何风险存在疑虑，或无法承担相应后果，请立即退出并卸载本工具。",
        "11. 本工具包含的第三方组件、预编译库与素材分别遵循其自身许可证，相关声明见应用内「关于 — 许可」页面与随仓库分发的 NOTICE 文件；这些组件不因本声明的签署而改变其许可条件。",
        "12. 作者保留在不另行通知的情况下修改、暂停或终止本项目及任何功能的权利；条款更新后若您继续使用，即视为接受更新后的全部内容。"
    )

    val legalDisclaimerPointsEn = listOf(
        "1. This tool is strictly intended for devices you personally own or have received explicit authorization to use, for system research, educational purposes, and routine maintenance. Any unauthorized use, use on property of others, or for unlawful purposes is strictly prohibited; all consequences arising therefrom shall be borne solely by the user.",
        "2. This tool exploits operating system kernel vulnerabilities to achieve temporary jailbreak and root privilege escalation. It deeply modifies OS runtime behavior, fundamentally differing from standard applications, and may be flagged as high-risk by antivirus software, enterprise MDM, or security scanners.",
        "3. During operation, device reboots, freezes, bootloops, data loss, or partition corruption may occur; in severe cases, flashing stock firmware or hardware servicing may be required. Fully back up all important data (photos, chats, keys, credentials) before proceeding and ensure you can recover your device.",
        "4. Jailbreaking weakens or bypasses system security mechanisms (SELinux, signature verification, verified boot). The device becomes more vulnerable to malware, and sensitive applications (banking, payment, enterprise apps) may refuse to run or detect an abnormal environment.",
        "5. Device warranty and after-sales service policies may be affected. System OTA updates may fail, be blocked, or cause boot failures due to version mismatches. Whether to retain root, when to revoke it, and when to update firmware is solely at your own discretion and risk.",
        "6. This tool is provided \"AS IS\" without warranty of any kind, express or implied, including but not limited to warranties of merchantability, fitness for a particular purpose, stability, and non-infringement. Authors do not warrant suitability for any specific device, OS build, or use case.",
        "7. To the maximum extent permitted by applicable law, the authors and contributors disclaim liability for any direct, indirect, incidental, special, or consequential damages, including loss of data, profit loss, business disruption, device damage, warranty invalidation, or third-party claims.",
        "8. You must ensure that your usage complies with applicable local laws and regulations as well as policies of organizations governing your device. You must not use this tool to bypass security controls on others' devices, avoid billing, crack protected content, or disrupt networks.",
        "9. This tool does not provide accounts, subscriptions, or cloud services; it does not collect or upload personal data. All operations execute strictly locally on your device. Unofficial builds may contain modifications; obtain builds only from official repositories and verify checksums.",
        "10. Whether to use this tool, on which devices, and to what extent is entirely your choice. If you have any doubts regarding the risks or cannot accept the potential consequences, please exit and uninstall this tool immediately.",
        "11. Third-party components, precompiled libraries, and assets bundled with this tool are subject to their respective licenses (see \"About — Licenses\" and the NOTICE file). Their licensing terms are not altered by this agreement.",
        "12. The authors reserve the right to modify, suspend, or terminate this project or any features without prior notice. Continued use following any revisions constitutes full acceptance of the updated terms."
    )

    val legalDisclaimerPointsRu = listOf(
        "1. Этот инструмент предназначен исключительно для устройств, которыми вы лично владеете или имеете явное разрешение на использование в исследовательских и образовательных целях. Любое несанкционированное или незаконное использование строго запрещено.",
        "2. Инструмент использует уязвимости ядра операционной системы для временного получения root-прав, глубоко изменяя поведение системы. Антивирусы и системы безопасности могут определять его как потенциально опасное ПО.",
        "3. В процессе работы возможны перезагрузки, зависания, потеря данных или сбои разделов. Обязательно создайте резервную копию всех важных данных перед использованием.",
        "4. Получение root-прав ослабляет встроенные механизмы безопасности (SELinux, целостность загрузки). Некоторые банковские и платежные приложения могут отказаться работать.",
        "5. Гарантийные обязательства производителя могут быть аннулированы. Обновления OTA могут работать со сбоями.",
        "6. Приложение предоставляется «КАК ЕСТЬ», без каких-либо явных или подразумеваемых гарантий стабильности и безопасности.",
        "7. Авторы не несут ответственности за прямой или косвенный ущерб, включая потерю данных или выход устройства из строя.",
        "8. Вы обязаны соблюдать местное законодательство и правила использования устройства.",
        "9. Инструмент работает полностью локально, не содержит облачных сервисов и не собирает персональные данные. Загружайте приложение только из официального репозитория.",
        "10. Решение об использовании инструмента принимается вами самостоятельно под личную ответственность.",
        "11. Сторонние библиотеки и бинарные компоненты подчиняются собственным лицензиям.",
        "12. Авторы оставляют за собой право изменять или прекращать развитие проекта в любое время."
    )

    val legalDisclaimerPointsPt = listOf(
        "1. Esta ferramenta destina-se estritamente a dispositivos de sua propriedade ou para os quais você possui autorização expressa, para fins de pesquisa e manutenção. O uso não autorizado é proibido.",
        "2. A ferramenta utiliza vulnerabilidades do kernel para obter privilégios de root temporários, modificando profundamente o comportamento do sistema operacional. Pode ser sinalizada por antivírus.",
        "3. Durante a execução, podem ocorrer reinicializações, travamentos ou perda de dados. Faça backup completo de suas fotos, mensagens e dados importantes antes de prosseguir.",
        "4. O jailbreak enfraquece mecanismos de segurança (SELinux, verificação de inicialização). Aplicativos bancários ou corporativos podem não funcionar.",
        "5. A garantia do dispositivo pode ser afetada e as atualizações OTA do sistema podem apresentar falhas.",
        "6. Esta ferramenta é fornecida \"COMO ESTÁ\", sem qualquer garantia expressa ou implícita.",
        "7. Os autores e colaboradores não se responsabilizam por danos diretos ou indiretos, perda de dados ou danos ao aparelho.",
        "8. O usuário deve garantir conformidade com as leis e regulamentos locais aplicáveis.",
        "9. O aplicativo executa todas as operações localmente e não coleta dados pessoais. Obtenha compilações apenas do repositório oficial.",
        "10. A decisão de usar esta ferramenta cabe inteiramente ao usuário.",
        "11. Componentes e bibliotecas de terceiros estão sujeitos às suas respectivas licenças.",
        "12. Os autores reservam-se o direito de atualizar estes termos a qualquer momento."
    )

    fun legalDisclaimerPoints(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.ZH -> legalDisclaimerPointsZh
        AppLanguage.RU -> legalDisclaimerPointsRu
        AppLanguage.PT -> legalDisclaimerPointsPt
        else -> legalDisclaimerPointsEn
    }

    fun legalLicenseSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "开源许可（GNU GPLv3）"
        AppLanguage.RU -> "Лицензия с открытым исходным кодом (GNU GPLv3)"
        AppLanguage.PT -> "Licença de Código Aberto (GNU GPLv3)"
        else -> "Open Source License (GNU GPLv3)"
    }

    val legalLicensePointsZh = listOf(
        "1. 本项目（含应用程序与内核载荷源码）以 GNU 通用公共许可证第 3 版（GPLv3）发布，许可证全文见项目根目录的 LICENSE 文件，二者内容不一致时以 LICENSE 全文为准。",
        "2. 您享有运行、研究、修改与再分发本程序的自由，但行使上述自由的前提是完整遵守 GPLv3 的全部条款。",
        "3. 再分发本程序或其修改版本时，必须保留原有的版权声明与许可证文本；在修改过的文件中显著标明修改内容及修改日期；并向接收者提供完整的对应源代码（§5）。",
        "4. 以目标代码（二进制）形式分发时，必须同时以不高于实际分发成本的合理方式提供对应源代码，或提供有效期不少于三年的书面获取承诺（§6）。",
        "5. 通过用户产品（如预装设备、定制固件）分发时，还须提供必要的「安装信息」，使接收者能够在其设备上安装修改后的版本（§6，防止通过硬件限制架空许可证）。",
        "6. 您不得对 GPLv3 授予的权利附加任何额外限制；分发时不得附加与本许可证冲突的条款，亦不得对接收者行使许可证权利收取许可费（§7、§10）。",
        "7. 本程序的贡献者授予您非排他性的专利许可；若您对任何贡献者发起专利诉讼，您依据本许可证获得的专利许可将相应终止（§11）。",
        "8. 许可证授权在您违反条款时自动终止；若您在收到通知后 30 天内纠正违规行为，授权可自纠正之日起恢复（§8）。",
        "9. 本程序不提供任何担保；在适用法律允许的范围内，作者与贡献者不对任何损害承担赔偿责任（§15、§16）。",
        "10. GPLv3 仅授予著作权与专利层面的许可，不授予本项目名称、图标、商标或品牌的商业使用权；未经许可不得以本项目名义发布衍生版本或暗示官方背书。"
    )

    val legalLicensePointsEn = listOf(
        "1. This project (including app and exploit loader source) is released under the GNU General Public License v3 (GPLv3). The complete license text is available in the repository root LICENSE file.",
        "2. You are free to run, study, modify, and redistribute this software, provided that you fully comply with all terms of the GPLv3.",
        "3. When redistributing this program or modified versions, you must retain existing copyright notices, prominently mark modifications and dates, and provide the complete corresponding source code (§5).",
        "4. Binary distributions must be accompanied by corresponding machine-readable source code or a written offer valid for at least three years (§6).",
        "5. If distributed in user products (pre-installed, firmware), you must provide necessary Installation Information so users can install modified software (§6).",
        "6. You may not impose further restrictions on the rights granted under GPLv3, nor charge licensing royalties for exercising permissions (§7, §10).",
        "7. Contributors grant non-exclusive patent licenses; initiating patent litigation against contributors terminates your patent grant (§11).",
        "8. Violations automatically terminate permissions under GPLv3, subject to the cure period specified in §8.",
        "9. The software is provided WITHOUT ANY WARRANTY; authors and contributors are not liable for any damages (§15, §16).",
        "10. GPLv3 does not grant trademark rights or permission to use project names, logos, or branding to endorse derived works."
    )

    val legalLicensePointsRu = listOf(
        "1. Этот проект распространяется под лицензией GNU General Public License v3 (GPLv3). Полный текст лицензии доступен в файле LICENSE.",
        "2. Вы имеете право запускать, изучать, модифицировать и распространять это ПО при условии полного соблюдения всех условий GPLv3.",
        "3. При распространении измененных версий необходимо сохранять авторские права и предоставлять полный исходный код (§5).",
        "4. Распространение бинарных файлов должно сопровождаться исходным кодом или письменным обязательством его предоставления (§6).",
        "5. Запрещено налагать дополнительные ограничения на права, предоставляемые лицензией GPLv3 (§7, §10).",
        "6. Программа предоставляется БЕЗ КАКИХ-ЛИБО ГАРАНТИЙ. Авторы не несут ответственности за возможный ущерб (§15, §16).",
        "7. GPLv3 не предоставляет прав на использование товарных знаков, логотипов и фирменного стиля проекта."
    )

    val legalLicensePointsPt = listOf(
        "1. Este projeto é distribuído sob a GNU General Public License v3 (GPLv3). O texto integral da licença está no arquivo LICENSE.",
        "2. Você tem a liberdade de executar, estudar, modificar e redistribuir este software, desde que cumpra integralmente os termos da GPLv3.",
        "3. Ao redistribuir versões modificadas, você deve manter os avisos de direitos autorais e fornecer o código-fonte correspondente (§5).",
        "4. Distribuições binárias devem ser acompanhadas pelo código-fonte correspondente (§6).",
        "5. Nenhuma restrição adicional aos direitos da GPLv3 pode ser imposta (§7, §10).",
        "6. O software é fornecido SEM QUALQUER GARANTIA (§15, §16).",
        "7. A GPLv3 não concede direitos sobre marcas registradas, nomes ou logotipos do projeto."
    )

    fun legalLicensePoints(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.ZH -> legalLicensePointsZh
        AppLanguage.RU -> legalLicensePointsRu
        AppLanguage.PT -> legalLicensePointsPt
        else -> legalLicensePointsEn
    }

    fun legalEnd(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "—— 条文结束 ——"
        AppLanguage.RU -> "—— Конец документа ——"
        AppLanguage.PT -> "—— Fim dos termos ——"
        else -> "—— End of Terms ——"
    }

    fun legalScrollPrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "请在阅读完整条文后，在下方输入框中完整键入以下文字："
        AppLanguage.RU -> "Прочитав условия до конца, введите следующую фразу в поле ниже:"
        AppLanguage.PT -> "Após ler todos os termos, digite a frase exata no campo abaixo:"
        else -> "After reading the complete terms, please enter the following text exactly:"
    }

    fun legalTapToFill(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "（可点击上方文字自动填入）"
        AppLanguage.RU -> "(Нажмите на текст выше для автозаполнения)"
        AppLanguage.PT -> "(Toque no texto acima para preenchimento automático)"
        else -> "(Tap text above to auto-fill)"
    }

    fun legalPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "在此输入确认文字"
        AppLanguage.RU -> "Введите текст подтверждения"
        AppLanguage.PT -> "Digite o texto de confirmação aqui"
        else -> "Enter confirmation text here"
    }

    fun legalConfirmButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "确认并进入"
        AppLanguage.RU -> "Подтвердить и войти"
        AppLanguage.PT -> "Confirmar e Entrar"
        else -> "Confirm & Proceed"
    }

    fun legalMustScroll(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "请先滑动到条文最底部"
        AppLanguage.RU -> "Пожалуйста, сначала прокрутите до самого низа"
        AppLanguage.PT -> "Por favor, role até o final dos termos primeiro"
        else -> "Please scroll to the very bottom first"
    }

    fun legalTextMismatch(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "输入内容与要求不一致，请原样输入"
        AppLanguage.RU -> "Введенный текст не совпадает с требуемой фразой"
        AppLanguage.PT -> "O texto inserido não corresponde à frase exigida"
        else -> "Input does not match the required phrase"
    }

    // Navigation Tabs
    fun tabHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "首页"
        AppLanguage.RU -> "Главная"
        AppLanguage.PT -> "Início"
        else -> "Home"
    }

    fun tabDevices(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "设备"
        AppLanguage.RU -> "Устройства"
        AppLanguage.PT -> "Dispositivos"
        else -> "Devices"
    }

    fun supportedDevicesTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "支持设备列表"
        AppLanguage.RU -> "Поддерживаемые устройства"
        AppLanguage.PT -> "Dispositivos Suportados"
        else -> "Supported Devices"
    }

    fun supportedDevicesCount(count: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "共支持 $count 款设备与适配内核"
        AppLanguage.RU -> "Поддерживается устройств: $count"
        AppLanguage.PT -> "$count dispositivos suportados"
        else -> "$count Devices Supported"
    }

    fun copiedKernel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "内核版本号已复制到剪贴板！"
        AppLanguage.RU -> "Версия ядра скопирована в буфер!"
        AppLanguage.PT -> "Versão do kernel copiada!"
        else -> "Kernel release copied to clipboard!"
    }

    fun activeKernelMatch(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "当前设备运行的内核 (完全匹配)"
        AppLanguage.RU -> "Текущее ядро устройства (совпадает)"
        AppLanguage.PT -> "Kernel ativo deste aparelho (compatível)"
        else -> "Active Kernel (Matches This Device)"
    }

    fun searchDevicesPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "搜索设备或内核版本..."
        AppLanguage.RU -> "Поиск устройства или ядра..."
        AppLanguage.PT -> "Buscar dispositivo ou kernel..."
        else -> "Search device or kernel..."
    }

    fun thisDeviceHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "本机状态"
        AppLanguage.RU -> "Текущее устройство"
        AppLanguage.PT -> "Este Dispositivo"
        else -> "Current Device"
    }

    fun thisDeviceStatusSupported(engine: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "已支持 ($engine)"
        AppLanguage.RU -> "Поддерживается ($engine)"
        AppLanguage.PT -> "Suportado ($engine)"
        else -> "Supported ($engine)"
    }

    fun thisDeviceStatusUnsupported(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "暂未适配 • 点击运行诊断"
        AppLanguage.RU -> "Не поддерживается • Нажмите для диагностики"
        AppLanguage.PT -> "Não suportado • Toque para diagnosticar"
        else -> "Unsupported • Tap to diagnose"
    }

    fun noDevicesFound(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "未找到匹配的设备或内核"
        AppLanguage.RU -> "Ничего не найдено"
        AppLanguage.PT -> "Nenhum dispositivo encontrado"
        else -> "No matching devices found"
    }

    fun tabLogs(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "日志"
        AppLanguage.RU -> "Логи"
        AppLanguage.PT -> "Logs"
        else -> "Logs"
    }

    fun tabHistory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "历史"
        AppLanguage.RU -> "История"
        AppLanguage.PT -> "Histórico"
        else -> "History"
    }

    fun tabAbout(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "关于"
        AppLanguage.RU -> "О приложении"
        AppLanguage.PT -> "Sobre"
        else -> "About"
    }

    // Top Bar
    fun titleExtras(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "拓展功能"
        AppLanguage.RU -> "Дополнительно"
        AppLanguage.PT -> "Extras"
        else -> "Extras"
    }

    fun titleManual(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "使用手册"
        AppLanguage.RU -> "Инструкция"
        AppLanguage.PT -> "Manual"
        else -> "Manual"
    }

    fun titleLicenses(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "开放源代码许可"
        AppLanguage.RU -> "Открытые лицензии"
        AppLanguage.PT -> "Licenças de Código Aberto"
        else -> "Open Source Licenses"
    }

    // Home Screen
    fun statusAuthorized(backend: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "$backend 已授权"
        AppLanguage.RU -> "$backend авторизован"
        AppLanguage.PT -> "$backend Autorizado"
        else -> "$backend Authorized"
    }

    fun statusNotAuthorized(backend: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "$backend 未授权"
        AppLanguage.RU -> "$backend не авторизован"
        AppLanguage.PT -> "$backend Não Autorizado"
        else -> "$backend Not Authorized"
    }

    fun statusReadyDesc(backend: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "已连接到 $backend 服务，可以开始越狱"
        AppLanguage.RU -> "Подключено к $backend, готово к получению root"
        AppLanguage.PT -> "Conectado ao $backend, pronto para root"
        else -> "Connected to $backend, ready to root"
    }

    fun statusNotReadyDesc(backend: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "未连接到 $backend 服务，请点击授权"
        AppLanguage.RU -> "Нет подключения к $backend, нажмите для авторизации"
        AppLanguage.PT -> "Não conectado ao $backend, toque para autorizar"
        else -> "Not connected to $backend, please grant permission"
    }

    fun actionRunning(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "执行中…"
        AppLanguage.RU -> "Выполняется…"
        AppLanguage.PT -> "Executando…"
        else -> "Running…"
    }

    fun actionStellarNotRunning(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Stellar 未运行（点击打开）"
        AppLanguage.RU -> "Stellar не запущен (нажмите для открытия)"
        AppLanguage.PT -> "Stellar não está rodando (toque para abrir)"
        else -> "Stellar not running (tap to open)"
    }

    fun actionAuthorizeShizuku(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "授权 Shizuku"
        AppLanguage.RU -> "Авторизовать Shizuku"
        AppLanguage.PT -> "Autorizar Shizuku"
        else -> "Authorize Shizuku"
    }

    fun actionReRoot(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "检查 / 重新越狱"
        AppLanguage.RU -> "Проверить / Повторить root"
        AppLanguage.PT -> "Verificar / Obter Root Novamente"
        else -> "Verify / Re-root"
    }

    fun actionOneClickRoot(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "一键越狱"
        AppLanguage.RU -> "Root в один клик"
        AppLanguage.PT -> "Root em Um Clique"
        else -> "One-Click Root"
    }

    fun rootManager(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Root 方案"
        AppLanguage.RU -> "Менеджер Root"
        AppLanguage.PT -> "Gerenciador Root"
        else -> "Root Manager"
    }

    fun extras(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "拓展功能"
        AppLanguage.RU -> "Дополнительно"
        AppLanguage.PT -> "Extras"
        else -> "Extras"
    }

    fun manual(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "使用手册"
        AppLanguage.RU -> "Инструкция"
        AppLanguage.PT -> "Manual"
        else -> "Manual"
    }

    fun manualSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "使用步骤、功能说明与排障"
        AppLanguage.RU -> "Установка, возможности и устранение неполадок"
        AppLanguage.PT -> "Configuração, recursos e solução de problemas"
        else -> "Setup, features, and troubleshooting"
    }

    fun kernelVersion(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "内核版本"
        AppLanguage.RU -> "Версия ядра"
        AppLanguage.PT -> "Versão do Kernel"
        else -> "Kernel Version"
    }

    fun exploitMethod(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "利用方案"
        AppLanguage.RU -> "Метод эксплойта"
        AppLanguage.PT -> "Método de Exploit"
        else -> "Exploit Method"
    }

    fun deviceModel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "设备机型"
        AppLanguage.RU -> "Модель устройства"
        AppLanguage.PT -> "Modelo do Dispositivo"
        else -> "Device Model"
    }

    fun buildFingerprint(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "构建指纹"
        AppLanguage.RU -> "Отпечаток сборки"
        AppLanguage.PT -> "Impressão digital da build"
        else -> "Build Fingerprint"
    }

    fun selinuxStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "SELinux 状态"
        AppLanguage.RU -> "Статус SELinux"
        AppLanguage.PT -> "Status do SELinux"
        else -> "SELinux Status"
    }

    fun unsupportedDevice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "当前机型不支持"
        AppLanguage.RU -> "Устройство не поддерживается"
        AppLanguage.PT -> "Dispositivo não suportado"
        else -> "Unsupported Device"
    }

    // About Screen
    fun appLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "应用语言"
        AppLanguage.RU -> "Язык приложения"
        AppLanguage.PT -> "Idioma do Aplicativo"
        else -> "App Language"
    }

    fun chooseAppLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "选择应用语言"
        AppLanguage.RU -> "Выберите язык приложения"
        AppLanguage.PT -> "Escolha o Idioma do Aplicativo"
        else -> "Choose App Language"
    }

    fun aboutIconNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "App 图标由本项目绘制；第三方组件版权归各自作者所有，完整许可见「开放源代码许可」。"
        AppLanguage.RU -> "Иконка приложения создана для этого проекта; авторские права на сторонние компоненты принадлежат их авторам."
        AppLanguage.PT -> "O ícone do app foi desenhado para este projeto; componentes de terceiros pertencem aos seus respectivos autores."
        else -> "The app icon was designed for this project; third-party components belong to their respective authors. Full texts are available in Open Source Licenses."
    }

    fun sourceCode(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "查看源代码"
        AppLanguage.RU -> "Исходный код"
        AppLanguage.PT -> "Código Fonte"
        else -> "Source Code"
    }

    fun sourceCodeSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "在 GitHub 上查看源代码"
        AppLanguage.RU -> "Просмотреть исходный код на GitHub"
        AppLanguage.PT -> "Ver código fonte no GitHub"
        else -> "View source code on GitHub"
    }

    fun community(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "加入社区"
        AppLanguage.RU -> "Сообщество"
        AppLanguage.PT -> "Comunidade"
        else -> "Community"
    }

    fun communitySummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Telegram 交流与支持群组"
        AppLanguage.RU -> "Группа поддержки в Telegram"
        AppLanguage.PT -> "Grupo de suporte no Telegram"
        else -> "Telegram Group & Support"
    }

    fun reportIssues(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "问题反馈"
        AppLanguage.RU -> "Сообщить об ошибке"
        AppLanguage.PT -> "Reportar Problemas"
        else -> "Report Issues"
    }

    fun reportIssuesSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "在 GitHub Issues 提交问题"
        AppLanguage.RU -> "Создать задачу в GitHub Issues"
        AppLanguage.PT -> "Abrir problema no GitHub Issues"
        else -> "Submit issues on GitHub Issues"
    }

    fun checkForUpdates(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "检查更新"
        AppLanguage.RU -> "Проверить обновления"
        AppLanguage.PT -> "Verificar Atualizações"
        else -> "Check for Updates"
    }

    fun currentVersionPrefix(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "当前 v"
        AppLanguage.RU -> "Текущая v"
        AppLanguage.PT -> "Versão atual v"
        else -> "Current v"
    }

    fun supportDeveloper(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "支持开发者"
        AppLanguage.RU -> "Поддержать разработчика"
        AppLanguage.PT -> "Apoiar o Desenvolvedor"
        else -> "Support the Developer"
    }

    fun supportDeveloperSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "赞赏支持项目维护与更新"
        AppLanguage.RU -> "Пожертвование на развитие проекта"
        AppLanguage.PT -> "Doações para apoiar o projeto"
        else -> "Donations to support development"
    }

    fun openSourceLicenses(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "开放源代码许可"
        AppLanguage.RU -> "Открытые лицензии"
        AppLanguage.PT -> "Licenças de Código Aberto"
        else -> "Open Source Licenses"
    }

    fun openSourceLicensesSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "查看本应用所使用的第三方开源库及其许可证信息"
        AppLanguage.RU -> "Просмотреть сторонние библиотеки и их лицензии"
        AppLanguage.PT -> "Ver bibliotecas de terceiros e detalhes de licenças"
        else -> "View third-party open source libraries and license details"
    }

    fun projectAndBundled(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "本项目与附加组件"
        AppLanguage.RU -> "Проект и встроенные компоненты"
        AppLanguage.PT -> "Projeto e Componentes Integrados"
        else -> "Project & Bundled Components"
    }

    fun thirdPartyDeps(count: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> if (count == 0) "第三方依赖" else "第三方依赖（$count）"
        AppLanguage.RU -> if (count == 0) "Сторонние зависимости" else "Сторонние зависимости ($count)"
        AppLanguage.PT -> if (count == 0) "Dependências de Terceiros" else "Dependências de Terceiros ($count)"
        else -> if (count == 0) "Third-Party Dependencies" else "Third-Party Dependencies ($count)"
    }

    // Extras Screen
    fun disableSecurityAlerts(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "越狱后停用安全警告"
        AppLanguage.RU -> "Отключить предупреждения безопасности"
        AppLanguage.PT -> "Desativar alertas de segurança após root"
        else -> "Disable security alerts after root"
    }

    fun softRebootAfterRoot(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "越狱后软重启"
        AppLanguage.RU -> "Мягкая перезагрузка после root"
        AppLanguage.PT -> "Reinicialização suave após o root"
        else -> "Soft reboot after root"
    }

    fun softRebootDelay(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "软重启延迟时间"
        AppLanguage.RU -> "Задержка мягкой перезагрузки"
        AppLanguage.PT -> "Atraso da reinicialização suave"
        else -> "Soft reboot delay"
    }

    fun unloadKernelGuard(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "自动卸载内核安全防护"
        AppLanguage.RU -> "Выгрузить защиту ядра"
        AppLanguage.PT -> "Descarregar proteção de segurança do kernel"
        else -> "Unload kernel security guard"
    }

    fun chaseRetry(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "打偏后追页重试"
        AppLanguage.RU -> "Повтор при промахе страниц"
        AppLanguage.PT -> "Tentar novamente ao errar página"
        else -> "Chase retry on miss"
    }

    fun autoJailbreak(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "开机自动越狱"
        AppLanguage.RU -> "Авто-рут при загрузке"
        AppLanguage.PT -> "Root automático ao iniciar"
        else -> "Auto jailbreak on boot"
    }

    fun susfsSupport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "SUSFS 隐藏支持"
        AppLanguage.RU -> "Поддержка скрытия SUSFS"
        AppLanguage.PT -> "Suporte a ocultação SUSFS"
        else -> "SUSFS Support"
    }

    fun vfsSupport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "VFS 虚拟文件系统"
        AppLanguage.RU -> "Виртуальная файловая система VFS"
        AppLanguage.PT -> "Suporte VFS"
        else -> "VFS Support"
    }

    fun hotSwitchManager(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "热切换 Root 管理器"
        AppLanguage.RU -> "Горячее переключение менеджера Root"
        AppLanguage.PT -> "Troca rápida de Gerenciador Root"
        else -> "Hot-Switch Manager"
    }

    fun spoofedManager(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "伪装版管理器包名"
        AppLanguage.RU -> "Пакет маскированного менеджера"
        AppLanguage.PT -> "Pacote do Gerenciador Camuflado"
        else -> "Spoofed Manager Package"
    }

    fun spoofedManagerScan(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "扫描并自动检测"
        AppLanguage.RU -> "Сканировать и определить"
        AppLanguage.PT -> "Escanear e Detectar"
        else -> "Scan & Auto-Detect"
    }

    fun spoofedManagerDetected(pkg: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "已检测到伪装管理器：$pkg"
        AppLanguage.RU -> "Обнаружен маскированный менеджер: $pkg"
        AppLanguage.PT -> "Gerenciador camuflado detectado: $pkg"
        else -> "Detected spoofed manager: $pkg"
    }

    fun spoofedManagerNotFound(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "未检测到已安装的伪装版管理器"
        AppLanguage.RU -> "Маскированный менеджер не найден"
        AppLanguage.PT -> "Nenhum gerenciador camuflado encontrado"
        else -> "No spoofed manager detected"
    }

    // Safe Mode (KernelSU Module Disabler)
    fun safeModeModules(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "安全救砖模式（禁用所有模块）"
        AppLanguage.RU -> "Безопасный режим (отключить модули)"
        AppLanguage.PT -> "Modo de Segurança (Desativar módulos)"
        else -> "Safe Mode (Disable All Modules)"
    }

    fun safeModeSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "临时禁用所有 KernelSU 模块以防软重启或变砖"
        AppLanguage.RU -> "Отключает все модули KernelSU для предотвращения bootloop"
        AppLanguage.PT -> "Desativa módulos KernelSU para evitar bootloops"
        else -> "Disables KernelSU modules to rescue device from bootloops"
    }

    // SELinux Mode Switcher
    fun selinuxMode(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "SELinux 运行模式"
        AppLanguage.RU -> "Режим SELinux"
        AppLanguage.PT -> "Modo do SELinux"
        else -> "SELinux Mode"
    }

    fun selinuxSwitched(mode: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "已切换 SELinux 为：$mode"
        AppLanguage.RU -> "Режим SELinux изменен на: $mode"
        AppLanguage.PT -> "SELinux alterado para: $mode"
        else -> "SELinux switched to: $mode"
    }

    // AMOLED Black Mode
    fun amoledMode(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "AMOLED 纯黑暗黑主题"
        AppLanguage.RU -> "AMOLED чистый черный цвет"
        AppLanguage.PT -> "Tema AMOLED Preto Puro"
        else -> "AMOLED Pure Black Theme"
    }

    fun amoledSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "深色模式下使用纯黑底色，省电护眼"
        AppLanguage.RU -> "Использовать глубокий черный цвет в темной теме"
        AppLanguage.PT -> "Usa fundo preto absoluto no modo escuro"
        else -> "Pure black backgrounds in dark mode to save OLED battery"
    }

    // Root Diagnostics & Export
    fun rootDiagnostics(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Root 环境诊断"
        AppLanguage.RU -> "Диагностика Root окружения"
        AppLanguage.PT -> "Diagnóstico do Ambiente Root"
        else -> "Root Environment Diagnostics"
    }

    fun exportKernelInfo(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "导出内核诊断信息"
        AppLanguage.RU -> "Экспорт информации о ядре"
        AppLanguage.PT -> "Exportar Informações do Kernel"
        else -> "Export Kernel Info"
    }

    fun kernelInfoCopied(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "内核诊断信息已复制到剪贴板"
        AppLanguage.RU -> "Информация о ядре скопирована в буфер"
        AppLanguage.PT -> "Informações do kernel copiadas para a área de transferência"
        else -> "Kernel diagnostic info copied to clipboard"
    }

    // Log Screen
    fun copyLogs(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "复制全部"
        AppLanguage.RU -> "Копировать все"
        AppLanguage.PT -> "Copiar Tudo"
        else -> "Copy All"
    }

    fun shareLogs(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "分享日志"
        AppLanguage.RU -> "Поделиться"
        AppLanguage.PT -> "Compartilhar"
        else -> "Share"
    }

    fun clearLogs(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "清空日志"
        AppLanguage.RU -> "Очистить"
        AppLanguage.PT -> "Limpar"
        else -> "Clear"
    }

    fun logsCopied(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "日志已复制到剪贴板"
        AppLanguage.RU -> "Логи скопированы в буфер обмена"
        AppLanguage.PT -> "Logs copiados para a área de transferência"
        else -> "Logs copied to clipboard"
    }

    fun logsCleared(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "日志已清空"
        AppLanguage.RU -> "Логи очищены"
        AppLanguage.PT -> "Logs limpos"
        else -> "Logs cleared"
    }

    fun filterAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "全部"
        AppLanguage.RU -> "Все"
        AppLanguage.PT -> "Todos"
        else -> "All"
    }

    fun filterErrors(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "错误 / 失败"
        AppLanguage.RU -> "Ошибки"
        AppLanguage.PT -> "Erros"
        else -> "Errors"
    }

    fun filterProbe(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "探测"
        AppLanguage.RU -> "Пробы"
        AppLanguage.PT -> "Sondas"
        else -> "Probe"
    }

    fun filterKsu(lang: AppLanguage): String = "KernelSU"

    fun noLogs(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "暂无日志"
        AppLanguage.RU -> "Логов пока нет"
        AppLanguage.PT -> "Nenhum log ainda"
        else -> "No logs yet"
    }

    // Auto-Jailbreak Background Service Notifications
    fun autoJailbreakPreparing(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "准备自动越狱…"
        AppLanguage.RU -> "Подготовка авто-рута…"
        AppLanguage.PT -> "Preparando root automático…"
        else -> "Preparing auto-jailbreak…"
    }

    fun autoJailbreakWaitingPriv(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "等待 Stellar/Shizuku…"
        AppLanguage.RU -> "Ожидание Stellar/Shizuku…"
        AppLanguage.PT -> "Aguardando Stellar/Shizuku…"
        else -> "Waiting for Stellar/Shizuku…"
    }

    fun autoJailbreakPrivNotReady(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Stellar/Shizuku 未就绪，跳过自动越狱"
        AppLanguage.RU -> "Stellar/Shizuku не готов, пропуск авто-рута"
        AppLanguage.PT -> "Stellar/Shizuku não pronto, ignorando root automático"
        else -> "Stellar/Shizuku not ready, skipping auto-jailbreak"
    }

    fun autoJailbreakPrivNotGranted(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "未授权 Stellar/Shizuku，请打开 Shark Neo 授权"
        AppLanguage.RU -> "Stellar/Shizuku не авторизован, откройте Shark Neo"
        AppLanguage.PT -> "Stellar/Shizuku não autorizado, abra o Shark Neo"
        else -> "Stellar/Shizuku not authorized, open Shark Neo to grant"
    }

    fun autoJailbreakKernelUnsupported(release: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "当前内核不支持自动越狱：$release"
        AppLanguage.RU -> "Ядро не поддерживается для авто-рута: $release"
        AppLanguage.PT -> "Kernel não suportado para root automático: $release"
        else -> "Kernel not supported for auto-jailbreak: $release"
    }

    fun autoJailbreakAlreadyActive(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "KernelSU 已在运行，跳过自动越狱"
        AppLanguage.RU -> "KernelSU уже запущен, пропуск авто-рута"
        AppLanguage.PT -> "KernelSU já em execução, ignorando root automático"
        else -> "KernelSU already running, skipping auto-jailbreak"
    }

    fun autoJailbreakManagerNotInstalled(pkg: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "未安装管理器 $pkg，跳过自动越狱"
        AppLanguage.RU -> "Менеджер $pkg не установлен, пропуск авто-рута"
        AppLanguage.PT -> "Gerenciador $pkg não instalado, ignorando root automático"
        else -> "Manager $pkg not installed, skipping auto-jailbreak"
    }

    fun autoJailbreakInProgress(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "越狱进行中…"
        AppLanguage.RU -> "Выполняется рут…"
        AppLanguage.PT -> "Root em andamento…"
        else -> "Jailbreak in progress…"
    }

    fun autoJailbreakSuccess(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "自动越狱成功，KernelSU 已加载"
        AppLanguage.RU -> "Авто-рут успешно выполнен, KernelSU загружен"
        AppLanguage.PT -> "Root automático concluído, KernelSU carregado"
        else -> "Auto-jailbreak succeeded, KernelSU loaded"
    }

    fun autoJailbreakFailed(rc: Int, tail: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "自动越狱失败 exit=$rc$tail，打开 Shark Neo 查看日志"
        AppLanguage.RU -> "Сбой авто-рута exit=$rc$tail, откройте Shark Neo"
        AppLanguage.PT -> "Falha no root automático exit=$rc$tail, abra o Shark Neo"
        else -> "Auto-jailbreak failed exit=$rc$tail, open Shark Neo to view logs"
    }

    fun autoJailbreakPanicDisabled(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "上次自动越狱引发重启，已关闭开机自动越狱"
        AppLanguage.RU -> "Предыдущий авто-рут вызвал перезагрузку, авто-рут отключен"
        AppLanguage.PT -> "Root automático anterior causou reinicialização, desativado"
        else -> "Previous auto-jailbreak caused reboot, auto-jailbreak disabled"
    }

    // Quick Settings Tile
    fun qsTileRootActive(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Root 已激活"
        AppLanguage.RU -> "Root активен"
        AppLanguage.PT -> "Root Ativo"
        else -> "Root Active"
    }

    fun qsTileRootInactive(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Root 未激活（点击越狱）"
        AppLanguage.RU -> "Root не активен (нажмите)"
        AppLanguage.PT -> "Root Inativo (toque para root)"
        else -> "Root Inactive (tap to root)"
    }

    // Update Dialog
    fun updateAvailable(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "发现新版本"
        AppLanguage.RU -> "Доступно обновление"
        AppLanguage.PT -> "Atualização Disponível"
        else -> "Update Available"
    }

    fun currentVersionLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "当前版本"
        AppLanguage.RU -> "Текущая версия"
        AppLanguage.PT -> "Versão atual"
        else -> "Current version"
    }

    fun latestVersionLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "最新版本"
        AppLanguage.RU -> "Новая версия"
        AppLanguage.PT -> "Nova versão"
        else -> "Latest version"
    }

    fun updateNow(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "立即更新"
        AppLanguage.RU -> "Обновить сейчас"
        AppLanguage.PT -> "Atualizar Agora"
        else -> "Update Now"
    }

    fun downloadBrowser(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "浏览器下载"
        AppLanguage.RU -> "Скачать в браузере"
        AppLanguage.PT -> "Baixar no Navegador"
        else -> "Browser Download"
    }

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "取消"
        AppLanguage.RU -> "Отмена"
        AppLanguage.PT -> "Cancelar"
        else -> "Cancel"
    }

    fun close(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "关闭"
        AppLanguage.RU -> "Закрыть"
        AppLanguage.PT -> "Fechar"
        else -> "Close"
    }

    fun retry(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "重试"
        AppLanguage.RU -> "Повторить"
        AppLanguage.PT -> "Tentar novamente"
        else -> "Retry"
    }

    // Kernel Diagnostic & Device Support
    fun kernelDiagnosticTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "内核诊断与设备适配"
        AppLanguage.RU -> "Диагностика ядра и адаптация"
        AppLanguage.PT -> "Diagnóstico de Kernel e Suporte"
        else -> "Kernel Diagnostic & Support"
    }

    fun kernelDiagnosticSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "通过 Shizuku 自动采集内核特征并一键提交 GitHub Issue"
        AppLanguage.RU -> "Сбор параметров ядра через Shizuku и отправка issue на GitHub"
        AppLanguage.PT -> "Colete dados do kernel via Shizuku e envie issue no GitHub"
        else -> "Inspect kernel parameters via Shizuku & request support on GitHub"
    }

    fun unsupportedKernelBannerTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "当前内核尚未适配"
        AppLanguage.RU -> "Текущее ядро не поддерживается"
        AppLanguage.PT -> "Kernel atual não suportado"
        else -> "Unsupported Kernel Detected"
    }

    fun unsupportedKernelBannerDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "点击此处通过 Shizuku 执行自动化诊断，并将参数提交至 GitHub 请求添加支持。"
        AppLanguage.RU -> "Нажмите здесь для запуска диагностики через Shizuku и отправки запроса на GitHub."
        AppLanguage.PT -> "Toque aqui para executar o diagnóstico via Shizuku e solicitar suporte no GitHub."
        else -> "Tap here to run automated Shizuku diagnostics and generate a GitHub support request."
    }

    fun actionDiagnoseAndReport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "运行诊断并请求适配"
        AppLanguage.RU -> "Диагностика и запрос поддержки"
        AppLanguage.PT -> "Diagnosticar e Solicitar Suporte"
        else -> "Diagnose & Request Support"
    }

    fun actionRunDiagnostic(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "开始诊断 (Shizuku)"
        AppLanguage.RU -> "Запустить диагностику (Shizuku)"
        AppLanguage.PT -> "Iniciar Diagnóstico (Shizuku)"
        else -> "Run Diagnostic (Shizuku)"
    }

    fun actionRunningDiagnostic(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "正在执行诊断命令..."
        AppLanguage.RU -> "Выполнение диагностики..."
        AppLanguage.PT -> "Executando diagnóstico..."
        else -> "Running diagnostic..."
    }

    fun actionSubmitGithubIssue(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "提交至 GitHub Issue"
        AppLanguage.RU -> "Создать заявку на GitHub"
        AppLanguage.PT -> "Abrir Issue no GitHub"
        else -> "Create GitHub Issue"
    }

    fun actionJoinTelegramSupport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "加入 Telegram 支持群组"
        AppLanguage.RU -> "Группа поддержки в Telegram"
        AppLanguage.PT -> "Entrar no Grupo do Telegram"
        else -> "Join Telegram Support Group"
    }

    fun actionCopyDiagnostic(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "复制诊断报告"
        AppLanguage.RU -> "Скопировать отчет"
        AppLanguage.PT -> "Copiar Relatório"
        else -> "Copy Diagnostic Report"
    }

    fun diagnosticCopied(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "诊断报告已复制到剪贴板！"
        AppLanguage.RU -> "Отчет о диагностике скопирован в буфер!"
        AppLanguage.PT -> "Relatório de diagnóstico copiado!"
        else -> "Diagnostic report copied to clipboard!"
    }

    fun diagnosticStatusSupported(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "内核已支持"
        AppLanguage.RU -> "Ядро поддерживается"
        AppLanguage.PT -> "Kernel suportado"
        else -> "Kernel Supported"
    }

    fun diagnosticStatusUnsupported(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "内核未在官方支持列表中"
        AppLanguage.RU -> "Ядро отсутствует в списке поддержки"
        AppLanguage.PT -> "Kernel não está na lista de suporte"
        else -> "Kernel Unsupported (Candidate for Port)"
    }

    // Beta Channel & Dynamic Profile Sync
    fun betaChannel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "Beta 测试通道"
        AppLanguage.RU -> "Канал тестирования Beta"
        AppLanguage.PT -> "Canal de Testes Beta"
        else -> "Beta Testing Channel"
    }

    fun betaChannelSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "接收测试版本与抢先体验更新"
        AppLanguage.RU -> "Получать экспериментальные сборки и предварительные версии"
        AppLanguage.PT -> "Receber compilações experimentais e versões prévias"
        else -> "Receive experimental builds and pre-releases"
    }

    fun syncProfiles(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "同步云端内核配置"
        AppLanguage.RU -> "Синхронизировать профили ядра"
        AppLanguage.PT -> "Sincronizar Perfis Online"
        else -> "Sync Online Profiles"
    }

    fun syncProfilesSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "从 GitHub 获取最新的设备支持配置"
        AppLanguage.RU -> "Получить свежие профили устройств с GitHub"
        AppLanguage.PT -> "Baixar perfis de dispositivos mais recentes do GitHub"
        else -> "Fetch latest device profiles from GitHub"
    }

    fun syncProfilesSuccess(count: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "成功同步 $count 个内核配置！"
        AppLanguage.RU -> "Синхронизировано профилей: $count!"
        AppLanguage.PT -> "$count perfis sincronizados com sucesso!"
        else -> "Successfully synchronized $count kernel profiles!"
    }

    fun syncProfilesFailed(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "同步云端配置失败，请检查网络"
        AppLanguage.RU -> "Ошибка синхронизации профилей, проверьте сеть"
        AppLanguage.PT -> "Falha ao sincronizar perfis, verifique a conexão"
        else -> "Failed to sync profiles, check network"
    }

    fun parseBootImage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "从 boot.img 解析内核符号"
        AppLanguage.RU -> "Анализ boot.img / символов ядра"
        AppLanguage.PT -> "Extrair Símbolos do boot.img"
        else -> "Parse boot.img / Kernel Symbols"
    }

    fun parseBootImageSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "从本地 boot.img / kernel.bin 自动提取符号并适配"
        AppLanguage.RU -> "Извлечь символы и адреса из локального boot.img / kernel.bin"
        AppLanguage.PT -> "Extrair símbolos e offsets do boot.img / kernel.bin local"
        else -> "Extract symbols & offsets from local boot.img / kernel.bin"
    }

    fun parsingBootTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "正在解析内核映像..."
        AppLanguage.RU -> "Анализ образа ядра..."
        AppLanguage.PT -> "Analisando imagem do kernel..."
        else -> "Parsing kernel image..."
    }

    fun parseBootSuccess(kernel: String, symbols: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "成功提取 $symbols 个内核符号并生成配置！"
        AppLanguage.RU -> "Успешно извлечено $symbols символов ядра!"
        AppLanguage.PT -> "$symbols símbolos do kernel extraídos com sucesso!"
        else -> "Successfully extracted $symbols symbols and added profile!"
    }

    fun parseBootFailed(error: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "解析失败: $error"
        AppLanguage.RU -> "Ошибка анализа: $error"
        AppLanguage.PT -> "Falha na análise: $error"
        else -> "Parse failed: $error"
    }

    fun softRebootNow(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "软重启 (应用模块)"
        AppLanguage.RU -> "Мягкая перезагрузка (применить модули)"
        AppLanguage.PT -> "Reinicialização Suave (Aplicar Módulos)"
        else -> "Soft Reboot (Apply Modules)"
    }

    fun softRebootNowSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "重启系统框架 (Zygote) 使模块生效，无需重启设备且保留 ROOT"
        AppLanguage.RU -> "Перезапустить Android framework (Zygote) для активации модулей без потери root"
        AppLanguage.PT -> "Reinicia a framework do Android (Zygote) para ativar módulos mantendo o root"
        else -> "Restart Android framework (Zygote) to activate modules without dropping root"
    }

    fun confirmSoftReboot(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "重启框架"
        AppLanguage.RU -> "Перезапустить"
        AppLanguage.PT -> "Reiniciar"
        else -> "Restart Framework"
    }

    fun softRebootExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "此操作将重启系统服务和 Zygote 框架，以便新加载的 KernelSU / Zygisk 模块对应用生效。系统内核不会重启，因此临时 Root 权限将完整保留。"
        AppLanguage.RU -> "Это перезапустит system server и Zygote, чтобы модули KernelSU / Zygisk активировались. Ядро устройства не перезагрузится, поэтому root сохранится."
        AppLanguage.PT -> "Isso reinicia o system server e o Zygote para que os módulos do KernelSU / Zygisk entrem em vigor. O kernel não será reiniciado, mantendo o root ativo."
        else -> "This restarts the system server and Zygote framework so newly loaded KernelSU / Zygisk modules hook newly started apps. The kernel will NOT reboot, so temporary jailbreak stays live."
    }

    fun rebootRequiredAlreadyRan(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "需重启设备 (此启动周期已运行)"
        AppLanguage.RU -> "Требуется перезагрузка (уже запускалось)"
        AppLanguage.PT -> "Reinicialização necessária (já executado)"
        else -> "Reboot Required (Already ran this boot)"
    }

    fun rerunBlockedTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "需要重启设备"
        AppLanguage.RU -> "Требуется перезагрузка"
        AppLanguage.PT -> "Reinicialização Necessária"
        else -> "Reboot Required"
    }

    fun rerunBlockedMessage(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "内核利用在此启动周期中只能安全执行一次。在已修改的内核状态上重复运行可能导致内核崩溃或设备重启。\n\n如需再次运行，请重启您的设备。"
        AppLanguage.RU -> "Операция может выполняться только один раз за сеанс загрузки. Повторный запуск на уже измененном ядре может привести к сбою устройства.\n\nПожалуйста, перезагрузите устройство для повторной попытки."
        AppLanguage.PT -> "A operação só pode ser executada com segurança uma vez por inicialização. Executar novamente em um estado de kernel modificado pode causar falha do dispositivo.\n\nPor favor, reinicie o dispositivo para tentar novamente."
        else -> "This operation can only be safely executed once per boot session. Running again on an already modified kernel state may cause a device crash or reboot.\n\nPlease reboot your device to run again."
    }

    fun submitDeviceForSupport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "上传设备信息到支持群组"
        AppLanguage.RU -> "Отправить данные в группу поддержки"
        AppLanguage.PT -> "Enviar Dispositivo ao Grupo de Suporte"
        else -> "Submit Device to Support Group"
    }

    fun submittingReport(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "正在上传到 Telegram..."
        AppLanguage.RU -> "Отправка в Telegram..."
        AppLanguage.PT -> "Enviando ao Telegram..."
        else -> "Uploading to Telegram..."
    }

    fun submitReportSuccess(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "已成功上传到 Telegram 支持群组！"
        AppLanguage.RU -> "Успешно отправлено в группу Telegram!"
        AppLanguage.PT -> "Enviado com sucesso ao grupo do Telegram!"
        else -> "Submitted to Telegram group successfully!"
    }

    fun submitReportFailed(error: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "上传失败：$error"
        AppLanguage.RU -> "Ошибка отправки: $error"
        AppLanguage.PT -> "Falha ao enviar: $error"
        else -> "Upload failed: $error"
    }

    fun submitProfileToGroup(lang: AppLanguage): String = when (lang) {
        AppLanguage.ZH -> "分享配置到支持群组"
        AppLanguage.RU -> "Поделиться профилем в группе"
        AppLanguage.PT -> "Share Profile to Support Group"
        else -> "Share Profile to Support Group"
    }
}
