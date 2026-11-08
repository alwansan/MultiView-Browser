# توقيع APK ثابت بدون خسارة الجلسات عند التحديث

**الوضع الحالي:** GitHub Actions يبني Debug APK بتوقيع قد يختلف بين تشغيل وآخر. إذا اختلف التوقيع، يرفض Android تثبيته فوق القديم وقد تحتاج إلى حذف التطبيق، وهذا يحذف الجلسات.

جهزت الـWorkflow ليستعمل **توقيعاً ثابتاً اختيارياً** عند إضافة أربعة أسرار GitHub Actions. لا ترفع ملف المفتاح إلى المستودع العام، ولا ترسل كلمات مرور المفاتيح داخل الدردشة.

## تجهيز المفتاح في Termux (مرة واحدة)

افتح Termux ونفذ:

```bash
pkg install openjdk-17 gh coreutils
gh auth login
mkdir -p "$HOME/.multiview-keys"
chmod 700 "$HOME/.multiview-keys"
read -r -s -p "Enter a strong signing password: " MV_PASS
printf '\n'
keytool -genkeypair \
  -keystore "$HOME/.multiview-keys/multiview.p12" \
  -storetype PKCS12 -alias multiview \
  -keyalg RSA -keysize 3072 -validity 10000 \
  -storepass "$MV_PASS" -keypass "$MV_PASS" \
  -dname "CN=MultiView Browser,O=Personal,C=IQ"

base64 -w 0 "$HOME/.multiview-keys/multiview.p12" \
  | gh secret set MULTIVIEW_KEYSTORE_B64 -R alwansan/MultiView-Browser
printf '%s' "$MV_PASS" \
  | gh secret set MULTIVIEW_STORE_PASS -R alwansan/MultiView-Browser
printf '%s' "$MV_PASS" \
  | gh secret set MULTIVIEW_KEY_PASS -R alwansan/MultiView-Browser
printf '%s' multiview \
  | gh secret set MULTIVIEW_KEY_ALIAS -R alwansan/MultiView-Browser
unset MV_PASS
```

بعدها يمكنك التأكد من **أسماء** الأسرار فقط:

```bash
gh secret list -R alwansan/MultiView-Browser
```

احفظ ملف `$HOME/.multiview-keys/multiview.p12` بنسخة احتياطية آمنة **خارج المستودع** واحتفظ بكلمة السر. ضياعهما يمنع توقيع تحديثات متوافقة بنفس المفتاح. لا تنشر المفتاح أو كلمة مروره.

يمكنك أيضاً إضافة الأسرار الأربعة من GitHub → Settings → Secrets and variables → Actions بدلاً من Termux.

## آلية العمل

عند وجود الأسرار الأربعة، تُوقّع النسخ المنشورة جميعها بنفس شهادة APK. إذا لم توجد، يستمر بناء Debug APK عادي. إذا أُضيف بعضها فقط، يفشل البناء بدلاً من نشر APK بتوقيع غير متوقع.

**تنبيه:** الانتقال من APK التجريبي الحالي بتوقيع سابق إلى أول APK بالتوقيع الثابت قد يتطلب حذفاً واحداً بعد حفظ روابطك. بعد ذلك، إذا حافظت على نفس المفاتيح والاسم والحزمة، يفترض أن تتثبت التحديثات اللاحقة فوق النسخة الموجودة دون حذف الجلسات. التوقيع الثابت لا يمنع Android من قتل WebView؛ هو يحل مشكلة توافق التحديثات فقط.

مصادر: https://developer.android.com/tools/apksigner
https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets
