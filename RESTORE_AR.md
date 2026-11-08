# رسالة استعادة مشروع MultiView Browser

اسم المشروع: MultiView Browser — Android native multi-pane browser.
المستودع الوحيد: https://github.com/alwansan/MultiView-Browser

استكمل العمل في هذه الدردشة. عند كل استعادة:
1. اقرأ الملفات AGENTS.md وPROJECT_MEMORY.md وdocs/PLAN_AR.md وdocs/ORIGINAL_APK_ANALYSIS.md وdocs/TEST_LOG.md كاملة.
2. راجع أحدث commit وجميع GitHub Actions الحديثة، ولا تفترض أن البناء نجح حتى تتحقق من النتيجة والـAPK.
3. استعد قرارات التنفيذ من الملفات، وأكمل المرحلة الحالية مباشرة. لا تطلب مني إعادة شرح الاحتياجات أو فتح جهاز حاسوب.
4. المطلوب إعادة إنشاء وظائف MultiView Browser 1.2.0 من الصفر بكود مملوك لنا: حتى 20 صفحة متزامنة، Grid/Vertical/Horizontal/Focus، حفظ الصفحات، التصفح المتقدم، وأفضل تصميم.
5. أولوية خاصة لصفحات ChatGPT الطويلة: احتفاظ بالـWebView عند تغيير التخطيط، عدم إعادة التحميل غير الضرورية، مراقبة الذاكرة، ولا تعد باستحالة قتل Android للعملية.
6. استخدم GitHub Actions لبناء APK والتحقق من الأخطاء، وثبّت نتيجة كل دورة في docs/TEST_LOG.md وPROJECT_MEMORY.md.
7. إن احتجت ملفاً خارجياً غير متاح، اذكر اسمه ورابط مصدر رسمي مباشر والخطوات، لكن تابع كل ما يمكن تنفيذه دون انتظار.
8. افحص أي ملف APK مرجعي مرفوع إذا كان متاحاً؛ لا تنسخ كود التطبيق التجاري أو موارده إلى المشروع.
9. المالك يكتب "continue" لاستكمال العمل. اتخذ قرارات التنفيذ الهندسية بنفسك.

آخر حالة: v0.1.1 فتح بنجاح حسب اختبار المالك؛ انتقل العمل إلى تحسين v0.2.0، وFit Grid وخمسة صفحات وتصفح 0.2.2. تحقق من آخر Actions وRelease قبل أي تعديل. هدف الدورة التالية: اقرأ docs/DEVICE_TEST_AR.md؛ اختبار خمس صفحات وChatGPT على الهاتف، ضبط تفاصيل UI/UX، وإصلاح نتائج الاختبار الفعلية. أحدث CI مؤكد عند كتابة هذه الرسالة: 42501943269 (v0.2.2، نجاح).


استعادة جديدة بعد طلب 2026-11-08: المالك اختبر v0.2.2 ولم يجد أعطالاً. الإصدار 0.3.0 يعمل على zoom (10–200% بفاصل 10) / reload all / العربية والإنجليزية / dark website (تجريبي)، مع سجل بناء يجب تأكيده. اقرأ PROJECT_MEMORY.md وdocs/TEST_LOG.md للتحقق من أحدث نتيجة. لا تعطل الويب في جلسات المستخدم أثناء تغيير التخطيط.

أحدث بناء مُعتمد: v0.3.1-preview من commit 8f3c95ff24bc83cd28a4d9792b0e363957043baa، نجاح Actions 42503968802، SHA256 3f3d55ec7682b2074a6ed168e957e66bbad7d4c21df9fa734d0e3bdb982e496d. الميزات الجديدة: Zoom 10–200%، حفظ/تطبيق للجميع، Reload All، العربية/الإنجليزية، Dark Websites تجريبي، Find in page. لا تعلن نجاح اختبار الجهاز قبل تأكيد المستخدم.


v0.4.0: جلسات الخلفية opt-in باستخدام SessionKeepAliveService، وعودة Back تصغّر التطبيق، واستعادة نشطة محدودة وسكرول، وخوارزمية SessionRestorePlan لتجنب إعادة تحميل 20 صفحة دفعة واحدة. روابط متعددة من زر Links الرئيسي مع السجل والمفضلة والقوائم المحفوظة. وثق نتائج CI والرابط الحقيقي بعد البناء ولا تدّعي ضمان بقاء صفحات JS للأبد.


v0.4.0 النهائي الحالي: Actions 42506003292 نجح، SHA 6788d8aec8e91a23ceee72a44fbd0d541f2cdb3e48150ff252170b74f011ef24، APK https://github.com/alwansan/MultiView-Browser/releases/download/preview-latest/app-debug.apk. اقرأ docs/BACKGROUND_RELIABILITY_AR.md وdocs/SIGNING_SETUP_AR.md. عدم إعادة التحميل أبداً مستحيل على Android بدون ضمانات النظام؛ تحقق على الجهاز، وحافظ على سلوك Back minimization وWebView.renderer recovery ولا تعد بإبقاء DOM بعد قتل العملية.


التحديث البصري: README إنجليزي + README.ar.md عربي، وأيقونات SVG وأيقونة Adaptive Android، تحسين تمييز الصفحة النشطة. الإصدار v0.4.2 يضيف Privacy & cleanup اختياري دون تنظيف آلي. راجع أحدث Actions والـAPK قبل إعطاء أي تأكيد. الصور بالمستودع تصاميم توضيحية وليست screenshots فعلية.


أحدث مرجع 2026-11-08: التطبيق 0.4.2-preview؛ آخر بناء ناجح 42507742254 من 6d75c27f8068b019e756ef6c925a3d59126073de، APK SHA256 f7d9485eb99b8d9985e28decd6f20ffd9d658b03a0c6438219a3cf4322b114b0. الروابط: README.md إنجليزي وREADME.ar.md عربي، صور SVG في assets/brand، أيقونة Android Adaptive، وPrivacy & cleanup لتفريغ cache/history/saved sets/blank pages/cookies بشكل اختياري. تم تجهيز CHANGELOG.md وdocs/README.md وCONTRIBUTING.md وSECURITY.md. لا تدعِ التحقق على الجهاز من هذه النسخة قبل اختبار المالك.
